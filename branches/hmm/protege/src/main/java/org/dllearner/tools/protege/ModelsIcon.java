package org.dllearner.tools.protege;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ModelsIcon implements Icon {

	@Override
	public int getIconHeight() {
		return 16;
	}

	@Override
	public int getIconWidth() {
		return 16;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.BLACK);
		g.drawLine(3, y, 3, getIconHeight());
		g.drawLine(3, 7, getIconWidth() - 2, 7);
		g.drawLine(3, 10,getIconWidth() - 2, 10);

	}

}
