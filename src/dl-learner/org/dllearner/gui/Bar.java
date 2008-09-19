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

import java.awt.*;
import javax.swing.*;

/**
 * @author Tilo Hielscher
 * 
 */
public class Bar extends JPanel {

	private static final long serialVersionUID = 8084274242520881523L;
	private int height;
	private int width;
	private double factor;

	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		super.setSize(this.width, this.height);
		g.fillRect(0, 0, (int) (this.width * this.factor), this.height);
	}

	/**
	 * Make a horizontal bar.
	 * 
	 * @param width
	 *            in pixel
	 * @param height
	 *            in pixel
	 * @param factor
	 *            should between 0 and 1 (1 for 100%)
	 */
	public Bar(int width, int height, double factor) {
		this.width = width;
		this.height = height;
		this.factor = factor;
		repaint();
	}

	/**
	 * Update horizontal bar.
	 * 
	 * @param newFactor
	 *            should between 0 and 1 (1 for 100%)
	 */
	public void update(double newFactor) {
		this.factor = newFactor;
		repaint();
	}
}
