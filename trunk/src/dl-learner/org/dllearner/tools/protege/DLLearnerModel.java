package org.dllearner.tools.protege;

import java.util.Set;
import java.util.TreeSet;
//import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;

import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.algorithms.SimpleSuggestionLearningAlgorithm;

import org.dllearner.core.owl.Description;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;

import org.dllearner.kb.OWLAPIOntology;

import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosNegDefinitionLP;

import org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor;
import org.dllearner.reasoning.OWLAPIReasoner;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLDescription;





/**2
 * This Class provides the necessary methods to learn Concepts from the DL-Learner.
 * @author Heero Yuy
 *
 */
public class DLLearnerModel implements Runnable{
	/**
	 * The Sting is for components that are available in the DL-Learner
	 */
	private String[] componenten={"org.dllearner.kb.OWLFile","org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.DIGReasoner","org.dllearner.reasoning.FastRetrievalReasoner","org.dllearner.learningproblems.PosNegInclusionLP"
			,"org.dllearner.learningproblems.PosNegDefinitionLP","org.dllearner.algorithms.RandomGuesser","org.dllearner.algorithms.BruteForceLearner","org.dllearner.algorithms.refinement.ROLearner","org.dllearner.algorithms.refexamples.ExampleBasedROLComponent","org.dllearner.algorithms.gp.GP"};	
	/**
	 * This Vector stores the check boxes for the view. 
	 */
	private Vector<JCheckBox> positiv;
	/**
	 * This Vector stores the negative Examples.
	 */
	private Vector<JCheckBox> negativ;
	/**
	 * 
	 */
	private ComponentManager cm;
	/**
	 * 
	 */
	private ReasoningService rs;
	/**
	 * 
	 */
	private KnowledgeSource source;
	/**
	 * 
	 */
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	/**
	 * This is the count of Concepts which you get after learning 
	 */
	private static final int anzahl = 6;
	/**
	 * 
	 */
	private Description[] description;
	/**
	 * 
	 */
	private LearningProblem lp;
	/**
	 * This boolean is 
	 */
	private boolean alreadyLearned=false;
	/**
	 * 
	 */
	private OWLOntology ontology;
	/**
	 * This is the learning algorithm 
	 */
	private LearningAlgorithm la = null;
	/**
	 * 
	 */
	private OWLEditorKit editor;
	/**
	 * 
	 */
	private OWLFrame<OWLClass> aktuell;
	/**
	 * 
	 */
	private OWLAPIReasoner reasoner;
	/**
	 * 
	 */
	private Set<OWLDescription> OWLDescription;
	/**
	 * This set stores the positive examples.
	 */
	private Set<String> positiveExamples;
	/**
	 * This set stores the negative examples that doesn't belong to the concept.
	 */
	private Set<String> negativeExamples;
	/**
	 * 
	 */
	private OWLDescription desc;
	/**
	 * 
	 */
	private String id;
	/**
	 * 
	 */
	private OWLDescription newConceptOWLAPI;
	/**
	 * 
	 */
	private OWLDescription oldConceptOWLAPI;
	/**
	 * 
	 */
	private Set<OWLDescription> ds;
	/**
	 * 
	 */
	private DefaultListModel suggestModel;
	/**
	 * 
	 */
	private Set<Individual> individual;
	/**
	 * 
	 */
	private SimpleSuggestionLearningAlgorithm test;
	/**
	 * 
	 */
	private String error;
	/**
	 * 
	 */
	private OWLAxiom axiomOWLAPI;
	private EvaluatedDescription evalDescription;
	/**
	 * This is the constructor for DL-Learner model
	 * @param editorKit
	 * @param h
	 * @param id String if it learns a subclass or a superclass.
	 * @param view current view of the DL-Learner tab
	 */
	public DLLearnerModel(OWLEditorKit editorKit, OWLFrame<OWLClass> h,String id,OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view)
	{
		editor=editorKit;
		aktuell=h;
		this.id=id;
		this.view=view;
		OWLDescription = new HashSet<OWLDescription>();
		positiv = new Vector<JCheckBox>();
		negativ = new Vector<JCheckBox>();
		test = new SimpleSuggestionLearningAlgorithm();
		ComponentManager.setComponentClasses(componenten);
		cm = ComponentManager.getInstance();
		ds = new HashSet<OWLDescription>();
		suggestModel = new DefaultListModel();
		
	}
	
