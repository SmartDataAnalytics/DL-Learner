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
import java.awt.Dimension;

import javax.swing.JTextPane;

public class HelpTextPanel extends JTextPane{

	private static final long serialVersionUID = 5077192574709760571L;
	private HyperLinkHandler hyperHandler;
	private DLLearnerView view;
	public HelpTextPanel(DLLearnerView v) {
		this.view = v;
		hyperHandler = view.getHyperLinkHandler();
	}
	
	public JTextPane renderHelpTextMessage(String currentClass) {
		String helpText = "<html><p style=\"text-align: justify;\">What does a sentence like 'Learning started. Currently searching class expressions with length between 4 and 7.' mean?</p>"
            + "<p style=\"text-align: justify;\">Length: In Manchester OWL Syntax (the syntax used for class expressions in Protege), we define length simply as the "
            + "number of words needed to write down the class expression.</p>"
            + "<p style=\"text-align: justify;\">The learning algorithm (called CELOE) for suggesting class expressions starts with the most general expression owl:Thing"
            + " and then further specializes it. Those class expressions, which fit the existing instances of a given class ("
            + currentClass
            + " in this case) "
            + "get a high accuracy and are displayed as suggestions. The learning algorithm prefers short expressions. 'Currently searching class expressions with length"
            + " between 4 and 7.' means that it has already evaluated all class expressions of length 1 to 3 or excluded them as possible suggestions. All the expressions"
            + " currently evaluated have length between 4 and 7. If you want to search for longer expressions, then you have to increase the maximum runtime setting (it is "
            + "set to " + view.getOptionsPanel().getMaxExecutionTimeInSeconds()
            + " seconds by default).</p>"
            + "<p>See <a href=\"http://dl-learner.org/wiki/ProtegePlugin\">DL-Learner plugin page</a> for more details.</p></html>";
		this.setEditable(false);
		this.setOpaque(true);
		this.setPreferredSize(new Dimension(500, 370));
		this.setContentType("text/html");
		this.setForeground(Color.black);
		this.addHyperlinkListener(hyperHandler);
		this.setBackground(view.getLearnerView().getBackground());
		this.setText(helpText);
		this.setVisible(true);
		return this;
	}
}
