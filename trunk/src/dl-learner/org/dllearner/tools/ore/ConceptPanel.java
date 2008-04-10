package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;


public class ConceptPanel extends JPanel{



	private javax.swing.JList conceptList;
	
	private JPanel contentPanel;
	
	private DefaultListModel model;
	private JXBusyLabel loadingLabel;	
	private JLabel statusLabel;
	
	@SuppressWarnings("unchecked")
	public ConceptPanel() {
		
		super();
		
		model = new DefaultListModel();
		loadingLabel = new JXBusyLabel(new Dimension(15,15));
		statusLabel = new JLabel();
		
	
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
		
		JPanel contentPanel1 = new JPanel();
		JScrollPane scroll = new JScrollPane();
		
			
		conceptList = new JList(model);
		conceptList.setCellRenderer(new ColorListCellRenderer());
		scroll.setPreferredSize(new Dimension(400,400));
		scroll.setViewportView(conceptList);
		contentPanel1.add(scroll);
		
		

		return contentPanel1;
	}
	
	public DefaultListModel getModel(){
		return model;
	}
	
	public void setModel(DefaultListModel dm){
		conceptList.setModel(dm);
	}
	
	public void addSelectionListener(ListSelectionListener l){
		conceptList.addListSelectionListener(l);
	}
          
    public JList getList(){
    	return conceptList;
    }
    
   
	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JXBusyLabel getLoadingLabel() {
		return loadingLabel;
	}
    
//    class BlinkLabel extends JLabel implements Runnable{
//    	private boolean blinking = false;
//    	    	
//    	public void start(){
//    		blinking = true;
//    		new Thread(this).start();
//    	}
//    	public void stop(){
//    		blinking = false;
//    		setForeground(Color.black);
//    		
//    	}
//    	public void run(){
//    		while(blinking){
//    			setText("Loading Concept");
//    			
//    			//setForeground(Color.red);
//    			try {
//					Thread.sleep(300);
//				} catch (InterruptedException e) {
//					return;
//				}
//				setText("");
//				
//    		}
//    		if(!blinking)
//    			setText("Done! Select Concept and press 'Next'");
//    	}
//    }
 


	

}