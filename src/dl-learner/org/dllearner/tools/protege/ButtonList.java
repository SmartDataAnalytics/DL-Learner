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

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLClassDescriptionEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrame;
import org.protege.editor.owl.ui.frame.InheritedAnonymousClassesFrameSection;
import org.protege.editor.owl.ui.frame.OWLClassAssertionAxiomIndividualSection;
import org.protege.editor.owl.ui.frame.OWLDisjointClassesAxiomFrameSection;
import org.protege.editor.owl.ui.frame.OWLEquivalentClassesAxiomFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRowObjectEditor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;

/**
 * This class manages the list of the lists for equivalent classes and so on.
 * This is necessary to implement the dllearner plugin in the 
 * OWLClassDescriptionEditor. 
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ButtonList extends AbstractOWLFrame<OWLClass> {
	private final OWLEquivalentClassesAxiomFrameSection equi;
	private final OWLSubClassAxiomFrameSection sub;
	private final DLLearnerView view;
	/**
	 * Constructor of the Buttonlist. 
	 * 
	 * @param editorKit EditorKit from Protege
	 */
	public ButtonList(OWLEditorKit editorKit) {

		super(editorKit.getModelManager().getOWLOntologyManager());
		equi = new OWLEquivalentClassesAxiomFrameSection(editorKit, this);
		view = new DLLearnerView(equi.getName(), editorKit);
		OWLFrameSectionRowObjectEditor<OWLDescription> edit = equi.getObjectEditor();
		OWLClassDescriptionEditor ed = (OWLClassDescriptionEditor)edit;
		ed.addPanel(view);
		//equi.getEditor().getEditorComponent().validate();
		sub = new OWLSubClassAxiomFrameSection(editorKit, this);
		OWLFrameSectionRowObjectEditor<OWLDescription> editor = sub.getObjectEditor();
		OWLClassDescriptionEditor edi = (OWLClassDescriptionEditor)editor;
		edi.addPanel(view);
		// own OWLEquivalentClassesAxiomFrameSection to add the dllearner plugin
		// to the
		// OWLClassDescritpionEditor
		addSection(equi);
		// own OWLEquivalentClassesAxiomFrameSection to add the dllearner plugin
		// to the
		// OWLClassDescritpionEditor
		addSection(sub);
		addSection(new InheritedAnonymousClassesFrameSection(editorKit, this));
		addSection(new OWLClassAssertionAxiomIndividualSection(editorKit, this));
		addSection(new OWLDisjointClassesAxiomFrameSection(editorKit, this));
	}
}
