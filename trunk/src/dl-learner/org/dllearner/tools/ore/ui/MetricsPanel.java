package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.metrics.AxiomCountMetric;
import org.semanticweb.owl.metrics.AxiomTypeMetric;
import org.semanticweb.owl.metrics.DLExpressivity;
import org.semanticweb.owl.metrics.GCICount;
import org.semanticweb.owl.metrics.HiddenGCICount;
import org.semanticweb.owl.metrics.OWLMetric;
import org.semanticweb.owl.metrics.OWLMetricManager;
import org.semanticweb.owl.metrics.ReferencedClassCount;
import org.semanticweb.owl.metrics.ReferencedDataPropertyCount;
import org.semanticweb.owl.metrics.ReferencedIndividualCount;
import org.semanticweb.owl.metrics.ReferencedObjectPropertyCount;
import org.semanticweb.owl.model.AxiomType;
import org.semanticweb.owl.model.OWLOntology;
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

    private JPopupMenu popupMenu;

    private AxiomCountMetric lastMetric;

    public MetricsPanel( OWLOntologyManager manager) {
    	this.manager = manager;
        
        initialiseOWLView();
        createPopupMenu();
    }


    private void createPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu.add(new AbstractAction("Show axioms") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
//                showAxiomTypeDialog();

            }
        });
    }


