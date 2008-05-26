package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
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
	
	private JList posFailureList;
	private	JList negFailureList;
	private DefaultListModel posFailureModel;
	private DefaultListModel negFailureModel;
	
	
	
	private JLabel statusLabel;
	private JXBusyLabel loadingLabel;
	
	private JButton saveButton;
	
	
	@SuppressWarnings("unchecked")
	public RepairPanel() {
		
		super();
		posFailureModel = new DefaultListModel();
		negFailureModel = new DefaultListModel();
				
		JPanel buttonPanel = new JPanel();
	
		saveButton = new JButton("save");
	
	
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
		
		JScrollPane posScroll = new JScrollPane();
		posFailureList = new JList(posFailureModel);
		posScroll.setPreferredSize(new Dimension(400, 400));
		posScroll.setViewportView(posFailureList);
		
		JScrollPane negScroll = new JScrollPane();
		negFailureList = new JList(negFailureModel);
		negScroll.setPreferredSize(new Dimension(400, 400));
		negScroll.setViewportView(negFailureList);
		
		
		contentPanel1.add(posScroll);
		contentPanel1.add(negScroll);
		
		

		return contentPanel1;
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

	public DefaultListModel getPosFailureModel() {
		return posFailureModel;
	}
	
	public DefaultListModel getNegFailureModel() {
		return negFailureModel;
	}
	
	public javax.swing.JList getPosFailureList() {
		return posFailureList;
	}
	
	public javax.swing.JList getNegFailureList() {
		return negFailureList;
	}
	
	public void addSelectionListener(ListSelectionListener l){
		posFailureList.addListSelectionListener(l);
		negFailureList.addListSelectionListener(l);
	}
	
	public void addMouseListener(MouseListener m){
		posFailureList.addMouseListener(m);
		negFailureList.addMouseListener(m);
	}
	
	
	
}  
    
 


	

