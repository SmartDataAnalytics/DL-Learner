package org.dllearner.tools.ore.ui.editor;

import javax.swing.JComponent;


public interface OWLAxiomEditor<T> {
	JComponent getEditorComponent();
	T getEditedObject();
}
