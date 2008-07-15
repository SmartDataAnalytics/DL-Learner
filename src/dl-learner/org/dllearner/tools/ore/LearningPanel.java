package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
	
	
	private DefaultListModel model;
	
	private JLabel statusLabel;
	private JXBusyLabel loadingLabel;
	
	
	
	private JList resultList;
	private JScrollPane listScrollPane;
	private JPanel listPanel;
	private JPanel noisePanel;
	private JSlider noiseSlider;
	private JLabel noiseLabel;
	private JButton stopButton;
	private JButton startButton;
	private JPanel buttonPanel;
	private JPanel buttonSliderPanel;
	private JLabel conceptLabel;
	private JLabel accuracyLabel;

	public LearningPanel() {
		
		super();
		model = new DefaultListModel();
			
		JPanel statusPanel = new JPanel();
		statusLabel = new JLabel();
		loadingLabel = new JXBusyLabel(new Dimension(15,15));
		BusyPainter<Object> painter = new BusyPainter<Object>(
		new RoundRectangle2D.Float(0, 0,6.0f,2.6f,10.0f,10.0f),
		new Ellipse2D.Float(2.0f,2.0f,11.0f,11.0f));
		painter.setTrailLength(2);
		painter.setPoints(7);
		painter.setFrame(-1);
		loadingLabel.setPreferredSize(new Dimension(15,15));
		loadingLabel.setIcon(new EmptyIcon(15,15));
		loadingLabel.setBusyPainter(painter);
		statusPanel.add(loadingLabel);
		statusPanel.add(statusLabel);
		
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		
		
		add(contentPanel,BorderLayout.CENTER);
		add(statusPanel, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		JPanel contentPanel = new JPanel();
		GridBagLayout contentLayout = new GridBagLayout();
		setPreferredSize(new Dimension(700, 500));
		contentLayout.rowWeights = new double[] {0.5};
		contentLayout.rowHeights = new int[] {500};
		contentLayout.columnWeights = new double[] {0.5, 0.0};
		contentLayout.columnWidths = new int[] {313, 7};
		this.setLayout(contentLayout);
		
		
		{
			listPanel = new JPanel();
			GridBagLayout jPanel1Layout = new GridBagLayout();
			contentPanel.add(listPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanel1Layout.rowWeights = new double[] {0.0, 0.5};
			jPanel1Layout.rowHeights = new int[] {16,400};
			jPanel1Layout.columnWeights = new double[] {0.0, 0.5};
			jPanel1Layout.columnWidths = new int[] {50, 700};
			listPanel.setLayout(jPanel1Layout);
			listPanel.setBorder(BorderFactory.createTitledBorder("Learned Concepts"));
			{
				listScrollPane = new JScrollPane();
				listPanel.add(listScrollPane, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					
					resultList = new JList(model);
					listScrollPane.setViewportView(resultList);
				}
			}
			{
				accuracyLabel = new JLabel();
				listPanel.add(accuracyLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				accuracyLabel.setText("Accuracy");
			}
			{
				conceptLabel = new JLabel();
				listPanel.add(conceptLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				conceptLabel.setText("Concept");
			}
		}
		{
			buttonSliderPanel = new JPanel();
			GridBagLayout buttonSliderPanelLayout = new GridBagLayout();
			contentPanel.add(buttonSliderPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			buttonSliderPanelLayout.rowWeights = new double[] {0.8, 0.0};
			buttonSliderPanelLayout.rowHeights = new int[] {200, 7};
			buttonSliderPanelLayout.columnWeights = new double[] {0.1};
			buttonSliderPanelLayout.columnWidths = new int[] {7};
			buttonSliderPanel.setLayout(buttonSliderPanelLayout);
			{
				buttonPanel = new JPanel();
				BoxLayout buttonPanelLayout = new BoxLayout(buttonPanel, javax.swing.BoxLayout.X_AXIS);
				buttonPanel.setLayout(buttonPanelLayout);
				buttonSliderPanel.add(buttonPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					startButton = new JButton();
					buttonPanel.add(startButton);
					startButton.setText("Start");
				}
				{
					stopButton = new JButton();
					buttonPanel.add(stopButton);
					stopButton.setText("Stop");
				}
			}
			{
				noisePanel = new JPanel();
				BoxLayout noisePanelLayout = new BoxLayout(noisePanel, javax.swing.BoxLayout.Y_AXIS);
				noisePanel.setLayout(noisePanelLayout);
				buttonSliderPanel.add(noisePanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					noiseLabel = new JLabel();
					noisePanel.add(noiseLabel);
					noiseLabel.setText("noise");
				}
				{
					noiseSlider = new JSlider(0, 100, 0);
					noiseSlider.setPaintTicks(true);
					noiseSlider.setMajorTickSpacing(10);
					noiseSlider.setMinorTickSpacing(5);
					Dictionary<Integer, JLabel> map = new Hashtable<Integer, JLabel>();
					map.put( new Integer(0), new JLabel("0%") );
					map.put( new Integer(50), new JLabel("50%") );
					map.put( new Integer(100),new JLabel("100%") );
					noiseSlider.setLabelTable( map );
					noiseSlider.setPaintLabels(true);
					noisePanel.add(noiseSlider);
				}
			}
		}
//		JPanel contentPanel1 = new JPanel();
//		JScrollPane scroll = new JScrollPane();
//		
//		
//		resultList = new JList(model);
////		resultList.setCellRenderer(new ColumnListCellRenderer());
//		scroll.setPreferredSize(new Dimension(900, 400));
//		scroll.setViewportView(resultList);
//			
//		contentPanel1.add(scroll);
	
	
		return contentPanel;
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
	
	public double getNoise(){
		return noiseSlider.getValue();
	}
	
	
}  
    
 


	

