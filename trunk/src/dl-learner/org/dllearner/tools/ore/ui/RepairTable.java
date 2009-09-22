package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.jdesktop.swingx.JXTable;
import org.semanticweb.owl.model.OWLOntologyChange;

public class RepairTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -621497634521668635L;
	private final Icon deleteIcon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/Delete16.gif");

	public RepairTable() {
		super(new RepairTableModel());
		setBackground(Color.WHITE);
		setShowHorizontalLines(true);
		setGridColor(Color.LIGHT_GRAY);
		setTableHeader(null);
		setRowHeight(getRowHeight() + 5);
		setRowHeightEnabled(true);
		getColumn(0).setMaxWidth(30);
		getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());
		getColumn(2).setMaxWidth(40);
		getColumn(2).setCellRenderer(new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable arg0,
					Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {

				return new JLabel(deleteIcon);
			}
		});

		addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}

		});

		addMouseMotionListener(new MouseAdapter() {

			final RepairTable table;
			{
				table = RepairTable.this;
			}

			public void mouseMoved(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int column = columnAtPoint(e.getPoint());

				if (column == 2 && row <= table.getRowCount() && row >= 0) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

				} else {
					setCursor(null);
				}
			}

		});

		addMouseListener(new MouseAdapter() {
			final RepairTable table;
			{
				table = RepairTable.this;
			}

			public void mouseClicked(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int column = columnAtPoint(e.getPoint());

				if (row >= 0 && row <= table.getRowCount() && column == 2) {
					OWLOntologyChange change = ((RepairTableModel) getModel())
							.getChangeAt(row);
					if (ImpactManager.getInstance(OREManager.getInstance())
							.isSelected(change.getAxiom())) {
						ImpactManager.getInstance(OREManager.getInstance())
								.removeSelection(change.getAxiom());
					}
					RepairManager.getInstance(OREManager.getInstance())
							.removeFromRepairPlan(change);
					setCursor(null);
				}
			}
		});

	}

	private void handleKeyPressed(KeyEvent e) {
		int selRow = getSelectedRow();
		OWLOntologyChange change = ((RepairTableModel) getModel())
				.getChangeAt(selRow);
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			RepairManager.getInstance(OREManager.getInstance())
					.removeFromRepairPlan(change);
			if (ImpactManager.getInstance(OREManager.getInstance()).isSelected(
					change.getAxiom())) {
				ImpactManager.getInstance(OREManager.getInstance())
						.removeSelection(change.getAxiom());
			}

		}

		getSelectionModel().clearSelection();
	}

}
