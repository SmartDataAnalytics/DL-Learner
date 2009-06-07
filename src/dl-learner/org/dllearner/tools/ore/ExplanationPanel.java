package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.progress.SwingProgressMonitor;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ExplanationPanel extends JPanel implements ListSelectionListener,
		ActionListener,ImpactManagerListener{

	private JXList unsatList;
	private JSplitPane splitPane;
	private JSplitPane statsSplitPane;
	private JScrollPane listScrollPane;
	private JScrollPane explanationsScrollPane;
	private JComponent explanationsPanel;
	private JPanel buttonExplanationsPanel;
	private JPanel buttonPanel;
	private ButtonGroup explanationType;
	private JRadioButton regularButton;
	private JRadioButton laconicButton;

	private UnsatClassesListCellRenderer listRenderer;

	private ExplanationManager expManager;
	private ImpactManager impManager;

	
	
	private OWLClass unsatClass;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2213073383532597460L;

	public ExplanationPanel(ExplanationManager expMan, ImpactManager impMan) {

		
		this.expManager = expMan;
		this.impManager = impMan;

		impManager.addListener(this);
		setLayout(new BorderLayout());

		Dimension minimumSize = new Dimension(400, 400);

		listRenderer = new UnsatClassesListCellRenderer(expManager);
		unsatList = new JXList();
		fillUnsatClassesList();
		unsatList.addListSelectionListener(this);
		unsatList.setCellRenderer(listRenderer);
		listScrollPane = new JScrollPane(unsatList);
		listScrollPane.setPreferredSize(minimumSize);

		explanationsPanel = new Box(1);

		JPanel pan = new JPanel(new BorderLayout());
		pan.add(explanationsPanel, BorderLayout.NORTH);
		explanationsScrollPane = new JScrollPane(pan);
		explanationsScrollPane.setPreferredSize(minimumSize);
		explanationsScrollPane.setBorder(BorderFactory
				.createLineBorder(Color.LIGHT_GRAY));
		explanationsScrollPane.getViewport().setOpaque(false);
		explanationsScrollPane.getViewport().setBackground(null);
		explanationsScrollPane.setOpaque(false);

		regularButton = new JRadioButton("regular", true);
		regularButton.setActionCommand("regular");
		regularButton.addActionListener(this);
		laconicButton = new JRadioButton("laconic");
		laconicButton.setActionCommand("laconic");
		laconicButton.addActionListener(this);
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		buttonPanel = new JPanel();
		buttonPanel.add(regularButton);
		buttonPanel.add(laconicButton);

		buttonExplanationsPanel = new JPanel();
		buttonExplanationsPanel.setLayout(new BorderLayout());
		buttonExplanationsPanel
				.add(explanationsScrollPane, BorderLayout.CENTER);
		buttonExplanationsPanel.add(buttonPanel, BorderLayout.NORTH);

		statsSplitPane = new JSplitPane(0);
		statsSplitPane.setResizeWeight(1.0D);
		statsSplitPane.setTopComponent(buttonExplanationsPanel);
		
		//repair panel
		JPanel impactRepairPanel = new JPanel();
		impactRepairPanel.setLayout(new BorderLayout());
		impactRepairPanel.add(new JLabel("Repair plan"), BorderLayout.NORTH);
		JSplitPane impRepSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		impRepSplit.setOneTouchExpandable(true);
		impRepSplit.setDividerLocation(600);
		impRepSplit.setBorder(null);
		impactRepairPanel.add(impRepSplit);
		
		JPanel impactPanel = new JPanel();
		impactPanel.setLayout(new BorderLayout());
		impactPanel.add(new JLabel("Lost entailments"), BorderLayout.NORTH);
		JScrollPane impScr = new JScrollPane(new ImpactTable(impManager));
		impactPanel.add(impScr);
		impRepSplit.setRightComponent(impactPanel);
		
		RepairPlanPanel repairPanel = new RepairPlanPanel(impManager); 
		impRepSplit.setLeftComponent(repairPanel);
		
		
		statsSplitPane.setBottomComponent(impactRepairPanel);
		
		statsSplitPane.setBorder(null);
		statsSplitPane.setDividerLocation(500);
		statsSplitPane.setOneTouchExpandable(true);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane,
				statsSplitPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		splitPane.setBorder(null);

		add(splitPane);
	}

	private void fillUnsatClassesList() {
		DefaultListModel model = new DefaultListModel();
		Set<OWLClass> rootClasses = new TreeSet<OWLClass>(expManager
				.getRootUnsatisfiableClasses());
		for (OWLClass root : rootClasses) {
			model.addElement(root);
		}
		Set<OWLClass> derivedClasses = new TreeSet<OWLClass>(expManager
				.getUnsatisfiableClasses());
		derivedClasses.removeAll(rootClasses);
		for (OWLClass unsat : derivedClasses) {
			model.addElement(unsat);

		}
		unsatList.setModel(model);
	}

	private void addExplanationTable(List<OWLAxiom> explanation, int number) {

		// DLSyntaxObjectRenderer r = new DLSyntaxObjectRenderer();
		// Vector<String> t = new Vector<String>();
		// for(OWLAxiom ax : explanation)
		// t.add(r.render(ax));
		// model.addColumn("axiom", t);
		
		ExplanationTable expTable = new ExplanationTable(explanation, impManager, expManager, unsatClass);
		explanationsPanel.add(new ExplanationTablePanel(expTable, number));

	}

	private void clearExplanationsPanel() {
		explanationsPanel.removeAll();
	}

	private void showLaconicExplanations() {
		clearExplanationsPanel();
		int counter = 1;
		for (List<OWLAxiom> explanation : expManager
				.getOrderedLaconicUnsatisfiableExplanations(unsatClass)) {
			addExplanationTable(explanation, counter);
			counter++;
		}
		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationsPanel.add(new JSeparator());
		explanationsPanel.add(Box.createVerticalStrut(10));
		this.updateUI();
	}

	private void showRegularExplanations() {
		clearExplanationsPanel();
		int counter = 1;
		for (List<OWLAxiom> explanation : expManager
				.getOrderedUnsatisfiableExplanations(unsatClass)) {
			addExplanationTable(explanation, counter);
			counter++;
		}
		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationsPanel.add(new JSeparator());
		explanationsPanel.add(Box.createVerticalStrut(10));
		this.updateUI();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		unsatClass = (OWLClass) ((JXList) e.getSource()).getSelectedValue();

//		OWLEditorKitFactory edFac = new OWLEditorKitFactory();
//		edFac.canLoad(URI.create("file:examples/ore/koala.owl"));
//		
//		try {
//			EditorKit kit = edFac.createEditorKit();
//		} catch (Exception e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		
//		OWLModelManager m = new OWLModelManagerImpl();
//		try {
//			m.loadOntology(URI.create("file:examples/ore/koala.owl"));
//		} catch (OWLOntologyCreationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		OWLExpressionCheckerFactory f = new ManchesterOWLExpressionCheckerFactory(m);
//		OWLExpressionChecker<OWLDescription> checker = f.getOWLDescriptionChecker();
//		
//		ExpressionEditor<OWLDescription> editor2 = new ExpressionEditor<OWLDescription>();
//		editor2.setVisible(true);
//		editor.setDescription(unsatClass);
		
		
		if (!unsatList.isSelectionEmpty() && regularButton.isSelected()) {
			showRegularExplanations();
		} else if(!unsatList.isSelectionEmpty()){
			showLaconicExplanations();
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("regular")) {
			showRegularExplanations();
		} else if (e.getActionCommand().equals("laconic")
				&& !unsatList.isSelectionEmpty()) {
			showLaconicExplanations();

		}

	}
	
	@Override
	public void axiomForImpactChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted() {
		explanationsPanel.removeAll();
		
		fillUnsatClassesList();
		repaint();
	}
	

	public static void main(String[] args) {

		try {
			String file = "file:examples/ore/tambis.owl";
			PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.SWING;
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI
					.create(file));
			
			org.mindswap.pellet.utils.progress.ProgressMonitor mon = new SwingProgressMonitor();
			org.mindswap.pellet.utils.progress.ProgressMonitor m = new ClassificationProgressMonitor();
			JFrame fr = new JFrame();
			fr.setSize(new Dimension(400, 400));
			fr.setLayout(new BorderLayout());
			fr.add((JPanel)m);
//			fr.setVisible(true);
			PelletReasonerFactory reasonerFactory = new PelletReasonerFactory();
			Reasoner reasoner = reasonerFactory.createReasoner(manager);
			reasoner.loadOntologies(Collections.singleton(ontology));
			reasoner.getKB().getTaxonomyBuilder().setProgressMonitor(mon);
//			mon.taskStarted();
			
			
			reasoner.classify();
			
			
			
			
			
//			try {
//				String text = "Koala SubclassOf Class: Animal";
//				OWLEntityChecker checker = new EntityChecker(manager);
//				ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(manager.getOWLDataFactory(),text);
//				parser.setOWLEntityChecker(checker);
//				parser.parseOntology(manager, ontology);
//				parser.setBase(ontology.getURI().toString() + "#");
//				parser.parseDescription();
//			} catch (OWLOntologyChangeException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ParserException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			
			
			ExplanationManager expManager = ExplanationManager
					.getExplanationManager(reasoner);
			ImpactManager impManager = ImpactManager.getImpactManager(
					reasoner);
			ExplanationPanel panel = new ExplanationPanel(expManager,
					impManager);
		
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			JFrame test = new JFrame();
			test.setLayout(new GridLayout(0, 1));
			test.setSize(new Dimension(1400, 1000));
			test.add(panel);
			test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			test.setVisible(true);
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	

	
}
