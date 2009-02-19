package org.dllearner.tools.protege;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;



public class GraphicalCoveragePanelHandler implements MouseMotionListener, PropertyChangeListener {

	private GraphicalCoveragePanel panel;

	public GraphicalCoveragePanelHandler(GraphicalCoveragePanel p) {
		this.panel = p;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent m) {
		Vector<IndividualPoint> v = panel.getIndividualVector();
		//System.out.println("hier: " + m.getX() + " " + m.getY());
		//System.out.println("bla: " + v.get(0).getXAxis() + " "
		//		+ v.get(0).getYAxis());
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i).getXAxis() >= m.getX() - 5
					&& v.get(i).getXAxis() <= m.getX() + 5
					&& v.get(i).getYAxis() >= m.getY() - 5
					&& v.get(i).getYAxis() <= m.getY() + 5) {
				panel.getGraphicalCoveragePanel().setToolTipText(v.get(i).getIndividualName());
			}

		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		panel.getMoreDetailForSuggestedConceptsPanel().repaint();	
	}


}
