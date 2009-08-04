/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

package org.dllearner.tools.ore;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.tools.ore.ui.DescriptionLabel;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;

import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

/**
 * This class contains init methods, and is used as broker between wizard and OWL-API. 
 * @author Lorenz Buehmann
 *
 */
public class ORE {
	
	private LearningAlgorithm la;
	
	private KnowledgeSource ks; 
	private LearningProblem lp;
	private ComponentManager cm;
	
	private ReasonerComponent fastReasoner;
	
	private PelletReasoner pelletReasoner;
	
	
	private NamedClass class2Learn;
	private EvaluatedDescriptionClass newClassDescription;

	
	private OntologyModifier modifier;
	
	private String baseURI;
	private Map<String, String> prefixes;
	
	private double noise = 0.0;
	
	private File owlFile;
	
	
	public ORE() {
		
		cm = ComponentManager.getInstance();
		
	}
	
	// step 1: detect knowledge sources
	
	/**
	 * Applying knowledge source.
	 */
	public void setKnowledgeSource(File f) {
		this.owlFile = f;

		ks = cm.knowledgeSource(OWLFile.class);

		try {
			cm.applyConfigEntry(ks, "url", f.toURI().toURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		try {
			ks.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void initPelletReasoner(){
		pelletReasoner = cm.reasoner(PelletReasoner.class, ks);
		try {
			pelletReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pelletReasoner.loadOntologies();
	}
	
	public boolean consistentOntology() throws InconsistentOntologyException{
		return pelletReasoner.isConsistent();
			}
	
	public PelletReasoner getPelletReasoner(){
		return pelletReasoner;
	}
	
	
	/**
	 * Initialize the reasoners.
	 */
	public void initReasoners(){
		
		fastReasoner = cm.reasoner(FastInstanceChecker.class, ks);
		try {
			fastReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pelletReasoner.loadOntologies();
		pelletReasoner.classify();
		modifier = new OntologyModifier(pelletReasoner);
		baseURI = fastReasoner.getBaseURI();
		prefixes = fastReasoner.getPrefixes();
		
	}
	
	public String getInconsistencyExplanationsString(){
		ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
		StringWriter buffer = new StringWriter();
		renderer.startRendering(buffer);
		try {
			renderer.render(getInconsistencyExplanations());
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderer.endRendering();
		return buffer.toString();
	}
	
	private Set<Set<OWLAxiom>> getInconsistencyExplanations(){
		return pelletReasoner.getInconsistencyReasons();
	}
	
		
	public OntologyModifier getModifier() {
		return modifier;
	}


	public EvaluatedDescriptionClass getNewClassDescription() {
		return newClassDescription;
	}


	public String getBaseURI() {
		return baseURI;
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	
	public ReasonerComponent getFastReasoner() {
		return fastReasoner;
	}

	public void setLearningProblem(){
		
		lp = ComponentFactory.getClassLearningProblem(fastReasoner, getClass2LearnAsURL());
		
		try {
			lp.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private URL getClass2LearnAsURL(){
		URL classURL = null;
		try {
			classURL = new URL(class2Learn.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classURL;
		
	}
	
	public void setNoise(double noise){
		System.out.println("setze noise auf" + noise);
		cm.applyConfigEntry(la, "noisePercentage", noise);
		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.noise = noise;
	}
	
	public void setLearningAlgorithm(){
		
		try {
			la = ComponentFactory.getCELOE(lp, fastReasoner);
			cm.applyConfigEntry(la, "useNegation", false);
		} catch (LearningProblemUnsupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Sets the class that has to be learned.
	 * @param oldClass class that is chosen to be (re)learned
	 */
	public void setClassToLearn(NamedClass class2Learn){
		this.class2Learn = class2Learn;
	}
	
	public void init(){
		
		this.setLearningProblem();
		this.setLearningAlgorithm();
			
	}
	
	/**
	 * Starts the learning algorithm, setting noise value and ignored concepts.
	 * 
	 */
	public void start(){

		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		la.start();
		
	}
		
	public void setNewClassDescription(EvaluatedDescriptionClass newClassDescription) {
		this.newClassDescription = newClassDescription;
	}

	public LearningAlgorithm getLa() {
		return la;
	}

	public NamedClass getIgnoredConcept() {
		return class2Learn;
	}

	
	
	/**
	 * Retrieves description parts that might cause inconsistency - for negative examples only.
	 * @param ind
	 * @param desc
	 */
	public Set<Description> getNegCriticalDescriptions(Individual ind, Description desc){
		
		Set<Description> criticals = new HashSet<Description>();
		List<Description> children = desc.getChildren();
		
		if(pelletReasoner.hasType(desc, ind)){
			
			if(children.size() >= 2){
				
				if(desc instanceof Intersection){
					for(Description d: children){
						criticals.addAll(getNegCriticalDescriptions(ind, d));
					}
				} else if(desc instanceof Union){
					for(Description d: children){
						if(pelletReasoner.hasType(d, ind)){
							criticals.addAll(getNegCriticalDescriptions(ind, d));
						}
					}
				}
			} else{
				criticals.add(desc);
			}
		}
		
		return criticals;
	}
	/**
	 * Retrieves the description parts, that might cause inconsistency - for negative examples.
	 * @param ind
	 * @param desc
	 * @return vector of JLabel 
	 */
	public Collection<JLabel> descriptionToJLabelNeg(Individual ind, Description desc){

		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
//		try {
			if(fastReasoner.hasType(desc, ind)){
				
				if(children.size() >= 2){
					
					if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(i)));
							criticals.add(new JLabel("and"));
							
						}
						criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					} else if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(fastReasoner.hasType(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(i)));
							} else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("or"));
						}
						if(fastReasoner.hasType(desc.getChild(children.size()-1), ind)){
							criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(children.size()-1)));
						} else{
							criticals.add(new JLabel(desc.getChild(children.size()-1).toManchesterSyntaxString(baseURI, prefixes)));
						}
						criticals.add(new JLabel(")"));
						
							
					}
				} else{
					
					criticals.add(new DescriptionLabel(desc, "neg"));
				}
			} else{
				criticals.add(new JLabel(desc.toManchesterSyntaxString(baseURI, prefixes)));
			}
//		} catch (ReasoningMethodUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	return criticals;
	}
	
	/**
	 * Retrieves the description parts that might cause inconsistency - for positive examples.
	 * @param ind
	 * @param desc
	 * @return vector of JLabel 
	 */
	public Collection<JLabel> descriptionToJLabelPos(Individual ind, Description desc){

		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
//		try {
			if(!fastReasoner.hasType(desc, ind)){
				
				if(children.size() >= 2){
					
					if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(descriptionToJLabelPos(ind, desc.getChild(i)));
							criticals.add(new JLabel("or"));
						}
						criticals.addAll(descriptionToJLabelPos(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					} else if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(!fastReasoner.hasType(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelPos(ind, desc.getChild(i)));
							} else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("and"));
						}
						if(!fastReasoner.hasType(desc.getChild(children.size()-1), ind)){
							criticals.addAll(descriptionToJLabelPos(ind, desc.getChild(children.size()-1)));
						} else{
							criticals.add(new JLabel(desc.getChild(children.size()-1).toManchesterSyntaxString(baseURI, prefixes)));
						}
						criticals.add(new JLabel(")"));
					}
				} else{
					criticals.add(new DescriptionLabel(desc, "pos"));
				}
			} else{
				criticals.add(new JLabel(desc.toManchesterSyntaxString(baseURI, prefixes)));
			}
//		} catch (ReasoningMethodUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	return criticals;
	}
	
	/**
	 * Returns individuals that are in range of property.
	 * @param objRestr
	 * @param ind
	 */
	public Set<Individual> getIndividualsInPropertyRange(ObjectQuantorRestriction objRestr, Individual ind){
		
		Set<Individual> individuals = pelletReasoner.getIndividuals(objRestr.getChild(0));
		individuals.remove(ind);
		
		return individuals;
	}
	
	/**
	 * Returns individuals that are not in range of property.
	 * @param objRestr
	 * @param ind
	 */
	public Set<Individual> getIndividualsNotInPropertyRange(ObjectQuantorRestriction objRestr, Individual ind){
		

		Set<Individual> allIndividuals = new HashSet<Individual>();
		
		for(Individual i : pelletReasoner.getIndividuals()){
			
//			try {
				if(!fastReasoner.hasType(objRestr.getChild(0), i)){
					allIndividuals.add(i);
				}
//			} catch (ReasoningMethodUnsupportedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
	
		return allIndividuals;
	}
	
	/**
	 * Returns classes where individual might moved to.
	 * @param ind the individual
	 * @return set of classes
	 */
	public Set<NamedClass> getpossibleClassesMoveTo(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : pelletReasoner.getNamedClasses()){
			if(!pelletReasoner.hasType(nc, ind)){
				moveClasses.add(nc);
			}
		}
		moveClasses.remove(class2Learn);
			
		return moveClasses;
	}
	
	/**
	 * Returns classes where individual might moved from.
	 * @param ind the individual
	 * @return set of classes
	 */
	public Set<NamedClass> getpossibleClassesMoveFrom(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : pelletReasoner.getNamedClasses()){
			if(pelletReasoner.hasType(nc, ind)){
				moveClasses.add(nc);
			}
		}
		moveClasses.remove(class2Learn);
			
		return moveClasses;
	}
	
