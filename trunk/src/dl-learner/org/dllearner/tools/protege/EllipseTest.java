package org.dllearner.tools.protege;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class EllipseTest extends JPanel {

	private static final long serialVersionUID = -5676466024192284648L;
	private Ellipse2D te;
	private Random random;

	public EllipseTest() {
		te = new Ellipse2D.Double(0, 0, 150, 150);
		this.setSize(500, 500);
		random = new Random();
		JDialog dialog = new JDialog();
		dialog.add(this);
		dialog.setSize(600, 600);
		dialog.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2D;
		g2D = (Graphics2D) g;
		g2D.setColor(Color.YELLOW);
		g2D.fill(te);
		g2D.setColor(Color.RED);
		double x = random.nextInt(500);
		double y = random.nextInt(500);
		int i = 0;
		while (i < 1000) {
			Point2D point = new Point2D.Double(x, y);
			if (te.contains(point)) {
				Ellipse2D circlePoint = new Ellipse2D.Double(x - 1, y - 1, 3, 3);
				g2D.draw(circlePoint);
				// g2D.drawString("*", ((int)x), ((int) y));
				x = random.nextInt(500);
				y = random.nextInt(500);
				i++;
			} else {
				x = random.nextInt(500);
				y = random.nextInt(500);
			}
		}
	}

	public static void main(String[] args) {
		new EllipseTest();
	}
}
