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

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;

public class GraphicalCoverageTextField extends JTextPane{

	private static final long serialVersionUID = 8971091768497004453L;
	private static final String EQUI_STRING = "equivalent class";
	private final String id;
	private DLLearnerModel model;
	EvaluatedDescription description;
	private String conceptNew;
	private final JScrollPane textScroll;
	
	public GraphicalCoverageTextField(EvaluatedDescription desc, DLLearnerModel m) {
		this.setContentType("text/html");
		this.setEditable(false);
		this.model = m;
		textScroll = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setBackground(model.getOWLEditorKit().getOWLWorkspace().getOWLComponentFactory().getOWLClassSelectorPanel().getBackground());
		this.id = model.getID();
		this.description = desc;
		
		for(String uri : model.getOntologyURIString()) {
			if(description.getDescription().toString().contains(uri)) {
				conceptNew = description.getDescription().toManchesterSyntaxString(uri, null);
			}
		}
		this.setText();
	}

	private void setText() {
		int coveredInstances = ((EvaluatedDescriptionClass) description).getCoveredInstances().size();
		int allInstances = coveredInstances + ((EvaluatedDescriptionClass) description).getNotCoveredInstances().size();
		int coverage = (int)(((EvaluatedDescriptionClass) description).getCoverage() * 100);
		String text ="<html><p><font size=\"3\" color=\"yellow\">\u25cf</font><font size=\"3\" color=\"black\"> " + model.getOldConceptOWLAPI().toString() + "</font></p>" +
			         "<p style=\"max-width:50px;\"><font size=\"3\" color=\"EE9A00\">\u25cf</font><font size=\"3\" color=\"black\"> " + conceptNew + "</font></p>" +
			         "<p><font size=\"1\" color=\"green\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font> <font size=\"3\" color=\"EE9A00\">\u25cf</font>" + 
		             "<font size=\"3\" color=\"black\"> and </font> <font size=\"3\" color=\"yellow\">\u25cf</font><font size=\"3\" color=\"black\"> (OK)</font></p> ";
		             if(id.equals(EQUI_STRING)) {
		             text += "<p><font size=\"1\" color=\"red\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"EE9A00\">\u25cf</font></font><font size=\"3\" color=\"black\"> (potential problem)</font></p>" + 
		                     "<p><font size=\"1\" color=\"red\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"yellow\">\u25cf</font></font><font size=\"3\" color=\"black\"> (potential problem)</font></p>"; 
		             } else {
		            	 text += "<p><font size=\"1\" color=\"green\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"EE9A00\">\u25cf</font></font><font size=\"3\" color=\"black\"> (no problem)</font></p>" + 
	                     "<p><font size=\"1\" color=\"red\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"yellow\">\u25cf</font></font><font size=\"3\" color=\"black\"> (potential problem)</font></p>";;
		             }
		             text += "<p><font size=\"3\" color=\"black\">Covers " + coveredInstances + 
						" of " + allInstances + "(" + coverage + " %) of class instances</font></p>" +
		             "<p><font size=\"3\" color=\"black\">Covers " + ((EvaluatedDescriptionClass) description).getAdditionalInstances().size() + " additional instances</font></p>";
		             if(!((EvaluatedDescriptionClass) description).isConsistent()) {
		            	 text += "<p style=\"max-width:100px;\"><font size=\"3\" color=\"red\">Adding this class expression may lead to an inconsistent ontology.</font></p>";
		             } 
		             if(description.getAccuracy() == 1.0) {
		            	 text += "<p><font size=\"3\" color=\"EE9A00\">\u25cf</font><font size=\"3\" color=\"black\"> and </font> <font size=\"3\" color=\"yellow\">\u25cf</font> cover the same instances.</p>";
		             }
		             text += "</html>";
		this.setText(text);
		textScroll.setViewportView(this);
	}
	
	public JScrollPane getTextScroll() {
		return textScroll;
	}
}
