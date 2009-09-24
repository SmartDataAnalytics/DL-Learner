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

package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.IntroductionPanel;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;


/**
 * Wizard panel descriptor with some informations for the tool.
 * @author Lorenz Buehmann
 *
 */
public class IntroductionPanelDescriptor extends WizardPanelDescriptor implements HyperlinkListener{
    
    public static final String IDENTIFIER = "INTRODUCTION_PANEL";
    public static final String INFORMATION = "";

    private  BrowserLauncher launcher;
       
    public IntroductionPanelDescriptor() {
    	IntroductionPanel panel = new IntroductionPanel();
        panel.addHyperLinkListener(this);
        setPanelComponent(panel);
        setPanelDescriptorIdentifier(IDENTIFIER);
        
        try {
			launcher = new BrowserLauncher();
		} catch (BrowserLaunchingInitializingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperatingSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return KnowledgeSourcePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return null;
    }
    
    @Override
	public void aboutToDisplayPanel() {
        getWizard().getInformationField().setText(INFORMATION);
    }

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		
		if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) { 
			 URL url;
			try {
				url = new URL(event.getDescription());
				launcher.openURLinBrowser(url.toString());		
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
    
}
