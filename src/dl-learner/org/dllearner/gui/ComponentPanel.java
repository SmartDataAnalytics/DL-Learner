package org.dllearner.gui;

import java.awt.LayoutManager;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.dllearner.core.Component;

/**
 * Class displaying a component (and its options).
 * 
 * @author Jens Lehmann
 *
 */
public abstract class ComponentPanel<T extends Component> extends JPanel implements ActionListener {

	private static final long serialVersionUID = -7678275020058043937L;

	public ComponentPanel(LayoutManager layout) {
		super(layout);
	}
	
	// called when panel is active
	public abstract void panelActivated();
}
