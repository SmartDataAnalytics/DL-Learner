package org.dllearner.tools.protege;

import org.protege.editor.owl.OWLEditorKit;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.protege.editor.owl.ui.frame.*;
import org.protege.editor.owl.ui.framelist.OWLFrameList2;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.core.ComponentInitException;
import org.dllearner.algorithms.refexamples.ExampleBasedROLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.semanticweb.owl.model.*;



public class DLLearnerOptionTab extends AbstractOWLFrameSectionRowObjectEditor<OWLDescription> 
         {
	
  private JLabel pos;
  private JComponent learner;
  private JSplitPane split;
  private JButton accept;
  private JButton config;
  private OWLEditorKit editor;
  private JPanel option;
  private JScrollPane suggest;
  private JPanel vorschlag;
  private Object[] blub;
  private OWLDescription initialDescription;
  private JLabel neg;
  private String test;
  private ActionHandler action;
  private DLLearnerComponentListener listen;
  private DLLearnerConfig dlLearnerConfig;
  private JCheckBox hi;
  

  	//TODO: Layout selber festlegen denn die standartlayouts sind scheisse
  	//TODO: ActionListener implementieren
  	//TODO: Reasoner, learningproblems und algos direkt vom dl-learner beziehen
  	//TODO: instanz vom DLLearnerOptionTab wieder zuruecksetzen damit beim laden neue Ontologie erkannt wird 
    public DLLearnerOptionTab(OWLEditorKit editorKit, OWLDescription description) {
     
    	dlLearnerConfig = new DLLearnerConfig();
    	editor = editorKit;
        initialDescription = description;
        split = new JSplitPane(split.HORIZONTAL_SPLIT,false);
    	suggest = new JScrollPane();
    	vorschlag = new JPanel(new GridLayout(3,1));
    	learner = new JPanel();
    	option = new JPanel(new GridLayout(20,1));
    	init();
    	hi= new JCheckBox("Test", true);
    	//buildLearnerOption();
  }
    
    public void startDLLearner()
    {
    	new DLLearnerConfig().DLLearnerStart();

    	
    }
    public void init()
    {
    	action = new ActionHandler(this.action);
    	listen = new DLLearnerComponentListener();
    }
    
    public OWLDescription getEditedObject()
    {
    	String expression = "JUHU";
    	try {
    	return editor.getOWLModelManager().getOWLDescriptionParser().createOWLDescription(expression);
    	}
    	catch (OWLException e){
    		return null;
    	}
    	
    }
  
    private void buildLearnerOption()
    {
    	learner.setPreferredSize(new Dimension(500, 400));
    	split = new JSplitPane(split.HORIZONTAL_SPLIT,false);
    	split.setResizeWeight(0.5);
    	
    	pos = new JLabel("Positive Examples");
    	option.add(pos);
    	blub=editor.getOWLModelManager().getActiveOntology().getReferencedIndividuals().toArray();
    	for(int j = 0; j<editor.getOWLModelManager().getActiveOntology().getReferencedIndividuals().size();j++)
    	{
    		option.add(new JCheckBox(editor.getOWLModelManager().getActiveOntology().getURI().toString()+"#"+blub[j], true)).addComponentListener(action);
    	}
    	neg = new JLabel("Negative Examples");
    	option.add(neg);
    	for(int j = 0; j<editor.getOWLModelManager().getActiveOntology().getReferencedIndividuals().size();j++)
    	{
    		option.add(new JCheckBox(editor.getOWLModelManager().getActiveOntology().getURI().toString()+"#"+blub[j], true)).addComponentListener(action);
    	}
    	config = new JButton("Config");
    	config.addActionListener(this.action);
    	option.add(config);
    	option.add(hi);
    	accept = new JButton("RUN");
    	accept.addActionListener(this.action);
    	vorschlag.add(suggest);
    	vorschlag.add(accept);
    	split.setLeftComponent(option);
        split.setRightComponent(vorschlag);
    	learner.add(split);
    	System.out.println("");
    	//System.out.println("hi: "+hi.get);
    	startDLLearner();
    	
    }
    
public void actionPerformed(ActionEvent a)
{
	System.out.println(a.getSource());
}
    public JComponent getEditorComponent()
    {
    	buildLearnerOption();
    	return learner;
    }
   
   public void clear()
   {
	   if(split!=null)
	   {
	   split.removeAll();
	   option.removeAll();
	   vorschlag.removeAll();
	   }
   }
   
   public void dispose(){
   }
   
   public Set<OWLDescription> getEditedObjects()
   {
	   return super.getEditedObjects();
   }
    }