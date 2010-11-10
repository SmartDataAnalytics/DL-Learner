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

import java.util.Collections;
import java.util.Set;

import javax.swing.JComponent;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.model.inference.NoOpReasoner;
import org.protege.editor.owl.ui.editor.AbstractOWLClassExpressionEditor;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * This is the class that must be implemented to get the plugin integrated in
 * protege.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ProtegePlugin extends AbstractOWLClassExpressionEditor {
	private static final long serialVersionUID = 728362819273927L;
	private DLLearnerView view;
	
	@Override
	public JComponent getComponent() {
		return view.getView();
	}

	@Override
	public Set<OWLClassExpression> getClassExpressions() {
		return Collections.emptySet();
	}

	@Override
	public boolean isValidInput() {
		if(getAxiomType() == AxiomType.EQUIVALENT_CLASSES) {
			Manager.getInstance(getOWLEditorKit()).setLearningType(LearningType.EQUIVALENT);
		} else if(getAxiomType() == AxiomType.SUBCLASS_OF) {
			Manager.getInstance(getOWLEditorKit()).setLearningType(LearningType.SUPER);
		}
		view.reset();
		OWLReasoner r = getOWLEditorKit().getModelManager().getReasoner();
		if(r instanceof NoOpReasoner){
			view.setHintMessage("<html><font size=\"3\" color=\"red\">You have to select a reasoner (click on menu \"Reasoner\"). We recommend to use Pellet. </font></html>");
			view.setRunButtonEnabled(false);
		} else {
			if(!Manager.getInstance().isPreparing() && Manager.getInstance().isReinitNecessary()){
				new ReadingOntologyThread(view).start();
			}
		}
		return true;
	}

	@Override
	public boolean setDescription(OWLClassExpression arg0) {
		return true;
	}

	@Override
	public void initialise() throws Exception {
		Manager.getInstance(getOWLEditorKit());
		view = new DLLearnerView(super.getOWLEditorKit());
		Manager.getInstance().setProgressMonitor(view.getStatusBar());
		System.out.println("Initializing DL-Learner plugin...");
		addListeners();
	}

	@Override
	public void dispose() throws Exception {
		view.dispose();
		Manager.getInstance().dispose();
		removeListeners();
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
	
	private void addListeners(){
		getOWLEditorKit().getOWLModelManager().addListener(Manager.getInstance(getOWLEditorKit()));
		getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().addListener(Manager.getInstance(getOWLEditorKit()));
	}
	
	private void removeListeners(){
		getOWLEditorKit().getOWLModelManager().removeListener(Manager.getInstance(getOWLEditorKit()));
		getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().removeListener(Manager.getInstance(getOWLEditorKit()));
	}
}
