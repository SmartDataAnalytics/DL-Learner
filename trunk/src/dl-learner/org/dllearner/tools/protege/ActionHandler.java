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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;

/**
 * This class processes input from the user.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ActionHandler implements ActionListener, ItemListener,
		MouseListener, ListSelectionListener, ListDataListener {

	// This is the DLLearnerModel.

	private final DLLearnerModel model;

	// This is the id that checks if the equivalent class or subclass button is
	// pressed in protege
	// this is a boolean that checked if the advanced button was pressed or not.
	private boolean toggled;
	// This is the Tread of the DL-Learner
	private EvaluatedDescription evaluatedDescription;
	// This is the view of the DL-Learner tab.
	private Timer timer;
	private LearningAlgorithm la;
	private SuggestionRetriever retriever;
	private final Color colorRed = new Color(139, 0, 0);
	private final Color colorGreen = new Color(0, 139, 0);
	private final DLLearnerView view;

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
	 * 
	 */
	public ActionHandler(DLLearnerModel m, DLLearnerView view) {
		this.view = view;
		this.model = m;
		toggled = false;

	}

	/**
	 * When a Button is pressed this method select the right.
	 * 
	 * @param z
	 *            ActionEvent
	 */
	public void actionPerformed(ActionEvent z) {

		if (z.getActionCommand().equals("suggest equivalent class expression") || z.getActionCommand().equals("suggest super class expression")) {
			model.setKnowledgeSource();
			model.setReasoner();
			model.setLearningProblem();
			model.setLearningAlgorithm();
			view.getRunButton().setEnabled(false);
			view.renderErrorMessage("learning\nstarted");
			retriever = new SuggestionRetriever();
			retriever.execute();
			// model.setCurrentConcept(null);

		}

		if (z.getActionCommand().equals("ADD")) {
			if (evaluatedDescription != null) {
				model
						.changeDLLearnerDescriptionsToOWLDescriptions(evaluatedDescription
								.getDescription());
			} else {
				model
						.changeDLLearnerDescriptionsToOWLDescriptions((Description) view
								.getSuggestClassPanel().getSuggestList()
								.getSelectedValue());
			}
			String message = "class expression\nadded";
			view.renderErrorMessage(message);
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
	}


	/**
	 * select/deselect the Check boxes.
	 * 
	 * @param i
	 *            ItemEvent
	 */
	public void itemStateChanged(ItemEvent i) {

	}

	/**
	 * Nothing happens here.
	 * 
	 * @param e
	 *            ListSelectionEvent
	 */
	public void valueChanged(ListSelectionEvent e) {

	}

	/**
	 * Nothing happens here.
	 * 
	 * @param m
	 *            MouseEvent
	 */
	public void mouseReleased(MouseEvent m) {

	}

	/**
	 * Nothing happens here.
	 * 
	 * @param m
	 *            MouseEvent
	 */
	public void mouseEntered(MouseEvent m) {

	}

	/**
	 * Choses the right EvaluatedDescription object after a concept is chosen in
	 * the list.
	 * 
	 * @param m
	 *            MouseEvent
	 */
	public void mouseClicked(MouseEvent m) {
		if (view.getSuggestClassPanel().getSuggestList().getSelectedValue() != null) {
			SuggestListItem item = (SuggestListItem) view
					.getSuggestClassPanel().getSuggestList().getSelectedValue();
			String desc = item.getValue();
			if (model.getEvaluatedDescriptionList() != null) {
				List<? extends EvaluatedDescription> evalList = model
						.getEvaluatedDescriptionList();
				Set<String> onto = model.getOntologyURIString();
				for (EvaluatedDescription eDescription : evalList) {
					for (String ont : onto) {
						if (desc.equals(eDescription.getDescription()
								.toManchesterSyntaxString(ont, null))) {
							evaluatedDescription = eDescription;
							break;
						}
					}
				}
			}
			view.getMoreDetailForSuggestedConceptsPanel()
					.renderDetailPanel(evaluatedDescription);
			view.setGraphicalPanel();
			view.getMoreDetailForSuggestedConceptsPanel().repaint();
		}
	}

	/**
	 * Nothing happens here.
	 * 
	 * @param m
	 *            MouseEvent
	 */
	public void mouseExited(MouseEvent m) {

	}

	/**
	 * Sets the ADD button enable after a concept is chosen.
	 * 
	 * @param m
	 *            MouseEvent
	 */
	public void mousePressed(MouseEvent m) {
		if (view.getSuggestClassPanel().getSuggestList().getSelectedValue() != null) {
			if (!view.getAddButton().isEnabled()) {
				view.getAddButton().setEnabled(true);
			}
		}
	}

	/**
	 * Destroys the Thread after the Pluigin is closed.
	 */
	public void destroyDLLearnerThread() {
		// dlLearner = null;
	}

	/**
	 * Resets the toggled Button after the plugin is closed.
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

	/**
	 * Inner Class that retrieves the concepts given by the DL-Learner.
	 * 
	 * @author Christian Koetteritzsch
	 * 
	 */
	class SuggestionRetriever
			extends
			SwingWorker<List<? extends EvaluatedDescription>, List<? extends EvaluatedDescription>> {

		private Thread dlLearner;
		private final DefaultListModel dm = new DefaultListModel();

		@SuppressWarnings("unchecked")
		@Override
		protected List<? extends EvaluatedDescription> doInBackground()
				throws Exception {
			la = model.getLearningAlgorithm();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					if (la != null) {
						publish(la.getCurrentlyBestEvaluatedDescriptions(view
								.getPosAndNegSelectPanel().getOptionPanel()
								.getNrOfConcepts()));
					}
				}

			}, 0, 500);

			dlLearner = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						model.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});
			dlLearner.start();

			try {
				dlLearner.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<? extends EvaluatedDescription> result = la
					.getCurrentlyBestEvaluatedDescriptions(view
							.getPosAndNegSelectPanel().getOptionPanel()
							.getNrOfConcepts());

			return result;
		}

		@Override
		public void done() {

			timer.cancel();
			List<? extends EvaluatedDescription> result = null;
			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			view.algorithmTerminated();
			updateList(result);
		}

		@Override
		protected void process(
				List<List<? extends EvaluatedDescription>> resultLists) {

			for (List<? extends EvaluatedDescription> list : resultLists) {
				updateList(list);
			}
		}

		private void updateList(
				final List<? extends EvaluatedDescription> result) {

			Runnable doUpdateList = new Runnable() {

				public void run() {
					model.setSuggestList(result);
					dm.clear();
					int i = 0;
					for (EvaluatedDescription eval : result) {
						Set<String> ont = model.getOntologyURIString();
						for (String ontology : ont) {
							if (eval.getDescription().toString().contains(
									ontology)) {
								// dm.add(i, new SuggestListItem(colorGreen,
								// eval
								// .getDescription().toManchesterSyntaxString
								// (ontology, null),
								// ((EvaluatedDescriptionClass)
								// eval).getAccuracy()*100));
								if (((EvaluatedDescriptionClass) eval).isConsistent()) {
									dm.add(i, new SuggestListItem(colorGreen,
											eval.getDescription()
													.toManchesterSyntaxString(
															ontology, null),
											((EvaluatedDescriptionClass) eval)
													.getAccuracy() * 100));
									break;
								} else {
									dm.add(i, new SuggestListItem(colorRed,
											eval.getDescription()
													.toManchesterSyntaxString(
															ontology, null),
											((EvaluatedDescriptionClass) eval)
													.getAccuracy() * 100));
									view.setIsInconsistent(true);
									break;
								}
							}
						}
					}
					view.getSuggestClassPanel().setSuggestList(dm);
					view.getLearnerView().repaint();
				}
			};
			SwingUtilities.invokeLater(doUpdateList);

		}

	}

}
