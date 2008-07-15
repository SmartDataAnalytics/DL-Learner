package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;

public class RepairPanel extends JPanel{

	private static final long serialVersionUID = -7411197973240429632L;

	private JPanel contentPanel;
	
	
	private DefaultListModel posFailureModel;
	private DefaultListModel negFailureModel;
	
	
	private JLabel statusLabel;
	private JXBusyLabel loadingLabel;
	
	
	
	private JPanel posPanel;
	private JList posList;
	private JScrollPane posScrollPane;
	private JScrollPane negScrollPane;
	private JButton pos_repairButton;
	private JButton pos_deleteButton;
	private JButton pos_removeButton;
	private JPanel posButtonPanel;
	private JButton neg_repairButton;
	private JButton neg_deleteButton;
	private JButton neg_addButton;
	private JPanel negButtonPanel;
	private JList negList;
	private JPanel negPanel;
	
	
	@SuppressWarnings("unchecked")
	public RepairPanel() {
		
		super();
		posFailureModel = new DefaultListModel();
		negFailureModel = new DefaultListModel();
		
		this.setLayout(new java.awt.BorderLayout());
		
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
		
		add(contentPanel,BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		JPanel contentPanel = new JPanel();
		GridBagLayout thisLayout = new GridBagLayout();
		thisLayout.rowWeights = new double[] {0.1};
		thisLayout.rowHeights = new int[] {7};
		thisLayout.columnWeights = new double[] {0.5, 0.5};
		thisLayout.columnWidths = new int[] {100, 100};
		contentPanel.setLayout(thisLayout);
		setPreferredSize(new Dimension(400, 300));
		{
			posPanel = new JPanel();
			GridBagLayout posPanelLayout = new GridBagLayout();
			contentPanel.add(posPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			posPanelLayout.rowWeights = new double[] {0.1};
			posPanelLayout.rowHeights = new int[] {7};
			posPanelLayout.columnWeights = new double[] {0.0, 0.5};
			posPanelLayout.columnWidths = new int[] {80, 110};
			posPanel.setLayout(posPanelLayout);
			posPanel.setPreferredSize(new java.awt.Dimension(182, 275));
			posPanel.setBorder(BorderFactory.createTitledBorder("positive examples"));
			{
				posScrollPane = new JScrollPane();
				posPanel.add(posScrollPane, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				posScrollPane.setSize(126, 276);
				{
					
					posList = new JList(posFailureModel);
					posScrollPane.setViewportView(posList);
					posList.setPreferredSize(new java.awt.Dimension(85, 93));
					posList.setSize(127, 273);
				}
			}
			{
				posButtonPanel = new JPanel();
				posButtonPanel.setName("positive");
				GroupLayout posButtonPanelLayout = new GroupLayout((JComponent)posButtonPanel);
				posPanel.add(posButtonPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				posButtonPanel.setLayout(posButtonPanelLayout);
				{
					pos_removeButton = new JButton();
					pos_removeButton.setName("posRemove");
					pos_removeButton.setText("remove");
				}
				{
					pos_deleteButton = new JButton();
					pos_deleteButton.setName("posDelete");
					pos_deleteButton.setText("delete");
				}
				{
					pos_repairButton = new JButton();
					pos_repairButton.setName("posRepair");
					pos_repairButton.setText("repair");
				}
					posButtonPanelLayout.setHorizontalGroup(posButtonPanelLayout.createSequentialGroup()
					.addGroup(posButtonPanelLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, posButtonPanelLayout.createSequentialGroup()
					        .addComponent(pos_removeButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
					        .addGap(10))
					    .addComponent(pos_deleteButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					    .addGroup(GroupLayout.Alignment.LEADING, posButtonPanelLayout.createSequentialGroup()
					        .addComponent(pos_repairButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
					        .addGap(10)))
					.addContainerGap(22, 22));
					posButtonPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {pos_repairButton, pos_deleteButton, pos_removeButton});
					posButtonPanelLayout.setVerticalGroup(posButtonPanelLayout.createSequentialGroup()
						.addComponent(pos_removeButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(pos_deleteButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(pos_repairButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(213, 213));
					posButtonPanelLayout.linkSize(SwingConstants.VERTICAL, new Component[] {pos_repairButton, pos_deleteButton, pos_removeButton});
			}
		}
		{
			negPanel = new JPanel();
			GridBagLayout negPanelLayout = new GridBagLayout();
			contentPanel.add(negPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			negPanelLayout.rowWeights = new double[] {0.1};
			negPanelLayout.rowHeights = new int[] {7};
			negPanelLayout.columnWeights = new double[] {0.5, 0.0};
			negPanelLayout.columnWidths = new int[] {110, 80};
			negPanel.setLayout(negPanelLayout);
			posPanel.setPreferredSize(new java.awt.Dimension(182, 275));
			negPanel.setBorder(BorderFactory.createTitledBorder(null, "negative examples", TitledBorder.LEADING, TitledBorder.TOP));
			{
				negScrollPane = new JScrollPane();
				negPanel.add(negScrollPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					negList = new JList(negFailureModel);
					negScrollPane.setViewportView(negList);
					negList.setPreferredSize(new java.awt.Dimension(85, 93));
					negList.setSize(127, 273);
				}
			}
			{
				negButtonPanel = new JPanel();
				negButtonPanel.setName("negative");
				GroupLayout negButtonPanelLayout = new GroupLayout((JComponent)negButtonPanel);
				negButtonPanel.setLayout(negButtonPanelLayout);
				negPanel.add(negButtonPanel, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					neg_addButton = new JButton();
					neg_addButton.setName("negAdd");
					neg_addButton.setText("add");
				}
				{
					neg_deleteButton = new JButton();
					neg_deleteButton.setName("negDelete");
					neg_deleteButton.setText("delete");
				}
				{
					neg_repairButton = new JButton();
					neg_repairButton.setName("negRepair");
					neg_repairButton.setText("repair");
				}
					negButtonPanelLayout.setHorizontalGroup(negButtonPanelLayout.createSequentialGroup()
					.addGroup(negButtonPanelLayout.createParallelGroup()
					    .addComponent(neg_addButton, GroupLayout.Alignment.LEADING, 0, 79, Short.MAX_VALUE)
					    .addComponent(neg_deleteButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
					    .addGroup(GroupLayout.Alignment.LEADING, negButtonPanelLayout.createSequentialGroup()
					        .addComponent(neg_repairButton, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
					        .addGap(7)))
					.addContainerGap());
					negButtonPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {neg_repairButton, neg_deleteButton, neg_addButton});
					negButtonPanelLayout.setVerticalGroup(negButtonPanelLayout.createSequentialGroup()
						.addComponent(neg_addButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(neg_deleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(neg_repairButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(213, 213));
					negButtonPanelLayout.linkSize(SwingConstants.VERTICAL, new Component[] {neg_repairButton, neg_deleteButton, neg_addButton});
			}
		}
		

		return contentPanel;
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
		return posList;
	}
	
	public javax.swing.JList getNegFailureList() {
		return negList;
	}
	
	public void addSelectionListeners(ListSelectionListener l){
		posList.addListSelectionListener(l);
		negList.addListSelectionListener(l);
	}
	

	public void addMouseListeners(MouseListener mL){
		posList.addMouseListener(mL);
		negList.addMouseListener(mL);
	}
	
	public void addActionListeners(ActionListener aL){
		pos_removeButton.addActionListener(aL);
		pos_deleteButton.addActionListener(aL);
		pos_repairButton.addActionListener(aL);
		neg_addButton.addActionListener(aL);
		neg_deleteButton.addActionListener(aL);
		neg_repairButton.addActionListener(aL);
	}
	
	public void setCellRenderers(ORE ore){
		ColorListCellRenderer cell = new ColorListCellRenderer(ore);
		posList.setCellRenderer(cell);
		negList.setCellRenderer(cell);
	}
	
	
	
}  
    
 


	

