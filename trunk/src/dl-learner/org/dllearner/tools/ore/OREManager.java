package org.dllearner.tools.ore;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.tools.ore.ui.DescriptionLabel;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;

public class OREManager {

	private static OREManager instance;

	private ComponentManager cm;

	private PelletReasoner reasoner;
	private ClassLearningProblem lp;
	private CELOE la;
	private KnowledgeSource ks;

	private String baseURI;
	private Map<String, String> prefixes;

	private NamedClass currentClass2Learn;
	private EvaluatedDescriptionClass learnedClassDescription;

	private double noisePercentage;
	private int maxExecutionTimeInSeconds = 10;
	private int maxNrOfResults = 10;

	private List<OREManagerListener> listeners;

	private OntologyModifier modifier;

	public OREManager(){
		cm = ComponentManager.getInstance();
		listeners = new ArrayList<OREManagerListener>();
	}
	
	public static synchronized OREManager getInstance() {
		if (instance == null) {
			instance = new OREManager();
		}
		return instance;
	}
	
	public void setCurrentKnowledgeSource(URI uri){
		ks = cm.knowledgeSource(OWLFile.class);
		try {
			((OWLFile)ks).getConfigurator().setUrl(uri.toURL());
			ks.init();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ComponentInitException e) {
			System.out.println("Could not init knowledge source");
			e.printStackTrace();
		}
		
	}
	
	public void setCurrentKnowledgeSource(SparqlKnowledgeSource ks){
		this.ks = ks;
		
	}
	
	public KnowledgeSource getKnowledgeSource(){
		return ks;
	}
	
	public void setLearningProblem(){
		
		lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		
		try {
			lp.getConfigurator().setClassToDescribe(getClass2LearnAsURL());
			lp.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setLearningAlgorithm(){
		
		try {
			la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			la.getConfigurator().setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
			la.getConfigurator().setUseNegation(false);
			la.getConfigurator().setNoisePercentage(noisePercentage);
			la.getConfigurator().setMaxNrOfResults(maxNrOfResults);
			
			
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
	
	
	public void initPelletReasoner(){
		reasoner = cm.reasoner(PelletReasoner.class, ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reasoner.loadOntologies();
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
		modifier = new OntologyModifier(reasoner);
		fireActiveOntologyChanged();
	}
	
	public void loadOntology(){
		reasoner.loadOntologies();	
	}
	
	public void makeOWAToCWA(){
		reasoner.dematerialise();
	}
	
	public void addListener(OREManagerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(OREManagerListener listener){
		listeners.remove(listener);
	}
	
	public boolean consistentOntology() throws InconsistentOntologyException{
		return reasoner.isConsistent();
	}
	
	public PelletReasoner getReasoner(){
		return reasoner;
	}
	
	private void fireActiveOntologyChanged(){
		for(OREManagerListener listener : listeners){
			listener.activeOntologyChanged();
		}
	}
		
	public OntologyModifier getModifier() {
		return modifier;
	}

	public EvaluatedDescriptionClass getNewClassDescription() {
		return learnedClassDescription;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	private URL getClass2LearnAsURL(){
		URL classURL = null;
		try {
			classURL = new URL(currentClass2Learn.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classURL;
		
	}
	
	public SortedSet<Individual> getPositiveFailureExamples(){
		SortedSet<Individual> posNotCovered = reasoner.getIndividuals(currentClass2Learn);
		posNotCovered.removeAll(learnedClassDescription.getCoveredInstances());
		return posNotCovered;
	}
	
	public SortedSet<Individual> getNegativeFailureExamples(){
		return new TreeSet<Individual>(learnedClassDescription.getAdditionalInstances());
	}
	
	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}
	
	public int getMaxNrOfResults(){
		return maxNrOfResults;
	}
		
	/**
	 * Sets the class that has to be learned.
	 * @param oldClass class that is chosen to be (re)learned
	 */
	public void setCurrentClass2Learn(NamedClass class2Learn){
		this.currentClass2Learn = class2Learn;
	}
	
	public NamedClass getCurrentClass2Learn(){
		return currentClass2Learn;
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
		learnedClassDescription = newClassDescription;
	}

	
	public CELOE getLa() {
		return la;
	}

	/**
	 * Retrieves description parts that might cause inconsistency - for negative examples only.
	 * @param ind
	 * @param desc
	 */
	public Set<Description> getNegCriticalDescriptions(Individual ind, Description desc){
		
		Set<Description> criticals = new HashSet<Description>();
		List<Description> children = desc.getChildren();
		
		if(reasoner.hasType(desc, ind)){
			
			if(children.size() >= 2){
				
				if(desc instanceof Intersection){
					for(Description d: children){
						criticals.addAll(getNegCriticalDescriptions(ind, d));
					}
				} else if(desc instanceof Union){
					for(Description d: children){
						if(reasoner.hasType(d, ind)){
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
			if(reasoner.hasType(desc, ind)){
				
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
							if(reasoner.hasType(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(i)));
							} else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("or"));
						}
						if(reasoner.hasType(desc.getChild(children.size()-1), ind)){
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
			if(!reasoner.hasType(desc, ind)){
				
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
							if(!reasoner.hasType(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelPos(ind, desc.getChild(i)));
							} else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("and"));
						}
						if(!reasoner.hasType(desc.getChild(children.size()-1), ind)){
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
	public Set<Individual> getIndividualsInPropertyRange(Description desc, Individual ind){
		
		Set<Individual> individuals = reasoner.getIndividuals(desc);
		individuals.remove(ind);
		
		return individuals;
	}
	
	/**
	 * Returns individuals that are not in range of property.
	 * @param objRestr
	 * @param ind
	 */
	public Set<Individual> getIndividualsNotInPropertyRange(Description desc, Individual ind){
		

		Set<Individual> allIndividuals = new HashSet<Individual>();
		
		for(Individual i : reasoner.getIndividuals()){
			
				if(!reasoner.hasType(desc, i)){
					allIndividuals.add(i);
				}

		}
		allIndividuals.remove(ind);
	
		return allIndividuals;
	}
	
	/**
	 * Returns classes where individual might moved to.
	 * @param ind the individual
	 * @return set of classes
	 */
	public Set<NamedClass> getpossibleClassesMoveTo(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : reasoner.getNamedClasses()){
			if(!reasoner.hasType(nc, ind)){
				moveClasses.add(nc);
			}
		}
		moveClasses.remove(currentClass2Learn);
			
		return moveClasses;
	}
	
	/**
	 * Returns classes where individual might moved from.
	 * @param ind the individual
	 * @return set of classes
	 */
	public Set<NamedClass> getpossibleClassesMoveFrom(Individual ind){
		Set<NamedClass> moveClasses = new HashSet<NamedClass>();
		for(NamedClass nc : reasoner.getNamedClasses()){
			if(reasoner.hasType(nc, ind)){
				moveClasses.add(nc);
			}
		}
		moveClasses.remove(currentClass2Learn);
			
		return moveClasses;
	}
		
	/**
	 * Get the complement classes where individual is asserted to.
	 * @param desc
	 * @param ind
	 */
	public Set<NamedClass> getComplements(Description desc, Individual ind){

		Set<NamedClass> complements = new HashSet<NamedClass>();
		
		for(NamedClass nc : reasoner.getNamedClasses()){
			if(!(nc.toString().endsWith("Thing"))){
				if(reasoner.hasType(nc, ind)){
					if(modifier.isComplement(desc, nc)){
						complements.add(nc);
					}
				}
			}
		}	
		return complements;
	}
	
}
