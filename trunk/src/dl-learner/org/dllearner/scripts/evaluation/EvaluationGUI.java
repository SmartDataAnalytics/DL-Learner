package org.dllearner.scripts.evaluation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ui.GraphicalCoveragePanel;
import org.dllearner.tools.ore.ui.MarkableClassesTable;
import org.dllearner.tools.ore.ui.ResultTable;
import org.dllearner.tools.ore.ui.SelectableClassExpressionsTable;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.mindswap.pellet.utils.SetUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.clarkparsia.owlapi.explanation.io.manchester.Keyword;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.owlapi.explanation.io.manchester.TextBlockWriter;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

public class EvaluationGUI extends JFrame implements ActionListener, ListSelectionListener, MouseMotionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3097551929270352556L;
	
	private File inputFile;

	private RatingTablePanel tab1;
	private RatingTablePanel tab2;
	private RatingTablePanel tab3;
	private RatingTablePanel tab4;
	private RatingTablePanel tab5;
	private RatingTablePanel tab6;
	private RatingTablePanel tab7;
	private RatingTablePanel tab8;
	private RatingTablePanel tab9;
	private RatingTablePanel tab10;

	private SelectableClassExpressionsTable defaultTab;
	private static String INCONSISTENCYWARNING = 
	"<html>Warning. Selected class expressions leads to an inconsistent ontology!<br>" +
	"(Often, suggestions leading to an inconsistency should still be added. They help to detect problems in " +
	"the ontology elsewhere.<br>" +
	" See http://dl-learner.org/files/screencast/protege/screencast.htm .)</html>";
	private JLabel inconsistencyLabel;
	private JCheckBox noSuggestionCheckBox;
	private JCheckBox alternateSuggestionCheckBox;
	private ButtonGroup bg;

	private GraphicalCoveragePanel graphPanel;
	private GraphicalCoveragePanel graphPanel2;

	private MarkableClassesTable classesTable;
	private JButton nextFinishButton;
	private JLabel messageLabel;

	private JWindow coverageWindow;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	private ResultTable mouseOverTable;
	private int oldRow;

	private static String SUPERCLASSTEXT = "Showing suggestions for superclasses of ";
	private static String EQUIVALENTCLASSTEXT = "Showing suggestions for classes equivalent to ";

	private static String SINGLETABLEVIEW = "single";
	private static String MULTITABLEVIEW = "multi";

	private int currentClassIndex = 0;

	private boolean showingEquivalentSuggestions = false;
	private boolean showingMultiTables = false;

	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceStandardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalencePredaccMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceGenFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceJaccardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();

	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastSuperStandardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastSuperFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastSuperPredaccMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastSuperGenFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastSuperJaccardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();

	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceStandardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalencePredaccMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceGenFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceJaccardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();

	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlSuperStandardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlSuperFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlSuperPredaccMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlSuperGenFMeasureMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlSuperJaccardMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();

	private Map<NamedClass, List<EvaluatedDescriptionClass>> defaultEquivalenceMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> defaultSuperMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();

	private String baseURI;
	private Map<String, String> prefixes;

	private Map<NamedClass, Set<OWLClassExpression>> assertedEquivalentClasses = new HashMap<NamedClass, Set<OWLClassExpression>>();
	private Map<NamedClass, Set<OWLClassExpression>> assertedSuperClasses = new HashMap<NamedClass, Set<OWLClassExpression>>();

	
	private Map<NamedClass, String> selectedEquivalenceMap = new HashMap<NamedClass, String>();
	private Map<NamedClass, String> selectedSuperMap = new HashMap<NamedClass, String>();
	
	private Map<NamedClass, List<Integer>> equivalentClassListRating = new HashMap<NamedClass, List<Integer>>();
	private Map<NamedClass, List<Integer>> superClassListRating = new HashMap<NamedClass, List<Integer>>();

	public EvaluationGUI(File input) throws ComponentInitException, MalformedURLException,
			LearningProblemUnsupportedException {
		super();
		inputFile = input;
		loadResults(input);
		setTitle(input.getName());
		createUI();
		createCoverageWindow();
		classesTable.setSelectedClass(currentClassIndex);
		graphPanel.initManchesterSyntax(baseURI, prefixes);
		graphPanel2.initManchesterSyntax(baseURI, prefixes);
		graphPanel.setConcept(classesTable.getSelectedClass(currentClassIndex));
		graphPanel2.setConcept(classesTable.getSelectedClass(currentClassIndex));
		if (defaultEquivalenceMap.get(classesTable.getSelectedClass(currentClassIndex)) != null) {
			showEquivalentSuggestions(classesTable.getSelectedClass(currentClassIndex));
		} else {
			showSuperSuggestions(classesTable.getSelectedClass(currentClassIndex));
		}

		cardLayout.last(cardPanel);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}

	private void createUI() {

		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setDividerLocation(0.3);
		split.setOneTouchExpandable(true);
		classesTable = new MarkableClassesTable();
		classesTable.addClasses(new TreeSet<NamedClass>(SetUtils.union(defaultEquivalenceMap.keySet(), defaultSuperMap
				.keySet())));
		JScrollPane classesScroll = new JScrollPane(classesTable);
		classesTable.addMouseMotionListener(this);
		split.setLeftComponent(classesScroll);

		JPanel holder = new JPanel(new BorderLayout());
		JScrollPane suggestionsScroll = new JScrollPane(createMainPanel());
		holder.add(suggestionsScroll, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		JSeparator separator = new JSeparator();
		buttonPanel.add(separator, BorderLayout.NORTH);
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		nextFinishButton = new JButton("Next");
		nextFinishButton.setActionCommand("next");
		nextFinishButton.addActionListener(this);
		buttonBox.add(nextFinishButton);
		buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
		holder.add(buttonPanel, BorderLayout.SOUTH);
		split.setRightComponent(holder);
		addMouseMotionListener(this);
		add(split);

	}

	private JPanel createMainPanel() {
		JPanel messageTablesPanel = new JPanel();
		messageTablesPanel.setLayout(new BorderLayout());

		messageLabel = new JLabel();
		messageLabel.addMouseMotionListener(this);
		messageTablesPanel.add(messageLabel, BorderLayout.NORTH);

		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
		cardLayout = new CardLayout();

		cardPanel.add(createMultiTablesPanel(), MULTITABLEVIEW);
		cardPanel.add(createSingleTablePanel(), SINGLETABLEVIEW);
		cardPanel.setLayout(cardLayout);

		messageTablesPanel.add(cardPanel, BorderLayout.CENTER);

		return messageTablesPanel;
	}
	
	private void showInconsistencyWarning(boolean show){
		if(show){
			inconsistencyLabel.setForeground(Color.BLACK);
		} else {
			inconsistencyLabel.setForeground(SystemColor.control);
		}
		
	}

	private JPanel createSingleTablePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.weighty = 0.0;
		JPanel tableHolderPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		defaultTab = new SelectableClassExpressionsTable();
		defaultTab.getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());
		defaultTab.getSelectionModel().addListSelectionListener(this);
		defaultTab.setRowHeightEnabled(true);
		tableHolderPanel.add(new JScrollPane(defaultTab), gbc);
		graphPanel = new GraphicalCoveragePanel("");
		gbc.weightx = 0.0;
		tableHolderPanel.add(graphPanel, gbc);
		panel.add(tableHolderPanel, c);
		
		inconsistencyLabel = new JLabel();
		panel.add(inconsistencyLabel, c);
		inconsistencyLabel.setText(INCONSISTENCYWARNING);
		inconsistencyLabel.setForeground(SystemColor.control);

		c.weightx = 1.0;
		c.weighty = 0.0;
		c.ipady = 10;
		c.fill = GridBagConstraints.HORIZONTAL;
		noSuggestionCheckBox = new JCheckBox();
		noSuggestionCheckBox.setAction(new AbstractAction(
				"There is no appropriate suggestion for this class in your opinion.") {

			/**
					 * 
					 */
					private static final long serialVersionUID = 5923669465504160583L;

			@Override
			public void actionPerformed(ActionEvent e) {
				defaultTab.clearSelection();
				defaultTab.removeSelection();
				graphPanel.clear();
				showInconsistencyWarning(false);
			}
		});
		panel.add(noSuggestionCheckBox, c);

		alternateSuggestionCheckBox = new JCheckBox();
		alternateSuggestionCheckBox.setAction(new AbstractAction(
				"There is an appropriate suggestion in your opinion, but the algorithm did not suggest it.") {

			/**
					 * 
					 */
					private static final long serialVersionUID = -8642827827310795390L;

			@Override
			public void actionPerformed(ActionEvent e) {
				defaultTab.clearSelection();
				defaultTab.removeSelection();
				graphPanel.clear();
				showInconsistencyWarning(false);
			}
		});
		panel.add(alternateSuggestionCheckBox, c);

		bg = new ButtonGroup();
		bg.add(alternateSuggestionCheckBox);
		bg.add(noSuggestionCheckBox);
		noSuggestionCheckBox.setSelected(true);
		return panel;
	}

	private JPanel createMultiTablesPanel() {
		JPanel tablesHolderPanel = new JPanel();
		tablesHolderPanel.setLayout(new GridLayout(5, 2, 5, 5));
		tablesHolderPanel.addMouseMotionListener(this);
		tab1 = new RatingTablePanel();
		tab1.addMouseMotionListener(this);
		tablesHolderPanel.add(tab1);
		tab2 = new RatingTablePanel();
		tab2.addMouseMotionListener(this);
		tablesHolderPanel.add(tab2);
		tab3 = new RatingTablePanel();
		tab3.addMouseMotionListener(this);
		tablesHolderPanel.add(tab3);
		tab4 = new RatingTablePanel();
		tab4.addMouseMotionListener(this);
		tablesHolderPanel.add(tab4);
		tab5 = new RatingTablePanel();
		tab5.addMouseMotionListener(this);
		tablesHolderPanel.add(tab5);
		tab6 = new RatingTablePanel();
		tab6.addMouseMotionListener(this);
		tablesHolderPanel.add(tab6);
		tab7 = new RatingTablePanel();
		tab7.addMouseMotionListener(this);
		tablesHolderPanel.add(tab7);
		tab8 = new RatingTablePanel();
		tab8.addMouseMotionListener(this);
		tablesHolderPanel.add(tab8);
		tab9 = new RatingTablePanel();
		tab9.addMouseMotionListener(this);
		tablesHolderPanel.add(tab9);
		tab10 = new RatingTablePanel();
		tab10.addMouseMotionListener(this);
		tablesHolderPanel.add(tab10);

		return tablesHolderPanel;
	}

	private void showSingleTable() {
		
		graphPanel.clear();
		cardLayout.last(cardPanel);
		showingMultiTables = false;
	}

	private void showMultiTables() {
		cardLayout.first(cardPanel);
		showingMultiTables = true;
	}

	private void showEquivalentSuggestions(NamedClass nc) {
		messageLabel.setText("<html>" + EQUIVALENTCLASSTEXT
				+ "<b>" + classesTable.getSelectedClass(currentClassIndex).toManchesterSyntaxString(baseURI, prefixes)
				+ "</b></html>");
		if (owlEquivalenceStandardMap.get(nc) != null) {

			tab1.addResults(owlEquivalenceStandardMap.get(nc));
			tab2.addResults(owlEquivalenceFMeasureMap.get(nc));
			tab3.addResults(owlEquivalencePredaccMap.get(nc));
			tab4.addResults(owlEquivalenceJaccardMap.get(nc));
			tab5.addResults(owlEquivalenceGenFMeasureMap.get(nc));

			tab6.addResults(fastEquivalenceStandardMap.get(nc));
			tab7.addResults(fastEquivalenceFMeasureMap.get(nc));
			tab8.addResults(fastEquivalencePredaccMap.get(nc));
			tab9.addResults(fastEquivalenceJaccardMap.get(nc));
			tab10.addResults(fastEquivalenceGenFMeasureMap.get(nc));
		}
		defaultTab.addResults(defaultEquivalenceMap.get(nc));

		showingEquivalentSuggestions = true;
	}

	private void showSuperSuggestions(NamedClass nc) {
		messageLabel.setText("<html>" + SUPERCLASSTEXT
				+ "<b>" + classesTable.getSelectedClass(currentClassIndex).toManchesterSyntaxString(baseURI, prefixes) + "</b></html>");

		if (owlSuperStandardMap.get(nc) != null) {

			tab1.addResults(owlSuperStandardMap.get(nc));
			tab2.addResults(owlSuperFMeasureMap.get(nc));
			tab3.addResults(owlSuperPredaccMap.get(nc));
			tab4.addResults(owlSuperJaccardMap.get(nc));
			tab5.addResults(owlSuperGenFMeasureMap.get(nc));

			tab6.addResults(fastSuperStandardMap.get(nc));
			tab7.addResults(fastSuperFMeasureMap.get(nc));
			tab8.addResults(fastSuperPredaccMap.get(nc));
			tab9.addResults(fastSuperJaccardMap.get(nc));
			tab10.addResults(fastSuperGenFMeasureMap.get(nc));
		}

		defaultTab.addResults(defaultSuperMap.get(nc));

		showingEquivalentSuggestions = false;
	}

	private void createCoverageWindow() {
		coverageWindow = new JWindow(this);
		graphPanel2 = new GraphicalCoveragePanel("");
		coverageWindow.add(graphPanel2);
		coverageWindow.pack();
		// coverageWindow.setLocationRelativeTo(classesTable);
	}

	@SuppressWarnings("unchecked")
	private void loadResults(File input) {
		InputStream fis = null;

		try {
			fis = new FileInputStream(input);
			ObjectInputStream o = new ObjectInputStream(fis);

			owlEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

			owlSuperStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperPredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

			fastEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

			fastSuperStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperPredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

			defaultEquivalenceMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			defaultSuperMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

			baseURI = (String) o.readObject();
			prefixes = (Map<String, String>) o.readObject();

			assertedEquivalentClasses = (Map<NamedClass, Set<OWLClassExpression>>) o.readObject();
			assertedSuperClasses = (Map<NamedClass, Set<OWLClassExpression>>) o.readObject();

		}

		catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}

	private void showCoveragePanel(boolean show) {
		final boolean visible = show;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				coverageWindow.setVisible(visible);

			}
		});

	}

	private void setCoverageLocationRelativeTo(Component component) {
		Component parent = component.getParent();
		Point componentLocation = component.getLocationOnScreen();
		Point p;
		if (componentLocation.getX() < parent.getSize().width / 2) {
			p = new Point((int) (componentLocation.getX() + parent.getSize().width / 2 + coverageWindow.getSize()
					.getWidth() / 2), componentLocation.y);
		} else {
			p = new Point((int) (componentLocation.getX() - parent.getSize().width / 2 - coverageWindow.getSize()
					.getWidth() / 2), componentLocation.y);
		}

		coverageWindow.setLocation(p);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		traceInput();
		if (e.getActionCommand().equals("next")) {
			defaultTab.clearSelection();
			
			NamedClass nc = classesTable.getSelectedClass(currentClassIndex);
			if(!showingMultiTables){
				
			}
			if (showingMultiTables && showingEquivalentSuggestions) {
				if (defaultSuperMap.get(nc) != null) {
					showSuperSuggestions(nc);
					showSingleTable();
				} else {
					currentClassIndex++;
					classesTable.setSelectedClass(currentClassIndex);
					graphPanel.setConcept(classesTable.getSelectedClass(currentClassIndex));

					
					if(defaultEquivalenceMap.get(classesTable.getSelectedClass(currentClassIndex)) != null){
						showEquivalentSuggestions(classesTable.getSelectedClass(currentClassIndex));
					} else {
						showSuperSuggestions(classesTable.getSelectedClass(currentClassIndex));
					}
					showSingleTable();
				}

			} else if (!showingMultiTables && showingEquivalentSuggestions) {
				if (owlEquivalenceStandardMap.get(nc) != null) {
					showMultiTables();
				} else if (defaultSuperMap.get(nc) != null) {
					showSuperSuggestions(nc);
					showSingleTable();
//					if (currentClassIndex + 1 >= defaultEquivalenceMap.keySet().size()) {
//						nextFinishButton.setText("Finish");
//						nextFinishButton.setActionCommand("finish");
//					}
				} else {
					currentClassIndex++;
					classesTable.setSelectedClass(currentClassIndex);
					graphPanel.setConcept(classesTable.getSelectedClass(currentClassIndex));
					if(defaultEquivalenceMap.get(classesTable.getSelectedClass(currentClassIndex)) != null){
						showEquivalentSuggestions(classesTable.getSelectedClass(currentClassIndex));
					} else {
						showSuperSuggestions(classesTable.getSelectedClass(currentClassIndex));
					}
					
					showSingleTable();
				}

			} else if (!showingMultiTables && !showingEquivalentSuggestions) {
				if (owlSuperStandardMap.get(nc) != null) {
					showMultiTables();

				} else {

					currentClassIndex++;
					classesTable.setSelectedClass(currentClassIndex);
					NamedClass newNc = classesTable.getSelectedClass(currentClassIndex);
					graphPanel.setConcept(newNc);
					if (defaultEquivalenceMap.get(newNc) != null) {
						showEquivalentSuggestions(newNc);
					} else {
						showSuperSuggestions(newNc);
					}

					showSingleTable();
				}

			} else {

				currentClassIndex++;
				classesTable.setSelectedClass(currentClassIndex);
				NamedClass newCl = classesTable.getSelectedClass(currentClassIndex);
				graphPanel.setConcept(newCl);
				if(defaultEquivalenceMap.containsKey(newCl)){
					showEquivalentSuggestions(newCl);
				} else {
					showSuperSuggestions(newCl);
				}
				
				showSingleTable();
			}
			setFinished();
			resetTablePanels();

		} else if (e.getActionCommand().equals("finish")) {
			closeDialog();
			saveInput();
		}

	}
	
	private void resetTablePanels(){
		tab1.reset();
		tab2.reset();
		tab3.reset();
		tab4.reset();
		tab5.reset();
		tab6.reset();
		tab7.reset();
		tab8.reset();
		tab9.reset();
		tab10.reset();
		bg.clearSelection();
		noSuggestionCheckBox.setSelected(true);
		showInconsistencyWarning(false);
	}
	
	private void traceInput(){
		NamedClass currentClass = classesTable.getSelectedClass(currentClassIndex);
		if (!showingMultiTables) {
			if (alternateSuggestionCheckBox.isSelected()) {
				if (showingEquivalentSuggestions) {
					selectedEquivalenceMap.put(currentClass, "m");
				} else {
					selectedSuperMap.put(currentClass, "m");
				}
			} else if (noSuggestionCheckBox.isSelected()) {
				if (showingEquivalentSuggestions) {
					selectedEquivalenceMap.put(currentClass, "n");
				} else {
					selectedSuperMap.put(currentClass, "n");
				}
			} else {
				int position = defaultTab.getSelectedPosition() - 1;
				if (showingEquivalentSuggestions) {
					selectedEquivalenceMap.put(currentClass, String.valueOf(position));
				} else {
					selectedSuperMap.put(currentClass, String.valueOf(position));
				}
			}
		} else {
			List<Integer> ratingList = new ArrayList<Integer>();
			ratingList.add(tab1.getRatingValue());
			ratingList.add(tab2.getRatingValue());
			ratingList.add(tab3.getRatingValue());
			ratingList.add(tab4.getRatingValue());
			ratingList.add(tab5.getRatingValue());
			ratingList.add(tab6.getRatingValue());
			ratingList.add(tab7.getRatingValue());
			ratingList.add(tab8.getRatingValue());
			ratingList.add(tab9.getRatingValue());
			ratingList.add(tab10.getRatingValue());
			if(showingEquivalentSuggestions){
				equivalentClassListRating.put(currentClass, ratingList);
			} else {
				superClassListRating.put(currentClass, ratingList);
			}
		}
	}
	
	
	private void saveInput(){
		OutputStream fos = null;
		int index = inputFile.getName().lastIndexOf('.');
		String fileName = "test.inp";
	    if (index>0&& index <= inputFile.getName().length() - 2 ) {
	    	  fileName = inputFile.getName().substring(0, index) + ".inp";
	    }  
		File file = new File(fileName);
		try {
			fos = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			
			o.writeObject(selectedEquivalenceMap);
			o.writeObject(selectedSuperMap);
			
			o.writeObject(equivalentClassListRating);
			o.writeObject(superClassListRating);
			
			o.flush();
			o.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	private void setFinished() {
		NamedClass nc = classesTable.getSelectedClass(currentClassIndex);
		if (currentClassIndex == SetUtils.union(defaultEquivalenceMap.keySet(), defaultSuperMap.keySet()).size() - 1) {
			if (showingEquivalentSuggestions && owlEquivalenceStandardMap.get(nc) == null
					&& defaultSuperMap.get(nc) == null || showingEquivalentSuggestions && showingMultiTables
					&& defaultSuperMap.get(nc) == null || !showingEquivalentSuggestions
					&& owlSuperStandardMap.get(nc) == null || !showingEquivalentSuggestions && showingMultiTables) {
				nextFinishButton.setText("Finish");
				nextFinishButton.setActionCommand("finish");
			}

		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && defaultTab.getSelectedRow() >= 0) {
			EvaluatedDescriptionClass cl = defaultTab.getSelectedValue();
			showInconsistencyWarning(!cl.isConsistent());
			graphPanel.setNewClassDescription(cl);
			if(defaultTab.getSelectedClassExpression() != null){
				bg.clearSelection();
			}
			
		}

	}
	

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getSource() instanceof ResultTable) {
			ResultTable result = ((ResultTable) e.getSource());
			int column = result.columnAtPoint(e.getPoint());
			int row = result.rowAtPoint(e.getPoint());

			if (column == 0 && row >= 0) {
				if (mouseOverTable != result || row != oldRow) {
					mouseOverTable = result;
					oldRow = row;
					EvaluatedDescriptionClass ec = result.getValueAtRow(row);
					graphPanel2.clear();
					graphPanel2.setNewClassDescription(ec);
					setCoverageLocationRelativeTo(result);
					showCoveragePanel(true);
				} else {
					showCoveragePanel(true);
				}
			} else {
				showCoveragePanel(false);
			}
		} else {
			showCoveragePanel(false);
		}

	}

	/**
	 * @param args
	 * @throws ComponentInitException
	 * @throws MalformedURLException
	 * @throws LearningProblemUnsupportedException
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws ComponentInitException,
			LearningProblemUnsupportedException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, URISyntaxException {

		UIManager.setLookAndFeel(new PlasticLookAndFeel());

		if (args.length == 0) {
			System.out.println("You need to give an file as argument.");
			System.exit(0);
		}
		final File input = new File(args[0]);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					new EvaluationGUI(input);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				} catch (LearningProblemUnsupportedException e) {
					e.printStackTrace();
				}

			}
		});

	}


	class RatingTablePanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7408917327199664584L;
		private ResultTable table;
		private RatingPanel rating;

		public RatingTablePanel() {
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			table = new ResultTable();
			table.getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());
			table.setRowHeightEnabled(true);
			add(table, BorderLayout.CENTER);
			rating = new RatingPanel();
			add(rating, BorderLayout.EAST);

		}

		public void addResults(List<EvaluatedDescriptionClass> resultList) {
			table.addResults(resultList);
		}

		public void reset() {
			rating.clearSelection();
		}

		public int getRatingValue() {
			return rating.getSelectedValue();
		}

		public void addMouseMotionListener(MouseMotionListener mL) {
			rating.addMouseMotionListener(mL);
			table.addMouseMotionListener(mL);
		}

	}

	class RatingPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -111227945780885551L;

		private JRadioButton rb1 = new JRadioButton("1");
		private JRadioButton rb2 = new JRadioButton("2");;
		private JRadioButton rb3 = new JRadioButton("3");;
		private JRadioButton rb4 = new JRadioButton("4");;
		private JRadioButton rb5 = new JRadioButton("5");;
		private ButtonGroup bg;
		private ImageIcon thumbs_up = new ImageIcon(EvaluationGUI.class.getResource("Thumb_up.png"));
		private ImageIcon thumbs_down = new ImageIcon(EvaluationGUI.class.getResource("Thumb_down.png"));

		public RatingPanel() {
			setLayout(new GridLayout(7, 1));
			bg = new ButtonGroup();

			add(new JLabel(thumbs_up));
			add(rb5);
			add(rb4);
			add(rb3);
			add(rb2);
			add(rb1);
			add(new JLabel(thumbs_down));
			bg.add(rb1);
			bg.add(rb2);
			bg.add(rb3);
			bg.add(rb4);
			bg.add(rb5);
			rb1.setSelected(true);
		}

		public int getSelectedValue() {
			if (rb1.isSelected()) {
				return 1;
			} else if (rb2.isSelected()) {
				return 2;
			} else if (rb3.isSelected()) {
				return 3;
			} else if (rb4.isSelected()) {
				return 4;
			} else {
				return 5;
			}
		}

		public void clearSelection() {
			rb1.setSelected(true);
		}

	}

}

class MultiLineTableCellRenderer extends JTextPane implements TableCellRenderer{

    /**
 * 
 */
