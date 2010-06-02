package org.dllearner.tools.ore.ui;

import javax.swing.table.AbstractTableModel;

import org.semanticweb.owlapi.metrics.OWLMetric;
import org.semanticweb.owlapi.metrics.OWLMetricManager;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jul-2007<br><br>
 */
public class MetricsTableModel extends AbstractTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2539916473163242316L;
	private OWLMetricManager metricManager;


    public MetricsTableModel(OWLMetricManager metricManager) {
        this.metricManager = metricManager;
    }


    public int getRowCount() {
        return metricManager.getMetrics().size();
    }


    public int getColumnCount() {
        return 2;
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        OWLMetric<?> metric = metricManager.getMetrics().get(rowIndex);
        if (columnIndex == 0) {
            return metric.getName();
        }
        else {
            return metric.getValue().toString();
        }
    }


    @Override
	public String getColumnName(int column) {
        if (column == 0) {
            return "Metric";
        }
        else {
            return "Value";
        }
    }


    public void includeImports(boolean b) {
        for (OWLMetric<?> metric : metricManager.getMetrics()) {
            metric.setImportsClosureUsed(b);
        }
        fireTableDataChanged();
    }
}
