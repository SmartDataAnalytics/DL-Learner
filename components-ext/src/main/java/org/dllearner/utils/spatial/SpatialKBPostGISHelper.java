package org.dllearner.utils.spatial;

import org.aksw.commons.util.Pair;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

enum PolygonType {
    POINT("point"), LINESTRING("linestring"), POLYGON("polygon");

    private String tableName;

    PolygonType(String tableName) {
        this.tableName = tableName;
    }

    String getTableName() {
        return this.tableName;
    }
}

/**
 * Helper class to perform all the tedious tasks like loading data into
 * PostGIS, adding spatial feature data etc. It is mainly used to build
 * tests.
 */
public class SpatialKBPostGISHelper {
    private List<OWLObjectProperty> propertyPathToGeometry;
    private OWLDataProperty wktLiteralProperty;
    private OWLOntology ontology;
    private static OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    private static OWLDataFactory df = man.getOWLDataFactory();

    public static final String ns = "http://dl-learner/spatial#";
    public static final OWLDatatype wktDType = df.getOWLDatatype(IRI.create("http://www.opengis.net/ont/geosparql#wktLiteral"));

    private int dummyIndividualCntr = 1;

    private OWLNamedIndividual getDummyIndividual() {
        String iriStr = ns + "dummy_individual_" + dummyIndividualCntr;
        dummyIndividualCntr++;

        return df.getOWLNamedIndividual(IRI.create(iriStr));
    }

    private OWLNamedIndividual getGeometryIndividual(OWLNamedIndividual individual, int propertyPathIdx) {
        if (propertyPathIdx == propertyPathToGeometry.size())
            return individual;

        Set<OWLNamedIndividual> valueIndividuals = ontology.getObjectPropertyAssertionAxioms(individual)
                .stream()
                .filter(ax -> ax.getProperty().equals(propertyPathToGeometry.get(propertyPathIdx)))
                .map(ax -> getGeometryIndividual(ax.getObject().asOWLNamedIndividual(), propertyPathIdx+1))
                .collect(Collectors.toSet());

        if (valueIndividuals.size() == 0)
            return null;

        assert valueIndividuals.size() == 1;
        return valueIndividuals.iterator().next();
    }

    private OWLLiteral getWKTLiteral(OWLNamedIndividual individual) {
        Set<OWLLiteral> wktValues = ontology.getDataPropertyAssertionAxioms(individual)
                .stream()
                .filter(ax -> ax.getProperty().equals(wktLiteralProperty))
                .map(OWLPropertyAssertionAxiom::getObject)
                .collect(Collectors.toSet());

        assert wktValues.size() == 1;

        return wktValues.iterator().next();
    }

    private Set<Pair<OWLNamedIndividual, OWLLiteral>> getWKTLiterals() {
        return ontology.getIndividualsInSignature()
                .stream()
                .map(i -> new Pair<OWLNamedIndividual, OWLNamedIndividual>(i, getGeometryIndividual(i, 0)))
                .filter(p -> p.getValue() != null)
                .map(p -> new Pair<OWLNamedIndividual, OWLLiteral>(p.getValue(), getWKTLiteral(p.getValue())))
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toSet());
    }

    private PolygonType getType(String wktLiteral) {
        if (wktLiteral.startsWith("POINT")) {
            return PolygonType.POINT;
        } else if (wktLiteral.startsWith("LINE")) {
            return PolygonType.LINESTRING;
        } else if (wktLiteral.startsWith("POLY")) {
            return PolygonType.POLYGON;
        } else {
            throw new RuntimeException("Unhandled polygon type of " + wktLiteral);
        }
    }

    public SpatialKBPostGISHelper(
            List<OWLObjectProperty> propertyPathToGeometry,
            OWLDataProperty wktLiteralProperty) {

        this.propertyPathToGeometry = propertyPathToGeometry;
        this.wktLiteralProperty = wktLiteralProperty;

        try {
            this.ontology = man.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSpatialInfoToPostGIS(Connection conn) {
        Set<Pair<OWLNamedIndividual, OWLLiteral>> individualsWithWKTLiteral = getWKTLiterals();

        for (Pair<OWLNamedIndividual, OWLLiteral> p : individualsWithWKTLiteral) {
            OWLNamedIndividual geomIndividual = p.getKey();
            String wktStr = p.getValue().getLiteral();

            String queryStr =
                    "INSERT INTO " + getType(wktStr).getTableName() + " " +
                    "VALUES (?, ST_GeomFromText(?))";

            try {
                PreparedStatement statement = conn.prepareStatement(queryStr);
                statement.setString(1, geomIndividual.toString());
                statement.setString(2, wktStr);

                statement.execute();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addSpatialFeature(String featureIRIStr, String geomIRIStr, String wktStr) {
        if (!featureIRIStr.startsWith("http"))
            featureIRIStr = ns + featureIRIStr;

        if (!geomIRIStr.startsWith("http"))
            geomIRIStr = ns + geomIRIStr;

        OWLNamedIndividual featureIndividual =
                df.getOWLNamedIndividual(IRI.create(featureIRIStr));

        OWLNamedIndividual geomIndividual =
                df.getOWLNamedIndividual(IRI.create(geomIRIStr));

        OWLLiteral wktLit = df.getOWLLiteral(wktStr, wktDType);

        OWLIndividual currLastIndividualInPropertyChain = featureIndividual;
        for (int propertyPathIdx = 0; propertyPathIdx < propertyPathToGeometry.size(); propertyPathIdx++) {
            if (propertyPathIdx == propertyPathToGeometry.size()-1) {

                OWLAxiom axiom = df.getOWLObjectPropertyAssertionAxiom(
                        propertyPathToGeometry.get(propertyPathIdx),
                        currLastIndividualInPropertyChain,
                        geomIndividual);

                man.addAxiom(ontology, axiom);

            } else {
                OWLIndividual dummyIndividual = getDummyIndividual();

                OWLAxiom axiom = df.getOWLObjectPropertyAssertionAxiom(
                        propertyPathToGeometry.get(propertyPathIdx),
                        currLastIndividualInPropertyChain,
                        dummyIndividual);

                man.addAxiom(ontology, axiom);

                currLastIndividualInPropertyChain = dummyIndividual;
            }
        }

        OWLAxiom axiom = df.getOWLDataPropertyAssertionAxiom(wktLiteralProperty, geomIndividual, wktLit);
        man.addAxiom(ontology, axiom);
    }

    public void createTables(Connection conn) {
        try {
            Statement statement = conn.createStatement();
            statement.execute(
                    "CREATE TABLE " + PolygonType.POINT.getTableName() +
                            " (iri character varying(255), the_geom geometry(Point))");

            statement = conn.createStatement();
            statement.execute(
                    "CREATE TABLE " + PolygonType.LINESTRING.getTableName() +
                            " (iri character varying(255), the_geom geometry(Linestring))");

            statement = conn.createStatement();
            statement.execute(
                    "CREATE TABLE " + PolygonType.POLYGON.getTableName() +
                            " (iri character varying(255), the_geom geometry(Polygon))");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
