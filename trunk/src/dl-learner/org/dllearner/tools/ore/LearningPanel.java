package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;

public class LearningPanel extends JPanel{

	private static final long serialVersionUID = -7411197973240429632L;

	private JPanel contentPanel;
	
	private javax.swing.JList resultList;
	private DefaultListModel model;
	
	private JLabel statusLabel;
	private JXBusyLabel loadingLabel;
	
	private JButton startButton;
	private JButton stopButton;
	
	@SuppressWarnings("unchecked")
	public LearningPanel() {
		
		super();
		model = new DefaultListModel();
		
	
		JPanel buttonPanel = new JPanel();
		startButton = new JButton("Start");
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		
		
		JPanel labelPanel = new JPanel();
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
		labelPanel.add(loadingLabel);
		labelPanel.add(statusLabel);
		
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		
		add(buttonPanel, BorderLayout.EAST);
		add(contentPanel,BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		
		JPanel contentPanel1 = new JPanel();
		JScrollPane scroll = new JScrollPane();
		
		
		resultList = new JList(model);
//		resultList.setCellRenderer(new ColumnListCellRenderer());
		scroll.setPreferredSize(new Dimension(400, 400));
		scroll.setViewportView(resultList);
				
		JSlider noise = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		noise.setMajorTickSpacing(50);
		noise.setMinorTickSpacing(25);
		noise.setPaintTicks(true);
		noise.setPaintLabels(true);
		
		contentPanel1.add(scroll);
		contentPanel1.add(noise);
		
		
		

		return contentPanel1;
	}
	
	public void addStartButtonListener(ActionListener a){
		startButton.addActionListener(a);
	}
	
	public void addStopButtonListener(ActionListener a){
		stopButton.addActionListener(a);
	}
	
	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JXBusyLabel getLoadingLabel() {
		return loadingLabel;
	}

	public JButton getStartButton() {
		return startButton;
	}

	public JButton getStopButton() {
		return stopButton;
	}

	public DefaultListModel getModel() {
		return model;
	}
	
	public javax.swing.JList getResultList() {
		return resultList;
	}
	
	public void addSelectionListener(ListSelectionListener l){
		resultList.addListSelectionListener(l);
	}
	
	
	
}  
    
 


	

