package org.dllearner.tools.protege;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.*;

import org.semanticweb.owl.model.OWLClass;
import org.dllearner.core.owl.Description;
import org.protege.editor.owl.ui.frame.OWLFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JSplitPane;


import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRowObjectEditor;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;


public class SuggestEquivalentClassView extends AbstractOWLFrameSectionRowObjectEditor<OWLDescription> implements Observer{
	  
	  private JLabel pos;
	  private Vector<JCheckBox> positive = new Vector<JCheckBox>();
	  private Vector<JCheckBox> negative = new Vector<JCheckBox>();
	  private JComponent learner;
	  private JSplitPane split;
	  private JButton accept;
	  private JButton run;
	  private OWLEditorKit editor;
	  private JPanel option;
	  private JPanel listPanel;
	  private JScrollPane test;
	  private JList suggest;
	  private JPanel vorschlag;
	  private Object[] blub;
	  private JLabel neg;
	  private ActionHandler action;
	  private DLLearnerModel model;
	  private OWLFrame<OWLClass> aktuell;
	  private JPanel panel;

	  public void update(Observable m,Object c)
	  {
		  if( model != m) return;
		   draw(); 
	  }
	  	//TODO: Layout selber festlegen denn die standartlayouts sind scheisse
	  	//TODO: MVC Achitektur erstellen
	  	//TODO: herrausfinden wie das mit dem scrollen geht
	    public SuggestEquivalentClassView(OWLEditorKit editorKit, OWLDescription description, OWLFrame<OWLClass> h) {
	     
	    	editor = editorKit;
	    	aktuell = h;
	        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,false);
		    model = new DLLearnerModel();
		    model.addObserver( this);
	    	vorschlag = new JPanel();
	    	panel = new JPanel(new GridLayout(0,1));
	    	panel.setPreferredSize(new Dimension(290,490));
	    	suggest = new JList();
	    	//positiv.setPreferredSize(new Dimension(190,200));
	    	//negativ.setPreferredSize(new Dimension(190,200));
	    	learner = new JPanel();
	    	listPanel = new JPanel();
	    	learner.setPreferredSize(new Dimension(600, 500));
	    	split.setResizeWeight(0.5);
	    	pos = new JLabel("Positive Examples");
	    	neg = new JLabel("Negative Examples");
	    	run = new JButton("RUN");
	    	accept = new JButton("ADD");
	    	//accept.setSize(190, 20);
	    	accept.setPreferredSize(new Dimension(290,50));
	    	action = new ActionHandler(this.action, model);
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
	    	test= new JScrollPane();
	    	option = new JPanel(new GridLayout(0,1));
	    	option.setPreferredSize(new Dimension(290,0));
	    	option.add(pos);
	    	blub=editor.getOWLModelManager().getActiveOntology().getReferencedIndividuals().toArray();
	    	for(int j = 0; j<blub.length;j++)
	    	{
	    		positive.add(new JCheckBox(editor.getOWLModelManager().getActiveOntology().getURI().toString()+"#"+blub[j].toString()));
	    		
	    	}
	    	for(int j=0; j<positive.size();j++)
	    	{
	    		option.add(positive.get(j));
	    	}
	    	option.add(neg);
	    	for(int j = 0; j<blub.length;j++)
	    	{
	    		negative.add(new JCheckBox(editor.getOWLModelManager().getActiveOntology().getURI().toString()+"#"+blub[j].toString()));
	    	}
	    	for(int i=0;i<negative.size();i++)
	    	{
	    	option.add(negative.get(i));
	    	}
	    	//individuals.add(negative);
	    	option.add(run);
	    	panel.add(suggest);
	    	panel.add(accept);
	    	test.add(option);
	    	split.setLeftComponent(option);
	        split.setRightComponent(panel);
	    	learner.add(split);
	    	System.out.println(aktuell.getRootObject());
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
		   if(split!=null)
		   {
		   split.removeAll();
		   panel.removeAll();
		   if(option!=null)
		   {
		   option.removeAll();
		   }
		   suggest.removeAll();
		   vorschlag.removeAll();
		   positive.removeAllElements();
		   negative.removeAllElements();
		   }
	   }
	   /**
	    * Methode die alle Buttons und CheckBoxes an dem ActionListener anmeldet
	    */
	   private void addListener()
	   {
		   run.addActionListener(this.action);
		   accept.addActionListener(this.action);
		   
		   for(int i=0;i<positive.size();i++)
		   {
			   positive.get(i).addItemListener(action);
		   }
		   
		   for(int i=0;i<negative.size();i++)
		   {
			   negative.get(i).addItemListener(action);
		   } 
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
		
		public void setSuggestionList(java.util.List<Description> list)
		{
			System.out.println(list.isEmpty());
			if(list.isEmpty())
			{
				listPanel.add(new JLabel("No Suggestions"));
			}
			else
			{
				for(int i = 0; i<list.size();i++)
				{
					listPanel.add(new JLabel(list.get(i).toString()));
				}
			}
		}

	   
	   public Set<OWLDescription> getEditedObjects()
	   {
		   return super.getEditedObjects();
	   }
	   private void resetPanel()
	   {
		   option.removeAll();
		   positive.removeAllElements();
		   negative.removeAllElements();
		   panel.removeAll();
	   }
	   public void release()
	   {
	     model.deleteObserver( this);
	     model = null;
	   }
	   
	   protected void draw() {
			if (model != null) {
				String desc[] = ((DLLearnerModel)model).getSolutions();

					suggest = new JList(desc);
					System.out.println("Hallo Welt");
					resetPanel();
					makeView();
			}
		}
	}
