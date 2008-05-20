package org.dllearner.tools.protege;

import org.dllearner.core.owl.Description;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLDescriptionChecker;
import org.protege.editor.owl.ui.selector.OWLClassSelectorPanel;
import org.protege.editor.owl.ui.selector.OWLObjectPropertySelectorPanel;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.protege.editor.owl.ui.frame.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.*;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 15-Feb-2007<br><br>
 */
public class OWLClassDescriptionEditorWithDLLearnerTab extends AbstractOWLFrameSectionRowObjectEditor<OWLDescription>
        implements VerifiedInputEditor {

    private static final String CLASS_EXPRESSION_EDITOR_LABEL = "Class expression editor";
    private static final String CLASS_TREE_LABEL = "Class tree";
    private static final String RESTRICTION_CREATOR_LABEL = "Restriction creator";
    private static final String SUGGEST_EQUIVALENT_CLASS_LABEL = "Suggest a equivalent class";
    private static final String SUGGEST_SUBCLASS_LABEL = "Suggest a subclass";

    private OWLEditorKit editorKit;

    private OWLDescriptionChecker checker;

    private ExpressionEditor<OWLDescription> editor;

    private JComponent editingComponent;

    private JTabbedPane tabbedPane;
    
    private DLLearnerView dllearner;
    
    private OWLClassSelectorPanel classSelectorPanel;

    private ObjectRestrictionCreatorPanel restrictionCreatorPanel;

    private OWLDescription initialDescription;

    private Set<InputVerificationStatusChangedListener> listeners = new HashSet<InputVerificationStatusChangedListener>();

    private DocumentListener editorListener = new DocumentListener(){
        public void insertUpdate(DocumentEvent documentEvent) {
            handleVerifyEditorContents();
        }
        public void removeUpdate(DocumentEvent documentEvent) {
            handleVerifyEditorContents();
        }
        public void changedUpdate(DocumentEvent documentEvent) {
            handleVerifyEditorContents();
        }
    };
    
    private ChangeListener changeListener = new ChangeListener(){
        public void stateChanged(ChangeEvent changeEvent) {
            handleVerifyEditorContents();
        }
    };

    public OWLDescription getInitialDescription()
    {
    	return initialDescription;
    }
    public OWLClassDescriptionEditorWithDLLearnerTab(OWLEditorKit editorKit, OWLDescription description,OWLFrame<OWLClass> frame, String label) {
        this.editorKit = editorKit;
        this.initialDescription = description;
        checker = new OWLDescriptionChecker(editorKit);
        editor = new ExpressionEditor<OWLDescription>(editorKit, checker);
        dllearner = new DLLearnerView(frame,label);
        editor.setExpressionObject(description);
        editor.getDocument().addDocumentListener(editorListener);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false);
        if(label.equals("Equivalent classes"))
        {
        	tabbedPane.add(SUGGEST_EQUIVALENT_CLASS_LABEL,dllearner);
        }
        if(label.equals("Superclasses"))
        {
        	tabbedPane.add(SUGGEST_SUBCLASS_LABEL,dllearner);
        }
        editingComponent = new JPanel(new BorderLayout());
        editingComponent.add(tabbedPane);
        editingComponent.setPreferredSize(new Dimension(600, 490));
        tabbedPane.add(CLASS_EXPRESSION_EDITOR_LABEL, new JScrollPane(editor));
       

        if (description == null || !description.isAnonymous()) {
            classSelectorPanel = new OWLClassSelectorPanel(editorKit);
            tabbedPane.add(CLASS_TREE_LABEL, classSelectorPanel);
            if (description != null) {
                classSelectorPanel.setSelectedClass(description.asOWLClass());
            }
            classSelectorPanel.addSelectionListener(changeListener);
            
            restrictionCreatorPanel = new ObjectRestrictionCreatorPanel();
            tabbedPane.add(RESTRICTION_CREATOR_LABEL, restrictionCreatorPanel);
            restrictionCreatorPanel.classSelectorPanel.addSelectionListener(changeListener);
            restrictionCreatorPanel.objectPropertySelectorPanel.addSelectionListener(changeListener);
            //dllearner.DLLearnerViewPanel.addChangeListener(changeListener);
            tabbedPane.addChangeListener(changeListener);
            
        }
    }

    private void handleVerifyEditorContents() {
        if (!listeners.isEmpty()){
            for (InputVerificationStatusChangedListener l : listeners){
                l.verifiedStatusChanged(isValidated());
            }
        }
    }


    private boolean isValidated() {
        boolean validated = false;
        final String selectedTabTitle = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        if (selectedTabTitle.equals(CLASS_EXPRESSION_EDITOR_LABEL)){
            validated = editor.isWellFormed();
        }
        else if (selectedTabTitle.equals(CLASS_TREE_LABEL)){
            validated = classSelectorPanel.getSelectedClass() != null;
        }
        else if (selectedTabTitle.equals(RESTRICTION_CREATOR_LABEL)){
            validated = restrictionCreatorPanel.classSelectorPanel.getSelectedClass() != null &&
                    restrictionCreatorPanel.objectPropertySelectorPanel.getSelectedOWLObjectProperty() != null;
        }
        else if(selectedTabTitle.equals(SUGGEST_EQUIVALENT_CLASS_LABEL)){
        	validated = dllearner.getSollution()!= null;
        }
        else if(selectedTabTitle.equals(SUGGEST_SUBCLASS_LABEL)){
        	validated = dllearner.getSollution()!= null;
        }
        return validated;
    }


    public JComponent getInlineEditorComponent() {
        // Same as general editor component
        return editingComponent;
    }


    /**
     * Gets a component that will be used to edit the specified
     * object.
     * @return The component that will be used to edit the object
     */
    public JComponent getEditorComponent() {
        return editingComponent;
    }


    public void clear() {
    	System.out.println("Und jetzt bin ich hier :-)");
    	dllearner.unsetEverything();
    	dllearner.makeView();
        initialDescription = null;
        editor.setText("");
    }


    public Set<OWLDescription> getEditedObjects() {
        if (tabbedPane.getSelectedComponent() == classSelectorPanel) {
            return classSelectorPanel.getSelectedClasses();
        }
        else if (tabbedPane.getSelectedComponent() == restrictionCreatorPanel) {
            return restrictionCreatorPanel.createRestrictions();
        }
        else if(tabbedPane.getSelectedComponent() == dllearner){
        	return dllearner.getSollutions(); 
        }
        return super.getEditedObjects();
    }


    /**
     * Gets the object that has been edited.
     * @return The edited object
     */
    public OWLDescription getEditedObject() {
        try {
            if (!editor.isWellFormed()) {
                return null;
            }
            String expression = editor.getText();
            if (editor.isWellFormed()) {
                return editorKit.getOWLModelManager().getOWLDescriptionParser().createOWLDescription(expression);
            }
            if(!dllearner.getSollutions().isEmpty()){
            	return dllearner.getSollution();
            }
            else {
                return null;
            }
        }
        catch (OWLException e) {
            return null;
        }
    }


    public void dispose() {
        if (classSelectorPanel != null) {
            classSelectorPanel.dispose();
        }
        if (restrictionCreatorPanel != null) {
            restrictionCreatorPanel.dispose();
        }
        if(dllearner !=null){
        	dllearner.dispose();
        }
    }


    private OWLDataFactory getDataFactory() {
        return editorKit.getOWLModelManager().getOWLDataFactory();
    }

    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.add(listener);
        listener.verifiedStatusChanged(isValidated());
    }


    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.remove(listener);
    }
    
    public class DLLearnerView extends JPanel{
  	  private JLabel pos;
  	  private final static long serialVersionUID = 624829578325729385L;
  	  private DLLearnerViewPanel panel;
	  private JComponent learner;
	  private JButton accept;
	  private JButton run;
	  private OWLEditorKit editor;
	  private JPanel option;
	  private JList suggest;
	  private JLabel neg;
	  private JScrollPane scrollPane;
	  private final Color Color_RED = Color.red;
	  private JButton cancel;
	  //private JPanel suggestPanel;
	  //private JButton helpForPosExamples;
	  //private JButton helpForNegExamples;
	  private JLabel errorMessage;
	  //private JScrollPane suggestScroll;
	  private JButton advanceButton;
	  private ActionHandler action;
	  private DLLearnerModel model;
	  private Description[] descriptions = new Description[10];
	  
	  public DLLearnerViewPanel getDLLearnerViewPanel()
	  {
		  return panel;
	  }
	  
	  public DLLearnerView(OWLFrame<OWLClass> aktuell,String label){
		  	editor = editorKit;
		  	model = new DLLearnerModel(editorKit,aktuell, label,this);
		    model.loadOntology(getUri());
		    //helpPanel.setLayout(new GridLayout(0,4));
		  	panel = new DLLearnerViewPanel(editor);
		  	action = new ActionHandler(this.action, model,this,label);
	    	System.out.println("Hallo test");
	    	//helpForPosExamples = new JButton("?");
	    	//helpForNegExamples = new JButton("?");
	    	run = new JButton("Suggest "+label);
	    	cancel = new JButton("Cancel");
	    	accept = new JButton("ADD");
	    	advanceButton = new JButton("Advanced");
		  	scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
	    	//suggestScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    	errorMessage = new JLabel();
	    	errorMessage.setForeground(Color_RED);
	    	learner = new JPanel();
	    	learner.setLayout(null);
	    	suggest = new JList();
	    	learner.setPreferredSize(new Dimension(600, 470));
	    	pos = new JLabel("Positive Examples");
	    	neg = new JLabel("Negative Examples");
	    	accept.setPreferredSize(new Dimension(290,50));
	    	option = new JPanel(new GridLayout(0,2));
	    	addAcceptButtonListener(this.action);
	    	addRunButtonListener(this.action);
	    	addCancelButtonListener(this.action);
		    
    }
	  
	  public void makeView()
	  {
		  	model.clearVector();
		  	model.initReasoner();
		    model.setPosVector();
		    model.setNegVector();
		    suggest = new JList();
	      	cancel.setEnabled(false);
	    	accept.setEnabled(false);
	    	setJCheckBoxen();
	    	if(option.getComponentCount()<=2)
	    	{
	    		run.setEnabled(false);
	    	}
	    	scrollPane.setViewportView(option);
	    	scrollPane.setBounds(10, 200, 490, 250);
	    	suggest.setBounds(10,40,490,110);
	    	suggest.setVisible(true);
	    	run.setBounds(10,0,200,30);
	        cancel.setBounds(260,0,200,30);
	        advanceButton.setBounds(260,160,200,30);
	        accept.setBounds(10,160,200,30);
	        errorMessage.setBounds(260,160,300,30);
	    	learner.add(run);
	    	learner.add(cancel);
	    	learner.add(suggest);
	    	learner.add(accept);
	    	learner.add(advanceButton);
	    	learner.add(scrollPane);
	    	add(learner);
	    	addListener();
	  }
	  
	  public JComponent getLearnerPanel()
	  {
		  return learner;
	  }
	  
	  public URI getUri()
	    {
			URI uri = editor.getOWLModelManager().getOntologyPhysicalURI(editor.getOWLModelManager().getActiveOntology());
	    	return uri;
	    }
	  
	  public Set<OWLDescription> getSollutions()
	  {
		  return model.getNewOWLDescription();
	  }
	  
	  public OWLDescription getSollution()
	  {
		  System.out.println("das ist die loesung: "+model.getSollution());
		  return model.getSollution();
	  }
	  
	   private void setJCheckBoxen()
	   {
	    	option.add(pos);
	    	option.add(neg);
	    	for(int j=0; j<model.getPosVector().size();j++)
	    	{
	    		option.add(model.getPositivJCheckBox(j));
	    		option.add(model.getNegativJCheckBox(j));
	    	}
	    	
	   }
	   
	   public void unsetEverything()
	   {
		 option.removeAll();
		 run.setEnabled(true);
		 action.destroyDLLearnerThread();
		 suggest.removeAll();
		 errorMessage.setText("");
		 learner.removeAll();
	   }
	   
	   public void destroyListener()
	   {
		   //run.removeActionListener(this.action);
		   //accept.removeActionListener(this.action);
		   //cancel.removeActionListener(this.action);
	   }
	   
	   public void renderErrorMessage(String s)
	   {
		   errorMessage.setText(s);
	   }
	   
	   private void addListener()
	   {
		   for(int i=0;i<model.getPosVector().size();i++)
		   {
			   model.getPositivJCheckBox(i).addItemListener(action);
		   }
		   
		   for(int i=0;i<model.getNegVector().size();i++)
		   {
			   model.getNegativJCheckBox(i).addItemListener(action);
		   } 
	   }
	   
	   public void update(Observable m,Object c)
		  {
			  if( model != m) return;
			   //draw(); 
		  }
	   
	   protected void draw(Description[] desc) {
			if (model != null) {
				run.setEnabled(true);
				cancel.setEnabled(false);
				accept.setEnabled(true);
				errorMessage.setText("");
				learner.remove(suggest);
				//learner.remove(3);
				descriptions = desc;
				suggest=new JList(descriptions);
		    	suggest.setBounds(10,40,490,110);
				suggest.setVisible(true);
				learner.add(suggest);
				suggest.addMouseListener(action);
				suggest.repaint();
				model.unsetJCheckBoxen();
				option.removeAll();
				setJCheckBoxen();
				
			}
		}
	   
	   public JList getSuggestionList()
	   {
		   return suggest;
	   }
	   
	   public JButton getRunButton()
	   {
		   return run;
	   }
	   
	   public JButton getCancelButton()
	   {
		   return cancel;
	   }
	   
	   public void dispose() {
     
       }
	   
	   public void addRunButtonListener(ActionListener a)
	   {
			run.addActionListener(a);
		}
		
		public void addCancelButtonListener(ActionListener a)
		{
			cancel.addActionListener(a);
		}
		
		public void addAcceptButtonListener(ActionListener a)
		{
			accept.addActionListener(a);
		}
		
		public void addAdvanceButtonListener(ActionListener a)
		{
			//advanceButton.addActionListener(a);
		}
    }
    
    private class ObjectRestrictionCreatorPanel extends JPanel {

        private OWLObjectPropertySelectorPanel objectPropertySelectorPanel;
        private final static long serialVersionUID = 12435463243L;
        private OWLClassSelectorPanel classSelectorPanel;

        private JSpinner cardinalitySpinner;

        private JComboBox typeCombo;


        public ObjectRestrictionCreatorPanel() {
            objectPropertySelectorPanel = new OWLObjectPropertySelectorPanel(editorKit);
            objectPropertySelectorPanel.setBorder(ComponentFactory.createTitledBorder("Restricted properties"));
            cardinalitySpinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
            JComponent cardinalitySpinnerEditor = cardinalitySpinner.getEditor();
            Dimension prefSize = cardinalitySpinnerEditor.getPreferredSize();
            cardinalitySpinnerEditor.setPreferredSize(new Dimension(50, prefSize.height));
            classSelectorPanel = new OWLClassSelectorPanel(editorKit);
            classSelectorPanel.setBorder(ComponentFactory.createTitledBorder("Restriction fillers"));
            setLayout(new BorderLayout());
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
            splitPane.setResizeWeight(0.5);
            splitPane.setLeftComponent(objectPropertySelectorPanel);
            splitPane.setRightComponent(classSelectorPanel);
            add(splitPane);
            splitPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            List<RestrictionCreator> types = new ArrayList<RestrictionCreator>();
            types.add(new RestrictionCreator("Some (existential)") {
                public void createRestrictions(Set<OWLObjectProperty> properties, Set<OWLDescription> fillers,
                                               Set<OWLDescription> result) {
                    for (OWLObjectProperty prop : properties) {
                        for (OWLDescription filler : fillers) {
                            result.add(getDataFactory().getOWLObjectSomeRestriction(prop, filler));
                        }
                    }
                }
            });
            types.add(new RestrictionCreator("Only (universal)") {
                public void createRestrictions(Set<OWLObjectProperty> properties, Set<OWLDescription> fillers,
                                               Set<OWLDescription> result) {
                    for (OWLObjectProperty prop : properties) {
                        if (fillers.isEmpty()) {
                            return;
                        }
                        OWLDescription filler;
                        if (fillers.size() > 1) {
                            filler = getDataFactory().getOWLObjectUnionOf(fillers);
                        }
                        else {
                            filler = fillers.iterator().next();
                        }
                        result.add(getDataFactory().getOWLObjectAllRestriction(prop, filler));
                    }
                }
            });
            types.add(new CardinalityRestrictionCreator("Min (min cardinality)", cardinalitySpinner) {
                public OWLDescription createRestriction(OWLObjectProperty prop, OWLDescription filler, int card) {
                    return getDataFactory().getOWLObjectMinCardinalityRestriction(prop, card, filler);
                }
            });
            types.add(new CardinalityRestrictionCreator("Exactly (exact cardinality)", cardinalitySpinner) {
                public OWLDescription createRestriction(OWLObjectProperty prop, OWLDescription filler, int card) {
                    return getDataFactory().getOWLObjectExactCardinalityRestriction(prop, card, filler);
                }
            });
            types.add(new CardinalityRestrictionCreator("Max (max cardinality)", cardinalitySpinner) {
                public OWLDescription createRestriction(OWLObjectProperty prop, OWLDescription filler, int card) {
                    return getDataFactory().getOWLObjectMaxCardinalityRestriction(prop, card, filler);
                }
            });
            typeCombo = new JComboBox(types.toArray());


            final JPanel typePanel = new JPanel();
            typePanel.setBorder(ComponentFactory.createTitledBorder("Restriction type"));
            add(typePanel, BorderLayout.SOUTH);
            typePanel.add(typeCombo);
            typeCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cardinalitySpinner.setEnabled(typeCombo.getSelectedItem() instanceof CardinalityRestrictionCreator);
                }
            });
            JPanel spinnerHolder = new JPanel(new BorderLayout(4, 4));
            spinnerHolder.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            spinnerHolder.add(new JLabel("Cardinality"), BorderLayout.WEST);
            spinnerHolder.add(cardinalitySpinner, BorderLayout.EAST);
            JPanel spinnerAlignmentPanel = new JPanel(new BorderLayout());
            spinnerAlignmentPanel.add(spinnerHolder, BorderLayout.WEST);
            typePanel.add(spinnerAlignmentPanel);
            cardinalitySpinner.setEnabled(typeCombo.getSelectedItem() instanceof CardinalityRestrictionCreator);
        }


        public Set<OWLDescription> createRestrictions() {
            Set<OWLDescription> result = new HashSet<OWLDescription>();
            RestrictionCreator creator = (RestrictionCreator) typeCombo.getSelectedItem();
            if (creator == null) {
                return Collections.emptySet();
            }
            creator.createRestrictions(objectPropertySelectorPanel.getSelectedOWLObjectProperties(),
                    classSelectorPanel.getSelectedClasses(),
                    result);
            return result;
        }


        public void dispose() {
            objectPropertySelectorPanel.dispose();
            classSelectorPanel.dispose();
        }
    }


    private abstract class RestrictionCreator {

        private String name;


        protected RestrictionCreator(String name) {
            this.name = name;
        }


        public String toString() {
            return name;
        }


        abstract void createRestrictions(Set<OWLObjectProperty> properties, Set<OWLDescription> fillers,
                                         Set<OWLDescription> result);
    }


    private abstract class CardinalityRestrictionCreator extends RestrictionCreator {

        private JSpinner cardinalitySpinner;


        protected CardinalityRestrictionCreator(String name, JSpinner cardinalitySpinner) {
            super(name);
            this.cardinalitySpinner = cardinalitySpinner;
        }


        public void createRestrictions(Set<OWLObjectProperty> properties, Set<OWLDescription> fillers,
                                       Set<OWLDescription> result) {
            for (OWLObjectProperty prop : properties) {
                for (OWLDescription desc : fillers) {
                    result.add(createRestriction(prop, desc, (Integer) cardinalitySpinner.getValue()));
                }
            }
        }


        public abstract OWLDescription createRestriction(OWLObjectProperty prop, OWLDescription filler, int card);
    }
    
}
