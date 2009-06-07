package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RepairPlanPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 537629900742120594L;
	private ImpactManager impMan;

	public RepairPlanPanel(ImpactManager impMan) {
		this.impMan = impMan;
		
		setLayout(new BorderLayout());
		add(new JLabel("Axioms to remove"), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(2));
		add(buttonPanel, "South");
		buttonPanel.add(new JButton(new AbstractAction("compute plan") {

			public void actionPerformed(ActionEvent e) {
				computeRepairPlan();
			}

		}));

		JScrollPane repScr = new JScrollPane(new RepairTable(impMan));
		repScr.setBackground(null);
		repScr.getViewport().setOpaque(false);
		add(repScr);
	}

	private void computeRepairPlan() {
		impMan.executeRepairPlan();
	}
}
