package org.dllearner.tools.protege;


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
    private ActionHandler action;
    
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
        action = new ActionHandler(this.action,null,dllearner,null);
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
        editingComponent.setPreferredSize(new Dimension(600, 520));
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
        	validated = true;
        }
        else if(selectedTabTitle.equals(SUGGEST_SUBCLASS_LABEL)){
        	validated = true;
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
    	handleVerifyEditorContents();
        initialDescription = null;
        editor.setText("");
    }

    @Override
	public Set<OWLDescription> getEditedObjects() {
        if (tabbedPane.getSelectedComponent() == classSelectorPanel) {
            return classSelectorPanel.getSelectedClasses();
        }
        else if (tabbedPane.getSelectedComponent() == restrictionCreatorPanel) {
            return restrictionCreatorPanel.createRestrictions();
        }
        else if(tabbedPane.getSelectedComponent() == dllearner){
        	System.out.println("die loesungen:"+dllearner.getSollutions());
        	
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
    /**
     * 
     * @author Heero Yuy
     *
     */
    public class DLLearnerView extends JPanel{
  	  /**
  	   * 
  	   */
      private final static long serialVersionUID = 624829578325729385L;
  	  /**
  	   * 
  	   */
      private DLLearnerViewPanel panel;
	  /**
	   * 
	   */
      private JComponent learner;
	  /**
	   * 
	   */
      private JButton accept;
	  /**
	   * 
	   */
      private JButton run;
	  /**
	   * 
	   */
      private OWLEditorKit editor;
	  /**
	   * 
	   */
      private JLabel adv;
	  /**
	   * 
	   */
      private final Color Color_RED = Color.red;
	  /**
	   * 
	   */
      private JButton cancel;
	  /**
	   * 
	   */
      private JLabel errorMessage;
	  /**
	   * 
	   */
	  private JToggleButton advanced; 
	  /**
	   * 
	   */
	  private ActionHandler action;
	  /**
	   * 
	   */
	  private DLLearnerModel model;
	  /**
	   * 
	   */
	  
	  private SuggestClassPanel sugPanel;
	  private PosAndNegSelectPanel posPanel;
	  private ImageIcon icon;
	  /**
	   * 
	   * @return
	   */
	  public DLLearnerViewPanel getDLLearnerViewPanel()
	  {
		  return panel;
	  }
	  /**
	   * The constructor for the DL-Learner tab in the class description editor 
	   * @param aktuell
	   * @param label
	   */
	  public DLLearnerView(OWLFrame<OWLClass> aktuell,String label){
		  	editor = editorKit;
		  	classSelectorPanel = new OWLClassSelectorPanel(editorKit);
		  	classSelectorPanel.firePropertyChange("test", false, true);
		  	icon = new ImageIcon(OWLClassDescriptionEditorWithDLLearnerTab.class.getResource("/bilder/org/dllearner/tools/protege/pfeil.gif"));
		  	model = new DLLearnerModel(editorKit,aktuell, label,this);
		  	panel = new DLLearnerViewPanel(editor);
		  	sugPanel = new SuggestClassPanel();
		  	action = new ActionHandler(this.action, model,this,label);
	    	adv = new JLabel("Advanced");
	    	advanced = new JToggleButton(icon);
	    	advanced.setIcon(icon);
	    	advanced.setVisible(true);
	    	run = new JButton("Suggest "+label);
	    	cancel = new JButton("Cancel");
	    	accept = new JButton("ADD");
	    	sugPanel.addSuggestPanelMouseListener(action);
	    	errorMessage = new JLabel();
	    	learner = new JPanel();
	    	advanced.setSize(20,20);
	    	learner.setLayout(null);
	    	learner.setPreferredSize(new Dimension(600, 520));
	    	accept.setPreferredSize(new Dimension(290,50));
	    	advanced.setName("Advanced");
	    	posPanel = new PosAndNegSelectPanel(model,action);
	    	addAcceptButtonListener(this.action);
	    	addRunButtonListener(this.action);
	    	addCancelButtonListener(this.action);
	    	addAdvancedButtonListener(this.action);

		    
    }
	  
	  public SuggestClassPanel getSuggestClassPanel()
	  {
		  return sugPanel;
	  }
	  
	  public PosAndNegSelectPanel getPosAndNegSelectPanel()
	  {
		  return posPanel;
	  }
	  /**
	   * 
	   */
	  public void makeView()
	  {
	    	model.clearVector();
		  	model.unsetListModel();
		  	model.initReasoner();
		  	model.setPosVector();
		  	posPanel.setJCheckBoxen();
	      	cancel.setEnabled(false);
	    	accept.setEnabled(false);
	    	action.resetToggled();
	    	sugPanel.setSuggestList(model.getSuggestList());
	    	sugPanel = sugPanel.updateSuggestClassList();
	    	advanced.setSelected(false);
	    	sugPanel.setBounds(10,40,490,110);
	    	adv.setBounds(40,200,200,20);
	    	run.setBounds(10,0,200,30);
	    	advanced.setBounds(10,200,20,20);
	    	sugPanel.setVisible(true);
	    	posPanel.setVisible(false);
			posPanel.setBounds(10, 230, 490, 250);
	        cancel.setBounds(260,0,200,30);
	        accept.setBounds(510,40,80,110);
	        errorMessage.setBounds(10,160,590,20);
	    	learner.add(run);
	    	learner.add(adv);
	    	learner.add(advanced);
	    	learner.add(cancel);
	    	learner.add(sugPanel);
	    	learner.add(accept);
	    	learner.add(errorMessage);
	    	learner.add(posPanel);
	    	posPanel.addHelpButtonListener(action);
	    	add(learner);
	  }
	  
	  public void setArrowForToggleButton()
	  {
		  
	  }
	  /**
	   * 
	   * @return
	   */
	  public JComponent getLearnerPanel()
	  {
		  return learner;
	  }
	  /**
	   * 
	   * @param visible
	   */
	  public void setExamplePanelVisible(boolean visible)
	  {
		  posPanel.setVisible(visible);
	  }
	  /**
	   * 
	   * @return
	   */
	  public JPanel getOptionPanel()
	  {
		  return null;
	  }
	  /**
	   * 
	   * @return
	   */
	  public JButton getAddButton()
	  {
		   return accept;
	  }
	
	  /**
	   * 
	   * @return
	   */
	  public Set<OWLDescription> getSollutions()
	  {
		  
		  return model.getNewOWLDescription();
	  }
	  /**
	   * 
	   * @return
	   */
	  public OWLDescription getSollution()
	  {
		  System.out.println(model.getSolution());
		  return model.getSolution();
	  }

	   /**
	    * 
	    */
	   public void unsetEverything()
	   {
		 run.setEnabled(true);
		 model.unsetNewConcepts();
		 action.destroyDLLearnerThread();
		 errorMessage.setText("");
		 posPanel.unsetPosAndNegPanel();
		 learner.removeAll();
	   }
	   /**
	    * 
	    * @param s
	    */
	   public void renderErrorMessage(String s)
	   {
		   errorMessage.setForeground(Color_RED);
		   errorMessage.setText(s);
	   }
	   
	   /**
	    * 
	    * @return
	    */
	   public JButton getRunButton()
	   {
		   return run;
	   }
	   /**
	    * 
	    * @return
	    */
	   public JButton getCancelButton()
	   {
		   return cancel;
	   }
	   /**
	    * 
	    */
	   public void dispose() {
     
       }

	   /**
	    * 
	    * @param a
	    */
	   public void addRunButtonListener(ActionListener a)
	   {
			run.addActionListener(a);
		}
		/**
		 * 
		 * @param a
		 */
		public void addCancelButtonListener(ActionListener a)
		{
			cancel.addActionListener(a);
		}
		/**
		 * 
		 * @param a
		 */
		public void addAcceptButtonListener(ActionListener a)
		{
			accept.addActionListener(a);
		}
		/**
		 * 
		 * @param a
		 */
		public void addAdvancedButtonListener(ActionListener a)
		{
			advanced.addActionListener(a);
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
                @Override
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
                @Override
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
                @Override
				public OWLDescription createRestriction(OWLObjectProperty prop, OWLDescription filler, int card) {
                    return getDataFactory().getOWLObjectMinCardinalityRestriction(prop, card, filler);
                }
            });
            types.add(new CardinalityRestrictionCreator("Exactly (exact cardinality)", cardinalitySpinner) {
                @Override
				public OWLDescription createRestriction(OWLObjectProperty prop, OWLDescription filler, int card) {
                    return getDataFactory().getOWLObjectExactCardinalityRestriction(prop, card, filler);
                }
            });
            types.add(new CardinalityRestrictionCreator("Max (max cardinality)", cardinalitySpinner) {
                @Override
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


        @Override
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


        @Override
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
