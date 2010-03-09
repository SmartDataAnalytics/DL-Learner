package org.dllearner.tools.ore.ui.editor;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;

public class OWLAxiomsEditor implements VerifiedInputEditor, OWLAxiomEditor<OWLAxiom>{
	
	private ExpressionEditor<OWLAxiom> editor;

    private JComponent editingComponent;
    
    private OREManager oreManager;


    public OWLAxiomsEditor(OREManager oreManager) {
    	this.oreManager = oreManager;
        editor = new ExpressionEditor<OWLAxiom>(oreManager, new OWLAxiomChecker(oreManager));

        editingComponent = new JPanel(new BorderLayout());
        editingComponent.add(editor);
        editingComponent.setPreferredSize(new Dimension(400, 200));
    }


    public boolean setEditedObject(OWLAxiom axiom) {
        if (axiom == null){
            editor.setText("");
        }
        else{
            editor.setText(oreManager.getManchesterSyntaxRendering(axiom));
        }
        return true;
    }


    public JComponent getInlineEditorComponent() {
        // Same as general editor component
        return editingComponent;
    }


    public String getEditorTypeName() {
        return "OWL Axiom";
    }


    public boolean canEdit(Object object) {
        return object instanceof OWLAxiom;
    }


    /**
     * Gets a component that will be used to edit the specified
     * object.
     * @return The component that will be used to edit the object
     */
    public JComponent getEditorComponent() {
        return editingComponent;
    }


    /**
     * Gets the object that has been edited.
     * @return The edited object
     */
    public OWLAxiom getEditedObject() {
        try {
            if (editor.isWellFormed()) {
                return editor.createObject();
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
    }
    
    public final void clear(){
        setEditedObject(null);
    }


    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        editor.addStatusChangedListener(listener);
    }


    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        editor.removeStatusChangedListener(listener);
    }
}

