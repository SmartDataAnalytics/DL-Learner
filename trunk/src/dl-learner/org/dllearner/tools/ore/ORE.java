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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JLabel;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;


public class ORE {
	
	private LearningAlgorithm la;
	private ReasoningService rs;
	private KnowledgeSource ks; 
	private PosNegDefinitionLP lp;
	private ComponentManager cm;
	
	private FastInstanceChecker fastReasoner;
	private OWLAPIReasoner owlReasoner;
	
	private SortedSet<Individual> posExamples;
	private SortedSet<Individual> negExamples;
	
	private NamedClass ignoredConcept;
	private Description newClassDescription;
	private Set<NamedClass> allAtomicConcepts;
	
	private OntologyModifier modifier;
	
	private String baseURI;
	private Map<String, String> prefixes;
	
	private double noise = 0.0;
	
	
	public ORE() {

		cm = ComponentManager.getInstance();

	}
	
	// step 1: detect knowledge sources
	
	public void setKnowledgeSource(File f) {

		Class<OWLFile> owl = OWLFile.class;
		ks = cm.knowledgeSource(owl);

		cm.applyConfigEntry(ks, "url", f.toURI().toString());
		
		try {
			ks.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	public void initReasoners(){
		
		fastReasoner = cm.reasoner(FastInstanceChecker.class, ks);
		try {
			fastReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		
		owlReasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		try {
			owlReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = cm.reasoningService(fastReasoner);
		modifier = new OntologyModifier(owlReasoner);
		baseURI = fastReasoner.getBaseURI();
		prefixes = fastReasoner.getPrefixes();
	
	}
	
	public ReasoningService getReasoningService(){
		return rs;
	}
	
	public SortedSet<Individual> getPosExamples(){
		return posExamples;
	}
	
	public void setPosNegExamples(){
		posExamples = rs.retrieval(ignoredConcept);
		negExamples = rs.getIndividuals();
		
		for (Individual pos : posExamples){
			negExamples.remove(pos);
		}
	}
	
	public SortedSet<Individual> getNegExamples(){
		return negExamples;
	}
	
	public OntologyModifier getModifier() {
		return modifier;
	}


	public Description getNewClassDescription() {
		return newClassDescription;
	}


	public String getBaseURI() {
		return baseURI;
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	public OWLAPIReasoner getOwlReasoner() {
		return owlReasoner;
	}
	
	public FastInstanceChecker getFastReasoner() {
		return fastReasoner;
	}

	public void setLearningProblem(){
		lp = new PosNegDefinitionLP(rs, posExamples, negExamples);
		lp.init();
	}
	
	public void setNoise(double noise){
		this.noise = noise;
	}
	
	public void setLearningAlgorithm(){
		try {
			la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);
		} catch (LearningProblemUnsupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		Set<String> t = new TreeSet<String>();
		
		
		t.add(ignoredConcept.getName());
		cm.applyConfigEntry(la, "ignoredConcepts", t);
		cm.applyConfigEntry(la, "guaranteeXgoodDescriptions", 10);
		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setConcept(NamedClass concept){
		this.ignoredConcept = concept;
	}
	
	public void init(){
		try {
			owlReasoner.init();
			fastReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setPosNegExamples();
		this.setLearningProblem();
		this.setLearningAlgorithm();
			
	}
	
	public LearningAlgorithm start(){
		Set<String> t = new TreeSet<String>();
		t.add(ignoredConcept.getName());
		cm.applyConfigEntry(la, "ignoredConcepts", t);
		cm.applyConfigEntry(la, "noisePercentage", noise);
		try {
			la.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		la.start();
		return la;
	}
	
	public List<Description> getLearningResults(int anzahl){
		return la.getCurrentlyBestDescriptions(anzahl, true);
	}
	
	/**
	 * returns accuracy of a description
	 * @param d
	 * @return
	 */
	public BigDecimal computeAccuracy(Description d){
		int numberPosExamples = 0;
		int numberNegExamples = 0;
		double result_tmp = 0.0f;
		
		for(Individual ind : posExamples){
			if(rs.instanceCheck(d, ind))
				numberPosExamples++;
		}
		for(Individual ind : negExamples){
			if(!rs.instanceCheck(d, ind))
				numberNegExamples++;
		}
		
		result_tmp = ((float)(numberPosExamples) + (float)(numberNegExamples))/((float)(posExamples.size())+(float)(negExamples.size())) * 100;
		BigDecimal result = new BigDecimal( result_tmp );
		result = result.setScale( 2, BigDecimal.ROUND_HALF_UP );
		return result;	
		
		
	}
	
	public HashSet<Individual> getNegFailureExamples(){
		FastInstanceChecker instanceReasoner = cm.reasoner(FastInstanceChecker.class, ks);
		try {
			instanceReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ReasoningService instanceRs = cm.reasoningService(instanceReasoner);
		
		HashSet<Individual> negFailureExamples = new HashSet<Individual>() ;
		
		for(Individual ind : negExamples){
			if(instanceRs.instanceCheck(newClassDescription, ind))
				negFailureExamples.add(ind);
				
		}
		
		return negFailureExamples;
	}
	
	public List<HashSet<Individual>> getFailureExamples(){
		List<HashSet<Individual>> list = new ArrayList<HashSet<Individual>>();
		
		FastInstanceChecker instanceReasoner = cm.reasoner(FastInstanceChecker.class, ks);
		try {
			instanceReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ReasoningService instanceRs = cm.reasoningService(instanceReasoner);
		
		HashSet<Individual> posFailureExamples = new HashSet<Individual>() ;
		for(Individual ind : posExamples){
			if(!instanceRs.instanceCheck(newClassDescription, ind))
				posFailureExamples.add(ind);
		}
		
		HashSet<Individual> negFailureExamples = new HashSet<Individual>() ;
		for(Individual ind : negExamples){
			if(instanceRs.instanceCheck(newClassDescription, ind))
				negFailureExamples.add(ind);
		}
		
		list.add(posFailureExamples);
		list.add(negFailureExamples);
		
		
		
		return list;
		
	}
	public HashSet<Individual> getPosFailureExamples(){
		FastInstanceChecker instanceReasoner = cm.reasoner(FastInstanceChecker.class, ks);
		try {
			instanceReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ReasoningService instanceRs = cm.reasoningService(instanceReasoner);
		
		HashSet<Individual> posFailureExamples = new HashSet<Individual>() ;
		
		
		for(Individual ind : posExamples){
			if(!instanceRs.instanceCheck(newClassDescription, ind))
				posFailureExamples.add(ind);
				
		}
		
		return posFailureExamples;
	}

	public void setNewClassDescription(Description newClassDescription) {
		this.newClassDescription = newClassDescription;
	}

	public LearningAlgorithm getLa() {
		return la;
	}

	public NamedClass getIgnoredConcept() {
		return ignoredConcept;
	}

	public void setAllAtomicConcepts(Set<NamedClass> allAtomicConcepts) {
		this.allAtomicConcepts = allAtomicConcepts;
	}
	
	/**
	 * finds out description parts that might cause inconsistency - for negative examples only
	 * @param ind
	 * @param desc
	 * @return
	 */
	public Set<Description> getNegCriticalDescriptions(Individual ind, Description desc){
		
		Set<Description> criticals = new HashSet<Description>();
		List<Description> children = desc.getChildren();
		
		if(owlReasoner.instanceCheck(desc, ind)){
			
			if(children.size() >= 2){
				
				if(desc instanceof Intersection){
					for(Description d: children)
						criticals.addAll(getNegCriticalDescriptions(ind, d));
				
				}
				else if(desc instanceof Union){
					for(Description d: children)
						if(owlReasoner.instanceCheck(d, ind))
							criticals.addAll(getNegCriticalDescriptions(ind, d));
				}
			}
			else
				criticals.add(desc);
		}
		
		return criticals;
	}
	/**
	 * finds the description that might cause inconsistency for negative examples
	 * @param ind
	 * @param desc
	 * @return vector of JLabel 
	 */
	public Collection<JLabel> descriptionToJLabelNeg(Individual ind, Description desc){

		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
		try {
			if(fastReasoner.instanceCheck(desc, ind)){
				
				if(children.size() >= 2){
					
					if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(i)));
							criticals.add(new JLabel("and"));
							
						}
						criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					}
					else if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(fastReasoner.instanceCheck(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(i)));
							}
							else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("or"));
						}
						if(fastReasoner.instanceCheck(desc.getChild(children.size()-1), ind)){
							criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(children.size()-1)));
						}
						else{
							criticals.add(new JLabel(desc.getChild(children.size()-1).toManchesterSyntaxString(baseURI, prefixes)));
						}
						criticals.add(new JLabel(")"));
						
							
					}
				}
				else{
					
					criticals.add(new DescriptionLabel(desc, "neg"));
				}
			}
			else
				criticals.add(new JLabel(desc.toManchesterSyntaxString(baseURI, prefixes)));
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	return criticals;
	}
	
	/**
	 * finds the description that might cause inconsistency for positive examples
	 * @param ind
	 * @param desc
	 * @return vector of JLabel 
	 */
	public Collection<JLabel> DescriptionToJLabelPos(Individual ind, Description desc){

		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
		try {
			if(!fastReasoner.instanceCheck(desc, ind)){
				
				if(children.size() >= 2){
					
					if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(i)));
							criticals.add(new JLabel("or"));
						}
						criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					}
					else if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(!fastReasoner.instanceCheck(desc.getChild(i), ind)){
								criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(i)));
							}
							else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("and"));
						}
						if(!fastReasoner.instanceCheck(desc.getChild(children.size()-1), ind)){
							criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(children.size()-1)));
						}
						else{
							criticals.add(new JLabel(desc.getChild(children.size()-1).toManchesterSyntaxString(baseURI, prefixes)));
						}
						criticals.add(new JLabel(")"));
						
							
					}
				}
				else{
					
					criticals.add(new DescriptionLabel(desc, "pos"));
				}
			}
			else
				criticals.add(new JLabel(desc.toManchesterSyntaxString(baseURI, prefixes)));
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	return criticals;
	}
	
	/**
	 * returns individuals that are in range of property
	 * @param objRestr
	 * @param ind
	 * @return
	 */
	public Set<Individual> getIndividualsInPropertyRange(ObjectQuantorRestriction objRestr, Individual ind){
		
		Set<Individual> individuals = owlReasoner.retrieval(objRestr.getChild(0));
		individuals.remove(ind);
		
		return individuals;
	}
	
	/**
	 * returns individuals that are not in range of property
	 * @param objRestr
	 * @param ind
	 * @return
	 */
	public Set<Individual> getIndividualsNotInPropertyRange(ObjectQuantorRestriction objRestr, Individual ind){
		

		Set<Individual> allIndividuals = new HashSet<Individual>();
		
		for(Individual i : owlReasoner.getIndividuals()){
			
			try {
				if(!fastReasoner.instanceCheck(objRestr.getChild(0), i)){
					allIndividuals.add(i);
				}
			} catch (ReasoningMethodUnsupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		return allIndividuals;
	}
	
	public Set<NamedClass> getpossibleClassesMoveTo(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : rs.getNamedClasses()){
			if(!rs.instanceCheck(nc, ind)){
				moveClasses.add(nc);
			}
		}
		moveClasses.remove(ignoredConcept);
			
		return moveClasses;
	}
	
	public Set<NamedClass> getpossibleClassesMoveFrom(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : rs.getNamedClasses()){
			if(rs.instanceCheck(nc, ind)){
				moveClasses.add(nc);
			}
		}
		moveClasses.remove(ignoredConcept);
			
		return moveClasses;
	}
	
	public void updateReasoner(){
		fastReasoner = cm.reasoner(FastInstanceChecker.class, new OWLAPIOntology(modifier.getOntology()));
		try {
			fastReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		owlReasoner = cm.reasoner(OWLAPIReasoner.class,new OWLAPIOntology(modifier.getOntology()));
		
		try {
			owlReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = cm.reasoningService(owlReasoner);
		setLearningAlgorithm();
	}
	
	/**
	 * get the complement classes where individual is asserted to
	 * @param desc
	 * @param ind
	 * @return
	 */
	public Set<NamedClass> getComplements(Description desc, Individual ind){
		Set<NamedClass> complements = new HashSet<NamedClass>();
		for(NamedClass nc : owlReasoner.getAtomicConcepts()){
			
			if(owlReasoner.instanceCheck(nc, ind)){
				if(modifier.isComplement(desc, nc)){
					complements.add(nc);
				}
			}
		}
		
		return complements;
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
		
		
		


	
	
	  
	