	/**
	 * This method initializes the SimpleSuggestionLearningAlgorithm and adds the 
	 * suggestions to the suggest panel model.
	 */
	public void initReasoner()
	{
		alreadyLearned = false;
		setKnowledgeSource();
		setReasoner();
		SortedSet<Individual> pos=rs.getIndividuals();
		Set<Description> desc = test.getSimpleSuggestions(rs, pos);
		int i = 0;
		for(Iterator<Description> j = desc.iterator();j.hasNext();)
		{
			suggestModel.add(i,j.next());
		}
	}
	
	/**
	 * This method adds the solutions from the DL-Learner to the 
	 * model for the 
	 */
	private void addToListModel()
	{
		evalDescription = la.getCurrentlyBestEvaluatedDescription();
		for(int j = 0;j<la.getCurrentlyBestEvaluatedDescriptions(anzahl).size();j++)
		{
			suggestModel.add(j,la.getCurrentlyBestEvaluatedDescriptions(anzahl).get(j).getDescription());
		}
	}
	
	/**
	 * This method checks which positive and negative examples are checked 
	 * and puts the checked examples into a treeset.
	 */
	public void setPositiveAndNegativeExamples()
	{
		positiveExamples = new TreeSet<String>();
		negativeExamples = new TreeSet<String>();
		for(int i=0;i<positiv.size();i++)
		{
			if(positiv.get(i).isSelected())
			{
				positiveExamples.add(positiv.get(i).getText());
			}
			
			if(negativ.get(i).isSelected())
			{
				negativeExamples.add(negativ.get(i).getText());
			}
		}
	}
	
	/**
	 * This method returns the data for the suggest panel.
	 * @return Model for the suggest panel.
	 */
	public DefaultListModel getSuggestList()
	{
		return suggestModel;
	}
	
	/**
	 * This method returns an array of descriptions learned by the DL-Learner.
	 * @return Array of descriptions learned by the DL-Learner.
	 */
	public Description[] getDescriptions()
	{
		return description;
	}
	
	/**
	 * This method sets the knowledge source for the learning process.
	 * Only OWLAPIOntology will be available.
	 */
	public void setKnowledgeSource()
	{
		this.source = new OWLAPIOntology(editor.getOWLModelManager().getActiveOntology());
	}
	
	/**
	 * This method sets the reasoner and the reasoning service
	 * Only OWLAPIReasoner is available.
	 */
	public void setReasoner()
	{
		this.reasoner =cm.reasoner(OWLAPIReasoner.class,source);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rs = cm.reasoningService(reasoner);
	}
	
