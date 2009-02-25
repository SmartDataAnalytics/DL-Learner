/**
 * Copyright (C) 2007-2009, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.tools.protege;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRowObjectEditor;
import org.protege.editor.owl.ui.frame.OWLSubClassAxiomFrameSectionRow;
import org.protege.editor.owl.ui.frame.cls.AbstractOWLClassAxiomFrameSection;
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
 * Here a only caged in the method getObjectEditor() the call for the
 * OWLClassDescriptionEditor to OWLClassDescriptionEditorWithDLLearnerTab.
 * @author Matthew Horridge and Christian Koetteritzsch<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br>
 * <br>
 */
public class OWLSubClassAxiomFrameSection extends AbstractOWLClassAxiomFrameSection<OWLSubClassAxiom, OWLDescription> {

    private static final String LABEL = "super classes";

    private final Set<OWLDescription> added = new HashSet<OWLDescription>();
    private final OWLFrame<OWLClass> frame;

    /**
     * Constructor.
     * @param editorKit editorKit
     * @param frame OWLFrame
     */
    public OWLSubClassAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame) {
        super(editorKit, LABEL, "Superclass", frame);
        this.frame = frame;
    }


    @Override
	protected void clear() {
        added.clear();
    }


    @Override
	protected void addAxiom(OWLSubClassAxiom ax, OWLOntology ont) {
        addRow(new OWLSubClassAxiomFrameSectionRow(getOWLEditorKit(), this, ont, getRootObject(), ax));
        added.add(ax.getSuperClass());
    }


    @Override
	protected Set<OWLSubClassAxiom> getClassAxioms(OWLDescription descr, OWLOntology ont) {
        if (!descr.isAnonymous()){
            return ont.getSubClassAxiomsForLHS(descr.asOWLClass());
        }else{
            Set<OWLSubClassAxiom> axioms = new HashSet<OWLSubClassAxiom>();
            for (OWLAxiom ax : ont.getGeneralClassAxioms()){
                if (ax instanceof OWLSubClassAxiom && ((OWLSubClassAxiom) ax).getSubClass().equals(descr)){
                    axioms.add((OWLSubClassAxiom) ax);
                }
            }
            return axioms;
        }
    }


    @Override
	protected void refillInferred() {
        try {
            if (getOWLModelManager().getReasoner().isSatisfiable(getRootObject())) {

                for (Set<OWLClass> descs : getOWLModelManager().getReasoner().getSuperClasses(getRootObject())) {
                    for (OWLDescription desc : descs) {
                        if (!added.contains(desc) && !getRootObject().equals(desc)) {
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
        }catch (OWLReasonerException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
	protected OWLSubClassAxiom createAxiom(OWLDescription object) {
            return getOWLDataFactory().getOWLSubClassAxiom(getRootObject(), object);
    }


    @Override
	public OWLFrameSectionRowObjectEditor<OWLDescription> getObjectEditor() {
        return new OWLClassDescriptionEditorWithDLLearnerTab(getOWLEditorKit(), null, frame, LABEL);
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
                }else {
                    desc = (OWLDescription) obj;
                }
                OWLAxiom ax = getOWLDataFactory().getOWLSubClassAxiom(getRootObject(), desc);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            } else if (obj instanceof OWLObjectProperty) {
                // Prime
                prop = (OWLObjectProperty) obj;
            }else {
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
    public Comparator<OWLFrameSectionRow<OWLDescription, OWLSubClassAxiom, OWLDescription>> getRowComparator() {
        return new Comparator<OWLFrameSectionRow<OWLDescription, OWLSubClassAxiom, OWLDescription>>() {


            public int compare(OWLFrameSectionRow<OWLDescription, OWLSubClassAxiom, OWLDescription> o1,
                               OWLFrameSectionRow<OWLDescription, OWLSubClassAxiom, OWLDescription> o2) {
                if (o1.isInferred()) {
                    if (!o2.isInferred()) {
                        return 1;
                    }
                }else {
                    if (o2.isInferred()) {
                        return -1;
                    }
                }
//                int val = o1.getAxiom().getSuperClass().compareTo(o2.getAxiom().getSuperClass());
                int val = getOWLModelManager().getOWLObjectComparator().compare(o1.getAxiom(), o2.getAxiom());

                if(val == 0) {
                    return o1.getOntology().getURI().compareTo(o2.getOntology().getURI());
                }else {
                    return val;
                }

            }
        };
    }
}
