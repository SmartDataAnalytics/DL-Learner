package org.dllearner.utilities.graph;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.RandomWalkIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import static org.apache.jena.sys.JenaSystem.forEach;

/**
 * Utility methods working on graph level by means of JGraphT API.
 *
 * @author Lorenz Buehmann
 */
public class GraphUtils {

    /**
     * Maps the ABox of an OWL ontology to a directed graph.
     * <p>
     * It just uses object property assertions to build the graph of the individuals. Edges are not labeled.
     *
     * @param ont the ontology
     * @return a directed graph with labeled vertices
     */
    public static Graph<OWLIndividual, DefaultEdge> aboxToGraph(OWLOntology ont) {

        Set<OWLObjectPropertyAssertionAxiom> axioms = ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION);

        Graph<OWLIndividual, DefaultEdge> g = GraphTypeBuilder
                .directed()
                .allowingMultipleEdges(true)
                .edgeClass(DefaultEdge.class)
                .vertexClass(OWLIndividual.class)
                .buildGraph();

        axioms.forEach(ax -> g.addEdge(ax.getSubject(), ax.getObject()));

        return g;
    }

    /**
     * Maps the ABox of an OWL ontology to a labeled directed graph.
     * <p>
     * It just uses object property assertions to build the graph of the individuals. Edges are labeled with the
     * object property.
     *
     * @param ont the ontology
     * @return a directed graph with labeled vertices and edges
     */
    public static Graph<OWLIndividual, OWLPropertyEdge> aboxToLabeledGraph(OWLOntology ont) {

        Set<OWLObjectPropertyAssertionAxiom> axioms = ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION);

        Graph<OWLIndividual, OWLPropertyEdge> g = GraphTypeBuilder
                .directed()
                .allowingMultipleEdges(true)
                .vertexClass(OWLIndividual.class)
                .edgeClass(OWLPropertyEdge.class)
                .buildGraph();

        axioms.forEach(ax -> {
            g.addVertex(ax.getSubject());
            g.addVertex(ax.getObject());
            g.addEdge(ax.getSubject(), ax.getObject(), new OWLPropertyEdge(ax.getProperty()));
        });

        return g;
    }

    private static TypedOWLIndividual toTypedIndividual(OWLIndividual ind, OWLOntology ont) {
        Set<OWLClass> types = ont.getClassAssertionAxioms(ind).stream()
                .map(OWLClassAssertionAxiom::getClassExpression)
                .filter(ce -> !ce.isAnonymous())
                .map(OWLClassExpression::asOWLClass)
                .collect(Collectors.toSet());

        return new TypedOWLIndividual(ind, types);
    }

    /**
     * Maps the ABox of an OWL ontology to a labeled directed graph.
     * <p>
     * It just uses object property assertions to build the graph of the individuals.
     * Edges are labeled with the object property.
     * Vertices are annotated with the types of the individuals.
     *
     * @param ont the ontology
     * @return a directed graph with labeled and type annotated vertices as well as labeled edges
     */
    public static Graph<TypedOWLIndividual, OWLPropertyEdge> aboxToLabeledGraphWithTypes(OWLOntology ont) {

        Set<OWLObjectPropertyAssertionAxiom> axioms = ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION);

        Graph<TypedOWLIndividual, OWLPropertyEdge> g = GraphTypeBuilder
                .directed()
                .allowingMultipleEdges(true)
                .vertexClass(TypedOWLIndividual.class)
                .edgeClass(OWLPropertyEdge.class)
                .buildGraph();

        // we cache the created vertices because computation of types can be expensive,
        // at least when inference would be used
        Map<OWLIndividual, TypedOWLIndividual> cache = new HashMap<>();

        axioms.forEach(ax -> {
            // process the subject
            OWLIndividual ind = ax.getSubject();
            TypedOWLIndividual source = cache.computeIfAbsent(ind, i -> toTypedIndividual(i, ont));

            // and the object
            ind = ax.getObject();
            TypedOWLIndividual target = cache.computeIfAbsent(ind, i -> toTypedIndividual(i, ont));

            g.addVertex(source);
            g.addVertex(target);
            g.addEdge(source, target, new OWLPropertyEdge(ax.getProperty()));
        });

        cache.clear();

        return g;
    }


    public static void main(String[] args) throws OWLOntologyCreationException {
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());

        OWLOntology ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("/home/user/work/datasets/poker/poker_straight_flush_p5-n347.owl"));
        OWLClass hand = OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://dl-learner.org/examples/uci/poker#Hand"));
        final String targetClass = "straight_flush";

        Graph<TypedOWLIndividual, OWLPropertyEdge> g = aboxToLabeledGraphWithTypes(ont);

        Set<TypedOWLIndividual> startNodes = ont.getIndividualsInSignature().stream()
                .filter(ind -> ont.getAnnotationAssertionAxioms(ind.asOWLNamedIndividual().getIRI()).stream()
                        .map(OWLAnnotationAssertionAxiom::annotationValue)
                        .map(OWLAnnotationValue::asLiteral)
                        .anyMatch(lit -> lit.isPresent() && lit.get().getLiteral().equals(targetClass)))
                .map(TypedOWLIndividual::new)
                .limit(1)
                .collect(Collectors.toSet());

        int maxPathLength = 10;

        startNodes.forEach(node -> {

            // compute all path up to length
            List<GraphPath<TypedOWLIndividual, OWLPropertyEdge>> paths = new AllPaths<>(g).getAllPaths(node, true, maxPathLength);

            // show all paths
            paths.forEach(System.out::println);

            // show all paths but just the edges
            List<List<OWLObjectPropertyExpression>> pathEdges = paths.stream()
                    .map(path -> path.getEdgeList().stream().map(LabeledEdge::getLabel).collect(Collectors.toList()))
                    .collect(Collectors.toList());
//            pathEdges.forEach(System.out::println);

            // show just the distinct list of the edge sequences
            List<List<OWLObjectPropertyExpression>> pathEdgesDistinct = new ArrayList<>(new HashSet<>(pathEdges));

            Comparator<List<OWLObjectPropertyExpression>> c = Comparator.<List<OWLObjectPropertyExpression>>comparingInt(List::size).thenComparing(Object::toString);

            Collections.sort(pathEdgesDistinct, c);
            pathEdgesDistinct.forEach(System.out::println);

        });
    }


}
