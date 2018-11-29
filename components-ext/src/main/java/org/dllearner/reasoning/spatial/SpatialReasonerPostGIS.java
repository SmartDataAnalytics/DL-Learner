package org.dllearner.reasoning.spatial;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.postgresql.util.PGobject;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpatialReasonerPostGIS extends AbstractReasonerComponent implements SpatialReasoner {
    private final static String defaultHost = "localhost";
    private final static int defaultPort = 5432;
    private DBConnectionSetting dbConnectionSetting;
    private Connection conn;

    private double nearRadiusInMeters = 5; // meters
    private double runsAlongToleranceInMeters = 20; // meters
    private boolean isContainmentRelationReflexive = false;
    private boolean isIsNearRelationReflexive = true;
    private Set<List<OWLProperty>> geometryPropertyPaths = new HashSet<>();

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
                                                reasoner.getRelatedIndividuals(i, objProp));
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
                                            reasoner.getPropertyMembers(objProp);

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

    protected AbstractReasonerComponent reasoner;
    // TODO: replace with more accepted IRIs
    private OWLClass areaFeatureClass = new OWLClassImpl(
            IRI.create("http://dl-learner.org/ont/spatial#AreaFeature"));
    private String areaFeatureTableName = "area_feature";
    private OWLClass lineFeatureClass = new OWLClassImpl(
            IRI.create("http://dl-learner.org/ont/spatial#LineFeature"));
    private String lineFeatureTableName = "line_feature";
    private OWLClass pointFeatureClass = new OWLClassImpl(
            IRI.create("http://dl-learner.org/ont/spatial#PointFeature"));
    private String pointFeatureTableName = "point_feature";

    public SpatialReasonerPostGIS() {
        super();
    }

    public SpatialReasonerPostGIS(
            AbstractReasonerComponent reasoner,
            DBConnectionSetting dbConnectionSetting) {
        super();
        this.dbConnectionSetting = dbConnectionSetting;
        this.reasoner = reasoner;
    }

    // <getter/setter>
    public void setDbConnectionSetting(DBConnectionSetting dbConnectionSetting) {
        this.dbConnectionSetting = dbConnectionSetting;
    }

    public void setReasoner(AbstractReasonerComponent reasoner) {
        this.reasoner = reasoner;
    }

    public void setNearRadiusInMeters(double nearRadiusInMeters) {
        this.nearRadiusInMeters = nearRadiusInMeters;
    }

    public void setRunsAlongToleranceInMeters(double runsAlongToleranceInMeters) {
        this.runsAlongToleranceInMeters = runsAlongToleranceInMeters;
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

    public void setAreaFeatureClass(OWLClass areaFeatureClass) {
        this.areaFeatureClass = areaFeatureClass;
    }

    public void setAreaFeatureClass(String iriString) {
        this.areaFeatureClass = new OWLClassImpl(IRI.create(iriString));
    }

    public void setLineFeatureClass(OWLClass lineFeatureClass) {
        this.lineFeatureClass = lineFeatureClass;
    }

    public void setLineFeatureClass(String iriString) {
        this.lineFeatureClass = new OWLClassImpl(IRI.create(iriString));
    }

    public void setPointFeatureClass(OWLClass pointFeatureClass) {
        this.pointFeatureClass = pointFeatureClass;
    }

    public void setPointFeatureClass(String iriString) {
        this.pointFeatureClass = new OWLClassImpl(IRI.create(iriString));
    }

    public void setIsContainmentRelationReflexive(boolean isContainmentRelationReflexive) {
        this.isContainmentRelationReflexive = isContainmentRelationReflexive;
    }

    public void setIsIsNearRelationReflexive(boolean isIsNearRelationReflexive) {
        this.isIsNearRelationReflexive = isIsNearRelationReflexive;
    }
    // </getter/setter>

    // <implemented interface/base class methods>
    @Override
    public void init() throws ComponentInitException {
        this.reasoner.init();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ComponentInitException(e);
        }
        StringBuilder url = new StringBuilder("jdbc:postgresql://")
                .append(dbConnectionSetting.host).append(":")
                .append(dbConnectionSetting.port).append("/")
                .append(dbConnectionSetting.dbName);

        try {
            conn = DriverManager.getConnection(
                    url.toString(), dbConnectionSetting.user,
                    dbConnectionSetting.password);
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
    public ReasonerType getReasonerType() {
        return reasoner.getReasonerType();
    }

    @Override
    public void releaseKB() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public OWLDatatype getDatatype(OWLDataProperty dp) {
        // TODO: Add spatial data property handling here

        return reasoner.getDatatype(dp);
    }

    @Override
    public void setSynchronized() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Set<OWLClass> getClasses() {
        return Sets.union(reasoner.getClasses(), SpatialVocabulary.spatialClasses);
    }

    @Override
    public SortedSet<OWLIndividual> getIndividuals() {
        return reasoner.getIndividuals();
    }

    @Override
    protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept) {
        if (!containsSpatialExpressions(concept)) {
            return reasoner.getIndividuals(concept);
        } else {
            if (concept instanceof OWLObjectIntersectionOf) {
                return getIndividualsOWLObjectIntersectionOf((OWLObjectIntersectionOf) concept);

            } else if (concept instanceof OWLObjectSomeValuesFrom) {
                return getIndividualsOWLObjectSomeValuesFrom((OWLObjectSomeValuesFrom) concept);

            } else if (concept instanceof OWLObjectMinCardinality) {
                return getIndividualsOWLObjectMinCardinality((OWLObjectMinCardinality) concept);

            } else if (concept instanceof OWLObjectAllValuesFrom) {
                return getIndividualsOWLObjectAllValuesFrom((OWLObjectAllValuesFrom) concept);

            } else if (concept instanceof OWLObjectMaxCardinality) {
                return getIndividualsOWLObjectMaxCardinality((OWLObjectMaxCardinality) concept);

            } else if (concept instanceof OWLObjectUnionOfImplExt) {
                return getIndividualsOWLObjectUnionOfImplExt((OWLObjectUnionOfImplExt) concept);

            } else {
                throw new RuntimeException(
                        "Support for class expression of type " + concept.getClass() +
                                " not implemented, yet");
            }
        }
    }

    @Override
    public String getBaseURI() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Map<String, String> getPrefixes() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getConnectedIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean areConnected(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getDisconnectedIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean areDisconnected(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getIndividualsWhichArePartOf(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isPartOf(OWLIndividual part, OWLIndividual whole) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getIndividualsWhichAreProperPartOf(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isProperPartOf(OWLIndividual part, OWLIndividual whole) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getSpatiallyEqualIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean areSpatiallyEqual(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getOverlappingIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean areOverlapping(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getIndividualsDiscreteFrom(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean areDiscreteFromEachOther(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getPartiallyOverlappingIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean arePartiallyOverlapping(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getExternallyConnectedIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean areExternallyConnected(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getIndividualsWhichAreTangentialProperPartOf(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isTangentialProperPartOf(OWLIndividual part, OWLIndividual whole) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Stream<OWLIndividual> getIndividualsWhichAreNonTangentialProperPartOf(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isSpatialSumOf(OWLIndividual sum, Set<OWLIndividual> parts) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isEquivalentToUniversalSpatialRegion(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isComplementOf(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isIntersectionOf(OWLIndividual intersection, OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isDifferenceOf(OWLIndividual difference, OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isNear(OWLIndividual individual1, OWLIndividual individual2) {
        String table1 = getTable(individual1);
        String table2 = getTable(individual2);

        OWLIndividual geometryIndividual1 = null;
        OWLIndividual geometryIndividual2 = null;

        try {
            geometryIndividual1 = feature2geom.get(individual1);
            geometryIndividual2 = feature2geom.get(individual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (geometryIndividual1.equals(geometryIndividual2)) {
            return isIsNearRelationReflexive;
        }

        StringBuilder queryStr = new StringBuilder();

        queryStr.append("SELECT ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ?) ")
                .append("FROM ")
                    .append(table1).append(" l, ").append(table2).append(" r ")
                .append("WHERE ")
                    .append("l.iri=? AND r.iri=?");

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr.toString());
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geometryIndividual1.toStringID());
            statement.setString(3, geometryIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();
            resSet.next();

            return resSet.getBoolean(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<OWLIndividual> getNearSpatialIndividuals(OWLIndividual featureIndividual) {
        // Non-feature individuals by definition aren't near anything
        if (!reasoner.hasType(SpatialVocabulary.SpatialFeature, featureIndividual))
            return new HashSet<>();

        OWLIndividual geometryIndividual;
        String indivTable = getTable(featureIndividual);

        try {
            geometryIndividual = feature2geom.get(featureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        StringBuilder queryStr = new StringBuilder();

        queryStr.append("SELECT ")
                    .append("l.iri ")
                .append("FROM ")
                    .append(pointFeatureTableName).append(" l, ")
                    .append(indivTable).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ?) ")
                .append("AND ")
                    .append("r.iri=? ");
        if (!isIsNearRelationReflexive) {
            queryStr.append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        queryStr.append("SELECT ")
                    .append("l.iri ")
                .append("FROM ")
                    .append(lineFeatureTableName).append(" l, ")
                    .append(indivTable).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ?) ")
                .append("AND ")
                    .append("r.iri=? ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        queryStr.append("SELECT ")
                    .append("l.iri ")
                .append("FROM ")
                    .append(areaFeatureTableName).append(" l, ")
                    .append(indivTable).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ?) ")
                .append("AND ")
                    .append("r.iri=?");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        Set<OWLIndividual> featureIndividuals = new HashSet<>();
        try {
            PreparedStatement statement = conn.prepareStatement(queryStr.toString());
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geometryIndividual.toStringID());
            statement.setDouble(3, nearRadiusInMeters);
            statement.setString(4, geometryIndividual.toStringID());
            statement.setDouble(5, nearRadiusInMeters);
            statement.setString(6, geometryIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            while (resSet.next()) {
                String indivIRIStr = resSet.getString(1);
                featureIndividuals.add(
                        geom2feature.get(new OWLNamedIndividualImpl(
                                IRI.create(indivIRIStr))));
            }


        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return featureIndividuals;
    }

    @Override
    public boolean isInside(OWLIndividual containedIndividual, OWLIndividual containingIndividual) {
        String containedTable = getTable(containedIndividual);
        String containerTable = getTable(containingIndividual);

//        OWLIndividual containedGeometryIndividual =
//                getGeometryIndividual(containedIndividual);
//        OWLIndividual containingGeometryIndividual =
//                getGeometryIndividual(containingIndividual);
        OWLIndividual containedGeometryIndividual = null;
        OWLIndividual containingGeometryIndividual = null;

        try {
            containedGeometryIndividual = feature2geom.get(containedIndividual);
            containingGeometryIndividual = feature2geom.get(containingIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // TODO: Think about this again. Not sure whether this test should be applied to features or geometries
        if (containedGeometryIndividual.equals(containingGeometryIndividual)) {
            return isContainmentRelationReflexive;
        }

        try {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT ST_CONTAINS(" + containerTable + ".the_geom, " +
                            containedTable + ".the_geom) " +
                        "FROM " + containerTable + ", " + containedTable + " " +
                        "WHERE " + containedTable + ".iri=? " +
                            "AND " + containerTable + ".iri=?");
            statement.setString(1, containedGeometryIndividual.toStringID());
            statement.setString(2, containingGeometryIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();
            resSet.next();
            return resSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<OWLIndividual> getContainedSpatialIndividuals(OWLIndividual featureIndividual) {
        // Non-feature individuals by definition aren't spatially contained anywhere
        if (!reasoner.hasType(SpatialVocabulary.SpatialFeature, featureIndividual))
            return new HashSet<>();

        OWLIndividual geometryIndividual = null;
        try {
            geometryIndividual = feature2geom.get(featureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        String containerTableName = getTable(featureIndividual);
        Set<String> containedGeomIndividualIRIStrings = new HashSet<>();

        StringBuilder queryStr = new StringBuilder();

        if (containerTableName.equals(pointFeatureTableName)) {
            // Point feature cannot contain anything but
            // - itself (should be filtered if `isContainmentRelationReflexive`
            //   is false!)
            queryStr.append("SELECT l.iri ")
                    .append("FROM ").append(pointFeatureTableName).append(" l, ")
                                    .append(pointFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                                    .append("AND r.iri=?");

        } else if (containerTableName.equals(lineFeatureTableName)) {
            // Line feature may contain
            // - itself (should be filtered if `isContainmentRelationReflexive`
            //   is false!)
            // - other line features comprised of a subset of consecutive points
            //   --> cross product with `lineFeatureTableName`
            // - point features which lie right on a line, i.e. being one of the
            //   line string points
            //   --> cross product with `pointFeatureTableName`
            // ==> UNION required to find all lines and points

            // e.g.
            // SELECT l.iri
            // FROM line_feature l, line_feature r
            // WHERE ST_Contains(r.the_geom, l.the_geom)
            //   AND r.iri='http://dl-learner.org/ont/spatial-test#turnerweg_geometry'
            // UNION
            // SELECT l.iri
            // FROM point_feature l, line_feature r
            // WHERE ST_Contains(r.the_geom, l.the_geom)
            //   AND r.iri='http://dl-learner.org/ont/spatial-test#turnerweg_geometry';

            // line-line comparison
            queryStr.append("SELECT l.iri ")
                    .append("FROM ").append(lineFeatureTableName).append(" l, ")
                                    .append(lineFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                                    .append("AND r.iri=? ")
            // line-point comparison
                    .append("UNION ")
                    .append("SELECT l.iri ")
                    .append("FROM ").append(pointFeatureTableName).append(" l, ")
                                    .append(lineFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                                    .append("AND r.iri=?");

        } else if (containerTableName.equals(areaFeatureTableName)) {
            // Area feature may contain
            // - itself (should be filtered if `isContainmentRelationReflexive`
            //   is false!)
            // - other area features
            //   --> cross product with `areaFeatureTableName`
            // - line features
            //   --> cross product with `lineFeatureTableName`
            // - point features
            //   --> cross product with `pointFeatureTableName`
            // ==> UNION required

            // area-area comparison
            queryStr.append("SELECT l.iri ")
                    .append("FROM ").append(areaFeatureTableName).append(" l, ")
                                    .append(areaFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                    .append("AND r.iri=? ")
            // area-line comparison
                    .append("UNION ")
                    .append("SELECT l.iri ")
                    .append("FROM ").append(lineFeatureTableName).append(" l, ")
                                    .append(areaFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                    .append("AND r.iri=? ")
            // area-point comparison
                    .append("UNION ")
                    .append("SELECT l.iri ")
                    .append("FROM ").append(pointFeatureTableName).append(" l, ")
                                    .append(areaFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                    .append("AND r.iri=?");
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr.toString());
            for (int i=1; i<=statement.getParameterMetaData().getParameterCount(); i++) {
                statement.setString(i, geometryIndividual.toStringID());
            }
            ResultSet resSet = statement.executeQuery();
            while (resSet.next()) {
                String indivIRIStr = resSet.getString(1);
                containedGeomIndividualIRIStrings.add(indivIRIStr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Stream<OWLIndividual> containedIndividuals =
                containedGeomIndividualIRIStrings.stream()
                        .map((String iriStr) -> new OWLNamedIndividualImpl(IRI.create(iriStr)))
//                        .map((OWLIndividual i) -> getFeatureIndividual(i))
                        .map((OWLIndividual i) -> {
                            try {
                                return geom2feature.get(i);
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(Objects::nonNull)
                        // if `isContainmentRelationReflexive` is set, do not filter; otherwise filter out input `featureIndividual`
                        .filter((OWLIndividual i) -> isContainmentRelationReflexive ? true : !i.equals(featureIndividual));

        return containedIndividuals.collect(Collectors.toSet());
    }

    /**
     * Returns `true` if the first input OWL individual runs along the second
     * input OWL individual, and `false` otherwise.
     * To let the PostGIS database do the heavy lifting the approach to get this
     * information is as follows:
     * a) Build a polygon representing the hull around the first OWL
     *    individual's geometry with a margin of `runsAlongToleranceInMeters`
     *    meters.
     * b) Build the intersection of the polygon built in a) and the second
     *    input OWL individual's geometry (which is again a line feature)
     * c) Check whether the length of the line feature built in b) is longer
     *    than 2.5 times the `runsAlongToleranceInMeters` tolerance (to
     *    eliminate line features crossing the first input OWL individual's
     *    geometry in more or less 90 degrees).
     *
     * Example query:
     *
     * SELECT
     *    ST_Length(
     *        ST_Intersection(  -- b)
     *            ST_Buffer(move.the_geom::geography, 20, 'endcap=flat'),  -- a)
     *            street.the_geom)::geography) > 50  -- c)
     * FROM
     *    line_feature move, line_feature street
     * WHERE
     *    move.iri='http://dl-learner.org/ont/spatial#move_02_geometry'
     * AND
     *    street.iri='http://dl-learner.org/ont/spatial#bergmannstr_05_geometry';
     *
     */
    @Override
    public boolean runsAlong(OWLIndividual individual1, OWLIndividual individual2) {
        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;
        try {
            geomIndividual1 = feature2geom.get(individual1);
            geomIndividual2 = feature2geom.get(individual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = new StringBuilder()
                .append("SELECT ")
                    .append("ST_Length(")
                        .append("ST_Intersection(")
                            .append("ST_Buffer(move.the_geom::geography, ?, 'endcap=flat'),")  // 1
                            .append("street.the_geom)::geography) > ? ") // 2
                .append("FROM ")
                    .append(lineFeatureTableName).append(" move, ")
                    .append(lineFeatureTableName).append(" street ")
                .append("WHERE ")
                    .append("move.iri=? ") // 3
                .append("AND ")
                    .append("street.iri=?").toString(); // 4

        boolean runsAlong;

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            // tolerance for the hull (ST_Buffer)
            statement.setDouble(1, runsAlongToleranceInMeters);
            // min expected length
            statement.setDouble(2, 2.5 * runsAlongToleranceInMeters);
            // move IRI
            statement.setString(3, geomIndividual1.toStringID());
            // street IRI
            statement.setString(4, geomIndividual2.toStringID());

            ResultSet resultSet = statement.executeQuery();

            resultSet.next();
            runsAlong = resultSet.getBoolean(1);

            assert !resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return runsAlong;
    }

    @Override
    public Set<OWLIndividual> getSpatialIndividualsRunningAlong(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean passes(OWLIndividual passingIndividual, OWLIndividual passedIndividual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Set<OWLIndividual> getPassedSpatialIndividuals(OWLIndividual passingIndividual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Set<OWLIndividual> getPassingSpatialIndividuals(OWLIndividual passedIndividual) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isNonTangentialProperPartOf(OWLIndividual part, OWLIndividual whole) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public AbstractReasonerComponent getBaseReasoner() {
        return reasoner;
    }
    // </implemented interface/base class methods>

    // <base reasoner interface methods>
    public boolean isSuperClassOf(OWLClassExpression superClass, OWLClassExpression subClass) {
        return reasoner.isSuperClassOf(superClass, subClass);
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyDomains() {
        Map<OWLObjectProperty, OWLClassExpression> domainsMap = reasoner.getObjectPropertyDomains();

        // TODO: Add spatial aspect-specific stuff here
        domainsMap.put(SpatialVocabulary.isInside, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.isNear, SpatialVocabulary.SpatialFeature);

        return domainsMap;
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRanges() {
        Map<OWLObjectProperty, OWLClassExpression> rangesMap = reasoner.getObjectPropertyRanges();

        // TODO: Add spatial aspect-specific stuff here
        rangesMap.put(SpatialVocabulary.isInside, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.isNear, SpatialVocabulary.SpatialFeature);

        return rangesMap;
    }

    @Override
    public Map<OWLDataProperty, OWLClassExpression> getDataPropertyDomains() {
        Map<OWLDataProperty, OWLClassExpression> domainsMap = reasoner.getDataPropertyDomains();

        // TODO: Add spatial aspect-specific stuff here

        return domainsMap;
    }

    @Override
    protected SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression ce) {
        SortedSet<OWLClassExpression> subClasses = reasoner.getSubClasses(ce);

        // TODO: Add spatial aspect-specific stuff here

        return subClasses;
    }

    @Override
    protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression ce) {
        SortedSet<OWLClassExpression> superClasses = reasoner.getSubClasses(ce);

        // TODO: Add spatial aspect-specific stuff here

        return superClasses;
    }

    @Override
    protected Set<OWLDataProperty> getDatatypePropertiesImpl() {
        Set<OWLDataProperty> dataProperties = reasoner.getDatatypeProperties();

        dataProperties.addAll(SpatialVocabulary.spatialDataProperties);

        return dataProperties;
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty objectProperty) {
        SortedSet<OWLObjectProperty> subProperties = reasoner.getSubProperties(objectProperty);

        // TODO: add spatial aspect-specific stuff here

        return subProperties;
    }

    @Override
    protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty dataProperty) {
        SortedSet<OWLDataProperty> subProperties = reasoner.getSubProperties(dataProperty);

        // TODO: add spatial aspect-specific stuff here

        return subProperties;
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty objectProperty) {
        SortedSet<OWLObjectProperty> superProperties = reasoner.getSuperProperties(objectProperty);

        // TODO: add spatial aspect-specific stuff here

        return superProperties;
    }

    @Override
    protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty dataProperty) {
        SortedSet<OWLDataProperty> superProperties = reasoner.getSuperProperties(dataProperty);

        // TODO: add spatial aspect-specific stuff here

        return superProperties;
    }

    @Override
    protected Set<OWLObjectProperty> getObjectPropertiesImpl() {
        return Sets.union(
                reasoner.getObjectProperties(),
                SpatialVocabulary.spatialObjectProperties);
    }

    @Override
    protected OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
        if (objectProperty.equals(SpatialVocabulary.isInside)) {
            return SpatialVocabulary.SpatialFeature;

        } else if (objectProperty.equals(SpatialVocabulary.isNear)) {
            return SpatialVocabulary.SpatialFeature;

            // TODO: Add further spatial object properties here
        } else {
            return reasoner.getRange(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
        if (objectProperty.equals(SpatialVocabulary.isInside)) {
            return SpatialVocabulary.SpatialFeature;

        } else if (objectProperty.equals(SpatialVocabulary.isNear)) {
            return SpatialVocabulary.SpatialFeature;

            // TODO: Add further spatial object properties here
        } else {
            return reasoner.getDomain(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLDataProperty dataProperty) {
        // TODO: Add spatial data property handling here

        return reasoner.getDomain(dataProperty);
    }

    @Override
    protected Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
        // TODO: Add spatial int data properties here

        return reasoner.getIntDatatypeProperties();
    }

    @Override
    protected Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
        // TODO: Add spatial double data properties here

        return reasoner.getDoubleDatatypeProperties();
    }

    @Override
    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(
            OWLObjectProperty objectProperty) {
        if (SpatialVocabulary.spatialObjectProperties.contains(objectProperty)) {
            if (objectProperty.equals(SpatialVocabulary.isInside)) {
                // isInside
                return getIsInsideMembers();

            } else if (objectProperty.equals(SpatialVocabulary.isNear)) {
                // isNear
                return getIsNearMembers();

                // TODO: Add further spatial object properties here
            } else {
                throw new RuntimeException(
                        "Spatial object property " + objectProperty + "not " +
                                "handled in getPropertyMembersImpl( )");
            }
        } else {
            return reasoner.getPropertyMembers(objectProperty);
        }
    }

    @Override
    protected Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
        // TODO: Add spatial boolean data properties here

        return reasoner.getBooleanDatatypeProperties();
    }

    @Override
    protected Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
        // TODO: Add spatial string data properties here

        return reasoner.getDatatypeProperties();
    }

    @Override
    public boolean hasTypeImpl(OWLClassExpression ce, OWLIndividual individual) {
        if (containsSpatialExpressions(ce)) {
            return hasTypeSpatial(ce, individual);
        } else {
            return reasoner.hasType(ce, individual);
        }
    }
    // </base reasoner interface methods>

    protected boolean hasTypeSpatial(OWLClassExpression ce, OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectUnionOfImplExt}. The
     * unraveling is needed to recursively cal getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectUnionOfImplExt(OWLObjectUnionOfImplExt concept) {
        Set<OWLClassExpression> unionParts = concept.getOperands();

        Set<OWLIndividual> resultIndividuals = new HashSet<>();

        for (OWLClassExpression unionPart : unionParts) {
            resultIndividuals.addAll(getIndividualsImpl(unionPart));
        }

        return new TreeSet<>(resultIndividuals);
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectMaxCardinality}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectMaxCardinality(OWLObjectMaxCardinality concept) {
        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();
        int maxCardinality = concept.getCardinality();

        // There are three cases to consider:
        // 1) `prop` is a known spatial property that requires special treatment
        // 2) `prop` is a non-atomic property --> still TODO
        // 3) `prop` ia a non-spatial atomic property and the filler contains
        //    spatial components

        // 1) `prop` is a known spatial property
        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {

            SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

            // isInside
            if (prop.equals(SpatialVocabulary.isInside)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    Set<OWLIndividual> indivsInsideFillerIndiv =
                            getContainedSpatialIndividuals(fillerIndiv);

                    updateCounterMap(individualsWCounts, indivsInsideFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isNear
            } else if (prop.equals(SpatialVocabulary.isNear)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    Set<OWLIndividual> indivsNearFillerIndiv =
                            getNearSpatialIndividuals(fillerIndiv);

                    updateCounterMap(individualsWCounts, indivsNearFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            } else {
                throw new RuntimeException(
                        "spatial object property " + prop + " not supported, yet");
            }

        } else {
            // 2) `prop` is a non-atomic property
            if (prop instanceof OWLObjectIntersectionOf) {
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

            // 3) `prop` ia a non-spatial atomic property and the filler contains
            //    spatial components
            } else {
                SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

                Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                        reasoner.getPropertyMembers(prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                assert propIndividuals != null;
                for (Map.Entry e : propIndividuals.entrySet()) {
                    OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                    SortedSet<OWLIndividual> values = (SortedSet<OWLIndividual>) e.getValue();

                    // set intersection with filler individuals
                    values.retainAll(fillerIndivs);

                    // now `values` only contains those OWL individuals, that
                    // - are instances of the filler class expression
                    // - are assigned to another OWL individual through the
                    //   property `prop`

                    if (values.size() <= maxCardinality) {
                        resultIndividuals.add(keyIndiv);
                    }

                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    private boolean areAllValuesFromFiller(
            SortedSet<OWLIndividual> propertyValues,
            SortedSet<OWLIndividual> fillerIndividuals) {

        // Check if there is any individual i2 with
        // <property>(<individual>, i2) and not <filler>(i2), i.e.
        // i2 not in `propertyValues` .

        if (propertyValues.isEmpty()) {
            // Trivial case where individual has no values assigned via
            // <property> and thus there are no values not being an instance of
            // <filler> --> all values are an instance of <filler>
            return true;
        }

        // Create copy, since remove is called on it, later
        TreeSet<OWLIndividual> propertyValuesCopy = new TreeSet<>(propertyValues);

        propertyValuesCopy.removeAll(fillerIndividuals);
        // Now, `propertyValuesCopy` only contains individuals that are not of
        // filler-type. If `propertyValuesCopy` is empty all property members
        // were of filler-type and <individual> is an instance of the overall
        // OWLObjectAllValuesFrom class expression
        if (propertyValuesCopy.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectAllValuesFrom}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectAllValuesFrom(OWLObjectAllValuesFrom concept) {
        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();

        // There are three cases to consider:
        // 1) `prop` is a known spatial property that requires special treatment
        // 2) `prop` is a non-atomic property --> still TODO
        // 3) `prop` ia a non-spatial atomic property and the filler contains
        //    spatial components

        // 1) `prop` is a known spatial property
        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {

            SortedSet<OWLIndividual> fillerIndividuals = getIndividualsImpl(filler);

            // isInside
            if (prop.equals(SpatialVocabulary.isInside)) {
                // All individuals are instances of \forall :isInside <filler>
                // as long as they aren't inside something not being of type
                // <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsInsideMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // isNear
            } else if (prop.equals(SpatialVocabulary.isNear)) {
                // All individuals are instances of \forall :isNear <filler>
                // as long as they aren't near something not being of type
                // <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsNearMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            } else {
                throw new RuntimeException("No implementation for " + prop + ", yet");
            }

        } else {
            // 2) `prop` is a non-atomic property
            if (prop instanceof OWLObjectInverseOf) {
                // TODO: Implement
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

            // 3) `prop` ia a non-spatial atomic property
            } else {
                // Get filler individuals and property members and check whether
                // all the property member values (i.e. objects viewed from a
                // triple perspective) are contained in the filler individuals
                // set

                // TODO: Consider sub-properties
                Set<OWLIndividual> fillerIndividuals = getIndividualsImpl(filler);
                Map<OWLIndividual, SortedSet<OWLIndividual>> propertyMembers =
                        reasoner.getPropertyMembers(prop.asOWLObjectProperty());
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                assert propertyMembers != null;
                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e : propertyMembers.entrySet()) {
                    // s --> subject in triple view; o --> object in triple view
                    OWLIndividual s = e.getKey();
                    Set<OWLIndividual> os = new HashSet<>(e.getValue());

                    if (os.isEmpty()) {
                        // Trivial case where `s` has no values assigned via
                        // `prop` and thus there are no values not being an
                        // instance of `filler` --> all values are an instance
                        // of `filler`
                        resultIndividuals.add(s);
                        continue;
                    }

                    os.removeAll(fillerIndividuals);
                    // Now, `os` only contains individuals that are not of
                    // filler-type. If `os` is empty all property members were
                    // of filler-type and `s` is an instance of the overall
                    // OWLObjectAllValuesFrom class expression
                    if (os.isEmpty()) {
                        resultIndividuals.add(s);
                    }
                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    private void updateCounterMap(Map<OWLIndividual, Integer> counterMap, Set<OWLIndividual> individuals) {
        for (OWLIndividual indivInsideFillerIndiv : individuals) {
            if (!counterMap.containsKey(indivInsideFillerIndiv)) {
                counterMap.put(indivInsideFillerIndiv, 1);

            } else {
                int tmpCnt = counterMap.get(indivInsideFillerIndiv);
                tmpCnt++;
                counterMap.put(indivInsideFillerIndiv, tmpCnt);
            }
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectMinCardinality}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectMinCardinality(OWLObjectMinCardinality concept) {
        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();
        int minCardinality = concept.getCardinality();

        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {

            SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

            // isInside
            if (prop.equals(SpatialVocabulary.isInside)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    Set<OWLIndividual> indivsInsideFillerIndiv =
                            getContainedSpatialIndividuals(fillerIndiv);

                    updateCounterMap(individualsWCounts, indivsInsideFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isNear
            } else if (prop.equals(SpatialVocabulary.isNear)) {
                Map<OWLIndividual, Integer> indivsNearFillerIndivWCount =
                        new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    Set<OWLIndividual> individualsNearFillerIndiv =
                            getNearSpatialIndividuals(fillerIndiv);

                    updateCounterMap(indivsNearFillerIndivWCount, individualsNearFillerIndiv);
                }

                return indivsNearFillerIndivWCount.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            } else {
                throw new RuntimeException(
                        "spatial object property " + prop + " not supported, yet");
            }

        } else {
            if (prop instanceof OWLObjectInverseOf) {
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

            } else {
                // TODO: Check whether sub-properties covered already!
                SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

                Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                        reasoner.getPropertyMembers(prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();
                assert propIndividuals != null;
                for (Map.Entry e : propIndividuals.entrySet()) {
                    OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                    SortedSet<OWLIndividual> values = (SortedSet<OWLIndividual>) e.getValue();

                    // set intersection with filler individuals
                    values.retainAll(fillerIndivs);

                    // now `values` only contains those OWL individuals, that
                    // - are instances of the filler class expressions
                    // - are assigned to another OWL individual through
                    //   the property `prop`

                    if (values.size() >= minCardinality) {
                        resultIndividuals.add(keyIndiv);
                    }
                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectSomeValuesFrom}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectSomeValuesFrom(OWLObjectSomeValuesFrom concept) {
        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();

        // There are four cases to consider
        // 1) The property expression is atomic and a spatial property
        //    --> query property members through PostGIS and recurse to get
        //        filler instances
        // 2) The property expression is not atomic but contains a spatial
        //    property
        //    --> not implemented, yet
        // 3) The property expression is non-spatial
        //    --> recurse

        // 1) The property expression is atomic and a spatial property
        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {
            SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

            // isInside
            if (prop.equals(SpatialVocabulary.isInside)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    Set<OWLIndividual> indivsInsideFillerIndiv =
                            getContainedSpatialIndividuals(fillerIndiv);
                    individuals.addAll(indivsInsideFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // isNear
            } else if (prop.equals(SpatialVocabulary.isNear)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    Set<OWLIndividual> individualsNearFillerIndiv =
                            getNearSpatialIndividuals(fillerIndiv);
                    individuals.addAll(individualsNearFillerIndiv);
                }

                return new TreeSet<>(individuals);

            } else {
                throw new RuntimeException(
                        "spatial object property " + prop + " not supported, yet");
            }
        } else {
            if (prop instanceof OWLObjectInverseOf) {
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

            } else {
                // TODO: Check whether super properties are covered already!

                SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

                Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                        reasoner.getPropertyMembers(prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();
                assert propIndividuals != null;

                for (Map.Entry e : propIndividuals.entrySet()) {
                    // e: an entry of the shape
                    //    OWLIndividual -> SortedSet<OWLIndividual>

                    OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                    SortedSet<OWLIndividual> values =
                            (SortedSet<OWLIndividual>) e.getValue();

                    // set intersection with filler individuals
                    values.retainAll(fillerIndivs);

                    // now values only contains those OWL individuals,
                    // that
                    // - are instances of the filler class expressions
                    // - are assigned to another OWL individual through
                    //   the property `prop`

                    if (!values.isEmpty()) {
                        resultIndividuals.add(keyIndiv);
                    }
                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    /**
     * Called in the getIndividualsImpl method in case the class expression to
     * get the instances for is an {@link OWLObjectIntersectionOf}. The
     * unraveling is needed to recusively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectIntersectionOf(OWLObjectIntersectionOf intersection) {
        Set<OWLIndividual> individuals = null;

        for (OWLClassExpression ce : intersection.getOperands()) {
            SortedSet<OWLIndividual> opIndividuals = getIndividualsImpl(ce);

            if (individuals == null) {
                individuals = opIndividuals;
            } else {
                individuals = Sets.intersection(individuals, opIndividuals);
            }
        }

        return new TreeSet<>(individuals);
    }

    private Map<OWLIndividual, SortedSet<OWLIndividual>> getIsInsideMembers() {
        StringBuilder queryString = new StringBuilder();
        /*
         * Expensive query which has to cover all those combinations
         *                --(contained in/is inside)-->
         * 1) `pointFeatureTableName` - `pointFeatureTableName`  (can be skipped if
         *    isContainmentRelationReflexive == false)
         * 2) `pointFeatureTableName` - `lineFeatureTableName`
         * 3) `pointFeatureTableName` - `areaFeatureTableName`
         * 4) `lineFeatureTableName` - `lineFeatureTableName`
         * 5) `lineFeatureTableName` - `areaFeatureTableName`
         * 6) `areaFeatureTableName` - `areaFeatureTableName`
         *
         */

        if (isContainmentRelationReflexive) {
            // 1) `pointFeatureTableName` - `pointFeatureTableName`
            queryString
                    .append("SELECT l.iri contained, r.iri container ")
                    .append("FROM ").append(pointFeatureTableName).append(" l, ")
                                    .append(pointFeatureTableName).append(" r ")
                    .append("WHERE ST_Contains(r.the_geom, l.the_geom) ")
                    .append("UNION ");
        }

        // 2) `pointFeatureTableName` - `lineFeatureTableName`
        queryString
                .append("SELECT l.iri contained, r.iri container ")
                .append("FROM ").append(pointFeatureTableName).append(" l, ")
                                .append(lineFeatureTableName).append(" r ")
                .append("WHERE ST_Contains(r.the_geom, l.the_geom) ");

        queryString.append("UNION ");

        // 3) `pointFeatureTableName` - `areaFeatureTableName`
        queryString
                .append("SELECT l.iri contained, r.iri container ")
                .append("FROM ").append(pointFeatureTableName).append(" l, ")
                                .append(areaFeatureTableName).append(" r ")
                .append("WHERE ST_Contains(r.the_geom, l.the_geom) ");

        queryString.append("UNION ");

        // 4) `lineFeatureTableName` - `lineFeatureTableName`
        queryString
                .append("SELECT l.iri contained, r.iri container ")
                .append("FROM ").append(lineFeatureTableName).append(" l, ")
                                .append(lineFeatureTableName).append(" r ")
                .append("WHERE ST_Contains(r.the_geom, l.the_geom) ");
        if (!isContainmentRelationReflexive) {
            queryString.append("AND NOT l.iri=r.iri ");
        }

        queryString.append("UNION ");

        // 5) `lineFeatureTableName` - `areaFeatureTableName`
        queryString
                .append("SELECT l.iri contained, r.iri container ")
                .append("FROM ").append(lineFeatureTableName).append(" l, ")
                                .append(areaFeatureTableName).append(" r ")
                .append("WHERE ST_Contains(r.the_geom, l.the_geom) ");

        queryString.append("UNION ");

        // 6) `areaFeatureTableName` - `areaFeatureTableName`
        queryString
                .append("SELECT l.iri contained, r.iri container ")
                .append("FROM ").append(areaFeatureTableName).append(" l, ")
                                .append(areaFeatureTableName).append(" r ")
                .append("WHERE ST_Contains(r.the_geom, l.the_geom) ");
        if (!isContainmentRelationReflexive) {
            queryString.append("AND NOT l.iri=r.iri");
        }

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryString.toString());

            while (resultSet.next()) {
                String containedIndivIRIStr = resultSet.getString("contained");
                String containerIndivIRIStr = resultSet.getString("container");

                OWLIndividual containedGeometryIndividual =
                        new OWLNamedIndividualImpl(IRI.create(containedIndivIRIStr));
                OWLIndividual containerGeometryIndividual =
                        new OWLNamedIndividualImpl(IRI.create(containerIndivIRIStr));

                // convert geometries to features
                OWLIndividual containedFeatureIndividual =
                        geom2feature.get(containedGeometryIndividual);
                OWLIndividual containerFeatureIndividual =
                        geom2feature.get(containerGeometryIndividual);

                if (!members.containsKey(containedFeatureIndividual)) {
                    members.put(containedFeatureIndividual, new TreeSet<>());
                }
                members.get(containedFeatureIndividual).add(containerFeatureIndividual);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    private Map<OWLIndividual, SortedSet<OWLIndividual>> getIsNearMembers() {
        StringBuilder queryStr = new StringBuilder();

        /*
         * Expensive query which has to cover all those combinations
         *                      --(isNear)-->
         * 1) `pointFeatureTableName` - `pointFeatureTableName`
         * 2) `pointFeatureTableName` - `lineFeatureTableName`
         * 3) `pointFeatureTableName` - `areaFeatureTableName`
         * 4) `lineFeatureTableName` - `lineFeatureTableName`
         * 5) `lineFeatureTableName` - `areaFeatureTableName`
         * 6) `areaFeatureTableName` - `areaFeatureTableName`
         */

        // 1) `pointFeatureTableName` - `pointFeatureTableName`
        queryStr.append("SELECT l.iri, r.iri ")
                .append("FROM ")
                    .append(pointFeatureTableName).append(" l, ")
                    .append(pointFeatureTableName).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ")
                        .append(nearRadiusInMeters).append(") ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        // 2) `pointFeatureTableName` - `lineFeatureTableName`
        queryStr.append("SELECT l.iri, r.iri ")
                .append("FROM ")
                    .append(pointFeatureTableName).append(" l, ")
                    .append(lineFeatureTableName).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ")
                        .append(nearRadiusInMeters).append(") ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        // 3) `pointFeatureTableName` - `areaFeatureTableName`
        queryStr.append("SELECT l.iri, r.iri ")
                .append("FROM ")
                    .append(pointFeatureTableName).append(" l, ")
                    .append(areaFeatureTableName).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ")
                        .append(nearRadiusInMeters).append(") ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        // 4) `lineFeatureTableName` - `lineFeatureTableName`
        queryStr.append("SELECT l.iri, r.iri ")
                .append("FROM ")
                    .append(lineFeatureTableName).append(" l, ")
                    .append(lineFeatureTableName).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ")
                        .append(nearRadiusInMeters).append(") ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        // 5) `lineFeatureTableName` - `areaFeatureTableName`
        queryStr.append("SELECT l.iri, r.iri ")
                .append("FROM ")
                    .append(lineFeatureTableName).append(" l, ")
                    .append(areaFeatureTableName).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ")
                        .append(nearRadiusInMeters).append(") ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        queryStr.append("UNION ");

        // 6) `areaFeatureTableName` - `areaFeatureTableName`
        queryStr.append("SELECT l.iri, r.iri ")
                .append("FROM ")
                    .append(areaFeatureTableName).append(" l, ")
                    .append(areaFeatureTableName).append(" r ")
                .append("WHERE ")
                    .append("ST_DWithin(l.the_geom::geography, r.the_geom::geography, ")
                        .append(nearRadiusInMeters).append(") ");
        if (!isIsNearRelationReflexive) {
            queryStr
                .append("AND NOT l.iri=r.iri ");
        }

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr.toString());

            while (resultSet.next()) {
                String individual1Str = resultSet.getString(1);
                String individual2Str = resultSet.getString(2);

                OWLIndividual geometryIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(individual1Str));
                OWLIndividual geometryIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(individual2Str));

                OWLIndividual featureIndividual1 = geom2feature.get(geometryIndividual1);
                OWLIndividual featureInfividual2 = geom2feature.get(geometryIndividual2);

                if (!members.containsKey(featureIndividual1)) {
                    members.put(featureIndividual1, new TreeSet<>());
                }

                if (!members.containsKey(featureInfividual2)) {
                    members.put(featureInfividual2, new TreeSet<>());
                }

                // Since isNear is symmetric
                members.get(featureIndividual1).add(featureInfividual2);
                members.get(featureInfividual2).add(featureIndividual1);
            }
        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    private String getTable(OWLIndividual individual) {
        if (reasoner.hasType(areaFeatureClass, individual)) {
            return areaFeatureTableName;
        } else if (reasoner.hasType(lineFeatureClass, individual)) {
            return lineFeatureTableName;
        } else if (reasoner.hasType(pointFeatureClass, individual)) {
            return pointFeatureTableName;
        } else {
            throw new RuntimeException("Individual " + individual + " is " +
                    "neither an area feature, nor a line feature, nor a " +
                    "point feature");
        }
    }

    private boolean containsSpatialExpressions(OWLClassExpression ce) {
        if (ce instanceof OWLClass) {
            return false;
        } else if (ce instanceof OWLObjectIntersectionOf) {
            return ((OWLObjectIntersectionOf) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectSomeValuesFrom) {
            OWLObjectPropertyExpression prop = ((OWLObjectSomeValuesFrom) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectSomeValuesFrom) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectMinCardinality) {
            OWLObjectPropertyExpression prop = ((OWLObjectMinCardinality) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectMinCardinality) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectUnionOfImpl) {
            return ((OWLObjectUnionOf) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectUnionOfImplExt) {
            return ((OWLObjectUnionOfImplExt) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectAllValuesFrom) {
            OWLObjectPropertyExpression prop = ((OWLObjectAllValuesFrom) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectAllValuesFrom) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectMaxCardinality) {
            OWLObjectPropertyExpression prop =
                    ((OWLObjectMaxCardinality) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectMaxCardinality) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else {
            throw new RuntimeException(
                    "Support for class expression of type " + ce.getClass() +
                            " not implemented, yet");
        }
    }

//    /**
//     * Gets the geo:Geometry OWL individual assigned to the input OWL individual
//     * via the geometry property path. In case there are multiple geometry
//     * OWL individuals assigned, I'll just pick the first one.
//     */
//    private OWLIndividual getGeometryIndividual(OWLIndividual featureIndividual) {
//        for (List<OWLProperty> propPath : geometryPropertyPaths) {
//            int pathLen = propPath.size();
//
//            // In case the geometry property path just contains one entry, one
//            // can assume it's a data property pointing to the geometry literal.
//            // So the input feature individual is already what's requested
//            // here.
//            if (pathLen == 1) {
//                assert propPath.get(0).isOWLDataProperty();
//
//                return featureIndividual;
//            }
//
//            // Strip off the last entry of the property path, which is a data
//            // property. All the preceding properties are assumed to be object
//            // properties.
//            List<OWLObjectProperty> objProps = propPath.stream()
//                    .limit(pathLen-1)
//                    .map(OWLProperty::asOWLObjectProperty)
//                    .collect(Collectors.toList());
//
//            Set<OWLIndividual> tmpS = Sets.newHashSet(featureIndividual);
//            Set<OWLIndividual> tmpO = new HashSet<>();
//
//            for (OWLObjectProperty objProp : objProps) {
//                for (OWLIndividual i : tmpS) {
//                    tmpO.addAll(reasoner.getRelatedIndividuals(i, objProp));
//                }
//
//                tmpS = tmpO;
//                tmpO = new HashSet<>();
//            }
//
//            if (!tmpS.isEmpty()) {
//                return tmpS.iterator().next();
//            }
//        }
//
//        return null;
//    }

//    /**
//     * Gets the geo:Feature OWL individual to which the input geo:Geometry OWL
//     * individual was assigned via the geometry property path. In case there are
//     * multiple feature OWL individuals to which the geometry OWL individual was
//     * assigned, I'll just pick the first one.
//     */
//    private OWLIndividual getFeatureIndividual(OWLIndividual geometryIndividual) {
//        for (List<OWLProperty> propPath : geometryPropertyPaths) {
//            int pathLen = propPath.size();
//
//            // In case the geometry property path just contains one entry, one
//            // can assume it's a data property pointing to the geometry literal.
//            // So the input geometry individual is already what's requested
//            // here.
//            if (pathLen == 1) {
//                assert propPath.get(0).isOWLDataProperty();
//
//                return geometryIndividual;
//            }
//
//            // Strip off the last entry of the property path, which is a data
//            // property. All the preceding properties are assumed to be object
//            // properties.
//            List<OWLObjectProperty> objProps = propPath.stream()
//                    .limit(pathLen-1)
//                    .map(OWLProperty::asOWLObjectProperty)
//                    .collect(Collectors.toList());
//
//            List<OWLObjectProperty> revObjProps = Lists.reverse(objProps);
//
//            // S --> subject, O --> object (from RDF triple view)
//            Set<OWLIndividual> tmpS = new HashSet<>();
//            Set<OWLIndividual> tmpO = Sets.newHashSet(geometryIndividual);
//
//            for (OWLObjectProperty objProp : revObjProps) {
//                /**
//                 * Looks sth like this:
//                 * {
//                 *   :bahnhof_dresden_neustadt_building : [
//                 *          :building_bhf_neustadt_geometry],
//                 *   :building_bhf_neustadt_geometry : [],
//                 *   :inside_building_bhf_neustadt_geometry : [],
//                 *   :on_turnerweg_geometry : [],
//                 *   :outside_building_bhf_neustadt_1_geometry : [],
//                 *   :outside_building_bhf_neustadt_2_geometry : [],
//                 *   :pos_inside_bhf_neustadt : [
//                 *          :inside_building_bhf_neustadt_geometry],
//                 *   :pos_on_turnerweg : [
//                 *          :on_turnerweg_geometry],
//                 *   :pos_outside_bhf_neustadt_1 : [
//                 *          :outside_building_bhf_neustadt_1_geometry],
//                 *   :pos_outside_bhf_neustadt_2 : [
//                 *          :outside_building_bhf_neustadt_2_geometry],
//                 *   :turnerweg : [
//                 *          :turnerweg_geometry],
//                 *   :turnerweg_geometry : [],
//                 *   :turnerweg_part : [
//                 *          :turnerweg_part_geometry],
//                 *   :turnerweg_part_geometry : []
//                 * }
//                 *
//                 * i.e. all geometry entries have an empty set as values and
//                 * all the feature entries have a non-empty set as values
//                 */
//                Map<OWLIndividual, SortedSet<OWLIndividual>> members =
//                        reasoner.getPropertyMembers(objProp);
//
//                /**
//                 * `members` keys which have a non-empty value, i.e. all feature
//                 * OWL individuals
//                 *
//                 * [
//                 *   :bahnhof_dresden_neustadt_building,
//                 *   :pos_inside_bhf_neustadt,
//                 *   :pos_on_turnerweg,
//                 *   :pos_outside_bhf_neustadt_1,
//                 *   :pos_outside_bhf_neustadt_2,
//                 *   :turnerweg,
//                 *   :turnerweg_part
//                 * ]
//                 */
//                List<OWLIndividual> sMembers = members.entrySet().stream()
//                        .filter((Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e) -> !e.getValue().isEmpty())
//                        .map((Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e) -> e.getKey())
//                        .collect(Collectors.toList());
//
//                for (OWLIndividual s : sMembers) {
//                    // - `tmpO` contains all the OWL individuals on object
//                    //   position (viewed from an RDF triple perspective)
//                    // - `sMembers` contains all those OWL individuals that
//                    //   actually have values for property `objProp`
//                    // - `s` is an OWL individual that actually has values for
//                    //   property `objProp`
//
//                    // The values of the current OWL individual
//                    SortedSet<OWLIndividual> os = members.get(s);
//
//                    // If any of the values of the current OWL individual is
//                    // contained in the set of value OWL individuals we're
//                    // interested in...
//                    if (os.stream().anyMatch(tmpO::contains)) {
//                        // ...then we'll consider this current OWL individual in
//                        // the next round (or in the final result set if this is
//                        // the last round)
//                        tmpS.add(s);
//                    }
//                }
//
//                tmpO = tmpS;
//                tmpS = new HashSet<>();
//            }
//
//            if (!tmpO.isEmpty()) {
//                return tmpO.iterator().next();
//            }
//        }
//
//        return null;
//    }

    /** Example/debug set-up */
    public static void main(String[] args) throws Exception {
        String exampleFilePath =
                SpatialReasonerPostGIS.class.getClassLoader()
                        .getResource("test/example_data.owl").getFile();
        KnowledgeSource ks = new OWLFile(exampleFilePath);
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
        SpatialReasonerPostGIS spatialReasoner = new SpatialReasonerPostGIS(
                cwr, new DBConnectionSetting(
                "localhost",5432, "dllearner",
                "postgres", "postgres"));
        spatialReasoner.init();
    }

}
