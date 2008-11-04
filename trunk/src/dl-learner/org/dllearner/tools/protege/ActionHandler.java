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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.protege.editor.owl.OWLEditorKit;

/**
 * This class  processes input from the user. 
 * @author Christian Koetteritzsch
 * 
 */
public class ActionHandler implements ActionListener, ItemListener,
		MouseListener, ListSelectionListener, ListDataListener {

	// This is the DLLearnerModel.

	private DLLearnerModel model;
	private OWLEditorKit editorKit;

	// This is the id that checks if the equivalent class or subclass button is
	// pressed in protege
	private String id;
	// this is a boolean that checked if the advanced button was pressed or not.
	private boolean toggled;
	// This is the Tread of the DL-Learner
	private Thread dlLearner;
	private EvaluatedDescription evaluatedDescription;
	// This is the view of the DL-Learner tab.
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;

	/**
	 * This is the constructor for the action handler.
	 * 
	 * @param a
	 *            ActionHandler
	 * @param m
	 *            DLLearnerModel
	 * @param view
	 *            DLlearner tab
	 * @param i
	 *            id if it is a subclass or an equivalent class
	 * @param editor OWLEditorKit
	 */
	public ActionHandler(ActionHandler a, DLLearnerModel m,
			OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view,
			String i, OWLEditorKit editor) {
		this.editorKit = editor;
		this.view = view;
		this.id = i;
		this.model = m;
		toggled = false;

	}

	/**
	 * When a Button is pressed this method select the right.
	 * @param z ActionEvent 
	 */
	public void actionPerformed(ActionEvent z) {

			
		if (z.getActionCommand().equals("Suggest " + id)) {
			if (model.getAlreadyLearned()) {
				model.unsetListModel();
			}
			if (view.getPosAndNegSelectPanel().getPosAndNegSelectPanel()
					.getComponentCount() <= 2) {
				view
						.renderErrorMessage("Could not start learning. No Examples where available");
			} else {
				view.getPosAndNegSelectPanel().setCheckBoxesEnable(false);
				model.setKnowledgeSource();
				model.setReasoner();
				model.setPositiveAndNegativeExamples();
				model.setLearningProblem();
				model.setLearningAlgorithm();
				this.dlLearner = new Thread(model);
				dlLearner.start();
				view.getRunButton().setEnabled(false);
				view.renderErrorMessage("Learning started");
				//view.getPosAndNegSelectPanel().unsetCheckBoxes();
			}
		}

		if (z.getActionCommand().equals("ADD")) {
			if (evaluatedDescription != null) {
				model.changeDLLearnerDescriptionsToOWLDescriptions(evaluatedDescription.getDescription());
			} else {
				model.changeDLLearnerDescriptionsToOWLDescriptions((Description) view.getSuggestClassPanel().getSuggestList().getSelectedValue());
			}
			String message = "Concept added";
			view.renderErrorMessage(message);
			view.updateWindow();
		}

		if (z.getActionCommand().equals("?")) {
			if (z.getSource().toString().contains("PosHelpButton")) {
				String help = "An individual that should be an instance of the learned class description.\n"
					+"Per Default all that belongs to the class.";
				view.getPosAndNegSelectPanel().renderHelpMessage(help);
			}

			if (z.getSource().toString().contains("NegHelpButton")) {
				String help = "A Instance tht doesn't follow from the classdescription.";
				view.getPosAndNegSelectPanel().renderHelpMessage(help);
			}

		}
		if (z.getActionCommand().equals("")) {
			if (!toggled) {
				toggled = true;
				view.setIconToggled(toggled);
				view.setExamplePanelVisible(toggled);
			} else {
				toggled = false;
				view.setIconToggled(toggled);
				view.setExamplePanelVisible(toggled);
			}
		}
		if (z.getActionCommand().equals("Why")) {
			view.getMoreDetailForSuggestedConceptsPanel().renderDetailPanel(
					evaluatedDescription);
		}
	}

	/**
	 * 
	 * @return id StringID if it is a Subclass or an equivalent class.
	 */
	public String getID() {
		return id;
	}

	
	/**
	 * select/deselect the Check boxes.
	 * @param i ItemEvent
	 */
	public void itemStateChanged(ItemEvent i) {
		if (i.getItem().toString().contains("Positive")) {
			for (int j = 0; j < model.getPosVector().size(); j++) {
				if (i.getItem().toString().contains(
						model.getPosVector().get(j).getText().toString())) {
					if (!model.getPosVector().get(j).isSelected()) {
						model.getPosVector().get(j).setSelected(true);
						break;
					}
					if (model.getPosVector().get(j).isSelected()) {
						model.getPosVector().get(j).setSelected(false);
						break;
					}
				}
			}
		}
		if (i.getItem().toString().contains("Negative")) {
			for (int j = 0; j < model.getNegVector().size(); j++) {
				if (i.getItem().toString().contains(
						model.getNegVector().get(j).getText().toString())) {
					if (!model.getNegVector().get(j).isSelected()) {
						model.getNegVector().get(j).setSelected(true);
						break;
					}
					if (model.getNegVector().get(j).isSelected()) {
						model.getNegVector().get(j).setSelected(false);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Nothing happens here.
	 * @param e ListSelectionEvent 
	 */
	public void valueChanged(ListSelectionEvent e) {

	}

	/**
	 * Nothing happens here.
	 * @param m MouseEvent
	 */
	public void mouseReleased(MouseEvent m) {

	}

 	/**
	 * Nothing happens here.
	 * @param m MouseEvent
	 */
	public void mouseEntered(MouseEvent m) {

	}

	/**
	 * Choses the right EvaluatedDescription object after a concept is chosen in the list.
	 * @param m MouseEvent
	 */
	public void mouseClicked(MouseEvent m) {
		EvaluatedDescription eDescription = null;
		if (view.getSuggestClassPanel().getSuggestList()
				.getSelectedValue() != null) {
			SuggestListItem item = (SuggestListItem) view.getSuggestClassPanel().getSuggestList()
			.getSelectedValue();
			String desc = item.getValue();
			if (model.getEvaluatedDescriptionList() != null) {
				for (Iterator<EvaluatedDescription> i = model
						.getEvaluatedDescriptionList().iterator(); i.hasNext();) {
					eDescription = i.next();
					if (desc.equals(eDescription.getDescription()
							.toManchesterSyntaxString(
									editorKit.getModelManager().getActiveOntology().getURI()
											+ "#", null))) {
						evaluatedDescription = eDescription;
						break;
					}

				}
			}
		
		
		if(m.getClickCount()==2) {
			view.getMoreDetailForSuggestedConceptsPanel().renderDetailPanel(
					evaluatedDescription);
		}
	} else {
		String message = "No concept to select.";
		view.renderErrorMessage(message);
	}
	}

	/**
	 * Nothing happens here.
	 * @param m MouseEvent
	 */
	public void mouseExited(MouseEvent m) {

	}

	/**
	 * Sets the ADD button enable after a concept is chosen.
	 * @param m MouseEvent
	 */
	public void mousePressed(MouseEvent m) {
		if (view.getSuggestClassPanel().getSuggestList()
				.getSelectedValue()!= null) {
			if (!view.getAddButton().isEnabled()) {
				view.getAddButton().setEnabled(true);
			} 
		} 
	}

	/**
	 * Destroys the Thread after the Pluigin is closed.
	 */
	public void destroyDLLearnerThread() {
		dlLearner = null;
	}

	/**
	 *  Resets the toggled Button after the plugin is closed.
	 */
	public void resetToggled() {
		toggled = false;
	}

	@Override
	public void contentsChanged(ListDataEvent listEvent) {
		System.out.println(listEvent);
		
	}

	@Override
	public void intervalAdded(ListDataEvent listEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void intervalRemoved(ListDataEvent listEvent) {
		// TODO Auto-generated method stub
		
	}

}
