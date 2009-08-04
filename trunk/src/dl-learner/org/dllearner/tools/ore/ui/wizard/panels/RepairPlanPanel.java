package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.ui.RepairTable;

public class RepairPlanPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 537629900742120594L;
	private RepairManager repMan;

	public RepairPlanPanel(RepairManager repMan) {
		this.repMan = repMan;
		
		setLayout(new BorderLayout());
		add(new JLabel("Axioms to remove"), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(2));
		add(buttonPanel, "South");
		buttonPanel.add(new JButton(new AbstractAction("compute plan") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				computeRepairPlan();
			}

		}));

		JScrollPane repScr = new JScrollPane(new RepairTable(repMan));
		repScr.setBackground(null);
		repScr.getViewport().setOpaque(false);
		add(repScr);
	}

	private void computeRepairPlan() {
		repMan.executeRepairPlan();
	}
}
