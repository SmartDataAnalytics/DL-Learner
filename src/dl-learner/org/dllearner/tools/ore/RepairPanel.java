package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;

public class RepairPanel extends JPanel{

	private static final long serialVersionUID = -7411197973240429632L;

	private JPanel contentPanel;
	
	private javax.swing.JList resultList;
	private DefaultListModel model;
	
	
	
	private JLabel statusLabel;
	private JXBusyLabel loadingLabel;
	
	private JButton deleteButton;
	private JButton moveButton;
	private JButton addButton;
	private JButton saveButton;
	
	
	@SuppressWarnings("unchecked")
	public RepairPanel() {
		
		super();
		model = new DefaultListModel();
		
				
		JPanel buttonPanel = new JPanel();
		deleteButton = new JButton("delete");
		moveButton = new JButton("move");
		addButton = new JButton("add property");
		saveButton = new JButton("save");
	
		buttonPanel.add(deleteButton);
		buttonPanel.add(moveButton);
		buttonPanel.add(addButton);
		buttonPanel.add(saveButton);
		buttonPanel.setLayout(new GridLayout(5,1,0,10));
		
		
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

		scroll.setPreferredSize(new Dimension(400, 400));
		
		scroll.setViewportView(resultList);
				
		contentPanel1.add(scroll);
		
		

		return contentPanel1;
	}
	
	public void addDeleteButtonListener(ActionListener a){
		deleteButton.addActionListener(a);
	}
	
	public void addMoveButtonListener(ActionListener a){
		moveButton.addActionListener(a);
	}
	
	public void addAddButtonListener(ActionListener a){
		addButton.addActionListener(a);
	}
	
	public void addSaveButtonListener(ActionListener a){
		saveButton.addActionListener(a);
	}
	
	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JXBusyLabel getLoadingLabel() {
		return loadingLabel;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public JButton getMoveButton() {
		return moveButton;
	}
	
	public JButton getAddButton() {
		return addButton;
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
    
 


	

