/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.test.junit;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.sparql.simple.SparqlSimpleExtractor;
import org.dllearner.accuracymethods.AccMethodFMeasure;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.junit.Ignore;
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
	
	@Ignore
    public void someOnlyTest() throws ComponentInitException, LearningProblemUnsupportedException {
        
        SortedSet<OWLIndividual> posExamples = new TreeSet<>();
        posExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Archytas")));
        posExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Pythagoras")));
        posExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Philolaus")));
        
        SortedSet<OWLIndividual> negExamples = new TreeSet<>();
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Democritus")));
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Zeno_of_Elea")));
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Plato")));
        negExamples.add(new OWLNamedIndividualImpl(IRI.create("http://dbpedia.org/resource/Socrates")));
        
        SortedSetTuple<OWLIndividual> examples = new SortedSetTuple<>(posExamples,
                negExamples);
        
        SparqlSimpleExtractor ks = new SparqlSimpleExtractor();
        ks.setInstances(new ArrayList<>(Helper.getStringSet(examples
                .getCompleteSet())));
        // ks.getConfigurator().setPredefinedEndpoint("DBPEDIA"); // TODO:
        // probably the official endpoint is too slow?
        ks.setEndpointURL("http://dbpedia.org/sparql");
        // ks.setUseLits(false);
        // ks.setUseCacheDatabase(true);
        ks.setRecursionDepth(1);
        ArrayList<String> ontologyUrls = new ArrayList<>();
        ontologyUrls.add("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl");
        ks.setOntologySchemaUrls(ontologyUrls);
        ks.setAboxfilter("FILTER ( !isLiteral(?o) &&   regex(str(?o), "
                + "'^http://dbpedia.org/resource/') && "
                + "! regex(str(?o), '^http://dbpedia.org/resource/Category')  ) ");
        ks.setTboxfilter("FILTER ( regex(str(?class), '^http://dbpedia.org/ontology/') ) .  ");
        
        ks.init();
        
        AbstractReasonerComponent rc = new ClosedWorldReasoner(ks);
//        ((FastInstanceChecker)rc).setForAllSemantics(ForallSemantics.SomeOnly);
        rc.init();
       
        
        PosNegLPStandard lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.setAccuracyMethod(new AccMethodFMeasure(true));
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
