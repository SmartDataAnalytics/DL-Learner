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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

	private Box box;
	
	private LinkLabel openFromFileLink;
	private LinkLabel openFromURILink;
	private LinkLabel loadFromSparqlEndpointLink;
	
	private List<LinkLabel> openFromRecentLinks;
	private Box recentLinkBox;
	
	private MetricsPanel metricsPanel;
	
	public KnowledgeSourcePanel() {		
		openFromRecentLinks = new ArrayList<LinkLabel>();
		createUI();
	}
	
	private void createUI(){
		setLayout(new GridBagLayout());
		
		int strutHeight = 10;
		 
        box = new Box(BoxLayout.Y_AXIS);
        box.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
        add(box);
        
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
        metricsPanel.setVisible(false);
        box.add(Box.createVerticalStrut(4 * strutHeight));
        box.add(metricsPanel);
	}
	
	public void updateMetrics(){
		metricsPanel.updateView(OREManager.getInstance().getReasoner().getOWLAPIOntologies());
		metricsPanel.setVisible(true);
	}
	
	public void updateRecentList(ActionListener aL){
		if(recentLinkBox == null){
			recentLinkBox = new Box(BoxLayout.Y_AXIS);

            recentLinkBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                    " Open recent ",
                    0,
                    0,
                    getFont().deriveFont(Font.BOLD),
                    Color.GRAY), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            box.add(recentLinkBox);
		}
		recentLinkBox.removeAll();
		openFromRecentLinks.clear();
		LinkLabel link;
		for (final URI uri : RecentManager.getInstance().getURIs()) {
        	link = new LinkLabel(uri.toString());
        	link.setName("recent");
        	openFromRecentLinks.add(link);
            recentLinkBox.add(link);
            link.addLinkListener(aL);
        }
		
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
