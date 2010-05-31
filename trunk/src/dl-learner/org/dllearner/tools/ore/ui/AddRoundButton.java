package org.dllearner.tools.ore.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;
import javax.swing.JButton;


	public class AddRoundButton extends JButton {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4728674196569042965L;
		
		public AddRoundButton(String label) {
			super(label);

			// These statements enlarge the button so that it
			// becomes a circle rather than an oval.
			Dimension size = getPreferredSize();
			size.width = size.height = Math.max(size.width, size.height);
			setPreferredSize(size);

			// This call causes the JButton not to paint
			// the background.
			// This allows us to paint a round background.
			setContentAreaFilled(false);
			setFocusPainted(false);
		}
		
		public AddRoundButton(){
//			super(null, null);	
			setIcon(new AddIcon());
			setRolloverIcon(new RollOverAddIcon());
			Icon icon = getIcon();
			int max = Math.max(icon.getIconWidth(), icon.getIconHeight());System.out.println(max);
			Dimension size = new Dimension(max, max);
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);

			// This call causes the JButton not to paint
			// the background.
			// This allows us to paint a round background.
			setContentAreaFilled(false);
			setFocusPainted(false);
		}
		
		public AddRoundButton(Icon icon) {
			super(null, icon);

			// These statements enlarge the button so that it
			// becomes a circle rather than an oval.
//			Dimension size = getPreferredSize();		
//			size.width = size.height = Math.max(size.width, size.height);
//			setPreferredSize(size);
			
			int max = Math.max(icon.getIconWidth(), icon.getIconHeight());
			Dimension size = new Dimension(max, max);
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);

			// This call causes the JButton not to paint
			// the background.
			// This allows us to paint a round background.
			setContentAreaFilled(false);
			setFocusPainted(false);
		}

		// Paint the round background and label.
		protected void paintComponent(Graphics g) {
			if (getModel().isArmed()) {
				// You might want to make the highlight color
				// a property of the RoundButton class.
				g.setColor(Color.lightGray);
			} else {
				g.setColor(getBackground());
			}System.out.println(getSize());
//			g.fillOval(0, 0, getSize().width - 1, getSize().height - 1);

			// This call will paint the label and the
			// focus rectangle.
			super.paintComponent(g);
		}

		// Paint the border of the button using a simple stroke.
		protected void paintBorder(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getForeground());
//			g2.drawOval(0, 0, getSize().width - 1, getSize().height - 1);
		}

		// Hit detection.
		Shape shape;

		public boolean contains(int x, int y) {
			// If the button has changed size,
			// make a new shape object.
			if (shape == null || !shape.getBounds().equals(getBounds())) {
				shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
			}
			return shape.contains(x, y);
		}

		static class AddIcon implements Icon{
			
			private static final Stroke BUTTON_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

			@Override
			public int getIconHeight() {
				return 17;
			}

			@Override
			public int getIconWidth() {
				return 17;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Dimension dim = new Dimension(16, 16);
				int inset = 5;
				g2.setColor(Color.DARK_GRAY);
				g2.fillOval(x, y, dim.width, dim.height);

				g2.setColor(Color.WHITE);
				g2.setStroke(BUTTON_STROKE);
				g2.drawLine(x + dim.width / 2, y + inset, x + dim.width / 2, y + dim.height - inset);
			    g2.drawLine(x + inset, y + dim.height / 2, x + dim.width - inset, y + dim.height / 2);

			}

			
		}
		
	static class RollOverAddIcon implements Icon{
			
			private static final Stroke BUTTON_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

			@Override
			public int getIconHeight() {
				return 17;
			}

			@Override
			public int getIconWidth() {
				return 17;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Dimension dim = new Dimension(16, 16);
				int inset = 5;
				g2.setColor(Color.GREEN.darker());
				g2.fillOval(x, y, dim.width, dim.height);

				g2.setColor(Color.WHITE);
				g2.setStroke(BUTTON_STROKE);
				g2.drawLine(x + dim.width / 2, y + inset, x + dim.width / 2, y + dim.height - inset);
			    g2.drawLine(x + inset, y + dim.height / 2, x + dim.width - inset, y + dim.height / 2);

			}

			
		}
		
	
}
