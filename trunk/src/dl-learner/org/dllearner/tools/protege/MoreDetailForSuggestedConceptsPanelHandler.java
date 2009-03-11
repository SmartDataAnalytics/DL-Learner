package org.dllearner.tools.protege;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MoreDetailForSuggestedConceptsPanelHandler implements PropertyChangeListener{
private final MoreDetailForSuggestedConceptsPanel panel;
	public MoreDetailForSuggestedConceptsPanelHandler(MoreDetailForSuggestedConceptsPanel m) {
		panel = m;
	}
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		panel.setInformation();
		panel.repaint();
	}

}
