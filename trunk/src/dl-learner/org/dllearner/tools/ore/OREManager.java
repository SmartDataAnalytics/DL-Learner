package org.dllearner.tools.ore;

import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.tools.ore.cache.DLSyntaxRenderingCache;
import org.dllearner.tools.ore.cache.ManchesterSyntaxRenderingCache;
import org.dllearner.tools.ore.cache.OWLEntityRenderingCache;
import org.dllearner.tools.ore.ui.DescriptionLabel;
import org.dllearner.tools.ore.ui.editor.OWLEntityFinder;
import org.dllearner.tools.ore.ui.rendering.KeywordColorMap;
import org.dllearner.tools.ore.ui.rendering.OWLEntityRenderer;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;


public class OREManager {

	private static OREManager instance;

	private ComponentManager cm;

	private PelletReasoner reasoner;
	private KnowledgeSource ks;

	private String baseURI;
	private Map<String, String> prefixes;

	private EvaluatedDescriptionClass learnedClassDescription;
	
	
	private ManchesterSyntaxRenderingCache manchesterSyntaxRenderingCache;
	private DLSyntaxRenderingCache dlSyntaxRenderingCache;
	private OWLEntityRenderingCache owlEntityRenderingCache;
	private OWLEntityRenderer owlEntityRenderer;
	private OWLEntityFinder owlEntityFinder;
	private Map<String, Color> keywordColorMap;

	private List<OREManagerListener> listeners;

	private OntologyModifier modifier;
	
	private boolean consistentOntology;

	public OREManager(){
		cm = ComponentManager.getInstance();
		listeners = new ArrayList<OREManagerListener>();
		manchesterSyntaxRenderingCache = new ManchesterSyntaxRenderingCache(this);
		dlSyntaxRenderingCache = new DLSyntaxRenderingCache(this);
		owlEntityRenderingCache = new OWLEntityRenderingCache(this);
		owlEntityRenderer = new OWLEntityRenderer();
		keywordColorMap = new KeywordColorMap();
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
	
	public void initPelletReasoner() throws URISyntaxException, OWLOntologyCreationException{
		reasoner = cm.reasoner(PelletReasoner.class, ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		reasoner.loadOntologies();
		reasoner.addProgressMonitor(TaskManager.getInstance().getStatusBar());
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
		modifier = new OntologyModifier(reasoner);
		fireActiveOntologyChanged();
		consistentOntology = reasoner.isConsistent();
	}
	
	public boolean isSourceOWLAxiom(OWLAxiom ax){
		for(OWLOntology ont : reasoner.getLoadedOWLAPIOntologies()){System.out.println(ont.getLogicalAxiomCount());
			if(ont.containsAxiom(ax)){
				return true;
			}
		}
		return false;
	}
	
	public Set<OWLOntology> getLoadedOntologies(){
		return reasoner.getLoadedOWLAPIOntologies();
	}
	
	public OWLDataFactory getOWLDataFactory(){
		return reasoner.getOWLDataFactory();
	}
	
	public Set<OWLOntology> getOWLOntologiesForOWLAxiom(OWLAxiom ax){
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		for(OWLOntology ont : getLoadedOntologies()){
			if(ont.containsAxiom(ax)){
				ontologies.add(ont);
			}
		}
		return ontologies;
	}
	/**
	 * Save the ontology in OWL/XML format.
	 * @param file The file to save as.
	 * @throws OWLOntologyStorageException
	 * 
	 */
	public void saveOntology(File file) throws OWLOntologyStorageException{
		
		try {
			reasoner.getOWLOntologyManager().saveOntology(reasoner.getOWLAPIOntologies(), new RDFXMLOntologyFormat(), IRI.create(file));
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
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
		return consistentOntology;
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
	
	public String getManchesterSyntaxRendering(OWLObject object){
		if(object instanceof OWLEntity){
			String rendering = owlEntityRenderingCache.getRendering((OWLEntity) object);
            if(rendering != null) {
                return rendering;
            }
            else {
                return owlEntityRenderer.render((OWLEntity) object);
            }
		}
		return manchesterSyntaxRenderingCache.getRendering(object);
	}
	
	public String getManchesterSyntaxRendering(Description description){
		return manchesterSyntaxRenderingCache.getRendering(description);
	}
	
	public String getManchesterSyntaxRendering(ObjectProperty property){
		return manchesterSyntaxRenderingCache.getRendering(property);
	}
	
	public String getManchesterSyntaxRendering(Individual individual){
		return manchesterSyntaxRenderingCache.getRendering(individual);
	}
	
	public String getDLSyntaxRendering(OWLObject object){
		return dlSyntaxRenderingCache.getRendering(object);
	}
	
	public OWLEntityRenderer getOWLEntityRenderer(){
		return owlEntityRenderer;
	}
	
	public OWLEntityFinder getOWLEntityFinder(){
		if (owlEntityFinder == null){
			owlEntityFinder = new OWLEntityFinder(this, owlEntityRenderingCache);
        }
        return owlEntityFinder;
	}
	
	public Map<String, Color> getKeywordColorMap(){
		return keywordColorMap;
	}

	
	public SortedSet<Individual> getPositiveFailureExamples(){
		SortedSet<Individual> posNotCovered = reasoner.getIndividuals(LearningManager.getInstance().getCurrentClass2Describe());
		posNotCovered.removeAll(learnedClassDescription.getCoveredInstances());
		return posNotCovered;
	}
	
	public SortedSet<Individual> getNegativeFailureExamples(){
		return new TreeSet<Individual>(learnedClassDescription.getAdditionalInstances());
	}
	
	
		
	public void setNewClassDescription(EvaluatedDescriptionClass newClassDescription) {
		learnedClassDescription = newClassDescription;
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
		System.out.println();
	
		return allIndividuals;
	}
	
	public boolean isAssertable(ObjectPropertyExpression role, Individual ind){
		OWLDataFactory factory = reasoner.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(role.getName()));
		
		//get the objectproperty domains
		Set<OWLClass> domains = getReasoner().getReasoner().
		getObjectPropertyDomains(property, false).getFlattened();
		
		//get the classes where the individual belongs to
		Set<NamedClass> classes = reasoner.getTypes(ind);
		
		//get the complements of the classes, the individual belongs to
		Set<Description> complements = new HashSet<Description>();
		for(NamedClass nc : classes){
			complements.addAll(reasoner.getComplementClasses(nc));
		}
		
		for(OWLClassExpression domain : domains){
			if(complements.contains(OWLAPIConverter.convertClass(domain.asOWLClass()))){
				System.out.println(domain);
				return false;
			}
		}
		return true;
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
		moveClasses.remove(LearningManager.getInstance().getCurrentClass2Describe());
			
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
		moveClasses.remove(LearningManager.getInstance().getCurrentClass2Describe());
			
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