//    private void showAxiomTypeDialog() {
//        Set<? extends OWLAxiom> axioms = lastMetric.getAxioms();
//        final OWLAxiomTypeFramePanel panel = new OWLAxiomTypeFramePanel(owlEditorKit);
//        Set<OWLAxiom> axs = new HashSet<OWLAxiom>(axioms);
//        panel.setRoot(axs);
//        panel.setPreferredSize(new Dimension(800, 300));
//        JOptionPane op = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
//        JDialog dlg = op.createDialog(this, lastMetric.getName());
//        dlg.setResizable(true);
//        dlg.addWindowListener(new WindowAdapter() {
//
//            public void windowClosed(WindowEvent e) {
//                panel.dispose();
//            }
//        });
//        dlg.setModal(false);
//        dlg.setVisible(true);
//    }


    protected void initialiseOWLView() {
        metricManagerMap = new LinkedHashMap<String, OWLMetricManager>();
        tableModelMap = new HashMap<OWLMetricManager, MetricsTableModel>();
        createBasicMetrics();
        createClassAxiomMetrics();
        createObjectPropertyAxiomMetrics();
        createDataPropertyAxiomMetrics();
        createIndividualAxiomMetrics();
        createAnnotationAxiomMetrics();
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
        Box box = new Box(BoxLayout.Y_AXIS);
        for (String metricsSet : metricManagerMap.keySet()) {
            MetricsTableModel tableModel = new MetricsTableModel(metricManagerMap.get(metricsSet));
            tableModelMap.put(metricManagerMap.get(metricsSet), tableModel);
            final JTable table = new JTable(tableModel);
            table.setGridColor(Color.LIGHT_GRAY);
            table.setRowHeight(table.getRowHeight() + 4);
            table.setShowGrid(true);
            table.getColumnModel().getColumn(1).setMaxWidth(150);
            table.getColumnModel().setColumnMargin(2);
            table.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    if(e.isPopupTrigger()) {
                        handleTablePopupRequest(table, e);
                    }
                }


                public void mouseReleased(MouseEvent e) {
                    if(e.isPopupTrigger()) {
                        handleTablePopupRequest(table, e);
                    }
                }

                private void handleTablePopupRequest(JTable table, MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if(row == -1 || col == -1) {
                        return;
                    }
                    MetricsTableModel model = (MetricsTableModel) table.getModel();
                    for(OWLMetricManager man : tableModelMap.keySet()) {
                        if(tableModelMap.get(man).equals(model)) {
                            OWLMetric<?> metric = man.getMetrics().get(row);
                            if(metric instanceof AxiomCountMetric) {
                                lastMetric = (AxiomCountMetric) metric;
                                popupMenu.show(table, e.getX(), e.getY());
                            }
                            break;
                        }
                    }

                }
            });

            final JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    if(e.isPopupTrigger()) {
                        showMenu(e);
                    }
                }


                public void mouseReleased(MouseEvent e) {
                    if(e.isPopupTrigger()) {
                        showMenu(e);
                    }
                }

                private void showMenu(MouseEvent e) {
                    JPopupMenu menu = new JPopupMenu();
                    menu.add(new AbstractAction("Copy metrics to clipboard") {

                        /**
						 * 
						 */
						private static final long serialVersionUID = 6638146469347852653L;

						public void actionPerformed(ActionEvent e) {
                            exportCSV();
                        }
                    });
                    menu.show(tablePanel, e.getX(), e.getY());
                }
            });
            tablePanel.add(table);
            tablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 14, 2),
                                                                    ComponentFactory.createTitledBorder(metricsSet)));
            table.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            box.add(tablePanel);
        }
        JScrollPane sp = new JScrollPane(box);
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


    private void createClassAxiomMetrics() {
        List<OWLMetric> metrics = new ArrayList<OWLMetric>();
        metrics.add(new AxiomTypeMetric(manager, AxiomType.SUBCLASS));
        metrics.add(new AxiomTypeMetric(manager, AxiomType.EQUIVALENT_CLASSES));
        metrics.add(new AxiomTypeMetric(manager, AxiomType.DISJOINT_CLASSES));
        metrics.add(new GCICount(manager));
        metrics.add(new HiddenGCICount(manager));
        OWLMetricManager metricManager = new OWLMetricManager(metrics);
        metricManagerMap.put("Class axioms", metricManager);
    }


    private void createObjectPropertyAxiomMetrics() {
        List<OWLMetric> metrics = new ArrayList<OWLMetric>();
        metrics.add(new AxiomTypeMetric(manager, AxiomType.SUB_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.EQUIVALENT_OBJECT_PROPERTIES));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.INVERSE_OBJECT_PROPERTIES));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.DISJOINT_OBJECT_PROPERTIES));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.FUNCTIONAL_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.TRANSITIVE_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.SYMMETRIC_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.ANTI_SYMMETRIC_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.REFLEXIVE_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.IRREFLEXIVE_OBJECT_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.OBJECT_PROPERTY_DOMAIN));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.OBJECT_PROPERTY_RANGE));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.PROPERTY_CHAIN_SUB_PROPERTY));
        OWLMetricManager metricManager = new OWLMetricManager(metrics);
        metricManagerMap.put("Object property axioms", metricManager);
    }


    private void createDataPropertyAxiomMetrics() {
        List<OWLMetric> metrics = new ArrayList<OWLMetric>();
        metrics.add(new AxiomTypeMetric(manager, AxiomType.SUB_DATA_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.EQUIVALENT_DATA_PROPERTIES));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.DISJOINT_DATA_PROPERTIES));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.FUNCTIONAL_DATA_PROPERTY));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.DATA_PROPERTY_DOMAIN));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.DATA_PROPERTY_RANGE));        
        OWLMetricManager metricManager = new OWLMetricManager(metrics);
        metricManagerMap.put("Data property axioms", metricManager);
    }


    private void createIndividualAxiomMetrics() {
        List<OWLMetric> metrics = new ArrayList<OWLMetric>();
        metrics.add(new AxiomTypeMetric(manager, AxiomType.CLASS_ASSERTION));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.OBJECT_PROPERTY_ASSERTION));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.DATA_PROPERTY_ASSERTION));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION));
        metrics.add(new AxiomTypeMetric(manager,
                                        AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION));
        metrics.add(new AxiomTypeMetric(manager, AxiomType.SAME_INDIVIDUAL));
        metrics.add(new AxiomTypeMetric(manager, AxiomType.DIFFERENT_INDIVIDUALS));
        OWLMetricManager metricManager = new OWLMetricManager(metrics);
        metricManagerMap.put("Individual axioms", metricManager);
    }


    private void createAnnotationAxiomMetrics() {
        List<OWLMetric> metrics = new ArrayList<OWLMetric>();
        metrics.add(new AxiomTypeMetric(manager, AxiomType.ENTITY_ANNOTATION));
        metrics.add(new AxiomTypeMetric(manager, AxiomType.AXIOM_ANNOTATION));
        OWLMetricManager metricManager = new OWLMetricManager(metrics);
        metricManagerMap.put("Annotation axioms", metricManager);
    }
    

    public void updateView(OWLOntology activeOntology) {
        for (OWLMetricManager man : metricManagerMap.values()) {
            man.setOntology(activeOntology);
        }
        repaint();
    }

    

    private void exportCSV() {
        StringBuilder sb = new StringBuilder();
        for(OWLMetricManager man : metricManagerMap.values()) {
            sb.append(man.toString());
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);
    }
    
    public static void main(String[] args) throws OWLOntologyCreationException{
    	String file = "file:examples/ore/tambis.owl";
  	  
  	 
  	  OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
  		OWLOntology ontology = manager.loadOntology( URI.create( file ) );
  		JFrame test = new JFrame();
  		test.setBounds(200, 200, 600, 200);
  		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  		test.add(new MetricsPanel(manager));
  		test.setVisible(true);
  		
  		
    }
}
