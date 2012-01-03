package org.dllearner.kb.sparql;

import com.hp.hpl.jena.rdf.model.Model;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitorImproved;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.extraction.Node;
import org.dllearner.kb.extraction.OWLAPIOntologyCollector;
import org.dllearner.kb.manipulator.Manipulator;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 17, 2010
 * Time: 10:33:47 AM
 * <p/>
 * Model Fragmenter that uses the Manager class - based on what was done in SparqlKnowledgeSource
 */
public class ManagerBasedModelFragmenter implements IModelFragmenter {

    private int recursionDepth = 3;


    @Override
    public OWLOntology buildFragment(Model model, Set<String> instanceURIs) {

        /**
         * Create the manager per call - this is not expensive so we can do this because we don't want to
         * keep state in this implementation (thread safety) - this is because the model can change from call
         * to call and we have to initialize the tuple acquisitor here.
         */
        Manager m = createManager(model);

        /** Extracts the nodes and the connections around them */
        List<Node> seedNodes = m.extract(instanceURIs);

        OWLOntology fragment = m.getOWLAPIOntologyForNodes(seedNodes, false);

        return fragment;
    }

    private Manager createManager(Model model) {
        Manager m = new Manager();
        Manipulator manipulator = new Manipulator();

        ModelBasedSPARQLTasks sparqlTasks = new ModelBasedSPARQLTasks();
        sparqlTasks.setModel(model);

        /** Get some weird erros with this one and blank nodes, but only when dissolve blank nodes = true */
        SparqlQueryMaker queryMaker = new SparqlQueryMaker();
        /** Later on use a setter/getter for this so we can do dependency injection */
        queryMaker.setLiterals(true);
        TupleAquisitor tupleAquisitor = new SparqlTupleAquisitorImproved(queryMaker, sparqlTasks, getRecursionDepth());
//        TupleAquisitor tupleAquisitor = new SparqlTupleAquisitor(new SparqlQueryMaker(), sparqlTasks);
        tupleAquisitor.dissolveBlankNodes = false;

        /** These properties are hard coded here due to time constraints but we could make these fields and inject them in the future */
        ExtractionAlgorithm extractionAlgorithm = new ExtractionAlgorithm();
        extractionAlgorithm.setRecursionDepth(getRecursionDepth());
        extractionAlgorithm.setManipulator(manipulator);
        extractionAlgorithm.setCloseAfterRecursion(true);
        extractionAlgorithm.setGetAllSuperClasses(true);
        extractionAlgorithm.setGetPropertyInformation(true);
        extractionAlgorithm.setBreakSuperClassesAfter(200);
        extractionAlgorithm.setDissolveBlankNodes(false);

        OWLAPIOntologyCollector ontologyCollector = new OWLAPIOntologyCollector();

        /** Configure the manager here */
        m.setExtractionAlgorithm(extractionAlgorithm);
        m.setOntologyCollector(ontologyCollector);
        m.setTupleAquisitor(tupleAquisitor);

        return m;
    }

    /**
     * Get the recursion depth.
     *
     * @return The recursion depth.
     */
    public int getRecursionDepth() {
        return recursionDepth;
    }

    /**
     * Set the recursion depth.
     *
     * @param recursionDepth The recursion depth.
     */
    public void setRecursionDepth(int recursionDepth) {
        this.recursionDepth = recursionDepth;
    }
}
