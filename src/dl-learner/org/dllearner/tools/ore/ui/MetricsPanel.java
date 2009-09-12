package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.protege.editor.core.PropertyUtil;
import org.protege.editor.core.ProtegeProperties;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.metrics.DLExpressivity;
import org.semanticweb.owl.metrics.OWLMetric;
import org.semanticweb.owl.metrics.OWLMetricManager;
import org.semanticweb.owl.metrics.ReferencedClassCount;
import org.semanticweb.owl.metrics.ReferencedDataPropertyCount;
import org.semanticweb.owl.metrics.ReferencedIndividualCount;
import org.semanticweb.owl.metrics.ReferencedObjectPropertyCount;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Oct-2007<br><br>
 */
public class MetricsPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5827197898985671614L;

	private Map<String, OWLMetricManager> metricManagerMap;

    private Map<OWLMetricManager, MetricsTableModel> tableModelMap;

    
    private OWLOntologyManager manager;


    public MetricsPanel() {
    	this.manager = OWLManager.createOWLOntologyManager();
        try {
			manager.createOntology(Collections.<OWLAxiom>emptySet());
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        initialiseOWLView();
    }

    protected void initialiseOWLView() {
        metricManagerMap = new LinkedHashMap<String, OWLMetricManager>();
        tableModelMap = new HashMap<OWLMetricManager, MetricsTableModel>();
        createBasicMetrics();
        createUI();
        updateView(manager.getOntologies().iterator().next());
        for(OWLMetricManager man : metricManagerMap.values()) {
            for(OWLMetric<?> m : man.getMetrics()) {
                m.setImportsClosureUsed(true);
                m.setOntology(manager.getOntologies().iterator().next());
            }
        }
    }

    private void createUI() {
    	
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        for (String metricsSet : metricManagerMap.keySet()) {
            MetricsTableModel tableModel = new MetricsTableModel(metricManagerMap.get(metricsSet));
            tableModelMap.put(metricManagerMap.get(metricsSet), tableModel);
            final JTable table = new JTable(tableModel);
            table.setGridColor(Color.LIGHT_GRAY);
            table.setRowHeight(table.getRowHeight() + 4);
            table.setShowGrid(true);
           
            table.getColumnModel().getColumn(1).setMaxWidth(150);
            table.getColumnModel().setColumnMargin(2);
            table.setFont(getFont().deriveFont(Font.BOLD, 12.0f));
            table.setForeground(PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.PROPERTY_COLOR_KEY),
                                          Color.GRAY));

            final JPanel tablePanel = new JPanel(new BorderLayout());
            
            tablePanel.add(table);
            tablePanel.setFont(getFont().deriveFont(Font.BOLD, 12.0f));
//            tablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 14, 2),
//                                                                    ComponentFactory.createTitledBorder(metricsSet)));
            table.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            panel.add(tablePanel);
        }
        JScrollPane sp = new JScrollPane(panel);
        sp.setOpaque(false);
        add(sp);
    }




    private void createBasicMetrics() {
        List<OWLMetric> metrics = new ArrayList<OWLMetric>();
        metrics.add(new ReferencedClassCount(manager));
        metrics.add(new ReferencedObjectPropertyCount(manager));
        metrics.add(new ReferencedDataPropertyCount(manager));
        metrics.add(new ReferencedIndividualCount(manager));
        metrics.add(new DLExpressivity(manager));
        OWLMetricManager metricManager = new OWLMetricManager(metrics);
        metricManagerMap.put("Metrics", metricManager);
    }

    public void updateView(OWLOntology activeOntology) {
        for (OWLMetricManager man : metricManagerMap.values()) {
            man.setOntology(activeOntology);
        }
        TitledBorder border = new TitledBorder(activeOntology.getURI().toString() + " successfully loaded");
        border.setTitleFont(getFont().deriveFont(Font.BOLD, 12.0f));
        border.setTitleColor(PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.PROPERTY_COLOR_KEY),
                Color.GRAY));
        setBorder(border);
        repaint();
    }
 
}
