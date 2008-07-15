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
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
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
	private SortedSet<Individual> posExamples;
	private SortedSet<Individual> negExamples;
	NamedClass ignoredConcept;
	Description conceptToAdd;
	OntologyModifierOWLAPI modi;
	private String baseURI;
	public String getBaseURI() {
		return baseURI;
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}






	private Map<String, String> prefixes;
	public Set<NamedClass> allAtomicConcepts;
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
		
				
		
		reasoner2 = cm.reasoner(OWLAPIReasoner.class, ks);
		try {
			reasoner2.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = cm.reasoningService(reasoner);
		modi = new OntologyModifierOWLAPI(reasoner2);
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
		
	
		
		
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
	
		Set<String> t = new TreeSet<String>();
		
		
		t.add(ignoredConcept.getName());
		cm.applyConfigEntry(la, "ignoredConcepts", t );
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
		
		this.setPosNegExamples();
		this.setLearningProblem();
		this.setLearningAlgorithm();
		try {
			reasoner2.init();
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public LearningAlgorithm start(){
		Set<String> t = new TreeSet<String>();
		
		cm.applyConfigEntry(la, "ignoredConcepts", t );
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
	
	public Collection<JLabel> DescriptionToJLabelNeg(Individual ind, Description desc){
//		Set<JLabel> criticals = new HashSet<JLabel>();
		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
		try {
			if(reasoner.instanceCheck(desc, ind)){
				
				if(children.size() >= 2){
					
					if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(DescriptionToJLabelNeg(ind, desc.getChild(i)));
							criticals.add(new JLabel("and"));
							
						}
						criticals.addAll(DescriptionToJLabelNeg(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					}
					else if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(reasoner.instanceCheck(desc.getChild(i), ind)){
								criticals.addAll(DescriptionToJLabelNeg(ind, desc.getChild(i)));
							}
							else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("or"));
						}
						if(reasoner.instanceCheck(desc.getChild(children.size()-1), ind)){
							criticals.addAll(DescriptionToJLabelNeg(ind, desc.getChild(children.size()-1)));
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
	
	public Collection<JLabel> DescriptionToJLabelPos(Individual ind, Description desc){

		Collection<JLabel> criticals = new Vector<JLabel>();
		List<Description> children = desc.getChildren();
		
		try {
			if(!reasoner.instanceCheck(desc, ind)){
				
				if(children.size() >= 2){
					
					if(desc instanceof Intersection){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(i)));
							criticals.add(new JLabel("and"));
						}
						criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(children.size()-1)));
						criticals.add(new JLabel(")"));
					}
					else if(desc instanceof Union){
						criticals.add(new JLabel("("));
						for(int i = 0; i<children.size()-1; i++){
							if(!reasoner.instanceCheck(desc.getChild(i), ind)){
								criticals.addAll(DescriptionToJLabelPos(ind, desc.getChild(i)));
							}
							else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("or"));
						}
						if(!reasoner.instanceCheck(desc.getChild(children.size()-1), ind)){
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
	
	public Set<Individual> getIndividualsOfPropertyRange(ObjectQuantorRestriction objRestr, Individual ind){
		
		Set<Individual> individuals = reasoner2.retrieval(objRestr.getChild(0));
		individuals.remove(ind);
		
		return individuals;
	}
	
	public Set<Individual> getIndividualsNotInPropertyRange(ObjectQuantorRestriction objRestr, Individual ind){
		

		Set<Individual> allIndividuals = new HashSet<Individual>();
		
		for(Individual i : reasoner2.getIndividuals()){
			
			try {
				if(!reasoner.instanceCheck(objRestr.getChild(0), i)){
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
		for(NamedClass nc : rs.getAtomicConcepts())
			if(!rs.instanceCheck(nc, ind))
				moveClasses.add(nc);

			
		return moveClasses;
	}
	
	public Set<NamedClass> getpossibleClassesMoveFrom(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : rs.getAtomicConcepts()){
			if(rs.instanceCheck(nc, ind))
				moveClasses.add(nc);
		}
		moveClasses.remove(ignoredConcept);
			
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
		reasoner2 = cm.reasoner(OWLAPIReasoner.class,new OWLAPIOntology(modi.ontology));
		
		try {
			reasoner2.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = cm.reasoningService(reasoner2);
//		setLearningAlgorithm();
	}
	
	public Set<NamedClass> getComplements(Description desc, Individual ind){
		Set<NamedClass> complements = new HashSet<NamedClass>();
		for(NamedClass nc : reasoner2.getAtomicConcepts()){
			if(reasoner2.instanceCheck(nc, ind)){
				if(modi.isComplement(desc, nc)){
					complements.add(nc);
				}
			}
		}

		return complements;
	}
	
	
	
		
	
	
	public static void main(String[] args){
		
		final ORE test = new ORE();
		
		File owlFile = new File("src/dl-learner/org/dllearner/tools/ore/neg_has_all.owl");
		
		test.setKnowledgeSource(owlFile);
	
		test.detectReasoner();
		
		Individual subject = new Individual("http://example.com/father#anton");
		
		Description range = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
													new NamedClass("http://example.com/father#female"));
		ObjectAllRestriction role = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
													range);
		Description d = new Intersection(new NamedClass("http://example.com/father#male"), role);
		try {
			System.out.println(test.reasoner.instanceCheck(d, subject));
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("vorher" + test.modi.ontology.getAxioms());
		Individual object = new Individual("http://example.com/father#markus");
		test.modi.addObjectProperty(subject, role, object);
		test.updateReasoner();
		try {
			System.out.println(test.reasoner.instanceCheck(d, subject));
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
		
		
	
		
		
		
		
	}
		
		
		

}
	
	
	  
	

