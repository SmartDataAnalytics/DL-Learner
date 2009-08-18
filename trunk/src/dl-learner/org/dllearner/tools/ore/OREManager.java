package org.dllearner.tools.ore;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
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
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.tools.ore.ui.DescriptionLabel;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;

import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

public class OREManager {

	
	private static OREManager instance;
	
	private ComponentManager cm;
	
	private PelletReasoner pelletReasoner;
	private ClassLearningProblem lp;
	private CELOE la;
	private OWLFile ks;
	
	private String baseURI;
	private Map<String, String> prefixes;
	
	private NamedClass currentClass2Learn;
	private EvaluatedDescriptionClass learnedClassDescription;
	
	private double noisePercentage;
	private int maxExecutionTimeInSeconds = 10;
	private int maxNrOfResults = 10;
	
	


	private OntologyModifier modifier;
	
	private Thread currentClassificationThread;



	public OREManager(){
		cm = ComponentManager.getInstance();
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
			ks.getConfigurator().setUrl(uri.toURL());
			ks.init();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ComponentInitException e) {
			System.out.println("Could not init knowledge source");
			e.printStackTrace();
		}
		
	}
	
	public void setLearningProblem(){
		
		lp = cm.learningProblem(ClassLearningProblem.class, pelletReasoner);
		
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
			la = cm.learningAlgorithm(CELOE.class, lp, pelletReasoner);
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
		pelletReasoner = cm.reasoner(PelletReasoner.class, ks);
		try {
			pelletReasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pelletReasoner.loadOntologies();
		baseURI = pelletReasoner.getBaseURI();
		prefixes = pelletReasoner.getPrefixes();
		modifier = new OntologyModifier(pelletReasoner);
	}
	
	public void loadOntology(){
		pelletReasoner.loadOntologies();
	}
	
	public void classifyAsynchronously(){
		currentClassificationThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				pelletReasoner.classify();
				
			}
		});
	}
	
	public void killCurrentClassificationThread(){
		currentClassificationThread.stop();
        
	}
	
	
	
	public boolean consistentOntology() throws InconsistentOntologyException{
		return pelletReasoner.isConsistent();
	}
	
	public PelletReasoner getPelletReasoner(){
		return pelletReasoner;
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
	
	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
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

	
	public LearningAlgorithm getLa() {
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
			if(pelletReasoner.hasType(desc, ind)){
				
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
							if(pelletReasoner.hasType(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelNeg(ind, desc.getChild(i)));
							} else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("or"));
						}
						if(pelletReasoner.hasType(desc.getChild(children.size()-1), ind)){
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
			if(!pelletReasoner.hasType(desc, ind)){
				
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
							if(!pelletReasoner.hasType(desc.getChild(i), ind)){
								criticals.addAll(descriptionToJLabelPos(ind, desc.getChild(i)));
							} else{
								criticals.add(new JLabel(desc.getChild(i).toManchesterSyntaxString(baseURI, prefixes)));
							}
							criticals.add(new JLabel("and"));
						}
						if(!pelletReasoner.hasType(desc.getChild(children.size()-1), ind)){
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
				if(!pelletReasoner.hasType(objRestr.getChild(0), i)){
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
		for(NamedClass nc : pelletReasoner.getNamedClasses()){
			if(pelletReasoner.hasType(nc, ind)){
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
	
	
}
