package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXTable;

public class ExplanationTablePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7836622769361235749L;
	private JXTable explanationTable;
	private int explanationNumber;
	
	public ExplanationTablePanel(JXTable explanationTable, int explanationNumber){
		this.explanationTable = explanationTable;
		this.explanationNumber = explanationNumber;
		createUI();
		
	}
	
	private void createUI(){
		setLayout(new BorderLayout(2, 2));
		JLabel label = new JLabel(new StringBuilder().append("Explanation ").append(explanationNumber).toString());
		add(label, BorderLayout.NORTH);
		JPanel tablePanel = new JPanel(new BorderLayout());
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 20, 0, 0);
		Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(emptyBorder, lineBorder));
		tablePanel.add(explanationTable.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(explanationTable);
		add(tablePanel);
		
	}
	

}
