package org.dllearner.reasoning.spatial;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utils.spatial.SpatialKBPostGISHelper;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.postgresql.util.PGobject;
import org.semanticweb.owlapi.model.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import javax.annotation.Nonnull;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpatialReasonerPostGIS extends AbstractReasonerComponent implements SpatialReasoner {
    // PostGIS settings
    private String hostname = "localhost";
    private String dbName = "dllearner";
    private int port = 5432;
    private String dbUser = "postgres";
    private String dbUserPW = "postgres";
    protected Connection conn;

    // OWL settings
    private Set<List<OWLProperty>> geometryPropertyPaths = new HashSet<>();
    private OWLClass areaFeatureClass = SpatialKBPostGISHelper.areaFeatureClass;
    private String areaFeatureTableName = SpatialKBPostGISHelper.areaGeomTableName;
    private OWLClass lineFeatureClass = SpatialKBPostGISHelper.lineFeatureClass;
    private String lineFeatureTableName = SpatialKBPostGISHelper.lineGeomTableName;
    private OWLClass pointFeatureClass = SpatialKBPostGISHelper.pointFeatureClass;
    private String pointFeatureTableName = SpatialKBPostGISHelper.pointGeomTableName;

    protected AbstractReasonerComponent baseReasoner;

    // TODO: make this configurable
    // "Specifies the maximum number of entries the cache may contain"
    private int maxFeatureGeometryCacheSize = 1000000;
    private LoadingCache<OWLIndividual, OWLIndividual> feature2geom =
            CacheBuilder.newBuilder().maximumSize(maxFeatureGeometryCacheSize)
                    .build(new CacheLoader<OWLIndividual, OWLIndividual>() {
                        @Override
                        public OWLIndividual load(
                                @Nonnull OWLIndividual featureIndividual) throws Exception {
                            /*
                             * Gets the geo:Geometry OWL individual assigned to
                             * the input OWL individual via the geometry
                             * property path. In case there are multiple
                             * geometry OWL individuals assigned, I'll just pick
                             * the first one.
                             */
                            for (List<OWLProperty> propPath : geometryPropertyPaths) {
                                int pathLen = propPath.size();

                                // In case the geometry property path just
                                // contains one entry, one can assume it's a
                                // data property pointing to the geometry
                                // literal. So the input feature individual is
                                // already what's requested here.
                                if (pathLen == 1) {
                                    assert propPath.get(0).isOWLDataProperty();

                                    return featureIndividual;
                                }

                                // Strip off the last entry of the property
                                // path, which is a data property. All the
                                // preceding properties are assumed to be object
                                // properties.
                                List<OWLObjectProperty> objProps = propPath.stream()
                                        .limit(pathLen-1)
                                        .map(OWLProperty::asOWLObjectProperty)
                                        .collect(Collectors.toList());

                                // S --> subject position, O --> object position
                                // from an RDF triple point of view
                                Set<OWLIndividual> tmpS =
                                        Sets.newHashSet(featureIndividual);
                                Set<OWLIndividual> tmpO = new HashSet<>();

                                for (OWLObjectProperty objProp : objProps) {
                                    for (OWLIndividual i : tmpS) {
                                        tmpO.addAll(
                                                baseReasoner.getRelatedIndividuals(i, objProp));
                                    }

                                    tmpS = tmpO;
                                    tmpO = new HashSet<>();
                                }

                                if (!tmpS.isEmpty()) {
                                    return tmpS.iterator().next();
                                }
                            }
                            return null;
                        }
                    });

    private LoadingCache<OWLIndividual, OWLIndividual> geom2feature =
            CacheBuilder.newBuilder().maximumSize(maxFeatureGeometryCacheSize)
                    .build(new CacheLoader<OWLIndividual, OWLIndividual>() {
                        @Override
                        public OWLIndividual load(
                                @Nonnull OWLIndividual geometryIndividual) throws Exception {
                            /*
                             * Gets the geo:Feature OWL individual to which the
                             * input geo:Geometry OWL individual was assigned
                             * via the geometry property path. In case there are
                             * multiple feature OWL individuals to which the
                             * geometry OWL individual was assigned, I'll just
                             * pick the first one.
                             */
                            for (List<OWLProperty> propPath : geometryPropertyPaths) {
                                int pathLen = propPath.size();

                                // In case the geometry property path just
                                // contains one entry, one can assume it's a
                                // data property pointing to the geometry
                                // literal. So the input geometry individual is
                                // already what's requested here.
                                if (pathLen == 1) {
                                    assert propPath.get(0).isOWLDataProperty();

                                    return geometryIndividual;
                                }

                                // Strip off the last entry of the property
                                // path, which is a data property. All the
                                // preceding properties are assumed to be object
                                // properties.
                                List<OWLObjectProperty> objProps = propPath.stream()
                                        .limit(pathLen-1)
                                        .map(OWLProperty::asOWLObjectProperty)
                                        .collect(Collectors.toList());

                                List<OWLObjectProperty> revObjProps =
                                        Lists.reverse(objProps);

                                // S --> subject, O --> object (from RDF triple
                                // view)
                                Set<OWLIndividual> tmpS = new HashSet<>();
                                Set<OWLIndividual> tmpO =
                                        Sets.newHashSet(geometryIndividual);

                                for (OWLObjectProperty objProp : revObjProps) {
                                    /**
                                     * Looks sth like this:
                                     * {
                                     *   :bahnhof_dresden_neustadt_building : [
                                     *          :building_bhf_neustadt_geometry],
                                     *   :building_bhf_neustadt_geometry : [],
                                     *   :inside_building_bhf_neustadt_geometry : [],
                                     *   :on_turnerweg_geometry : [],
                                     *   :outside_building_bhf_neustadt_1_geometry : [],
                                     *   :outside_building_bhf_neustadt_2_geometry : [],
                                     *   :pos_inside_bhf_neustadt : [
                                     *          :inside_building_bhf_neustadt_geometry],
                                     *   :pos_on_turnerweg : [
                                     *          :on_turnerweg_geometry],
                                     *   :pos_outside_bhf_neustadt_1 : [
                                     *          :outside_building_bhf_neustadt_1_geometry],
                                     *   :pos_outside_bhf_neustadt_2 : [
                                     *          :outside_building_bhf_neustadt_2_geometry],
                                     *   :turnerweg : [
                                     *          :turnerweg_geometry],
                                     *   :turnerweg_geometry : [],
                                     *   :turnerweg_part : [
                                     *          :turnerweg_part_geometry],
                                     *   :turnerweg_part_geometry : []
                                     * }
                                     *
                                     * i.e. all geometry entries have an empty
                                     * set as values and all the feature entries
                                     * have a non-empty set as values
                                     */
                                    Map<OWLIndividual, SortedSet<OWLIndividual>> members =
                                            baseReasoner.getPropertyMembers(objProp);

                                    /**
                                     * `members` keys which have a non-empty
                                     * value, i.e. all feature OWL individuals
                                     *
                                     * [
                                     *   :bahnhof_dresden_neustadt_building,
                                     *   :pos_inside_bhf_neustadt,
                                     *   :pos_on_turnerweg,
                                     *   :pos_outside_bhf_neustadt_1,
                                     *   :pos_outside_bhf_neustadt_2,
                                     *   :turnerweg,
                                     *   :turnerweg_part
                                     * ]
                                     */
                                    List<OWLIndividual> sMembers = members.entrySet().stream()
                                            .filter((Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e) -> !e.getValue().isEmpty())
                                            .map((Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e) -> e.getKey())
                                            .collect(Collectors.toList());

                                    for (OWLIndividual s : sMembers) {
                                        // - `tmpO` contains all the OWL
                                        //   individuals on object position
                                        //   (viewed from an RDF triple
                                        //   perspective)
                                        // - `sMembers` contains all those OWL
                                        //   individuals that actually have
                                        //   values for property `objProp`
                                        // - `s` is an OWL individual that
                                        //   actually has values for property
                                        //   `objProp`

                                        // The values of the current OWL individual
                                        SortedSet<OWLIndividual> os = members.get(s);

                                        // If any of the values of the current
                                        // OWL individual is contained in the
                                        // set of value OWL individuals we're
                                        // interested in...
                                        if (os.stream().anyMatch(tmpO::contains)) {
                                            // ...then we'll consider this
                                            // current OWL individual in the
                                            // next round (or in the final
                                            // result set if this is the last
                                            // round)
                                            tmpS.add(s);
                                        }
                                    }

                                    tmpO = tmpS;
                                    tmpS = new HashSet<>();
                                }

                                if (!tmpO.isEmpty()) {
                                    return tmpO.iterator().next();
                                }
                            }

                            return null;
                        }
                    });
    /**
     * @param featureIndividual The feature OWL individual to get the PostGIS
     *                          table for, not the geometry OWL individual
     */
    private String getTable(OWLIndividual featureIndividual) {
        if (baseReasoner.hasType(areaFeatureClass, featureIndividual)) {
            return areaFeatureTableName;
        } else if (baseReasoner.hasType(lineFeatureClass, featureIndividual)) {
            return lineFeatureTableName;
        } else if (baseReasoner.hasType(pointFeatureClass, featureIndividual)) {
            return pointFeatureTableName;
        } else {
            throw new RuntimeException(
                    "Individual " + featureIndividual + " is neither an area " +
                            "feature, nor a line feature, nor a point feature");
        }
    }

    public SpatialReasonerPostGIS() {
        super();
    }

    // -------------------------------------------------------------------------
    // base reasoner/spatial reasoner switch methods

    @Override
    public boolean isSuperClassOf(OWLClassExpression superClass, OWLClassExpression subClass) {
        return baseReasoner.isSuperClassOf(superClass, subClass);
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyDomains() {
        Map<OWLObjectProperty, OWLClassExpression> domainsMap = baseReasoner.getObjectPropertyDomains();

        // TODO: Add spatial aspect-specific stuff here
        domainsMap.put(SpatialVocabulary.isConnectedWith, SpatialVocabulary.SpatialFeature);

        return domainsMap;
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRanges() {
        Map<OWLObjectProperty, OWLClassExpression> rangesMap = baseReasoner.getObjectPropertyRanges();

        // TODO: Add spatial aspect-specific stuff here
        rangesMap.put(SpatialVocabulary.isConnectedWith, SpatialVocabulary.SpatialFeature);

        return rangesMap;
    }

    @Override
    public Map<OWLDataProperty, OWLClassExpression> getDataPropertyDomains() {
        Map<OWLDataProperty, OWLClassExpression> domainsMap = baseReasoner.getDataPropertyDomains();

        // TODO: Add spatial aspect-specific stuff here

        return domainsMap;
    }

    @Override
    protected SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression ce) {
        SortedSet<OWLClassExpression> subClasses = baseReasoner.getSubClasses(ce);

        // TODO: Add spatial aspect-specific stuff here

        return subClasses;
    }

    @Override
    protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression ce) {
        SortedSet<OWLClassExpression> superClasses = baseReasoner.getSubClasses(ce);

        // TODO: Add spatial aspect-specific stuff here

        return superClasses;
    }

    @Override
    protected Set<OWLDataProperty> getDatatypePropertiesImpl() {
        Set<OWLDataProperty> dataProperties = baseReasoner.getDatatypeProperties();

        dataProperties.addAll(SpatialVocabulary.spatialDataProperties);

        return dataProperties;
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty objectProperty) {
        SortedSet<OWLObjectProperty> subProperties = baseReasoner.getSubProperties(objectProperty);

        // TODO: add spatial aspect-specific stuff here

        return subProperties;
    }

    @Override
    protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty dataProperty) {
        SortedSet<OWLDataProperty> subProperties = baseReasoner.getSubProperties(dataProperty);

        // TODO: add spatial aspect-specific stuff here

        return subProperties;
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty objectProperty) {
        SortedSet<OWLObjectProperty> superProperties = baseReasoner.getSuperProperties(objectProperty);

        // TODO: add spatial aspect-specific stuff here

        return superProperties;
    }

    @Override
    protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty dataProperty) {
        SortedSet<OWLDataProperty> superProperties = baseReasoner.getSuperProperties(dataProperty);

        // TODO: add spatial aspect-specific stuff here

        return superProperties;
    }

    @Override
    protected Set<OWLObjectProperty> getObjectPropertiesImpl() {
        return Sets.union(
                baseReasoner.getObjectProperties(),
                SpatialVocabulary.spatialObjectProperties);
    }

    @Override
    protected OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
        if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
            return SpatialVocabulary.SpatialFeature;

        // TODO: Add further spatial object properties here
        } else {
            return baseReasoner.getRange(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
        if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
            return SpatialVocabulary.SpatialFeature;


        // TODO: Add further spatial object properties here
        } else {
            return baseReasoner.getDomain(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLDataProperty dataProperty) {
        // TODO: Add spatial data property handling here

        return baseReasoner.getDomain(dataProperty);
    }

    @Override
    protected Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
        // TODO: Add spatial int data properties here

        return baseReasoner.getIntDatatypeProperties();
    }

    @Override
    protected Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
        // TODO: Add spatial double data properties here

        return baseReasoner.getDoubleDatatypeProperties();
    }

    @Override
    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(
            OWLObjectProperty objectProperty) {
        if (SpatialVocabulary.spatialObjectProperties.contains(objectProperty)) {
            if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
                // isConnectedWith
                return getIsConnectedWithMembers();


            // TODO: Add further spatial object properties here
            } else {
                throw new RuntimeException(
                        "Spatial object property " + objectProperty + "not " +
                                "handled in getPropertyMembersImpl( )");
            }
        } else {
            return baseReasoner.getPropertyMembers(objectProperty);
        }
    }

    @Override
    protected Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
        // TODO: Add spatial boolean data properties here

        return baseReasoner.getBooleanDatatypeProperties();
    }

    @Override
    protected Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
        // TODO: Add spatial string data properties here

        return baseReasoner.getDatatypeProperties();
    }

    @Override
    public boolean hasTypeImpl(OWLClassExpression ce, OWLIndividual individual) {
        // FIXME
//        if (containsSpatialExpressions(ce)) {
//            return hasTypeSpatial(ce, individual);
//        } else {
            return baseReasoner.hasType(ce, individual);
//        }
    }

    // -------------------------------------------------------------------------
    // get-members methods
    private Map<OWLIndividual, SortedSet<OWLIndividual>> getIsConnectedWithMembers() {
        throw new NotImplementedException();
    }

    // -------------------------------------------------------------------------

    @Override
    public ReasonerType getReasonerType() {
        throw new NotImplementedException();
    }

    @Override
    public void releaseKB() {
        throw new NotImplementedException();
    }

    @Override
    public OWLDatatype getDatatype(OWLDataProperty dp) {
        throw new NotImplementedException();
    }

    @Override
    public void setSynchronized() {
        throw new NotImplementedException();
    }

    @Override
    public Set<OWLClass> getClasses() {
        return Sets.union(baseReasoner.getClasses(), SpatialVocabulary.spatialClasses);
    }

    @Override
    public SortedSet<OWLIndividual> getIndividuals() {
        return baseReasoner.getIndividuals();
    }

    @Override
    protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept) {
        // FIXME
//        if (!containsSpatialExpressions(concept)) {
            return baseReasoner.getIndividuals(concept);
//        } else {
//            if (concept instanceof OWLObjectIntersectionOf) {
//                return getIndividualsOWLObjectIntersectionOf((OWLObjectIntersectionOf) concept);
//
//            } else if (concept instanceof OWLObjectSomeValuesFrom) {
//                return getIndividualsOWLObjectSomeValuesFrom((OWLObjectSomeValuesFrom) concept);
//
//            } else if (concept instanceof OWLObjectMinCardinality) {
//                return getIndividualsOWLObjectMinCardinality((OWLObjectMinCardinality) concept);
//
//            } else if (concept instanceof OWLObjectAllValuesFrom) {
//                return getIndividualsOWLObjectAllValuesFrom((OWLObjectAllValuesFrom) concept);
//
//            } else if (concept instanceof OWLObjectMaxCardinality) {
//                return getIndividualsOWLObjectMaxCardinality((OWLObjectMaxCardinality) concept);
//
//            } else if (concept instanceof OWLObjectUnionOfImplExt) {
//                return getIndividualsOWLObjectUnionOfImplExt((OWLObjectUnionOfImplExt) concept);
//
//            } else {
//                throw new RuntimeException(
//                        "Support for class expression of type " + concept.getClass() +
//                                " not implemented, yet");
//            }
//        }
    }

    @Override
    public String getBaseURI() {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> getPrefixes() {
        throw new NotImplementedException();
    }

    @Override
    public void init() throws ComponentInitException {
        baseReasoner.init();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ComponentInitException(e);
        }
        StringBuilder url = new StringBuilder("jdbc:postgresql://")
                .append(hostname).append(":")
                .append(port).append("/")
                .append(dbName);

        try {
            conn = DriverManager.getConnection(url.toString(), dbUser, dbUserPW);
        } catch (SQLException e) {
            throw new ComponentInitException(e);
        }

        try {
            ((org.postgresql.PGConnection) conn).addDataType(
                    "geometry",
                    (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
        } catch (SQLException | ClassNotFoundException e) {
            throw new ComponentInitException(e);
        }
    }

    @Override
    public boolean isConnectedWith(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {
        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;
        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        /* Two individuals are connected if they have at least one point in
         * common. This is pretty well covered by the ST_Intersects function for
         * all kinds of geo features.
         */
        String queryStr =
                "SELECT " +
                        "ST_Intersects(l.the_geom, r.the_geom) c " +
                "FROM " +
                        tableName1 + " l, " +
                        tableName2 + " r " +
                "WHERE " +
                        "l.iri=? " + // #1
                "AND " +
                        "r.iri=? ";  // #2
        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areConnected = resSet.getBoolean("c");
            return areConnected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsConnectedWith(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;
        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        /* Two individuals are connected if they have at least one point in
         * common. This is pretty well covered by the ST_Intersects function for
         * all kinds of geo features.
         */
        String queryStr =
                "SELECT " +
                        "r.iri c " +
                "FROM " +
                        tableName + " l, " +
                        pointFeatureTableName + " r " +
                "WHERE " +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                        "l.iri=? " + // #1
                "UNION " +
                "SELECT " +
                        "r.iri c " +
                "FROM " +
                        tableName + " l, " +
                        lineFeatureTableName + " r " +
                "WHERE " +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                        "l.iri=? " + // #2
                "UNION " +
                "SELECT " +
                        "r.iri c " +
                "FROM " +
                        tableName + " l, " +
                        areaFeatureTableName + " r " +
                "WHERE " +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                        "l.iri=?"; // #3

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("c");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean overlapsWith(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {
        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (tableName1.equals(pointFeatureTableName) || tableName2.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "ST_Intersects(l.the_geom, r.the_geom) o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";

        } else if (tableName1.equals(lineFeatureTableName) && tableName2.equals(areaFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) " +
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) " +
                    ") o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";
        } else if (tableName1.equals(areaFeatureTableName) && tableName2.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Overlaps(ST_Boundary(l.the_geom), r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    ") o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";
        } else {
            queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) " +
                    ") o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";
        }

        queryStr +=
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areOverlapping = resSet.getBoolean("o");
            return areOverlapping;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsOverlappingWith(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        /* Note: ST_Overlaps( ) doesn't do here. This is the description from
         * the PostGIS website:
         * "Geometry A contains Geometry B if and only if no points of B lie in
         *  the exterior of A, and at least one point of the interior of B lies
         *  in the interior of A. An important subtlety of this definition is
         *  that A does not contain its boundary, but A does contain itself."
         *
         * So the rather means 'partially overlaps' in our definition.
         */

        String queryStr = "";

        if (tableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "AND " +
                    "l.iri=?" +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? ";

        } else {
            queryStr +=
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom)" +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) ";
            if (tableName.equals(areaFeatureTableName)) {
                queryStr +=
                    "OR " +
                        "ST_Overlaps(ST_Boundary(l.the_geom), r.the_geom) ";
            } else {
                queryStr +=
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) ";
            }
            queryStr +=
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) ";
            if (tableName.equals(lineFeatureTableName)) {
                queryStr +=
                    "OR " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) ";
            }
            queryStr +=
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom)" +
                    ") " +
                "AND " +
                    "l.iri=?";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("o");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPartOf(OWLIndividual part, OWLIndividual whole) {
        String partTable = getTable(part);
        String wholeTable = getTable(whole);

        OWLIndividual partGeom;
        OWLIndividual wholeGeom;

        try {
            partGeom = feature2geom.get(part);
            wholeGeom = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (wholeTable.equals(pointFeatureTableName) &&
                (partTable.equals(lineFeatureTableName) || partTable.equals(areaFeatureTableName))) {
            return false;
        } else if (wholeTable.equals(lineFeatureTableName) && partTable.equals(areaFeatureTableName)) {
            return false;
        }

        String queryStr = "";

        if (wholeTable.equals(pointFeatureTableName)) {
            // point - point
            queryStr +=
                "SELECT " +
                    "part.the_geom=whole.the_geom p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=?";
        } else if (wholeTable.equals(lineFeatureTableName)) {
            // line - point
            if (partTable.equals(pointFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "ST_Intersects(part.the_geom, whole.the_geom) p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";

            // line - line
            } else {
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Equals(whole.the_geom, part.the_geom) " +
                    ") p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }

        } else if (wholeTable.equals(areaFeatureTableName)) {
            if (partTable.equals(pointFeatureTableName)) {
                // area - point
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else if (partTable.equals(lineFeatureTableName)) {
                // area - line
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else {
                // area - area
                queryStr +=
                "SELECT " +
                    "ST_Contains(whole.the_geom, part.the_geom) p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeom.toStringID());
            statement.setString(2, wholeGeom.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean isPartOf = resSet.getBoolean("p");
            return isPartOf;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsPartOf(OWLIndividual whole) {
        String tableName = getTable(whole);

        OWLIndividual wholeGeom;

        try {
            wholeGeom = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";


        if (tableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "AND " +
                    "l.iri=?";
        } else if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom)" +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "(" +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Equals(l.the_geom, r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? ";
        } else {
            queryStr +=
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(l.the_geom), r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(l.the_geom), r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Contains(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=?";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, wholeGeom.toStringID());

            if (tableName.equals(lineFeatureTableName)) {
                statement.setString(2, wholeGeom.toStringID());
            } else if (tableName.equals(areaFeatureTableName)) {
                statement.setString(2, wholeGeom.toStringID());
                statement.setString(3, wholeGeom.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("p");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPart(OWLIndividual whole, OWLIndividual part) {
        return isPartOf(part, whole);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsHavingPart(OWLIndividual part) {
        String tableName = getTable(part);

        OWLIndividual partGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (tableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    pointFeatureTableName + " whole " +
                "WHERE " +
                    "part.the_geom=whole.the_geom " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";
        } else if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Equals(whole.the_geom, part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";
        } else {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=?";
        }
//
        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());

            if (tableName.equals(lineFeatureTableName)) {
                statement.setString(2, partGeomIndividual.toStringID());
            } else if (tableName.equals(pointFeatureTableName)) {
                statement.setString(2, partGeomIndividual.toStringID());
                statement.setString(3, partGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("w");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isProperPartOf(OWLIndividual part, OWLIndividual whole) {
        String partTable = getTable(part);
        String wholeTable = getTable(whole);

        if (wholeTable.equals(pointFeatureTableName)) {
            return false;

        } else if (wholeTable.equals(lineFeatureTableName) &&
                partTable.equals(areaFeatureTableName)) {

            return false;
        }

        OWLIndividual partGeomIndividual;
        OWLIndividual wholeGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (wholeTable.equals(lineFeatureTableName)) {
            if (partTable.equals(pointFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "ST_Intersects(whole.the_geom, part.the_geom) pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=?";

            } else {
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "AND " +
                        "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                    ") pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }
        } else if (wholeTable.equals(areaFeatureTableName)) {
            if (partTable.equals(pointFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else if (partTable.equals(lineFeatureTableName)) {
                queryStr +=
                "SELECT  " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else {
                // area feature
                queryStr +=
                "SELECT " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=?";
            }
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean isProperPartOf = resSet.getBoolean("pp");
            return isProperPartOf;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsProperPartOf(OWLIndividual whole) {
        String tableName = getTable(whole);

        if (tableName.equals(pointFeatureTableName)) {
            // A point cannot have a proper part
            return Stream.empty();
        }

        OWLIndividual wholeGeomIndividual;

        try {
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";
        if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +  // as ST_ContainsProperly() has slightly different semantics
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? ";
        } else {
            // area feature
            queryStr +=
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ")" +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    areaFeatureTableName + " part " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, wholeGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            if (tableName.equals(areaFeatureTableName)) {
                statement.setString(3, wholeGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("pp");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasProperPart(OWLIndividual whole, OWLIndividual part) {
        return isProperPartOf(part, whole);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsHavingProperPart(OWLIndividual part) {
        String partTableName = getTable(part);

        OWLIndividual partGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (partTableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";

        } else if (partTableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";
        } else {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=?";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());

            if (partTableName.equals(lineFeatureTableName) ||
                    partTableName.equals(pointFeatureTableName)) {

                statement.setString(2, partGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("w");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // ---

    @Override
    public boolean isInside(OWLIndividual inner, OWLIndividual container) {
        return isPartOf(inner, container);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsInside(OWLIndividual container) {
        return getIndividualsPartOf(container);
    }
    // --------- getter/setter ------------------------------------------------
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbUserPW(String dbUserPW) {
        this.dbUserPW = dbUserPW;
    }

    public void setBaseReasoner(AbstractReasonerComponent baseReasoner) {
        this.baseReasoner = baseReasoner;
    }

    public void addGeometryPropertyPath(List<OWLProperty> propertyPath) {
        assert propertyPath.size() > 0;
        int i = 0;
        int len = propertyPath.size();

        while (i < (len-1)) {
            assert propertyPath.get(i) instanceof OWLObjectProperty;
            i++;
        }

        assert propertyPath.get(i) instanceof OWLDataProperty;

        geometryPropertyPaths.add(propertyPath);
    }

    protected void clearGeometryPropertyPaths() {
        geometryPropertyPaths = new HashSet<>();
    }
}
