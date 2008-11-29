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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;
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

import org.apache.log4j.Logger;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.Description;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owl.model.OWLOntology;

/**
 * This class processes input from the user.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ActionHandler implements ActionListener, ItemListener,
		MouseListener, ListSelectionListener, ListDataListener {

	// This is the DLLearnerModel.

	private static Logger logger = Logger.getLogger(ActionHandler.class);
	
	private DLLearnerModel model;
	private OWLEditorKit editorKit;

	// This is the id that checks if the equivalent class or subclass button is
	// pressed in protege
	private String id;
	// this is a boolean that checked if the advanced button was pressed or not.
	private boolean toggled;
	// This is the Tread of the DL-Learner
	private EvaluatedDescription evaluatedDescription;
	// This is the view of the DL-Learner tab.
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	private Timer timer;
	private LearningAlgorithm la;
	private SuggestionRetriever retriever;

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
	 * @param editor
	 *            OWLEditorKit
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
	 * 
	 * @param z
	 *            ActionEvent
	 */
	public void actionPerformed(ActionEvent z) {

		if (z.getActionCommand().equals(id)) {
			if (model.getAlreadyLearned()) {
				model.unsetListModel();
			}
			model.setKnowledgeSource();
			model.setReasoner();
			model.setPositiveAndNegativeExamples();
			model.setLearningProblem();
			model.setLearningAlgorithm();
			view.getRunButton().setEnabled(false);
			view.renderErrorMessage("learning started");
			view.getPosAndNegSelectPanel().setCheckBoxesEnable(false);
			retriever = new SuggestionRetriever();
			//
			// dlLearner.start();
			retriever.execute();

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
			String message = "class description added";
			view.renderErrorMessage(message);
			view.updateWindow();
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
		EvaluatedDescription eDescription = null;
		
		if (view.getSuggestClassPanel().getSuggestList().getSelectedValue() != null) {
			SuggestListItem item = (SuggestListItem) view
					.getSuggestClassPanel().getSuggestList().getSelectedValue();
			String desc = item.getValue();
			if (model.getEvaluatedDescriptionList() != null) {
				for (Iterator<EvaluatedDescription> i = model
						.getEvaluatedDescriptionList().iterator(); i.hasNext();) {
					eDescription = i.next();
					if (desc.equals(eDescription.getDescription()
							.toManchesterSyntaxString(
									editorKit.getModelManager()
											.getActiveOntology().getURI().toString()
											, null))) {
						evaluatedDescription = eDescription;
						
						break;
					}

				}
			}

			if (m.getClickCount() == 2) {
				view.getMoreDetailForSuggestedConceptsPanel()
						.renderDetailPanel(evaluatedDescription);
			}
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
		//dlLearner = null;
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
 * @author Christian Koetteritzsch
 *
 */
	class SuggestionRetriever extends
			SwingWorker<List<EvaluatedDescription>, List<EvaluatedDescription>> {
		
		private Thread dlLearner;
		private DefaultListModel dm = new DefaultListModel();
		Logger logger = Logger.getLogger(SuggestionRetriever.class);
		Logger rootLogger = Logger.getRootLogger();
		
		@SuppressWarnings("unchecked")
		@Override
		protected List<EvaluatedDescription> doInBackground() throws Exception {
			la = model.getLearningAlgorithm();
			timer = new Timer();
			timer.schedule(new TimerTask(){
				
				@Override
				public void run() {
					System.out.println("DA BIN ICH:");	
					if (la != null) {
							
							//System.out.println("EVAL: " + la.getCurrentlyBestEvaluatedDescriptions().isEmpty());
							//System.out.println("SIZE: " + la.getCurrentlyBestEvaluatedDescriptions().size());
						publish(la.getCurrentlyBestEvaluatedDescriptions(view.getPosAndNegSelectPanel().getOptionPanel().getNrOfConcepts()
								, view.getPosAndNegSelectPanel().getOptionPanel().getMinAccuracy(), true));
					}
				}

			}, 0, 1000);

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
			List<EvaluatedDescription> result = la.getCurrentlyBestEvaluatedDescriptions(view.getPosAndNegSelectPanel().getOptionPanel().getNrOfConcepts()
					, view.getPosAndNegSelectPanel().getOptionPanel().getMinAccuracy(), true);
			
			return result;
		}

		@Override
		public void done() {

			timer.cancel();
			List<EvaluatedDescription> result = null;
			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			view.getRunButton().setEnabled(true);
			System.out.println("DONE");
			updateList(result);
		}

		@Override
		protected void process(List<List<EvaluatedDescription>> resultLists) {


			for (List<EvaluatedDescription> list : resultLists) {
				updateList(list);
			}
		}

		private void updateList(final List<EvaluatedDescription> result) {

			logger.debug("update list with " + result);
			
			Runnable doUpdateList = new Runnable() {

				

				public void run() {
					System.out.println("JETZT HIER:");
					model.setSuggestList(result);
					// learnPanel.getListModel().clear();
					Iterator<EvaluatedDescription> it = result.iterator();
					int i = 0;
					while (it.hasNext()) {
						Iterator<OWLOntology> ont = model.getOWLEditorKit().getModelManager().getActiveOntologies().iterator();
						EvaluatedDescription eval = it.next();
						while(ont.hasNext()) {
							String onto = ont.next().getURI().toString();
							
							if(eval.getDescription().toString().contains(onto)) {
								if(model.isConsistent(eval)) {
									dm.add(i, new SuggestListItem(Color.GREEN, eval.getDescription().toManchesterSyntaxString(onto, null)));
									i++;
									break;
								} else {
									dm.add(i, new SuggestListItem(Color.RED, eval.getDescription().toManchesterSyntaxString(onto, null)));
									i++;
									break;
								}
							}
						}
					}
					System.out.println("NAJA NUN HIER");
					view.getSuggestClassPanel().setSuggestList(dm);
				}
			};
			SwingUtilities.invokeLater(doUpdateList);

		}

	}

}
