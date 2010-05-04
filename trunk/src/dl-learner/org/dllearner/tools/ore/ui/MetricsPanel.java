package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.dllearner.tools.ore.OREManager;
import org.protege.editor.core.PropertyUtil;
import org.protege.editor.core.ProtegeProperties;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.metrics.OWLMetric;
import org.semanticweb.owlapi.metrics.OWLMetricManager;
import org.semanticweb.owlapi.metrics.ReferencedClassCount;
import org.semanticweb.owlapi.metrics.ReferencedDataPropertyCount;
import org.semanticweb.owlapi.metrics.ReferencedIndividualCount;
import org.semanticweb.owlapi.metrics.ReferencedObjectPropertyCount;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Oct-2007<br>
 * <br>
 */
public class MetricsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5827197898985671614L;

	private OWLOntologyManager manager;

	private OWLMetricManager metricManager;

	public MetricsPanel() {
//		this.manager = OWLManager.createOWLOntologyManager();
//		initialiseOWLView();
	}

	protected void initialiseOWLView() {
		createBasicMetrics();
		createUI();
	}

	private void createUI() {

		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		MetricsTableModel tableModel = new MetricsTableModel(metricManager);

		final JTable table = new JTable(tableModel);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setRowHeight(table.getRowHeight() + 4);
		table.setShowGrid(true);
		table.setBackground(SystemColor.control);
		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().setColumnMargin(2);
		table.setFont(getFont().deriveFont(Font.BOLD, 12.0f));
		table.setForeground(PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(
				ProtegeProperties.PROPERTY_COLOR_KEY), Color.GRAY));

		final JPanel tablePanel = new JPanel(new BorderLayout());

		tablePanel.add(table);
		tablePanel.setFont(getFont().deriveFont(Font.BOLD, 12.0f));
		// tablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,
		// 2, 14, 2),
		// ComponentFactory.createTitledBorder(metricsSet)));
		table.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panel.add(tablePanel);

		JScrollPane sp = new JScrollPane(panel);
		sp.setOpaque(false);
		add(sp);
	}

	@SuppressWarnings("unchecked")
	private void createBasicMetrics() {
		List<OWLMetric> metrics = new ArrayList<OWLMetric>();
		metrics.add(new ReferencedClassCount(manager));
		metrics.add(new ReferencedObjectPropertyCount(manager));
		metrics.add(new ReferencedDataPropertyCount(manager));
		metrics.add(new ReferencedIndividualCount(manager));
		metrics.add(new DLExpressivity(manager));
		metricManager = new OWLMetricManager(metrics);
		
	}

	public void updateView(OWLOntology activeOntology) {
		removeAll();
		manager = OREManager.getInstance().getReasoner().getOWLOntologyManager();
		createBasicMetrics();
		metricManager.setOntology(activeOntology);
		for (OWLMetric<?> m : metricManager.getMetrics()) {
			m.setImportsClosureUsed(true);
		}
		createUI();

		TitledBorder border = new TitledBorder(activeOntology.getOntologyID().toString() + " successfully loaded");
		border.setTitleFont(getFont().deriveFont(Font.BOLD, 12.0f));
		border.setTitleColor(PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(
				ProtegeProperties.PROPERTY_COLOR_KEY), Color.GRAY));
		setBorder(border);
		repaint();
	}
	

}
