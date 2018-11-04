package org.dllearner.reasoning.spatial;

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerType;
import org.postgis.PGgeometry;
import org.postgresql.util.PGobject;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpatialReasonerPostGIS extends AbstractReasonerComponent implements SpatialReasoner {
    private final static String defaultHost = "localhost";
    private final static int defaultPort = 5432;
    private DBConnectionSetting dbConnectionSetting;
    private Connection conn;

    private double nearRadiusInMeters = 5; // meters
    private Set<List<OWLProperty>> geometryPropertyPaths;

    private AbstractReasonerComponent reasoner;
    // TODO: replace with more accepted IRIs
    private OWLClass areaFeatureClass = new OWLClassImpl(
            IRI.create("http://dl-learner.org/ont/spatial-test#AreaFeature"));
    private String areaFeatureTableName = "area_feature";
    private OWLClass lineFeatureClass = new OWLClassImpl(
            IRI.create("http://dl-learner.org/ont/spatial-test#LineFeature"));
    private String lineFeatureTableName = "line_feature";
    private OWLClass pointFeatureClass = new OWLClassImpl(
            IRI.create("http://dl-learner.org/ont/spatial-test#PointFeature"));
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

        geometryPropertyPaths = new HashSet<>();
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
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public void setSynchronized() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public Set<OWLClass> getClasses() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public SortedSet<OWLIndividual> getIndividuals() {
        throw new RuntimeException("Not implemented, yet");
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

        OWLIndividual containedGeometryIndividual =
                getGeometryIndividual(containedIndividual);
        OWLIndividual containingGeometryIndividual =
                getGeometryIndividual(containingIndividual);

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
        throw new RuntimeException("Not implemented, yet");
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
    // </implemented interface/base class methods>


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

    /**
     * Gets the geo:Geometry OWL individual assigned to the input OWL individual
     * via the geometry property path. In case there are multiple geometry
     * OWL individuals assigned, I'll just pick the first one.
     */
    private OWLIndividual getGeometryIndividual(OWLIndividual individual) {
        for (List<OWLProperty> propPath : geometryPropertyPaths) {
            int pathLen = propPath.size();

            // In case the geometry property path just contains one entry, one
            // can assume it's a data property pointing to the geometry literal.
            // So the input geometry individual is already what's requested
            // here.
            if (pathLen == 1) {
                assert propPath.get(0).isOWLDataProperty();

                return individual;
            }

            // Strip off the last entry of the property path, which is a data
            // property. All the preceding properties are assumed to be object
            // properties.
            List<OWLObjectProperty> objProps = propPath.stream()
                    .limit(pathLen-1)
                    .map(OWLProperty::asOWLObjectProperty)
                    .collect(Collectors.toList());

            Set<OWLIndividual> tmpS = Sets.newHashSet(individual);
            Set<OWLIndividual> tmpO = new HashSet<>();

            for (OWLObjectProperty objProp : objProps) {
                for (OWLIndividual i : tmpS) {
                    tmpO.addAll(reasoner.getRelatedIndividuals(i, objProp));
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
