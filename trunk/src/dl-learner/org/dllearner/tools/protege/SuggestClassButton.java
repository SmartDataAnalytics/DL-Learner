package org.dllearner.tools.protege;

import java.util.Comparator;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRowObjectEditor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.util.CollectionFactory;


public class SuggestClassButton extends AbstractOWLFrameSection<OWLClass, OWLEquivalentClassesAxiom, OWLDescription> {
	
	private static final String LABEL = "Suggest a class";

	
	public SuggestClassButton(OWLEditorKit editorKit, OWLFrame<OWLClass> frame)
	{
		super(editorKit, LABEL, frame);
	}
	
    protected void clear() {
    	
    }



    protected void refill(OWLOntology ontology) {

    }


    protected void refillInferred() {

    }


    public void visit(SuggestClassButton axiom) {


    }


    protected OWLEquivalentClassesAxiom createAxiom(OWLDescription object) {
        return getOWLDataFactory().getOWLEquivalentClassesAxiom(CollectionFactory.createSet(getRootObject(), object));
    }


    public OWLFrameSectionRowObjectEditor<OWLDescription> getObjectEditor() {
   
        return new DLLearnerOptionTab(getOWLEditorKit(), null);

    }


    public Comparator<OWLFrameSectionRow<OWLClass, OWLEquivalentClassesAxiom, OWLDescription>> getRowComparator() {
        return null;
    }
}
