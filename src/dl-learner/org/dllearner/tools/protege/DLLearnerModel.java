package org.dllearner.tools.protege;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.OWLFile;

import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.io.*;
import javax.swing.JCheckBox;
import org.dllearner.algorithms.refinement.*;
import org.dllearner.core.*;
import org.dllearner.reasoning.*;
import org.dllearner.learningproblems.*;

public class DLLearnerModel extends Observable{
	private String[] componenten={"org.dllearner.kb.OWLFile","org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.DIGReasoner","org.dllearner.reasoning.FastRetrievalReasoner","org.dllearner.learningproblems.PosNegInclusionLP"
			,"org.dllearner.learningproblems.PosNegDefinitionLP","org.dllearner.algorithms.RandomGuesser","org.dllearner.algorithms.BruteForceLearner","org.dllearner.algorithms.refinement.ROLearner","org.dllearner.algorithms.refexamples.ExampleBasedROLComponent","org.dllearner.algorithms.gp.GP"};	
	private String uri;
	private Vector<JCheckBox> positiv;
	private Vector<JCheckBox> negativ;
	private ComponentManager cm;
	private ReasonerComponent reasoner;
	private ReasoningService rs;
	private static final int anzahl = 10;
	private String[] description = new String[anzahl];
	
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
	public void configDLLearner()
	{
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
	}
	
	public void startPosNegDefinitionReasoning()
	{
		
	}
	public void DLLearnerStart()
	{
		// create a learning problem and set positive and negative examples
		LearningProblem lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
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
		LearningAlgorithm la = null;
		try {
			la = cm.learningAlgorithm(ROLearner.class, lp, rs);
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
		description[0]=la.getBestSolution().toString();
		setChanged();
		notifyObservers(description);
	}
	
	public String[] getSolutions()
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
	
	public void setDescriptionList(String[] list)
	{
		description=list;
	}
	
	public String[] getSollutions()
	{
		return description;
	}
	

}