	/**
	 * Update reasoners ontology.
	 */
	public void updateReasoner(){
		fastReasoner = cm.reasoner(FastInstanceChecker.class, new OWLAPIOntology(modifier.getOntology()));
		try {
			fastReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			pelletReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setLearningAlgorithm();
	}
	
	/**
	 * Get the complement classes where individual is asserted to.
	 * @param desc
	 * @param ind
	 */
	public Set<NamedClass> getComplements(Description desc, Individual ind){

		Set<NamedClass> complements = new HashSet<NamedClass>();
		System.out.println(pelletReasoner.getComplementClasses(desc));
		for(NamedClass nc : pelletReasoner.getNamedClasses()){
			if(!(nc.toString().endsWith("Thing"))){
				if(pelletReasoner.hasType(nc, ind)){
					if(modifier.isComplement(desc, nc)){
						complements.add(nc);
					}
				}
			}
		}
		
		
		return complements;
	}

	

	public static void main(String[] args){
		try{
		ComponentManager cm = ComponentManager.getInstance();
		
		// create knowledge source
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		String example = "examples/ore/inconsistent.owl";
		cm.applyConfigEntry(source, "url", new File(example).toURI().toURL());
		source.init();
		
		// create OWL API reasoning service with standard settings
		ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, source);
		reasoner.init();
		
		// create a learning problem and set positive and negative examples
		LearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
//		cm.applyConfigEntry(lp, "type", "superClass");
		cm.applyConfigEntry(lp, "classToDescribe", "http://cohse.semanticweb.org/ontologies/people#mad+cow");
		lp.init();
		
		// create the learning algorithm
		LearningAlgorithm la = null;
		try {
			la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			la.init();
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
	
		// start the algorithm and print the best concept found
		la.start();
	
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ComponentInitException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
		
		
		
		
		
	
}


	
//	public static void main(String[] args){
//		
//		final ORE test = new ORE();
//		
//		File owlFile = new File("src/dl-learner/org/dllearner/tools/ore/neg_has_all.owl");
//		
//		test.setKnowledgeSource(owlFile);
//		test.initReasoners();
//		
//		Individual subject = new Individual("http://example.com/father#patrick");
//		Description newClass = new Intersection(new NamedClass("http://example.com/father#female"), 
//												new Negation(new NamedClass("http://example.com/father#bird")));
//		Description desc = new NamedClass("http://example.com/father#bird");
//		
//		Description range = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//				new NamedClass("http://example.com/father#female"));
//		ObjectAllRestriction role = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//				range);
//		System.out.println(role.toManchesterSyntaxString(test.getBaseURI(), test.getPrefixes()));
//		try {
//			System.out.println(test.fastReasoner.instanceCheck(role, subject));
//		} catch (ReasoningMethodUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		test.modifier.addObjectProperty(subject, role, new Individual("http://example.com/father#anna"));
//		test.updateReasoner();
//		try {
//			System.out.println(test.fastReasoner.instanceCheck(role, subject));
//		} catch (ReasoningMethodUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		test.modifier.saveOntology();
//		
//	
//		
//		
		
//		System.out.println(test.owlReasoner.getInconsistentClasses());
//		test.getModi().reason();

//		
//		Individual subject = new Individual("http://example.com/father#anton");
//		
//		Description range = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//													new NamedClass("http://example.com/father#female"));
//		ObjectAllRestriction role = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//													range);
//		Description d = new Intersection(new NamedClass("http://example.com/father#male"), role);
//		try {
//			System.out.println(test.fastReasoner.instanceCheck(d, subject));
//		} catch (ReasoningMethodUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("vorher" + test.modi.ontology.getAxioms());
//		Individual object = new Individual("http://example.com/father#markus");
//		test.modi.addObjectProperty(subject, role, object);
//		test.updateReasoner();
//		try {
//			System.out.println(test.fastReasoner.instanceCheck(d, subject));
//		} catch (ReasoningMethodUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
//		test.modi.reason();
		
//		Individual ind = new Individual("http://example.com/father#heinz");
//		Description d1 = new Intersection(new NamedClass("http://example.com/father#male"), new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"), new NamedClass("http://example.com/father#male")));
//		System.out.println(test.reasoner2.instanceCheck(d, ind));
		
		
//		test.setConcept(new NamedClass("http://example.com/father#father"));
//		test.setPosNegExamples();
//		System.out.println(test.posExamples);
//		System.out.println(test.negExamples);
//		test.start();
//		Individual ind = new Individual("http://www.test.owl#lorenz");
		
//		test.modi.addClassAssertion(ind, new NamedClass("http://www.test.owl#B"));
//		System.out.println(test.reasoner2.getInconsistentClasses());
//		Description d = new Intersection(new NamedClass("http://www.test.owl#A"), new Union(new NamedClass("http://www.test.owl#B"),
//				new NamedClass("http://www.test.owl#C")));
//		System.out.println(d);
//		System.out.println(test.getCriticalDescriptions(ind, d));
//		JFrame testFrame = new JFrame();
//		JPanel j = new JPanel();
//		testFrame.add(j);
//		testFrame.setSize(new Dimension(400, 400));
//		for(JLabel jLab : test.DescriptionToJLabel(ind, d))
//			j.add(jLab);
//		testFrame.setVisible(true);
//}
		
		
		


	
	
	  
	

