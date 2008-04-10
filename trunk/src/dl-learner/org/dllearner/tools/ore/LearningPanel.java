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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;

public class LearningPanel extends JPanel{


	private JPanel contentPanel;
	
	private javax.swing.JList resultList;
	private DefaultListModel model;
	
	private JXTable resultTable;
	private DefaultTableModel model1;
	
	private JLabel statusLabel;
	private JXBusyLabel loadingLabel;
	
	private JButton startButton;
	private JButton stopButton;
	
	@SuppressWarnings("unchecked")
	public LearningPanel() {
		
		super();
		model = new DefaultListModel();
		
		model1 = new DefaultTableModel();
		model1.addColumn("Description");
		model1.addColumn("Correctness");
		
		
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
		
		resultTable = new JXTable(model1 );
		
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultTable.setHighlighters(HighlighterFactory.createSimpleStriping());
		
		
		resultList = new JList(model);
//		resultList.setCellRenderer(new ColumnListCellRenderer());
		scroll.setPreferredSize(new Dimension(400, 400));
		
		scroll.setViewportView(resultList);
				
		contentPanel1.add(scroll);
		
		

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
	
//	public DefaultTableModel getModel() {
//		return model1;
//	}

	public javax.swing.JList getResultList() {
		return resultList;
	}
	
	
	
	
	
}  
    
 


	

