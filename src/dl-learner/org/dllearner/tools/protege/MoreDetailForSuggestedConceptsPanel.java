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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Set;

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
	 
	private JScrollPane posCoveredScroll;
	private JScrollPane posNotCoveredScroll;
	private JScrollPane negCoveredScroll;
	private JScrollPane negNotCoveredScroll;
	 // Evaluated description of the selected concept
	private JPanel conceptPanel;
	private JPanel accuracyPanel;
	private JPanel posCoveredPanel;
	private JPanel posNotCoveredPanel;
	private JPanel negCoveredPanel;
	private JPanel negNotCoveredPanel;
	private EvaluatedDescription eval;
	private JTextArea concept;
	private Set<String> ontologiesStrings;
	private JTextArea conceptText;
	private final Color colorRed = new Color(139, 0, 0);
	private final Color colorGreen = new Color(0, 139, 0);
	/**
	 * This is the constructor for the Panel.
	 * @param model DLLearnerModel
	 */
	public MoreDetailForSuggestedConceptsPanel(DLLearnerModel model) {
		super();
		setLayout(null);
		setPreferredSize(new Dimension(600, 500));
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
		unsetEverything();
		posCoveredScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		posCoveredScroll.setBounds(5, 150, 280, 140);
		posNotCoveredScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		posNotCoveredScroll.setBounds(300, 150, 280, 140);
		negCoveredScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		negCoveredScroll.setBounds(5, 325, 280, 140);
		negNotCoveredScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		negNotCoveredScroll.setBounds(300, 325, 280, 140);
		eval = desc;
		concept = new JTextArea("Class Description:");
		concept.setEditable(false);
		coveredPositiveExamples = new JLabel("Covered Positive Examples:");
		coveredPositiveExamples.setForeground(colorGreen);
		coveredPositiveExamples.setBounds(5, 110, 280, 30);
		notCoveredPositiveExamples = new JLabel("Not Covered Positive Examples");
		notCoveredPositiveExamples.setForeground(colorRed);
		notCoveredPositiveExamples.setBounds(300, 110, 280, 30);
		coveredNegativeExamples = new JLabel("Covered Negative Examples:");
		coveredNegativeExamples.setForeground(colorRed);
		coveredNegativeExamples.setBounds(5, 295, 280, 30);
		notCoveredNegativeExamples = new JLabel("Not Covered Negative Examples");
		notCoveredNegativeExamples.setForeground(colorGreen);
		notCoveredNegativeExamples.setBounds(300, 295, 280, 30);
		
		conceptPanel = new JPanel(new GridLayout(0, 1));
		conceptPanel.setBounds(5, 0, 600, 50);
		accuracyPanel = new JPanel(new GridLayout(0, 1));
		accuracyPanel.setBounds(5, 60, 600, 50);
		
		posCoveredPanel = new JPanel(new GridLayout(0, 1));
		posNotCoveredPanel = new JPanel(new GridLayout(0, 1));
		negCoveredPanel = new JPanel(new GridLayout(0, 1));
		negNotCoveredPanel = new JPanel(new GridLayout(0, 1));
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
		//this method adds the informations for the selected concept to the panel
		setInformation();
		detailPopup = new JDialog();
		detailPopup.setSize(600, 500);
		 //window will be disposed if the x button is pressed
		detailPopup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		detailPopup.setVisible(true);
		detailPopup.setResizable(false);
		//adds all information to the example panel
		conceptPanel.add(concept);
		conceptPanel.add(conceptText);

		accuracyPanel.add(accuracy);
		accuracyPanel.add(accuracyText);

		posCoveredScroll.setViewportView(posCoveredPanel);
		posNotCoveredScroll.setViewportView(posNotCoveredPanel);
		negCoveredScroll.setViewportView(negCoveredPanel);
		negNotCoveredScroll.setViewportView(negNotCoveredPanel);
		
		add(conceptPanel);
		add(accuracyPanel);
		add(coveredPositiveExamples);
		add(notCoveredPositiveExamples);
		add(coveredNegativeExamples);
		add(notCoveredNegativeExamples);
		add(posCoveredScroll);
		add(posNotCoveredScroll);
		add(negCoveredScroll);
		add(negNotCoveredScroll);
		detailPopup.add(this);
	}
	
	private void unsetEverything() {
		removeAll();
	}
	/**
	 * This method sets the Informations of the selected description.
	 */
	private void setInformation() {
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
			double acc = (eval.getAccuracy())*100;
			accuracyText.setText(String.valueOf(acc)+"%");
			
			//Sets positive Covered Examples for the detail panel
			Set<Individual> indi = eval.getCoveredPositives();
			for(Individual ind : indi) {
				for(String ontology : ontologiesStrings) {
					if(ind.toString().contains(ontology)) {
						JLabel posLabel = new JLabel(ind.toManchesterSyntaxString(ontology, null));
						posLabel.setForeground(colorGreen);
						posCoveredPanel.add(posLabel);
						break;
					} 
				}
			}
				
				
			
			//sets the positive examples that are not covered
			Set<Individual> individuals = eval.getNotCoveredPositives();
			for(Individual ind : individuals) {
				for(String onto : ontologiesStrings) {
					if(ind.toString().contains(onto)) {
						JLabel posLabel = new JLabel(ind.toManchesterSyntaxString(onto, null));
						posLabel.setForeground(colorRed);
						posNotCoveredPanel.add(posLabel);
						break;
					}
				}
			}


			//sets the negative examples that are covered
			Set<Individual> negCoveredIndi = eval.getCoveredNegatives(); 
			for(Individual negIndi : negCoveredIndi) {
				for(String ont : ontologiesStrings) {
					if(negIndi.toString().contains(ont)) {
						JLabel posLabel = new JLabel(negIndi.toManchesterSyntaxString(ont, null));
						posLabel.setForeground(colorRed);
						negCoveredPanel.add(posLabel);
						break;
					}
				}
			}	

			//sets the negative examples that are not covered
			Set<Individual> negNotCoveredIndi = eval.getNotCoveredNegatives();
			for(Individual negNotIndi : negNotCoveredIndi) {
				for(String ontol : ontologiesStrings) {
					if(negNotIndi.toString().contains(ontol)) {
						JLabel posLabel = new JLabel(negNotIndi.toManchesterSyntaxString(ontol, null));
						posLabel.setForeground(colorGreen);
						negNotCoveredPanel.add(posLabel);
						break;
					}
				} 	
			}
		}
		}
	}	
