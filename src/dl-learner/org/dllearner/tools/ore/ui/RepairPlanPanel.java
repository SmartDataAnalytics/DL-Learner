package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;

public class RepairPlanPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 537629900742120594L;
	private RepairManager repMan;
	private JButton undoButton;

	public RepairPlanPanel() {
		this.repMan = RepairManager.getInstance(OREManager.getInstance());
		
		setLayout(new BorderLayout());
		add(new JLabel("Axioms"), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(2));
		add(buttonPanel, "South");
		undoButton = new JButton(new AbstractAction("Undo") {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -6089211629549822981L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				undo();
				
			}
		});
		undoButton.setEnabled(false);
		buttonPanel.add(undoButton);
		
		buttonPanel.add(new JButton(new AbstractAction("Apply") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				computeRepairPlan();
			}

		}));

		JScrollPane repScr = new JScrollPane(new RepairTable());
		repScr.setBackground(null);
		repScr.getViewport().setOpaque(false);
		add(repScr);
	}

	private void computeRepairPlan() {
		repMan.executeRepairPlan();
		undoButton.setEnabled(repMan.isUndoable());
	}
	
	private void undo() {
		repMan.undo();
		undoButton.setEnabled(repMan.isUndoable());
	}
	
}
