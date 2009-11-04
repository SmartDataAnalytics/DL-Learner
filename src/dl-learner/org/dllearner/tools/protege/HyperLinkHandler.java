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

public class HyperLinkHandler implements HyperlinkListener {

	private  BrowserLauncher launcher;
	
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
