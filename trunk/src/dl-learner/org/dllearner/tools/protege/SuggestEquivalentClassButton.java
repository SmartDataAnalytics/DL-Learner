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


public class SuggestEquivalentClassButton extends AbstractOWLFrameSection<OWLClass, OWLEquivalentClassesAxiom, OWLDescription> {
	
	private static final String LABEL = "Suggest a equivalent Class";
	private OWLFrame<OWLClass> frame;
	private SuggestEquivalentClassView view;
	public SuggestEquivalentClassButton(OWLEditorKit editorKit, OWLFrame<OWLClass> frame)
	{
		super(editorKit, LABEL, frame);
		this.frame = frame;
	}
	
	@Override
    protected void clear() {
    	
    }


	@Override
    protected void refill(OWLOntology ontology) {

    }

	@Override
    protected void refillInferred() {

    }


    public void visit(SuggestEquivalentClassButton axiom) {


    }

    @Override
    protected OWLEquivalentClassesAxiom createAxiom(OWLDescription object) {
        return getOWLDataFactory().getOWLEquivalentClassesAxiom(CollectionFactory.createSet(getRootObject(), object));
    }

    @Override
    public OWLFrameSectionRowObjectEditor<OWLDescription> getObjectEditor() {
    	view = new SuggestEquivalentClassView(getOWLEditorKit(), null, frame);
    	view.setView(view);
        return view;

    }


    public Comparator<OWLFrameSectionRow<OWLClass, OWLEquivalentClassesAxiom, OWLDescription>> getRowComparator() {
        return null;
    }
}