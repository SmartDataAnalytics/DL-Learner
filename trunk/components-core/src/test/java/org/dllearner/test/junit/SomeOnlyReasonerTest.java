/**
 * 
 */
package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.kb.sparql.simple.SparqlSimpleExtractor;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.junit.Test;

/**
 * @author didiers
 * 
 */
public class SomeOnlyReasonerTest {
	
    @Test
    public void someOnlyTest() throws ComponentInitException, LearningProblemUnsupportedException {
        // TODO: use aksw-commons-sparql instead of sparql-scala
        
        SortedSet<Individual> posExamples = new TreeSet<Individual>();
        posExamples.add(new Individual("http://dbpedia.org/resource/Archytas"));
        posExamples.add(new Individual("http://dbpedia.org/resource/Pythagoras"));
        posExamples.add(new Individual("http://dbpedia.org/resource/Philolaus"));
        
        SortedSet<Individual> negExamples = new TreeSet<Individual>();
        negExamples.add(new Individual("http://dbpedia.org/resource/Democritus"));
        negExamples.add(new Individual("http://dbpedia.org/resource/Zeno_of_Elea"));
        negExamples.add(new Individual("http://dbpedia.org/resource/Plato"));
        negExamples.add(new Individual("http://dbpedia.org/resource/Socrates"));
        
        SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples,
                negExamples);
        
        ComponentManager cm = ComponentManager.getInstance();
        
        SparqlSimpleExtractor ks = cm.knowledgeSource(SparqlSimpleExtractor.class);
        ks.setInstances(new ArrayList<String>(Datastructures.individualSetToStringSet(examples
                .getCompleteSet())));
        // ks.getConfigurator().setPredefinedEndpoint("DBPEDIA"); // TODO:
        // probably the official endpoint is too slow?
        ks.setEndpointURL("http://dbpedia.org/sparql");
        // ks.setUseLits(false);
        // ks.setUseCacheDatabase(true);
        ks.setRecursionDepth(1);
        ArrayList<String> ontologyUrls = new ArrayList<String>();
        ontologyUrls.add("http://downloads.dbpedia.org/3.6/dbpedia_3.6.owl");
        ks.setOntologySchemaUrls(ontologyUrls);
        ks.setAboxfilter("FILTER ( !isLiteral(?o) &&   regex(str(?o), "
                + "'^http://dbpedia.org/resource/') && "
                + "! regex(str(?o), '^http://dbpedia.org/resource/Category')  ) ");
        ks.setTboxfilter("FILTER ( regex(str(?class), '^http://dbpedia.org/ontology/') ) .  ");
        
        ks.init();
        
        AbstractReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, ks);
//        ((FastInstanceChecker)rc).setForAllSemantics(ForallSemantics.SomeOnly);
        rc.init();
       
        
        PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.setAccuracyMethod("fmeasure");
        lp.setUseApproximations(false);
        lp.init();
        
        CELOE la = cm.learningAlgorithm(CELOE.class, lp, rc);
        // CELOEConfigurator cc = la.getConfigurator();
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
        
        cm.freeAllComponents();
        Description desc = la.getCurrentlyBestDescription();
//        assertTrue( this.containsObjectAllRestriction(desc));
        
    }
    
    private boolean containsObjectAllRestriction(Description d){
        if(d instanceof ObjectAllRestriction){
            return false;
        }
        for(Description child:d.getChildren()){
            if(!this.containsObjectAllRestriction(child)){
                return false;
            }
        }
        return true;
    }
}
