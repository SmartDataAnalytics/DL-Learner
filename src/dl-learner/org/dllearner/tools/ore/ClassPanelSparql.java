package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;


public class ClassPanelSparql extends JPanel{

	private static final long serialVersionUID = 3026319637264844550L;

	private JTextField classField;
	
	private JPanel contentPanel;
	
	private JXBusyLabel loadingLabel;	
	private JLabel statusLabel;
	
	@SuppressWarnings("unchecked")
	public ClassPanelSparql() {
		
		super();
		
		statusLabel = new JLabel();
		
		loadingLabel = new JXBusyLabel(new Dimension(15,15));
		BusyPainter painter = new BusyPainter(
		new RoundRectangle2D.Float(0, 0,6.0f,2.6f,10.0f,10.0f),
		new Ellipse2D.Float(2.0f,2.0f,11.0f,11.0f));
		painter.setTrailLength(2);
		painter.setPoints(7);
		painter.setFrame(-1);
		loadingLabel.setPreferredSize(new Dimension(15,15));
		loadingLabel.setIcon(new EmptyIcon(15,15));
		loadingLabel.setBusyPainter(painter);

				
		JPanel labelPanel = new JPanel();
		labelPanel.add(loadingLabel);
		labelPanel.add(statusLabel);
				
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		add(contentPanel,BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		
		JPanel contentPanel = new JPanel();
				
		classField = new JTextField();
		
		contentPanel.add(classField);
		
		return contentPanel;
	}
	
		
	public JTextField getClassField(){
    	return classField;
    }
    
   
	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JXBusyLabel getLoadingLabel() {
		return loadingLabel;
	}
    

 


	

}