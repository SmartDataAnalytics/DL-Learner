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

import java.util.Set;

import javax.swing.JComponent;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.ui.editor.AbstractOWLDescriptionEditor;
import org.semanticweb.owl.model.OWLDescription;

/**
 * This is the class that must be implemented to get the plugin integrated in
 * protege.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ProtegePlugin extends AbstractOWLDescriptionEditor {
	private static final long serialVersionUID = 728362819273927L;
	private DLLearnerView view;

	
	@Override
	public JComponent getComponent() {
		return view.getLearnerView();
	}

	@Override
	public Set<OWLDescription> getDescriptions() {
		return view.getSolutions();
	}

	@Override
	public boolean isValidInput() {
		view.makeView();
		return true;
	}

	@Override
	public boolean setDescription(OWLDescription arg0) {
		return true;
	}

	@Override
	public void initialise() throws Exception {
		view = new DLLearnerView("equivalent class", super.getOWLEditorKit());
		view.makeView();
	}

	@Override
	public void dispose() throws Exception {
		view.dispose();
		view = null;
	}

	@Override
	public void addStatusChangedListener(
			InputVerificationStatusChangedListener arg0) {
	}

	@Override
	public void removeStatusChangedListener(
			InputVerificationStatusChangedListener arg0) {
	}




}