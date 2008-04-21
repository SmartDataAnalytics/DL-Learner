package org.dllearner.tools.protege;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.*;

import org.semanticweb.owl.model.OWLClass;
import org.protege.editor.owl.ui.frame.OWLFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JList;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRowObjectEditor;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;


public class SuggestEquivalentClassView extends AbstractOWLFrameSectionRowObjectEditor<OWLDescription> implements Observer{
	  
	  private JLabel pos;
	  private Vector<JCheckBox> positive = new Vector<JCheckBox>();
	  private Vector<JCheckBox> negative = new Vector<JCheckBox>();
	  private JComponent learner;
	  private JButton accept;
	  private JButton run;
	  private OWLEditorKit editor;
	  private JPanel option;
	  private JList suggest;
	  private Object[] instances;
	  private JLabel neg;
	  private final static Color Color_RED = Color.red;
	  private JButton cancel;
	  private JLabel errorMessage;
	  private ActionHandler action;
	  private DLLearnerModel model;
	  private String[] descriptions = new String[10];
	  //private OWLFrame<OWLClass> aktuell;
	  private SuggestEquivalentClassView view;
	  
	  public void update(Observable m,Object c)
	  {
		  if( model != m) return;
		   draw(); 
	  }
	  
	  	//TODO: MVC Achitektur erstellen
	  	//TODO: herrausfinden wie das mit dem scrollen geht
	    public SuggestEquivalentClassView(OWLEditorKit editorKit, OWLDescription description, OWLFrame<OWLClass> h) {
	     
	    	editor = editorKit;
	    	//aktuell = h;
		    model = new DLLearnerModel();
	    	errorMessage = new JLabel();
	    	errorMessage.setForeground(Color_RED);
	    	suggest = new JList(descriptions);
	    	learner = new JPanel();
	    	learner.setLayout(null);
	    	learner.setPreferredSize(new Dimension(600, 480));
	    	pos = new JLabel("Positive Examples");
	    	neg = new JLabel("Negative Examples");
	    	run = new JButton("RUN");
	    	cancel = new JButton("Cancel");
	    	accept = new JButton("ADD");
	    	accept.setPreferredSize(new Dimension(290,50));
	    	option = new JPanel(new GridLayout(0,2));
	    	cancel.setEnabled(false);
	    	option.setPreferredSize(new Dimension(290,0));
	    	model.addObserver(this);
	    	
	  }
	    public void setView(SuggestEquivalentClassView v)
	    {
	    	view = v;
	    	action = new ActionHandler(this.action, model,view);
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
	  
	    public void makeView()
	    {
	    	suggest = new JList(descriptions);
	    	option.add(pos);
	    	option.add(neg);
	    	instances=editor.getOWLModelManager().getActiveOntology().getReferencedIndividuals().toArray();
	    	for(int j = 0; j<instances.length;j++)
	    	{
	    		positive.add(new JCheckBox(editor.getOWLModelManager().getActiveOntology().getURI().toString()+"#"+instances[j].toString()));
	    		
	    	}
	    	for(int j = 0; j<instances.length;j++)
	    	{
	    		negative.add(new JCheckBox(editor.getOWLModelManager().getActiveOntology().getURI().toString()+"#"+instances[j].toString()));
	    	}
	    	for(int j=0; j<positive.size();j++)
	    	{
	    		option.add(positive.get(j));
	    		option.add(negative.get(j));
	    	}
	    	//test.add(option);
	        option.setBounds(0, 0, 490, 250);
	        run.setBounds(0,260,200,30);
	        cancel.setBounds(210,260,200,30);
	        suggest.setBounds(0,300,490,110);
	        accept.setBounds(0,420,200,30);
	        errorMessage.setBounds(210,420,300,30);
	        System.out.println("blub2");
	    	learner.add(option);
	    	learner.add(run);
	    	learner.add(cancel);
	    	learner.add(suggest);
	    	learner.add(accept);
	    	learner.add(errorMessage);
	    	addListener();
	    	model.setDLLearnerModel(positive,negative,getUri());
	    }

	    public JComponent getEditorComponent()
	    {
	    	makeView();
	    	return learner;
	    }
	   /**
	    * Methode die den View wieder leert nachdem er nicht mehr gebraucht wird
	    */
	   public void clear()
	   {

		   if(option!=null)
		   {
		   option.removeAll();
		   }
		   suggest.removeAll();
		   positive.removeAllElements();
		   negative.removeAllElements();
		   errorMessage.setText("");
		   for(int i=0; i<descriptions.length;i++)
		   {
		   descriptions[i]="";
		   }
	   }
	   /**
	    * Methode die alle Buttons und CheckBoxes an dem ActionListener anmeldet
	    */
	   private void addListener()
	   {
		   run.addActionListener(this.action);
		   accept.addActionListener(this.action);
		   cancel.addActionListener(this.action);
		   for(int i=0;i<positive.size();i++)
		   {
			   positive.get(i).addItemListener(action);
		   }
		   
		   for(int i=0;i<negative.size();i++)
		   {
			   negative.get(i).addItemListener(action);
		   } 
	   }
	   public void destroyListener()
	   {
		   run.removeActionListener(this.action);
		   accept.removeActionListener(this.action);
		   System.out.println("hihihihi");
		   cancel.removeActionListener(this.action);
		   for(int i=0;i<positive.size();i++)
		   {
			   positive.get(i).removeItemListener(action);
		   }
		   
		   for(int i=0;i<negative.size();i++)
		   {
			   negative.get(i).removeItemListener(action);
		   } 
	   }
	 public void setSuggestedClass()
	 {	
		 //TODO: Description umwandeln und in ontologie einfuegen
		 //editor.getOWLModelManager().getActiveOntology().getClassAxioms().add(e);
	 }
	   public void dispose(){
	   }
		public String getUri()
	    {
	    	char[] test = editor.getOWLModelManager().getOntologyPhysicalURI(editor.getOWLModelManager().getActiveOntology()).toString().toCharArray();
	    	String uri=""; 
	    	for(int i =6; i<test.length;i++)
	    	{
	    		uri=uri+test[i];
	    	}
	    	return uri;
	    }
		


		@Override
	   public Set<OWLDescription> getEditedObjects()
	   {
		   return super.getEditedObjects();
	   }
	   private void resetPanel()
	   {
		   option.removeAll();
		   System.out.println("blub1");
		   positive.removeAllElements();
		   negative.removeAllElements();
		   learner.removeAll();
	   }
	   
	   public void release()
	   {
	     model.deleteObserver( this);
	     model = null;
	   }
	   
	   protected void draw() {
			if (model != null) {
				run.setEnabled(true);
				cancel.setEnabled(false);
				System.out.println("blub");
				descriptions = ((DLLearnerModel)model).getSolutions();
					resetPanel();
					makeView();
			}
		}
	   
	   public void disableRunButtons()
	   {
		   run.setEnabled(false);
		   cancel.setEnabled(true);
		   resetPanel();
		   makeView();
	   }
	   
	   public void renderErrorMessage(String s)
	   {
		   errorMessage.setText(s);
	   }
	   
	   public JButton getStartButton()
	   {
		   return run;
	   }
	   
	   public JButton getStopButton()
	   {
		   return cancel;
	   }
	}
