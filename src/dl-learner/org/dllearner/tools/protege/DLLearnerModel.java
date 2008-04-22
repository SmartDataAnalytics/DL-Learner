package org.dllearner.tools.protege;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.OWLFile;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.io.*;
import org.dllearner.core.owl.Description;
import javax.swing.JCheckBox;
import org.dllearner.algorithms.refinement.*;
//import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.dllearner.core.*;
import org.dllearner.reasoning.*;
import org.dllearner.learningproblems.*;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

public class DLLearnerModel extends Observable implements Runnable{
	private String[] componenten={"org.dllearner.kb.OWLFile","org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.DIGReasoner","org.dllearner.reasoning.FastRetrievalReasoner","org.dllearner.learningproblems.PosNegInclusionLP"
			,"org.dllearner.learningproblems.PosNegDefinitionLP","org.dllearner.algorithms.RandomGuesser","org.dllearner.algorithms.BruteForceLearner","org.dllearner.algorithms.refinement.ROLearner","org.dllearner.algorithms.refexamples.ExampleBasedROLComponent","org.dllearner.algorithms.gp.GP"};	
	private String uri;
	private Vector<JCheckBox> positiv;
	private Vector<JCheckBox> negativ;
	private ComponentManager cm;
	//private ReasonerComponent reasoner;
	private ReasoningService rs;
	private static final int anzahl = 10;
	private Description[] description = new Description[anzahl];
	private LearningProblem lp;
	private LearningAlgorithm la = null;
	OWLAPIReasoner reasoner;
	

	public DLLearnerModel()

	{
		positiv = new Vector<JCheckBox>();
		negativ = new Vector<JCheckBox>();

	}
	/**
	 * String um die Componenten des DL-Learners anzumelden
	 */
	public void setDLLearnerModel(Vector<JCheckBox> pos, Vector<JCheckBox> neg, String s)
	{
		positiv=pos;
		negativ=neg;
		uri=s;
	}

	
	public void startPosNegDefinitionReasoning()
	{
		
	}
	public void run()
	{
		resetSuggestionList();
		ComponentManager.setComponentClasses(componenten);
		// get singleton instance of component manager
		cm = ComponentManager.getInstance();
		
		// create knowledge source
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(source, "url", new File(uri).toURI().toString());
		try{
		source.init();
		}
		catch(Exception e){
		}
		// create DIG reasoning service with standard settings
		reasoner = cm.reasoner(OWLAPIReasoner.class, source);
		// ReasoningService rs = cm.reasoningService(DIGReasonerNew.class, source);
		try{
		reasoner.init();
		}
		catch(Exception e){
			
		}
		rs = cm.reasoningService(reasoner);
		lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
		// create a learning problem and set positive and negative examples

		Set<String> positiveExamples = new TreeSet<String>();
		for(int i=0;i<positiv.size();i++)
		{
			if(positiv.get(i).isSelected())
			{
				positiveExamples.add(positiv.get(i).getText());
			}
		}
		Set<String> negativeExamples = new TreeSet<String>();
		for(int i=0;i<negativ.size();i++)
		{
			if(negativ.get(i).isSelected())
			{
				negativeExamples.add(negativ.get(i).getText());
			}
		}
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		try{
		lp.init();
		}
		catch(Exception e){
			
		}
		
		// create the learning algorithm
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
			catch(Exception e){
				
			}
		
		// start the algorithm and print the best concept found
		la.start();
		description = new Description[la.getBestSolutions(anzahl).size()];
		for(int j = 0;j<la.getBestSolutions(anzahl).size();j++)
		{
			description[j]=la.getBestSolutions(anzahl).get(j);
		}
		setChanged();
		notifyObservers(description);

	}
	
	public Description[] getSolutions()
	{
		return description;
	}
	public Vector<JCheckBox> getPosVector()
	{
		return positiv;
	}
	
	public Vector<JCheckBox> getNegVector()
	{
		return negativ;
	}
	
	public void setPosVector(Vector<JCheckBox> a)
	{
		positiv =a;
	}
	
	public void setNegVector(Vector<JCheckBox> b)
	{
		negativ = b;
	}
	
	public void addToPosVector(JCheckBox a)
	{
		positiv.add(a);
	}
	
	public void addToNegVector(JCheckBox b)
	{
		negativ.add(b);
	}
	
	public void clearVector()
	{
		positiv.removeAllElements();
		negativ.removeAllElements();
	}
	
	public String getUri()
	{
		return uri;
	}
	
	public void setDescriptionList(Description[] list)
	{
		description=list;
	}
	
	
	public LearningAlgorithm getLearningAlgorithm()
	{
		return la;
	}
	
	
	public void resetSuggestionList()
	{
		/*for(int i=0;i<description.length;i++)
		{
			description[i]="";
		}*/
	}
	
	public void changeDLLearnerDescriptionsToOWLDescriptions(Description desc)
	{
		OWLDescription newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		System.out.println(newConceptOWLAPI);
		//OWLDescription oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(concept);
	}
	
	public void addAxiomToOWL(Description desc,Description concept){
		OWLDescription newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		OWLDescription oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(concept);
		
		OWLOntology ontology = reasoner.getOWLAPIOntologies().get(0);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		Set<OWLDescription> ds = new HashSet<OWLDescription>();
		ds.add(newConceptOWLAPI);
		ds.add(oldConceptOWLAPI);
		
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		
		

		AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			manager.saveOntology(ontology);
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
