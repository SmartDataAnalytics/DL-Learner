package org.dllearner.tools.ore;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.dllearner.core.owl.Nothing;
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
	
	FastInstanceChecker reasoner;
	OWLAPIReasoner reasoner2;
	SortedSet<Individual> posExamples;
	SortedSet<Individual> negExamples;
	NamedClass ignoredConcept;
	Description conceptToAdd;
	OntologyModifierOWLAPI modi;
	Set<NamedClass> allAtomicConcepts;
	private double noise = 0.0;
	
	Thread t;
	
	
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
	
	
	
	public void detectReasoner(){
		
		reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
//		rs = cm.reasoningService(reasoner);
		reasoner2 = cm.reasoner(OWLAPIReasoner.class, ks);
		try {
			reasoner2.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		modi = new OntologyModifierOWLAPI(reasoner2);
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
		
		for (Individual rem_pos : posExamples)
			negExamples.remove(rem_pos);
	}
	
	public SortedSet<Individual> getNegExamples(){
		return negExamples;
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
		//la = new ROLearner(lp, rs);
		
		Set<String> t = new TreeSet<String>();
		t.add(ignoredConcept.getName());
		cm.applyConfigEntry(la, "ignoredConcepts", t );
		cm.applyConfigEntry(la, "noisePercentage", noise);
		cm.applyConfigEntry(la, "guaranteeXgoodDescriptions", 5);
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

	
	public LearningAlgorithm start(){
		
		this.setPosNegExamples();
		this.setLearningProblem();
		this.setLearningAlgorithm();
		
		la.start();
			
		return la;
		
	}
	
	public Description getLearningResult(){
		return la.getCurrentlyBestDescription();
	}
	
//	public List<Description> getSolutions(){
//		return la.getCurrentlyBestDescriptions();
//	}
	
	public List<Description> getLearningResults(int anzahl){
		return la.getCurrentlyBestDescriptions(anzahl);
	}
	
	/**
	 * 
	 * @param d
	 * @return
	 */
	public BigDecimal getCorrectness(Description d){
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
			if(instanceRs.instanceCheck(conceptToAdd, ind))
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
			if(!instanceRs.instanceCheck(conceptToAdd, ind))
				posFailureExamples.add(ind);
		}
		
		HashSet<Individual> negFailureExamples = new HashSet<Individual>() ;
		for(Individual ind : negExamples){
			if(instanceRs.instanceCheck(conceptToAdd, ind))
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
			if(!instanceRs.instanceCheck(conceptToAdd, ind))
				posFailureExamples.add(ind);
				
		}
		
		return posFailureExamples;
	}


	
		
	

	public Description getConceptToAdd() {
		return conceptToAdd;
	}

	public void setConceptToAdd(Description conceptToAdd) {
		this.conceptToAdd = conceptToAdd;
	}

	public LearningAlgorithm getLa() {
		return la;
	}

	public OntologyModifierOWLAPI getModi() {
		return modi;
	}

	public NamedClass getIgnoredConcept() {
		return ignoredConcept;
	}

	public void setAllAtomicConcepts(Set<NamedClass> allAtomicConcepts) {
		this.allAtomicConcepts = allAtomicConcepts;
	}
	
	public Set<Description> getAllChildren(Description desc){
		Set<Description> allChildren = new HashSet<Description>();
		List<Description> children = desc.getChildren();
		
		if(children.size() >= 2)
			for(Description d : children)
				allChildren.addAll(getAllChildren(d));
		else
			allChildren.add(desc);
				
		return allChildren;
	}
		
	public Set<Description> getCriticalDescriptions(Individual ind, Description desc){
		
	
		Set<Description> criticals = new HashSet<Description>();
		List<Description> children = desc.getChildren();
		
				
		if(reasoner2.instanceCheck(desc, ind)){
			
			if(children.size() >= 2){
				
				if(desc instanceof Intersection){
					for(Description d: children)
						criticals.addAll(getCriticalDescriptions(ind, d));
				
				}
				else if(desc instanceof Union){
					for(Description d: children)
						if(reasoner2.instanceCheck(d, ind))
							criticals.addAll(getCriticalDescriptions(ind, d));
				}
			}
			else
				criticals.add(desc);
		}
		
		
		return criticals;
	}
	
	public Collection<JLabel> DescriptionToJLabel(Individual ind, Description desc){
//		Set<JLabel> criticals = new HashSet<JLabel>();
		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
		try {
			if(reasoner.instanceCheck(desc, ind)){
				if(children.size() >= 2){
					
					if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(DescriptionToJLabel(ind, desc.getChild(i)));
							criticals.add(new JLabel("AND"));
							System.out.println(true);
						}
						criticals.addAll(DescriptionToJLabel(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					}
					else if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(reasoner.instanceCheck(desc.getChild(i), ind)){
								criticals.addAll(DescriptionToJLabel(ind, desc.getChild(i)));
							}
							else{
								criticals.add(new JLabel(desc.getChild(i).toString()));
							}
							criticals.add(new JLabel("OR"));
						}
						if(reasoner.instanceCheck(desc.getChild(children.size()-1), ind)){
							criticals.addAll(DescriptionToJLabel(ind, desc.getChild(children.size()-1)));
						}
						else{
							criticals.add(new JLabel(desc.getChild(children.size()-1).toString()));
						}
						criticals.add(new JLabel(")"));
						
							
					}
				}
				else{
				
					criticals.add(new DescriptionLabel(desc));
				}
			}
			else
				criticals.add(new JLabel(desc.toString()));
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	return criticals;
	}
	
	public Set<Individual> getIndividualsOfPropertyRange(ObjectQuantorRestriction objRestr){
		
		
		System.out.println(objRestr.getChild(0));
		
		
		Set<Individual> individuals = rs.retrieval(objRestr.getChild(0));
		System.out.println(objRestr.getRole());
		
		System.out.println(individuals);
		
		return individuals;
	}
	
	public Set<NamedClass> getpossibleMoveClasses(Individual ind){
		Set<NamedClass> moveClasses = rs.getAtomicConcepts();
		Set<NamedClass> indClasses = new HashSet<NamedClass>();
		
		for(NamedClass moveNc : moveClasses)
			if(rs.instanceCheck(moveNc, ind)){
				indClasses.add(moveNc);
		}
		moveClasses.removeAll(indClasses);
		
		for(NamedClass moveNc : moveClasses)
			for(NamedClass indNc : indClasses )
				if(new Intersection(moveNc, indNc).equals(new Nothing()))
					moveClasses.remove(moveNc);
			
		return moveClasses;
	}
	
	public void updateReasoner(){
		reasoner = cm.reasoner(FastInstanceChecker.class, new OWLAPIOntology(modi.ontology));
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean hasComplement(Description desc, Individual ind){
		
		for(NamedClass nc : reasoner2.getAtomicConcepts())
			if(reasoner2.instanceCheck(nc, ind))
				if(modi.isComplement(desc, nc))
					return true;
							
		return false;
	}
	
		
	
	
	public static void main(String[] args){
		
		final ORE test = new ORE();
		
		File owlFile = new File("src/dl-learner/org/dllearner/tools/ore/inconsistent.owl");
		
		test.setKnowledgeSource(owlFile);
	
		test.detectReasoner();
//		System.out.println(test.reasoner2.getInconsistentClasses());
//		System.err.println("Concepts :" + rs.getAtomicConcepts());
		
		
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
		
		
	
		
		
		
		
	}
		
		
		

}
	
	
	  
	

