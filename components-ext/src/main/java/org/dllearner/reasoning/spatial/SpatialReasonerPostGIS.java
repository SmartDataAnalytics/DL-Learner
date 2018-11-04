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

    private AbstractReasonerComponent reasoner;

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
        throw new RuntimeException("Not implemented, yet");
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

    /** Example/debug set-up */
    public static void main(String[] args) throws Exception {
        String exampleFilePath =
                SpatialReasonerPostGIS.class.getClassLoader()
                        .getResource("test/example_data.owl").getFile();
        KnowledgeSource ks = new OWLFile(exampleFilePath);
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
        SpatialReasonerPostGIS spatialReasoner = new SpatialReasonerPostGIS(
                cwr, new DBConnectionSetting(
                "localhost",5432, "trial1",
                "postgres", "postgres"));
        spatialReasoner.init();
    }

}
