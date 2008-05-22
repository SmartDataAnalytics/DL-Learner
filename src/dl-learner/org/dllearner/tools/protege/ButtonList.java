package org.dllearner.tools.protege;

import org.protege.editor.owl.ui.frame.AbstractOWLFrame;
import org.protege.editor.owl.ui.frame.InheritedAnonymousClassesFrameSection;
import org.protege.editor.owl.ui.frame.OWLClassAssertionAxiomIndividualSection;
import org.protege.editor.owl.ui.frame.OWLDisjointClassesAxiomFrameSection;
import org.semanticweb.owl.model.OWLClass;
import org.protege.editor.owl.OWLEditorKit;

public class ButtonList extends AbstractOWLFrame<OWLClass>{

	public ButtonList(OWLEditorKit editorKit)
	{
		super(editorKit.getOWLModelManager().getOWLOntologyManager());
        addSection(new OWLEquivalentClassesAxiomFrameSection(editorKit, this));
        addSection(new OWLSubClassAxiomFrameSection(editorKit, this));
        addSection(new InheritedAnonymousClassesFrameSection(editorKit, this));
        addSection(new OWLClassAssertionAxiomIndividualSection(editorKit, this));
        addSection(new OWLDisjointClassesAxiomFrameSection(editorKit, this));
	}
}
