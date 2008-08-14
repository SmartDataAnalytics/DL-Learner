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

import org.protege.editor.owl.ui.frame.AbstractOWLFrame;
import org.protege.editor.owl.ui.frame.InheritedAnonymousClassesFrameSection;
import org.protege.editor.owl.ui.frame.OWLClassAssertionAxiomIndividualSection;
import org.protege.editor.owl.ui.frame.OWLDisjointClassesAxiomFrameSection;
import org.semanticweb.owl.model.OWLClass;
import org.protege.editor.owl.OWLEditorKit;

/**
 * This class manages the list of the lists for equivalent classes and so on.
 * This is necessary to implement the dllearner plugin in the 
 * OWLClassDescriptionEditor. 
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ButtonList extends AbstractOWLFrame<OWLClass> {
	/**
	 * Construktor of the Buttonlist. 
	 * 
	 * @param editorKit EditorKit from Protege
	 */
	public ButtonList(OWLEditorKit editorKit) {
		super(editorKit.getOWLModelManager().getOWLOntologyManager());
		// own OWLEquivalentClassesAxiomFrameSection to add the dllearner plugin
		// to the
		// OWLClassDescritpionEditor
		addSection(new OWLEquivalentClassesAxiomFrameSection(editorKit, this));
		// own OWLEquivalentClassesAxiomFrameSection to add the dllearner plugin
		// to the
		// OWLClassDescritpionEditor
		addSection(new OWLSubClassAxiomFrameSection(editorKit, this));
		addSection(new InheritedAnonymousClassesFrameSection(editorKit, this));
		addSection(new OWLClassAssertionAxiomIndividualSection(editorKit, this));
		addSection(new OWLDisjointClassesAxiomFrameSection(editorKit, this));
	}
}
