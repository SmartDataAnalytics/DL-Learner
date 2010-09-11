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

//import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.protege.editor.owl.OWLEditorKit;

public class GraphicalCoverageTextField extends JTextPane{

	private static final long serialVersionUID = 8971091768497004453L;
	EvaluatedDescription description;
	private String newConceptRendered;
	private String oldConceptRendered;
	//private final JScrollPane textScroll;
	
	public GraphicalCoverageTextField(EvaluatedDescription desc, OWLEditorKit editorKit) {
		this.setContentType("text/html");
		this.setEditable(false);
		//textScroll = new JScrollPane(
		//		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		//		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setBackground(editorKit.getOWLWorkspace().getOWLComponentFactory().getOWLClassSelectorPanel().getBackground());
		this.description = desc;
	}
	
	public void setDescription(EvaluatedDescription description){
		this.description = description;
		Manager manager = Manager.getInstance();
		newConceptRendered = manager.getRendering(description.getDescription());
		oldConceptRendered = manager.getRendering(manager.getCurrentlySelectedClass());
		setText();
	}

	private void setText() {
		int coveredInstancesCount = ((EvaluatedDescriptionClass) description).getCoveredInstances().size();
		int allInstancesCount = coveredInstancesCount + ((EvaluatedDescriptionClass) description).getNotCoveredInstances().size();
		int additionalInstancesCount = ((EvaluatedDescriptionClass) description).getAdditionalInstances().size();
		int coverage = (int)(((EvaluatedDescriptionClass) description).getCoverage() * 100);
		             
		StringBuffer sb = new StringBuffer();
		sb.append("<html><p><font size=\"3\" color=\"yellow\">\u25cf</font><font size=\"3\" color=\"black\"> ");
		sb.append(oldConceptRendered);
		sb.append("</font></p>");
		sb.append("<p style=\"max-width:50px;\"><font size=\"3\" color=\"EE9A00\">\u25cf</font><font size=\"3\" color=\"black\"> ");
		sb.append(newConceptRendered);
		sb.append("</font></p>");
		sb.append("<p><font size=\"1\" color=\"green\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font> <font size=\"3\" color=\"EE9A00\">\u25cf</font>");
		sb.append("<font size=\"3\" color=\"black\"> and </font> <font size=\"3\" color=\"yellow\">\u25cf</font><font size=\"3\" color=\"black\"> (OK)</font></p> ");
		if(Manager.getInstance().getLearningType() == LearningType.EQUIVALENT){
			sb.append("<p><font size=\"1\" color=\"red\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"EE9A00\">\u25cf</font></font><font size=\"3\" color=\"black\"> (potential problem)</font></p>");
			sb.append("<p><font size=\"1\" color=\"red\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"yellow\">\u25cf</font></font><font size=\"3\" color=\"black\"> (potential problem)</font></p>");
		} else {
			sb.append("<p><font size=\"1\" color=\"green\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"EE9A00\">\u25cf</font></font><font size=\"3\" color=\"black\"> (no problem)</font></p>");
			sb.append("<p><font size=\"1\" color=\"red\">\u25aa </font><font size=\"3\" color=\"black\">individuals covered by </font><font size=\"3\" color=\"yellow\">\u25cf</font></font><font size=\"3\" color=\"black\"> (potential problem)</font></p>");
		}
		sb.append("<p><font size=\"3\" color=\"black\">Covers ").append(coveredInstancesCount).append(" of ").append(allInstancesCount).append("(").append(coverage).append(" %) of class instances</font></p>");
		sb.append("<p><font size=\"3\" color=\"black\">Covers ").append(additionalInstancesCount).append(additionalInstancesCount == 1 ? " additional instance" : " additional instances").append("</font></p>");
		if(!((EvaluatedDescriptionClass) description).isConsistent()) {
			sb.append("<p style=\"max-width:100px;\"><font size=\"3\" color=\"red\">Adding this class expression may lead to an inconsistent ontology.</font></p>");
        } 
        if(description.getAccuracy() == 1.0) {
        	sb.append("<p><font size=\"3\" color=\"EE9A00\">\u25cf</font><font size=\"3\" color=\"black\"> and </font> <font size=\"3\" color=\"yellow\">\u25cf</font> cover the same instances.</p>");
        }
		sb.append("</html>");
		
		
		this.setText(sb.toString());
		//textScroll.setViewportView(this);
	}
	
	public JTextPane getTextScroll() {
		return this;
	}
}
