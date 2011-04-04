/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 *
 */
package org.dllearner.scripts.improveWikipedia;

import org.aksw.commons.sparql.core.ResultSetRenderer;
import org.aksw.commons.sparql.core.SparqlEndpoint;
import org.aksw.commons.sparql.core.SparqlTemplate;
import org.aksw.commons.sparql.core.decorator.CachingSparqlEndpoint;
import org.aksw.commons.sparql.core.impl.HttpSparqlEndpoint;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.owl.*;
import org.dllearner.gui.Config;
import org.dllearner.gui.ConfigSave;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SortedSetTuple;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * A script, which learns definitions / super classes of classes in the DBpedia ontology.
 *
 * @author Jens Lehmann
 */
public class DBpediaClassLearnerCELOE {
    SparqlEndpoint sparqlEndpoint = new CachingSparqlEndpoint(new HttpSparqlEndpoint("http://dbpedia.org/sparql", "http://dbpedia.org"), "cache/");

    public DBpediaClassLearnerCELOE() {
        // OPTIONAL: if you want to do some case distinctions in the learnClass method, you could add
        // parameters to the constructure e.g. YAGO_
    }

    public KB learnAllClasses(Set<String> classesToLearn) throws LearningProblemUnsupportedException, IOException {
        KB kb = new KB();
        for (String classToLearn : classesToLearn) {
            Description d = learnClass(classToLearn);
            if (d == null) {
                continue;
            }
            kb.addAxiom(new EquivalentClassesAxiom(new NamedClass(classToLearn), d));
        }
        return kb;
    }

    public Description learnClass(String classToLearn) throws LearningProblemUnsupportedException, IOException {
        Set<String> posEx = getPosEx(classToLearn);
        if (posEx.isEmpty()) {
            return null;
        }
        Set<String> classes = new HashSet<String>();

        for (String pos : posEx) {
            SparqlTemplate st = new SparqlTemplate(0);
            st.addFilter(sparqlEndpoint.like("classes", new HashSet<String>(Arrays.asList(new String[]{"http://dbpedia.org/ontology/"}))));
            VelocityContext vc = new VelocityContext();
            vc.put("instance", pos );
            String query = st.getQuery("sparqltemplates/directClassesOfInstance.vm", vc);
            classes.addAll(ResultSetRenderer.asStringSet(sparqlEndpoint.executeSelect(query)));
            classes.remove(classToLearn);
        }
        System.out.println(classes.size());
        System.exit(0);

        Set<String> negEx = new HashSet<String>();
        for (String oneClass : classes) {
           /* st = new SparqlTemplate(0);
            st.addFilter(sparqlEndpoint.like("classes", new HashSet<String>(Arrays.asList(new String[]{"http://dbpedia.org/ontology/"}))));
            query = st.getQuery("sparqltemplates/classesOfInstance.vm", new VelocityContext());
            classes.addAll(ResultSetRenderer.asStringSet(sparqlEndpoint.executeSelect(query)));
            classes.remove(classToLearn);*/
        }
        //System.out.println(posEx);
        System.exit(0);
        //Set<String> negEx = InstanceFinderSPARQL.findInstancesWithSimilarClasses(posEx, -1, sparqlEndpoint);
        SortedSet<Individual> posExamples = Helper.getIndividualSet(posEx);
        SortedSet<Individual> negExamples = Helper.getIndividualSet(negEx);
        SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples, negExamples);
        System.out.println(posEx.size());
        System.out.println(negEx.size());
        System.exit(0);
        ComponentManager cm = ComponentManager.getInstance();

        SparqlKnowledgeSource ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
        ks.getConfigurator().setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
        ks.getConfigurator().setPredefinedEndpoint("DBPEDIA"); // TODO: probably the official endpoint is too slow?

        ReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, ks);

        PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
        lp.getConfigurator().setAccuracyMethod("fMeasure");
        lp.getConfigurator().setUseApproximations(false);

        CELOE la = cm.learningAlgorithm(CELOE.class, lp, rc);
        CELOEConfigurator cc = la.getConfigurator();
        cc.setMaxExecutionTimeInSeconds(100);
        cc.setNoisePercentage(20);
        // TODO: set more options as needed

        // to write the above configuration in a conf file (optional)
        Config cf = new Config(cm, ks, rc, lp, la);
        new ConfigSave(cf).saveFile(new File("/dev/null"));

        la.start();

        return la.getCurrentlyBestDescription();
    }

    public static void main(String args[]) throws LearningProblemUnsupportedException, IOException {

        DBpediaClassLearnerCELOE dcl = new DBpediaClassLearnerCELOE();
        SparqlTemplate st = new SparqlTemplate(0);
        st.addFilter(dcl.sparqlEndpoint.like("classes", new HashSet<String>(Arrays.asList(new String[]{"http://dbpedia.org/ontology/"}))));

        String query = st.getQuery("sparqltemplates/allClasses.vm", new VelocityContext());
        //System.out.println(query);
        Set<String> classesToLearn = ResultSetRenderer.asStringSet(dcl.sparqlEndpoint.executeSelect(query));
        //System.out.println(classesToLearn);

        KB kb = dcl.learnAllClasses(classesToLearn);
        System.exit(0);
        kb.export(new File("/dev/null"), OntologyFormat.RDF_XML); // TODO: pick appropriate place to save ontology
    }


    public Set<String> getPosEx(String clazz) {
        VelocityContext vc = new VelocityContext();
        vc.put("class", clazz);
        vc.put("limit", 0);
        String query = SparqlTemplate.instancesOfClass(vc);
        return ResultSetRenderer.asStringSet(sparqlEndpoint.executeSelect(query));
    }

}
