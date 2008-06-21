package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectQuantorRestriction;

public class ChooseDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3554461981184458137L;
	
	
	ORE ore;
	DefaultListModel model;
	JList list;
	private JButton okButton;
	private JButton cancelButton;
	private Object selectedElement;
	private Object chooseValue;
	
	public ChooseDialog(JDialog parent, ORE ore, Object chooseValue){
		super(parent, true);
		this.ore = ore;
		this.chooseValue = chooseValue;
		init();
		
		
	}
	
	public void init(){
		setSize(new Dimension(400, 400));
		setLayout(new BorderLayout());
		
		model = new DefaultListModel();
		System.out.println(chooseValue.getClass());
		if(chooseValue instanceof Description){
			setTitle("select object for property");
			for(Individual ind : ore.getIndividualsOfPropertyRange((ObjectQuantorRestriction)chooseValue))
				model.addElement(ind);
		}
		else if(chooseValue instanceof Individual){
			setTitle("choose new class");
			for(NamedClass nc : ore.getpossibleMoveClasses((Individual)chooseValue))
				model.addElement(nc);
		}
			
		
		list = new JList(model);
		
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		
		
		JPanel buttonPanel = new JPanel();
        JSeparator separator = new JSeparator();
        Box buttonBox = new Box(BoxLayout.X_AXIS);
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(cancelButton);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        
        getContentPane().add(list, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModal(true);
        setVisible(true);
		
		
	}
	
	public Object getSelectedElement(){
		return selectedElement;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(okButton)){
			selectedElement = list.getSelectedValue();
			setVisible(false);
			dispose();
		}
		else if(e.getSource().equals(cancelButton)){
			selectedElement = null;
			setVisible(false);
			dispose();
		}
		
	}

}
