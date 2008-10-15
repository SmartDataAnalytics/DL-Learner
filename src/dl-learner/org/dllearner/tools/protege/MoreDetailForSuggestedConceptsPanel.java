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
import java.awt.GridLayout;
import java.awt.Color;

import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;
/**
 * This class shows more details of the suggested concepts. It shows the positive and negative examples
 * that are covered and that are not covered by the suggested concepts. It also shows the accuracya of the 
 * selected concept.
 * @author Christian Koetteritzsch
 *
 */
public class MoreDetailForSuggestedConceptsPanel extends JPanel {

	private static final long serialVersionUID = 785272797932584581L;
	
	 // Model of the dllearner
	 
	private DLLearnerModel model;
	
	 // Textarea to render the accuracy of the concept
	 
	private JTextArea accuracy;
	
	 // Label for the positive examples that are covered by the concept
	 
	private JLabel coveredPositiveExamples;
	
	 // Label for the negative examples that are covered by the concept
	 
	private JLabel coveredNegativeExamples;
	
	 // Label for the positive examples that are not covered by the concept
	 
	private JLabel notCoveredPositiveExamples;
	
	 // Label for the negative examples that are not covered by the concept
	 
	private JLabel notCoveredNegativeExamples;
	
	 // Pop up panel for the informations of the selected concept
	 
	private JDialog detailPopup;
	
	 // Panel where the informations of the selected panel are rendered
	 
	private JPanel examplePanel;
	
	 // Text area that shows the covered positive examples
	 
	private JTextArea posCoveredText;
	
	 // Text area that shows the positive examples that are not covered by the selected concept
	 
	private JTextArea posNotCoveredText;
	
	 // Text area that shows the covered negative examples
	 
	private JTextArea negCoveredText;
	
	 // Text area that shows the negative examples that are not covered by the selected concept 
	 
	private JTextArea negNotCoveredText;
	
	 // Text area that shows the accurcy of the selected concept
	 
	private JTextArea accuracyText;
	
	 // Scroll pane if scroll bar is necessary to show all covered examples
	 
	private JScrollPane detailScroll;
	
	 // Evaluated description of the selected concept
	
	private EvaluatedDescription eval;
	private final Color colorRed = Color.red;
	private JTextArea concept;
	private JTextArea conceptText;
	private final Color colorGreen = Color.green;
	/**
	 * This is the constructor for the Panel.
	 * @param model DLLearnerModel
	 */
	public MoreDetailForSuggestedConceptsPanel(DLLearnerModel model) {
		this.model = model;
	}
	/**
	 * This method returns the Detail Panel.
	 * @return DetailPanel
	 */
	public JDialog getMoreDialog() {
		return detailPopup;
	}
	

	/**
	 * This method renders the output for the detail panel.
	 * @param desc selected description
	 */
	public void renderDetailPanel(EvaluatedDescription desc) {
		eval = desc;
	
		accuracy = new JTextArea("Accuracy:");
		accuracy.setEditable(false);
		conceptText = new JTextArea();
		conceptText.setEditable(false);
		posCoveredText = new JTextArea();
		posCoveredText.setForeground(colorGreen);
		//sets covered positive examples text area not editable
		posCoveredText.setEditable(false);
		posNotCoveredText = new JTextArea();
		posNotCoveredText.setForeground(colorRed);
		//sets not covered positive examples text area not editable
		posNotCoveredText.setEditable(false);
		negCoveredText = new JTextArea();
		negCoveredText.setForeground(colorRed);
		//sets covered negative examples text area not editable
		negCoveredText.setEditable(false);
		negNotCoveredText = new JTextArea();
		negNotCoveredText.setForeground(colorGreen);
		//sets not covered negative examples text area not editable
		negNotCoveredText.setEditable(false);
		accuracyText = new JTextArea();
		//sets accuracy text area not editable
		accuracyText.setEditable(false);
		//panel for the informations of the selected concept
		examplePanel = new JPanel(new GridLayout(0, 2));
		//this method adds the informations for the selected concept to the panel
		setInformation();
		concept = new JTextArea("Concept:");
		concept.setEditable(false);
		coveredPositiveExamples = new JLabel("Covered Positive Examples:");
		coveredPositiveExamples.setForeground(colorGreen);
		notCoveredPositiveExamples = new JLabel("Not Covered Positive Examples");
		notCoveredPositiveExamples.setForeground(colorRed);
		coveredNegativeExamples = new JLabel("Covered Negative Examples:");
		coveredNegativeExamples.setForeground(colorRed);
		notCoveredNegativeExamples = new JLabel("Not Covered Negative Examples");
		notCoveredNegativeExamples.setForeground(colorGreen);
		detailPopup = new JDialog();
		detailPopup.setSize(400, 400);
		 //window will be disposed if the x button is pressed
		detailPopup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		detailPopup.setVisible(true);
		detailPopup.setResizable(false);
		detailScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//adds all information to the example panel
		examplePanel.add(concept);
		examplePanel.add(conceptText);
		examplePanel.add(accuracy);
		examplePanel.add(accuracyText);
		examplePanel.add(coveredPositiveExamples);
		examplePanel.add(notCoveredPositiveExamples);
		examplePanel.add(posCoveredText);
		examplePanel.add(posNotCoveredText);
		examplePanel.add(coveredNegativeExamples);
		examplePanel.add(notCoveredNegativeExamples);
		examplePanel.add(negCoveredText);
		examplePanel.add(negNotCoveredText);
		detailScroll.setViewportView(examplePanel);
		detailPopup.add(detailScroll);
	}
	/**
	 * This method sets the Informations of the selected description.
	 */
	private void setInformation() {
		if(eval!=null) {
			//sets the accuracy of the selected concept
			conceptText.append(eval.getDescription().toManchesterSyntaxString(model.getURI().toString()+"#",null));
			double acc = (eval.getAccuracy())*100;
			accuracyText.append(String.valueOf(acc)+"%");
			//sets the positive examples that are covered
			for(Iterator<Individual> i = eval.getCoveredPositives().iterator(); i.hasNext();) {
				posCoveredText.append(i.next().toManchesterSyntaxString(model.getURI().toString()+"#", null)+"\n");
			}
			//sets the positive examples that are not covered
			for(Iterator<Individual> i = eval.getNotCoveredPositives().iterator(); i.hasNext();) {
				posNotCoveredText.append(i.next().toManchesterSyntaxString(model.getURI().toString()+"#", null)+"\n");
			}
			//sets the negative examples that are covered
			for(Iterator<Individual> i = eval.getCoveredNegatives().iterator(); i.hasNext();) {
				negCoveredText.append(i.next().toManchesterSyntaxString(model.getURI().toString()+"#", null)+"\n");
			}
			//sets the negative examples that are not covered
			for(Iterator<Individual> i = eval.getNotCoveredNegatives().iterator(); i.hasNext();) {
				negNotCoveredText.append(i.next().toManchesterSyntaxString(model.getURI().toString()+"#", null)+"\n");
			}
		}
	}
}
