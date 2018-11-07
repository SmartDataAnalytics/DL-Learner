package org.dllearner.reasoning.spatial;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.postgresql.util.PGobject;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

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
    private boolean isContainmentRelationReflexive = false;
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

    private AbstractReasonerComponent reasoner;
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
                SortedSet<OWLIndividual> individuals = null;

                for (OWLClassExpression ce : ((OWLObjectIntersectionOf) concept).getOperands()) {
                    SortedSet<OWLIndividual> opIndividuals = getIndividualsImpl(ce);

                    if (individuals == null) {
                        individuals = opIndividuals;
                    } else {
                        individuals.addAll(opIndividuals);
                    }
                }

                return individuals;

            } else if (concept instanceof OWLObjectSomeValuesFrom) {
                OWLObjectPropertyExpression prop = ((OWLObjectSomeValuesFrom) concept).getProperty();
                OWLClassExpression filler = ((OWLObjectSomeValuesFrom) concept).getFiller();

                if ((prop instanceof OWLObjectProperty)
                        && SpatialVocabulary.spatialObjectProperties.contains(prop)) {
                    SortedSet<OWLIndividual> fillerIndivs = getIndividuals(filler);

                    // isInside
                    if (prop.equals(SpatialVocabulary.isInside)) {
                        Set<OWLIndividual> individuals = new HashSet<>();

                        for (OWLIndividual fillerIndiv : fillerIndivs) {
                            Set<OWLIndividual> indivsInsideFillerIndiv =
                                    getContainedSpatialIndividuals(fillerIndiv);
                            individuals.addAll(indivsInsideFillerIndiv);
                        }

                        return new TreeSet<>(individuals);
                    } else {
                        throw new RuntimeException(
                                "spatial object property " + prop + " not supported, yet");
                    }
                } else {
                    if (prop instanceof OWLObjectInverseOf) {
                        throw new RuntimeException("Not implemented, yet");
                    } else {
                        // TODO: consider super properties!
                        SortedSet<OWLIndividual> fillerIndivs = getIndividuals(filler);
                        Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                                reasoner.getPropertyMembers((OWLObjectProperty) prop);

                        Set<OWLIndividual> resultIndividuals = new HashSet<>();
                        assert propIndividuals != null;
                        for (Map.Entry e : propIndividuals.entrySet()) {
                            OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                            SortedSet<OWLIndividual> values = (SortedSet<OWLIndividual>) e.getValue();

                            values.retainAll(fillerIndivs);

                            if (!values.isEmpty()) {
                                resultIndividuals.add(keyIndiv);
                            }
                        }

                        return new TreeSet<>(resultIndividuals);
                    }
                }

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
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Set<OWLIndividual> getNearSpatialIndividuals(OWLIndividual individual) {
        throw new RuntimeException("Not implemented, yet");
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
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Set<OWLIndividual> getContainedSpatialIndividuals(OWLIndividual individual) {
//        OWLIndividual geometryIndividual = getGeometryIndividual(individual);
        OWLIndividual geometryIndividual = null;
        try {
            geometryIndividual = feature2geom.get(individual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        String containerTableName = getTable(individual);
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
                        // if `isContainmentRelationReflexive` is set, do not filter; otherwise filter out input `individual`
                        .filter((OWLIndividual i) -> isContainmentRelationReflexive ? true : !i.equals(individual));

        return containedIndividuals.collect(Collectors.toSet());
    }

    @Override
    public boolean runsAlong(OWLIndividual individual1, OWLIndividual individual2) {
        throw new RuntimeException("Not implemented, yet");
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
    public Map<OWLObjectProperty,OWLClassExpression> getObjectPropertyDomains() {
        Map<OWLObjectProperty, OWLClassExpression> domainsMap = reasoner.getObjectPropertyDomains();

        domainsMap.put(SpatialVocabulary.isInside, SpatialVocabulary.SpatialFeature);

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

            // TODO: Add further spatial object properties here
        } else {
            return reasoner.getRange(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
        if (objectProperty.equals(SpatialVocabulary.isInside)) {
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
    // </base reasoner interface methods>

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
