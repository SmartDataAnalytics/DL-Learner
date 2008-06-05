package org.dllearner.tools.protege;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.*;
import org.protege.editor.owl.ui.OWLDescriptionComparator;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLSubClassAxiom;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLSubClassAxiomFrameSection extends AbstractOWLFrameSection<OWLClass, OWLSubClassAxiom, OWLDescription> {

    private static final String LABEL = "Superclasses";

    private Set<OWLDescription> added = new HashSet<OWLDescription>();

    private OWLFrame<OWLClass> frame;
    public OWLSubClassAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame) {
        super(editorKit, LABEL, frame);
        this.frame = frame;
    }


    @Override
	protected void clear() {
        added.clear();
    }


    /**
     * Refills the section with rows.  This method will be called
     * by the system and should be directly called.
     */
    @Override
	protected void refill(OWLOntology ontology) {
        for (OWLSubClassAxiom ax : ontology.getSubClassAxiomsForLHS(getRootObject())) {
            addRow(new OWLSubClassAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), ax));
            added.add(ax.getSuperClass());
        }
    }


    @Override
	protected void refillInferred() {
        try {
            if (getOWLModelManager().getReasoner().isSatisfiable(getRootObject())) {
                for (Set<OWLClass> descs : getOWLModelManager().getReasoner().getSuperClasses(getRootObject())) {
                    for (OWLClass desc : descs) {
                        if (!added.contains(desc)) {
                            addRow(new OWLSubClassAxiomFrameSectionRow(getOWLEditorKit(),
                                                                       this,
                                                                       null,
                                                                       getRootObject(),
                                                                       getOWLModelManager().getOWLDataFactory().getOWLSubClassAxiom(
                                                                               getRootObject(),
                                                                               desc)));
                            added.add(desc);
                        }
                    }
                }
            }
        }
        catch (OWLReasonerException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
	protected OWLSubClassAxiom createAxiom(OWLDescription object) {
        return getOWLDataFactory().getOWLSubClassAxiom(getRootObject(), object);
    }


    @Override
	public OWLFrameSectionRowObjectEditor<OWLDescription> getObjectEditor() {
        return new OWLClassDescriptionEditorWithDLLearnerTab(getOWLEditorKit(), null, frame,LABEL);
    }


    @Override
	public boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLDescription)) {
                return false;
            }
        }
        return true;
    }


    private OWLObjectProperty prop;


    @Override
	public boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLObject obj : objects) {
            if (obj instanceof OWLDescription) {
                OWLDescription desc;
                if (prop != null) {
                    desc = getOWLDataFactory().getOWLObjectSomeRestriction(prop, (OWLDescription) obj);
                }
                else {
                    desc = (OWLDescription) obj;
                }
                OWLAxiom ax = getOWLDataFactory().getOWLSubClassAxiom(getRootObject(), desc);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            }
            else if (obj instanceof OWLObjectProperty) {
                // Prime
                prop = (OWLObjectProperty) obj;
            }
            else {
                return false;
            }
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }


    @Override
	public void visit(OWLSubClassAxiom axiom) {
        if (axiom.getSubClass().equals(getRootObject())) {
            reset();
        }
    }


    /**
     * Obtains a comparator which can be used to sort the rows
     * in this section.
     * @return A comparator if to sort the rows in this section,
     *         or <code>null</code> if the rows shouldn't be sorted.
     */
    public Comparator<OWLFrameSectionRow<OWLClass, OWLSubClassAxiom, OWLDescription>> getRowComparator() {
        return new Comparator<OWLFrameSectionRow<OWLClass, OWLSubClassAxiom, OWLDescription>>() {

            private OWLDescriptionComparator comparator = new OWLDescriptionComparator(getOWLModelManager());


            public int compare(OWLFrameSectionRow<OWLClass, OWLSubClassAxiom, OWLDescription> o1,
                               OWLFrameSectionRow<OWLClass, OWLSubClassAxiom, OWLDescription> o2) {
                int val = comparator.compare(o1.getAxiom().getSuperClass(), o2.getAxiom().getSuperClass());
                if (o1.isInferred()) {
                    if (o2.isInferred()) {
                        return val;
                    }
                    else {
                        return 1;
                    }
                }
                else {
                    if (o2.isInferred()) {
                        return -1;
                    }
                    else {
                        return val;
                    }
                }
            }
        };
    }
}
