/**
 * 
 */
package org.dllearner.test.junit;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.sparql.simple.SparqlSimpleExtractor;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.junit.Test;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * @author didiers
 * 
 */
public class SomeOnlyReasonerTest {
	
//    @Test
    public void someOnlyTest() throws ComponentInitException, LearningProblemUnsupportedException {
        // TODO: use aksw-commons-sparql instead of sparql-scala
        
        SortedSet<OWLIndividual> posExamples = new TreeSet<OWLIndividual>();
        posExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Archytas")));
        posExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Pythagoras")));
        posExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Philolaus")));
        
        SortedSet<OWLIndividual> negExamples = new TreeSet<OWLIndividual>();
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Democritus")));
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Zeno_of_Elea")));
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Plato")));
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Socrates")));
        
        SortedSetTuple<OWLIndividual> examples = new SortedSetTuple<OWLIndividual>(posExamples,
                negExamples);
        
        SparqlSimpleExtractor ks = new SparqlSimpleExtractor();
        ks.setInstances(new ArrayList<String>(Datastructures.individualSetToStringSet(examples
                .getCompleteSet())));
        // ks.getConfigurator().setPredefinedEndpoint("DBPEDIA"); // TODO:
        // probably the official endpoint is too slow?
        ks.setEndpointURL("http://dbpedia.org/sparql");
        // ks.setUseLits(false);
        // ks.setUseCacheDatabase(true);
        ks.setRecursionDepth(1);
        ArrayList<String> ontologyUrls = new ArrayList<String>();
        ontologyUrls.add("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl");
        ks.setOntologySchemaUrls(ontologyUrls);
        ks.setAboxfilter("FILTER ( !isLiteral(?o) &&   regex(str(?o), "
                + "'^http://dbpedia.org/resource/') && "
                + "! regex(str(?o), '^http://dbpedia.org/resource/Category')  ) ");
        ks.setTboxfilter("FILTER ( regex(str(?class), '^http://dbpedia.org/ontology/') ) .  ");
        
        ks.init();
        
        AbstractReasonerComponent rc = new FastInstanceChecker(ks);
//        ((FastInstanceChecker)rc).setForAllSemantics(ForallSemantics.SomeOnly);
        rc.init();
       
        
        PosNegLPStandard lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.setAccuracyMethod("fmeasure");
        lp.setUseApproximations(false);
        lp.init();
        
        CELOE la = new CELOE(lp, rc);
        la.setMaxExecutionTimeInSeconds(10);
        la.init();
        RhoDRDown op = (RhoDRDown) la.getOperator();
        
        op.setUseNegation(false);
        op.setUseAllConstructor(true);
        op.setUseCardinalityRestrictions(false);
        op.setUseHasValueConstructor(true);
        la.setNoisePercentage(20);
        la.init();
        la.start();
        
        OWLClassExpression desc = la.getCurrentlyBestDescription();
//        assertTrue( this.containsObjectAllRestriction(desc));
        
    }
    
    private boolean containsObjectAllRestriction(OWLClassExpression d){
        for(OWLClassExpression child : d.getNestedClassExpressions()){
            if(child.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM){
                return true;
            }
        }
        return false;
    }
}