	/**
	 * This method sets the Learning problem for the learning process.
	 * PosNegDefinitonLp for equivalent classes and
	 * PosNegInclusionLP for superclasses.
	 */
	public void setLearningProblem()
	{
		if(id.equals("Equivalent classes"))
		{
			lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
		}
		if(id.equals("Superclasses"))
		{
			lp = cm.learningProblem(PosNegInclusionLP.class, rs);
		}
		
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		try{
		lp.init();
		}
		catch(ComponentInitException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method sets the learning algorithm for the learning process.
	 */
	public void setLearningAlgorithm()
	{
		try {
			this.la = cm.learningAlgorithm(ROLearner.class, lp, rs);
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cm.applyConfigEntry(la, "numberOfTrees", 100);
		cm.applyConfigEntry(la, "maxDepth", 5);
		try{
			la.init();
			}
			catch(ComponentInitException e){
				e.printStackTrace();
			}
			alreadyLearned = true;
	}

	/**
	 * This method starts the learning process.
	 */
	public void run()
	{	
		error = "Learning succesful";
		// start the algorithm and print the best concept found
		la.start();
		description = new Description[la.getCurrentlyBestEvaluatedDescriptions(anzahl).size()];
		addToListModel();
		view.renderErrorMessage(error);
		view.getRunButton().setEnabled(true);
		view.getCancelButton().setEnabled(false);
		view.getSuggestClassPanel().setSuggestList(suggestModel);
	}
	
	/**
	 * This method returns the Concepts from the DL-Learner. 
	 * @return Array of learned Concepts. 
	 */
	public Description[] getSolutions()
	{
		return description;
	}
	
	/**
	 * This method returns the check boxes for the positive examples.
	 * @return Vector of check boxes for positive examples
	 */
	public Vector<JCheckBox> getPosVector()
	{
		return positiv;
	}
	
	/**
	 * This method returns the check boxes for the negative examples.
	 * @return Vector of check boxes for negative examples
	 */
	public Vector<JCheckBox> getNegVector()
	{
		return negativ;
	}
	
	/**
	 * This method gets an error message and storess it.
	 * @param error error message
	 */
	public void setErrorMessage(String error)
	{
		this.error = error;
	}
	
	/**
	 * This method sets the check boxes for the positive check boxes checked 
	 * if the individuals matches the concept that is chosen in protege.
	 */
	public void setPosVector()
	{	
		setPositiveConcept();
		for(Iterator<Individual> j = rs.getIndividuals().iterator(); j.hasNext();)
		{
			String ind = j.next().toString();
			if(setPositivExamplesChecked(ind))
			{
				JCheckBox box = new JCheckBox(ind.toString(),true);
				box.setName("Positive");
				positiv.add(box);
				
			}
			else
			{
				JCheckBox box = new JCheckBox(ind.toString(),false);
				box.setName("Positive");
				positiv.add(box);
			}
			JCheckBox box = new JCheckBox(ind.toString(),false);
			box.setName("Negative");
			negativ.add(box);
		}
	}

	public EvaluatedDescription getEvaluatedDescription()
	{
		return evalDescription;
	}
	/**
	 * This method resets the Concepts that are learned.
	 */
	public void unsetNewConcepts()
	{
		while(OWLDescription.iterator().hasNext())
		{
			OWLDescription.remove(OWLDescription.iterator().next());
		}
	}
	
	/**
	 * This method sets the individuals that belong to the concept which is chosen in protege.
	 */
	public void setPositiveConcept()
	{
		SortedSet<Individual> individuals = null;
		if(!aktuell.getRootObject().toString().equals("Thing"))
		{
			for(Iterator<NamedClass> i = rs.getAtomicConcepts().iterator(); i.hasNext();)
			{
				if(individuals==null)
				{
					NamedClass concept = i.next();
					if(concept.toString().endsWith("#"+aktuell.getRootObject().toString()))
					{
						if(rs.retrieval(concept)!=null)
							{
							individual = rs.retrieval(concept);
							break;
							}
					}
				}
			}
		}
		else
		{
			individual = rs.getIndividuals();
		}
	}
	
	/**
	 * This method gets an Individual and checks if this individual belongs to the concept
	 * chosen in protege.
	 * @param indi Individual to check if it belongs to the chosen concept 
	 * @return is Individual belongs to the concept which is chosen in protege.
	 */
	public boolean setPositivExamplesChecked(String indi)
	{
			boolean isChecked = false;
			if(individual != null)
			{
				if(individual.toString().contains(indi))
				{
					isChecked = true;
				}
			}
    	return isChecked;
	
	}

	/**
	 * This method resets the vectors where the check boxes for positive and negative Examples
	 * are stored. It is called when the DL-Learner View is closed. 
	 */
	public void clearVector()
	{
		positiv.removeAllElements();
		negativ.removeAllElements();
	}
	
	/**
	 * This method gets an array of concepts from the DL-Learner and stores it
	 * in the description array.
	 * @param list Array of concepts from DL-Learner
	 */
	public void setDescriptionList(Description[] list)
	{
		description=list;
	}
	
	/**
	 * This method returns the current learning algorithm that is used to learn new concepts.
	 * @return Learning algorithm that is used for learning concepts.
	 */
	public LearningAlgorithm getLearningAlgorithm()
	{
		return la;
	}
	
	/**
	 * This method gets an integer to return the positive examples check box on that position.
	 * @param i integer for the position in the vector
	 * @return Positive examples check box on position i.
	 */
	public JCheckBox getPositivJCheckBox(int i)
	{
		return positiv.get(i);
	}
	
	/**
	 * This method gets an integer to return the negative examples check box on that position.
	 * @param i integer for the position in the vector
	 * @return Negative examples check box on position i.
	 */
	public JCheckBox getNegativJCheckBox(int i)
	{
		return negativ.get(i);
	}
	
	/**
	 * This method resets the array of concepts from the DL_Learner.
	 * It is called after the DL-Learner tab is closed.
	 */
	public void resetSuggestionList()
	{
		for(int i=0;i<description.length;i++)
		{
			description[i]=null;
		}
	}
	
	/**
	 * This method unchecks the checkboxes that are checked after the process
	 * of learning.
	 */
	public void unsetJCheckBoxen()
	{
		for(int j=0;j<positiv.size();j++)
		   {
			   if(positiv.get(j).isSelected())
			   {
				   JCheckBox i = positiv.get(j);
				   i.setSelected(false);
				   positiv.set(j, i); 
			   }
			   if(negativ.get(j).isSelected())
			   {
				   JCheckBox i = negativ.get(j);
				   i.setSelected(false);
				   negativ.set(j, i);
			   }
		   }
	}

	/**
	 * This method resets the model for the suggest panel.
	 * It is called befor the DL-Learner learns the second time or when the 
	 * DL-Learner tab is closed.
	 */
	public void unsetListModel()
	{
		if(suggestModel!=null)
		{
			suggestModel.removeAllElements();
		}
	}

	/**
	 * This method gets a description from the DL-Learner and adds is to the model from the suggest panel. 
	 * @param desc Description from the DL-Learner
	 */
	public void setSuggestModel(Description desc)
	{
		suggestModel.add(0, desc);
	}
	
	/**
	 * This method returns the current OWLOntology that is loaded in protege.
	 * @return current ontology
	 */
	public OWLOntology getOWLOntology()
	{
		return ontology;
	}
	
	/**
	 * This method returns a set of concepts that are learned by the DL-Learner.
	 * They are already converted into the OWLDescription format. 
	 * @return Set of learned concepts in OWLDescription format
	 */
	public Set<OWLDescription> getNewOWLDescription()
	{
		return OWLDescription;
	}
	
	/**
	 * This method returns the old concept which is chosen in protege in OWLDescription format. 
	 * @return Old Concept in OWLDescription format.
	 */
	public OWLDescription getOldConceptOWLAPI()
	{
		return oldConceptOWLAPI;
	}
	
	/**
	 * This method returns the currently learned description in OWLDescription format.
	 * @return currently used description in OWLDescription format
	 */
	public OWLDescription getSolution()
	{
		return desc;
	}
	
	/**
	 * Thsi method gets a description learned by the DL-Learner an converts it
	 * to the OWLDescription format.
	 * @param desc Description learned by the DL-Learner
	 */
	public void setNewConceptOWLAPI(Description desc)
	{
		newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		ds.add(newConceptOWLAPI);
		OWLDescription.add(newConceptOWLAPI);
		this.desc = newConceptOWLAPI;
	}
	
	/**
	 * This method gets the old concept from checking the positive examples.
	 */
	public void setOldConceptOWLAPI()
	{
		SortedSet<Individual> indi=rs.getIndividuals();
		for(Iterator<Individual> i = indi.iterator(); i.hasNext();)
		{
			Individual indi2 = i.next();
			if(positiveExamples.toString().contains(indi2.toString()))
			{
				Set<NamedClass> concept=reasoner.getConcepts(indi2);
				for(Iterator<NamedClass> k = concept.iterator();k.hasNext();)
				{
					OWLDescription oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(k.next());
					ds.add(oldConceptOWLAPI);
				}
					
			}
		}
	}
	
	/**
	 * This method stores the new concept learned by the DL-Learner in the Ontology.
	 * @param desc Description learne by the DL-Learner
	 */
	public void changeDLLearnerDescriptionsToOWLDescriptions(Description desc)
	{
		setNewConceptOWLAPI(desc);
		setOldConceptOWLAPI();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLDataFactory factory = manager.getOWLDataFactory();

		axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);

		OWLOntology ontology = editor.getOWLModelManager().getActiveOntology();
		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		}
	
	/**
	 * This method returns the currently used reasoning service.
	 * @return current reasoning service
	 */
	public ReasoningService getReasoningService()
	{
		return rs;
	}
	
	/**
	 * This method gets the status if the DL-Learner has already learned.
	 *  It is only for reseting the suggest panel.
	 * @return boolean if the learner has already learned
	 */
	public boolean getAlreadyLearned()
	{
		return alreadyLearned;
	}	
	
}