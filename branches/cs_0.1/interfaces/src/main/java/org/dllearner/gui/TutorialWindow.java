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
package org.dllearner.gui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

/**
 * Window displaying a tutorial for the DL-Learner GUI.
 * 
 * @author Jens Lehmann
 * 
 */
public class TutorialWindow extends JFrame {

	private static final long serialVersionUID = 9152567539729126842L;

	public TutorialWindow() {
		setTitle("Quick Tutorial");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(525, 600);
		setVisible(true);		

		getContentPane().setLayout(new BorderLayout());
		final JLabel label = new JLabel("Loading HTML help page.");
		add(label, BorderLayout.NORTH);
		
		final JTextPane tp = new JTextPane();
		JScrollPane js = new JScrollPane();
		js.getViewport().add(tp);
		add(js, BorderLayout.CENTER);		
		
		SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
			@Override
			public Boolean doInBackground() {

				try {
					URL url = getClass().getResource("tutorial.html");
					tp.setPage(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				label.setText("Displaying tutorial.html.");
				remove(label);
				repaint();
				
				return true;
			}
		};
		worker.execute();
		
		// display tutorial text
		// String text = "<html><h2>Quick Tutorial</h2><p
		// align=\"justify\">DL-Learner has a component" +
		// " based design to make it easier to extend and use. There are four "
		// +
		// "components corresponding to the tabs you see: knowledge source, " +
		// "reasoner, learning problem, and learning algorithm. There are
		// various " +
		// "components available of each type (selectable at the top of each
		// tab). " +
		// "Each component has configuration options associated with it (middle
		// of the" +
		// " tab, scrollable), which you can use to specify the settings for the
		// component.</p>" +
		// "<br /><p align=\"justify\">In order to create a learning problem, "
		// +
		// "you have to choose and configure all four types of components." +
		// " The run tab plays a special role: It is used to start the learning
		// algorithm" +
		// " and display statistical information.</p>" +
		// "<br /><p><i>Tab color explanation:</i> <br />gray = cannot be
		// configured yet (mandatory configuration values missing)<br />" +
		// "red = needs to be initialised<br />black = component has been
		// initialised</p>" +
		// "<br /><p><i>Further references:</i><br />" +
		// "Homepage: <a
		// href=\"http://dl-learner.org\">http://dl-learner.org</a><br />" +
		// "DL-Learner Architecture: <a
		// href=\"http://dl-learner.org/wiki/Architecture\">http://dl-learner.org/wiki/Architecture</a>"
		// +
		// "</p><br /><p>Please send questions to
		// lehmann@informatik.uni-leipzig.de.</p></html>";
		// JLabel label = new JLabel(text);
		// label.setMaximumSize(new Dimension(300,500));
		// add(label);

		// setVisible(true);
	}

}
