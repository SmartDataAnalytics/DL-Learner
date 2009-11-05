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

import java.awt.Cursor;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * This is the Hyperlink Handler that handles what happens when a 
 * hyperlink is clicked.
 * @author Christian Koetteritzsch
 *
 */
public class HyperLinkHandler implements HyperlinkListener {

	private  BrowserLauncher launcher;
	
	/**
	 * This is the constructor that instantiate the
	 * BrowserLauncher.
	 */
	public HyperLinkHandler() {
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
	/**
	 * This methode handles what happens when a hyperlink is clicked.
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			((JEditorPane) event.getSource()).setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
			((JEditorPane) event.getSource()).setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
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
