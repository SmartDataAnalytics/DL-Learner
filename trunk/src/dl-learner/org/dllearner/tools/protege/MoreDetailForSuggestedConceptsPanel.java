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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.dllearner.algorithms.EvaluatedDescriptionClass;
import org.dllearner.core.EvaluatedDescription;



/**
 * This class shows more details of the suggested concepts. It shows the positive and negative examples
 * that are covered and that are not covered by the suggested concepts. It also shows the accuracy of the 
 * selected concept.
 * @author Christian Koetteritzsch
 *
 */

public class MoreDetailForSuggestedConceptsPanel extends JPanel {

	private static final long serialVersionUID = 785272797932584581L;
	
	 // Model of the dllearner
	 
	private final DLLearnerModel model;
	
	 // Textarea to render the accuracy of the concept
	 
	private final  JTextArea accuracy;

	 
	private final  JTextArea accuracyText;
	 // Evaluated description of the selected concept
	private final  JPanel conceptPanel;

	private EvaluatedDescription eval;
	private final  JTextArea concept;
	private Set<String> ontologiesStrings;
	private final  JTextArea conceptText;
	private static final int HEIGHT = 350;
	private static final int WIDTH = 600;
	private GraphicalCoveragePanel p;
	private final MoreDetailForSuggestedConceptsPanelHandler handler;

	/**
	 * This is the constructor for the Panel.
	 * @param model DLLearnerModel
	 */
	public MoreDetailForSuggestedConceptsPanel(DLLearnerModel model) {
		super();
		setLayout(null);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.model = model;
		handler = new MoreDetailForSuggestedConceptsPanelHandler(this);
		concept = new JTextArea("Class Description:");
		
		concept.setEditable(false);
		
		
		conceptPanel = new JPanel(new GridLayout(0, 2));
		conceptPanel.setBounds(5, 0, 800, 50);

		accuracy = new JTextArea("Accuracy:");
		accuracy.setEditable(false);
		conceptText = new JTextArea();
		conceptText.setEditable(false);

		accuracyText = new JTextArea();
		//sets accuracy text area not editable
		accuracyText.setEditable(false);
		accuracy.setVisible(false);
		accuracyText.setVisible(false);
		concept.setVisible(false);
		conceptText.setVisible(false);

	}

	/**
	 * This method renders the output for the detail panel.
	 * @param desc selected description
	 */
	public void renderDetailPanel(EvaluatedDescription desc) {
		accuracy.setVisible(false);
		accuracyText.setVisible(false);
		concept.setVisible(false);
		conceptText.setVisible(false);
		eval = desc;

		//panel for the informations of the selected concept
		//this method adds the informations for the selected concept to the panel
		this.setInformation();
		p = new GraphicalCoveragePanel(eval, model, conceptText.getText(), this);
		p.setBounds(5, 0, 600, 350);
		//adds all information to the example panel
		unsetEverything();
		conceptPanel.removeAll();
		conceptPanel.add(concept);
		conceptPanel.add(accuracy);
		conceptPanel.add(conceptText);
		conceptPanel.add(accuracyText);
		conceptPanel.setVisible(true);
		//this.add(conceptPanel);
		this.add(p);
		this.addPropertyChangeListener(handler);
		//conceptPanel.addPropertyChangeListener(handler);
		this.repaint();
	}

	private void unsetEverything() {
		removeAll();
	}
	/**
	 * This method sets the Informations of the selected description.
	 */
	public void setInformation() {
		ontologiesStrings = model.getOntologyURIString();
		if(eval!=null) {
			//sets the accuracy of the selected concept
			for(String ontoString : ontologiesStrings) {
				if(eval.getDescription().toString().contains(ontoString)) {
					conceptText.setText(eval.getDescription().toManchesterSyntaxString(ontoString, null));
					break;
				}
			}
			
			//sets the accuracy of the concept
			double acc = ((EvaluatedDescriptionClass) eval).getAccuracy()*100;
			accuracyText.setText(String.valueOf(acc)+"%");
			}
		accuracy.setVisible(true);
		accuracyText.setVisible(true);
		concept.setVisible(true);
		conceptText.setVisible(true);
		}


	public GraphicalCoveragePanel getGraphicalCoveragePanel() {
		return p;
	}
	public JPanel getConceptPanel() {
		return conceptPanel;
	}
	public void unsetPanel() {
		unsetEverything();
		conceptPanel.removeAll();
		accuracy.setVisible(false);
		accuracyText.setVisible(false);
		concept.setVisible(false);
		conceptText.setVisible(false);
		if(p != null) {
			p.unsetPanel();
		}
		conceptPanel.add(concept);
		conceptPanel.add(accuracy);
		conceptPanel.add(conceptText);
		conceptPanel.add(accuracyText);
		conceptPanel.setVisible(false);
		this.add(conceptPanel);

		repaint();
	}
	
}