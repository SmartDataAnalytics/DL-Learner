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
import javax.swing.JScrollPane;
import org.dllearner.core.owl.Description;
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
	  private JScrollPane scrollPane;
	  private JScrollPane suggestScroll;
	  private final static Color Color_RED = Color.red;
	  private JButton cancel;
	  private JLabel errorMessage;
	  private ActionHandler action;
	  private DLLearnerModel model;
	  private Description[] descriptions = new Description[10];
	  private OWLFrame<OWLClass> aktuell;
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
	    	scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
	    	suggestScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    	aktuell = h;
		    model = new DLLearnerModel();
	    	errorMessage = new JLabel();
	    	System.out.println("test: "+aktuell.getRootObject());
	    	errorMessage.setForeground(Color_RED);
	    	suggest = new JList();
	    	suggest.setEnabled(true);
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
	  
	    public JComponent makeView()
	    {
	    	suggest = new JList();
	    	
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
	    	for(int j = 0; j<positive.size();j++)
	    	{
	    		Object[] test=editor.getOWLModelManager().getActiveOntology().getClassAssertionAxioms(aktuell.getRootObject()).toArray();
	    		for(int i = 0;i<test.length;i++)
	    		{
	    			String k = test[i].toString();
	    			String l = instances[j].toString();
	    			if(k.contains(l)&&positive.get(j).getText().contains(l))
	    			{
	    				JCheckBox n=positive.get(j);
	    				n.setSelected(true);
	    				positive.set(j, n);
	    			}
	    		}
	    		
	    		
	    		
	    	}
	    	scrollPane.setViewportView(option);
	    	scrollPane.setBounds(0, 0, 490, 250);
	    	run.setBounds(0,260,200,30);
	        cancel.setBounds(210,260,200,30);
	        suggestScroll.setBounds(0,300,490,110);
	        suggestScroll.setViewportView(suggest);
	        suggestScroll.setVisible(true);
	        //suggest.setBounds(0,300,490,110);
	        accept.setBounds(0,420,200,30);
	        errorMessage.setBounds(210,420,300,30);
	        System.out.println("blub2");
	        learner.add(scrollPane);
	    	learner.add(run);
	    	learner.add(cancel);
	    	learner.add(suggest);
	    	learner.add(accept);
	    	learner.add(errorMessage);
	    	addListener();
	    	return learner;
	    }

	    public JComponent getEditorComponent()
	    {
	    	System.out.println("das ist ein test");
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
		   model.resetSuggestionList();
		   learner.removeAll();
		   positive.removeAllElements();
		   negative.removeAllElements();
		   errorMessage.setText("");
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
				System.out.println(((DLLearnerModel)model).getSolutions().length);
				descriptions = ((DLLearnerModel)model).getSolutions();
				suggest=new JList(descriptions);
				//learner.remove(3);
				suggest.setBounds(0,300,490,110);
				learner.add(suggest);
				unsetJCheckBoxen();
				suggest.addMouseListener(action);
				
				returnLearner();
				
			}
		}
	   
	   public JComponent returnLearner()
	   {
		   return learner;
	   }
	   
	   public void renderErrorMessage(String s)
	   {
		   errorMessage.setText(s);
	   }
	   private void unsetJCheckBoxen()
	   {
		   for(int j=0;j<positive.size();j++)
		   {
			   if(positive.get(j).isSelected())
			   {
				   JCheckBox i = positive.get(j);
				   i.setSelected(false);
				   positive.set(j, i); 
			   }
		   }
		   for(int j=0;j<negative.size();j++)
		   {
			   if(negative.get(j).isSelected())
			   {
				   JCheckBox i = negative.get(j);
				   i.setSelected(false);
				   negative.set(j, i);
			   }
		   }
	   }
	   public JButton getStartButton()
	   {
		   return run;
	   }
	   
	   public JButton getStopButton()
	   {
		   return cancel;
	   }
	   
	   public Vector<JCheckBox> getPositiveVector()
	   {
		   return positive;
	   }
	   
	   public Vector<JCheckBox> getNegativeVector()
	   {
		   
		   return negative;
	   }
	   
	   public JList getSuggestionList()
	   {
		   return suggest;
	   }
	   
	   
	   
	   
	}