private static final long serialVersionUID = -5375479462711405013L;
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	
	private StringWriter buffer;
	private TextBlockWriter writer;
	private ManchesterSyntaxObjectRenderer renderer;

	private StyledDocument doc;
	Style style;
    public MultiLineTableCellRenderer() {
        super();

        
        setContentType("text/html");
        setBorder(noFocusBorder);
      
        buffer = new StringWriter();
		writer = new TextBlockWriter(buffer);
		renderer = new ManchesterSyntaxObjectRenderer(writer);
		renderer.setWrapLines( false );
		renderer.setSmartIndent( true );
		
		
		doc = (StyledDocument)getDocument();
        style = doc.addStyle("StyleName", null);
        StyleConstants.setItalic(style, true);
     
            

    }


    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column)
    {
        if (isSelected)
        {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }
        else
        {
            super.setForeground(table.getForeground());
            super.setBackground(table.getBackground());
        }

        setFont(table.getFont());

        if (hasFocus)
        {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            if (!isSelected && table.isCellEditable(row, column))
            {
                Color col;
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null)
                {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null)
                {
                    super.setBackground(col);
                }
            }
        }
        else
        {
            setBorder(noFocusBorder);
        }

        setEnabled(table.isEnabled());

        setValue(table, row, column, value);

        return this;
    }

    protected void setValue(JTable table, int row, int column, Object value)
    {
        if (value != null)
        {	
        	String text = value.toString();
        	setText(text);
          
            if(value instanceof Description){
            	OWLClassExpression desc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression((Description)value);
				desc.accept(renderer);
				
				writer.flush();
				String newAxiom = buffer.toString();

				StringTokenizer st = new StringTokenizer(newAxiom);
			
				StringBuffer bf = new StringBuffer();
				bf.append("<html>");
					
				String token;
				while(st.hasMoreTokens()){
					token = st.nextToken();
					
					String color = "black";
					
					boolean isReserved = false;
					if(!token.equals("type") && !token.equals("subClassOf")){
						for(Keyword key : Keyword.values()){
							if(token.equals(key.getLabel())){
								color = key.getColor();
								isReserved = true;break;
							} 
						}
					}
					if(isReserved){
						bf.append("<b><font color=" + color + ">" + token + " </font></b>");
					} else {
						bf.append(" " + token + " ");
					}
				}
				bf.append("</html>");
				newAxiom = bf.toString();
				setText(newAxiom);
//				oldAxioms.add(buffer.toString());
				buffer.getBuffer().delete(0, buffer.toString().length());
			}

            
            View view = getUI().getRootView(this);
            view.setSize((float) table.getColumnModel().getColumn(column).getWidth() - 3, -1);
            float y = view.getPreferredSpan(View.Y_AXIS);
            int h = (int) Math.ceil(y + 3);
            
            if (table.getRowHeight(row) != h)
            {
                table.setRowHeight(row, h );
            }
        }
        else
        {
            setText("");
        }
    }

}
