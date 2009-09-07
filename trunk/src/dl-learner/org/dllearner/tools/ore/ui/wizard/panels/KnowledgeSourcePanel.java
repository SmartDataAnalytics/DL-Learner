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

package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RecentManager;
import org.dllearner.tools.ore.ui.LinkLabel;
import org.dllearner.tools.ore.ui.MetricsPanel;

/**
 * Wizard panel  for choosing knowledge source.
 * @author Lorenz Buehmann
 *
 */
public class KnowledgeSourcePanel extends JPanel{

	private static final long serialVersionUID = -3997200565180270088L;

	
	private JPanel contentPanel;

	private Box box;
	private LinkLabel openFromFileLink;
	private LinkLabel openFromURILink;
	private LinkLabel loadFromSparqlEndpointLink;
	private List<LinkLabel> openFromRecentLinks;
	private Box recentLinkBox;
	private GridBagConstraints c;
	
	private MetricsPanel metricsPanel;
	
	private JLabel ontologyName;

	
	
	public KnowledgeSourcePanel() {
//		setBackground(Color.WHITE);
		new LeftPanel(1);
		contentPanel = getContentPanel();

		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    add(contentPanel, c);
//	    addMetricsPanel();
	   

	}

	private JPanel getContentPanel() {

		JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
       
        int strutHeight = 10;
 
        box = new Box(BoxLayout.Y_AXIS);
        box.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
        panel.add(box);
        
        openFromFileLink = new LinkLabel("Open OWL-Ontology from filesystem");
        openFromFileLink.setName("openFromFileLink");
        box.add(openFromFileLink);
        
        box.add(Box.createVerticalStrut(strutHeight));
        
        openFromURILink = new LinkLabel("Open OWL-Ontology from URI");
        openFromURILink.setName("openFromURILink");
        box.add(openFromURILink);
        
        box.add(Box.createVerticalStrut(strutHeight));
        
        loadFromSparqlEndpointLink = new LinkLabel("Open OWL-Ontology from Sparql-Endpoint");
        loadFromSparqlEndpointLink.setName("loadFromSparqlEndpointLink");
        box.add(loadFromSparqlEndpointLink);
        
        box.add(Box.createVerticalStrut(2 * strutHeight));
        
        
        
        
        if (RecentManager.getInstance().getURIs().size() > 0) {
            recentLinkBox = new Box(BoxLayout.Y_AXIS);

            recentLinkBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                    " Open recent ",
                    0,
                    0,
                    getFont().deriveFont(Font.BOLD),
                    Color.GRAY), BorderFactory.createEmptyBorder(20, 20, 20, 20)));

            openFromRecentLinks = new ArrayList<LinkLabel>();
            LinkLabel link;
            for (final URI uri : RecentManager.getInstance().getURIs()) {
            	link = new LinkLabel(uri.toString());
            	link.setName("recent");
            	openFromRecentLinks.add(link);
                recentLinkBox.add(link);
                
            }
            box.add(recentLinkBox);
        }

        metricsPanel = new MetricsPanel();
        box.add(Box.createVerticalStrut(4 * strutHeight));
        box.add(metricsPanel);
        panel.add(box);
        
        
		return panel;
	}
	
	public void addMetricsPanel() {
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		
		
		metricsPanel = new MetricsPanel();
		
		add(metricsPanel, c);
	}
	
	public void updateMetrics(){
		metricsPanel.updateView(OREManager.getInstance().getReasoner().getOWLAPIOntologies());
	}
	
	public void addListeners(ActionListener aL) {
		openFromFileLink.addLinkListener(aL);
		openFromURILink.addLinkListener(aL);
		loadFromSparqlEndpointLink.addLinkListener(aL);
		for(LinkLabel link : openFromRecentLinks){
			link.addLinkListener(aL);
		}
		
    }
	
	
}
