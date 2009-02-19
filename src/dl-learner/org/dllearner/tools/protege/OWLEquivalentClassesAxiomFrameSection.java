/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLEquivalentClassesAxiomFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRowObjectEditor;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.inference.UnsupportedReasonerOperationException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.util.CollectionFactory;

/**
  * Here a only caged in the method getObjectEditor() the call for the
 * OWLClassDescriptionEditor to OWLClassDescriptionEditorWithDLLearnerTab.
 * @author Matthew Horridge and Christian Koetteritzsch<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br>
 * <br>
 */
public class OWLEquivalentClassesAxiomFrameSection
		extends
		AbstractOWLFrameSection<OWLClass, OWLEquivalentClassesAxiom, OWLDescription> {

	private static final String LABEL = "equivalent classes";

	private Set<OWLClass> added;

	private boolean inferredEquivalentClasses = true;
	private OWLClassDescriptionEditorWithDLLearnerTab dlLearner;

	private OWLFrame<OWLClass> frame;
	/**
	 * Constructor of the OWLEquivalentClassesAxiomFrameSection.
	 * @param editorKit OWLEditorKit
	 * @param frame OWLFrame
	 */
	public OWLEquivalentClassesAxiomFrameSection(OWLEditorKit editorKit,
			OWLFrame<OWLClass> frame) {
		super(editorKit, LABEL, frame);
		added = new HashSet<OWLClass>();
		this.frame = frame;
	}

	@Override
	protected void clear() {
		added.clear();
	}

	/**
	 * Refills the section with rows. This method will be called by the system
	 * and should be directly called.
	 * @param ontology OWLOntology
	 */
	@Override
	protected void refill(OWLOntology ontology) {
		for (OWLEquivalentClassesAxiom ax : ontology
				.getEquivalentClassesAxioms(getRootObject())) {
			addRow(new OWLEquivalentClassesAxiomFrameSectionRow(
					getOWLEditorKit(), this, ontology, getRootObject(), ax));
			for (OWLDescription desc : ax.getDescriptions()) {
				if (!desc.isAnonymous()) {
					added.add(desc.asOWLClass());
				}
			}
		}
	}

	@Override
	protected void refillInferred() {
		if (!inferredEquivalentClasses) {
			return;
		}
		try {
			if (!getOWLModelManager().getReasoner().isSatisfiable(
					getRootObject())) {
				addRow(new OWLEquivalentClassesAxiomFrameSectionRow(
						getOWLEditorKit(), this, null, getRootObject(),
						getOWLDataFactory().getOWLEquivalentClassesAxiom(
								CollectionFactory.createSet(getRootObject(),
										getOWLModelManager()
												.getOWLDataFactory()
												.getOWLNothing()))));
				return;
			}
			for (OWLClass cls : getOWLModelManager().getReasoner()
					.getEquivalentClasses(getRootObject())) {
				if (!added.contains(cls) && !cls.equals(getRootObject())) {
					addRow(new OWLEquivalentClassesAxiomFrameSectionRow(
							getOWLEditorKit(), this, null, getRootObject(),
							getOWLDataFactory().getOWLEquivalentClassesAxiom(
									CollectionFactory.createSet(
											getRootObject(), cls))));
				}
			}
		} catch (UnsupportedReasonerOperationException e) {
			inferredEquivalentClasses = false;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		if (axiom.getDescriptions().contains(getRootObject())) {
			reset();
		}
	}

	@Override
	protected OWLEquivalentClassesAxiom createAxiom(OWLDescription object) {
		return getOWLDataFactory().getOWLEquivalentClassesAxiom(
				CollectionFactory.createSet(getRootObject(), object));
	}

	@Override
	public OWLFrameSectionRowObjectEditor<OWLDescription> getObjectEditor() {
		// Own OWLClassDescriptionEditor to integrate the dllearner in protege
		// This is to suggest equivalent classes
		dlLearner = new OWLClassDescriptionEditorWithDLLearnerTab(getOWLEditorKit(),
				null, frame, LABEL);
		return dlLearner;
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

	@Override
	public boolean dropObjects(List<OWLObject> objects) {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLObject obj : objects) {
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;
				OWLAxiom ax = getOWLDataFactory().getOWLEquivalentClassesAxiom(
						CollectionFactory.createSet(getRootObject(), desc));
				changes.add(new AddAxiom(getOWLModelManager()
						.getActiveOntology(), ax));
			} else {
				return false;
			}
		}
		getOWLModelManager().applyChanges(changes);
		return true;
	}

	/**
	 * Obtains a comparator which can be used to sort the rows in this section.
	 * 
	 * @return A comparator if to sort the rows in this section, or
	 *         <code>null</code> if the rows shouldn't be sorted.
	 */
	public Comparator<OWLFrameSectionRow<OWLClass, OWLEquivalentClassesAxiom, OWLDescription>> getRowComparator() {
		return null;
	}
}
