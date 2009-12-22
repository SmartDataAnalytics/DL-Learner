package org.dllearner.scripts.evaluation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ui.GraphicalCoveragePanel;
import org.dllearner.tools.ore.ui.MarkableClassesTable;
import org.dllearner.tools.ore.ui.ResultTable;
import org.dllearner.tools.ore.ui.SelectableClassExpressionsTable;

public class EvaluationGUI extends JFrame implements ActionListener, ListSelectionListener, MouseMotionListener{
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3097551929270352556L;
	
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
	
	private GraphicalCoveragePanel graphPanel;
	private GraphicalCoveragePanel graphPanel2;
	
	private MarkableClassesTable classesTable;
	private JButton nextFinishButton;
	private JLabel messageLabel;
	
	private JWindow coverageWindow;
	
	private JPanel cardPanel;
	private CardLayout cardLayout;
	
	private static String SUPERCLASSTEXT = "Showing suggestions for super class";
	private static String EQUIVALENTCLASSTEXT = "Showing suggestions for equivalent class";
	
	private static String SINGLETABLEVIEW = "single";
	private static String MULTITABLEVIEW ="multi";
	
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
	
	private Map<NamedClass, EvaluatedDescriptionClass> selectedEquivalenceMap = new HashMap<NamedClass, EvaluatedDescriptionClass>();
	private Map<NamedClass, EvaluatedDescriptionClass> selectedSuperMap = new HashMap<NamedClass, EvaluatedDescriptionClass>();
	

	public EvaluationGUI(File input) throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException{
		super();
		loadResults(input);
		setTitle(input.getName());
		createUI();
		createCoverageWindow();
		classesTable.setSelectedClass(currentClassIndex);
		graphPanel.setConcept(classesTable.getSelectedClass(currentClassIndex));
		graphPanel2.setConcept(classesTable.getSelectedClass(currentClassIndex));
		showEquivalentSuggestions(classesTable.getSelectedClass(currentClassIndex));
		cardLayout.last(cardPanel);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}
	
	private void createUI(){
		
		setLayout(new BorderLayout());
		
		classesTable = new MarkableClassesTable();
		classesTable.addClasses(new TreeSet<NamedClass>(defaultEquivalenceMap.keySet()));
		JScrollPane classesScroll = new JScrollPane(classesTable);
		classesTable.addMouseMotionListener(this);
		add(classesScroll, BorderLayout.WEST);
		
		JScrollPane suggestionsScroll = new JScrollPane(createMainPanel());
		add(suggestionsScroll, BorderLayout.CENTER);
		
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
        add(buttonPanel, BorderLayout.SOUTH);
        
        addMouseMotionListener(this);
        
	}
	
	private JPanel createMainPanel(){
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
	
	private JPanel createSingleTablePanel(){
		JPanel panel = new JPanel(new GridLayout(3,1));
		
		JPanel tableHolderPanel = new JPanel(new BorderLayout());
		defaultTab = new SelectableClassExpressionsTable();
		defaultTab.getSelectionModel().addListSelectionListener(this);
		tableHolderPanel.add(defaultTab);
		
		graphPanel = new GraphicalCoveragePanel("");
		tableHolderPanel.add(graphPanel, BorderLayout.EAST);
		
		JPanel noSuggestionPanel = new JPanel();
		noSuggestionPanel.add(new JCheckBox("There is no appropriate suggestion for this class in your opinion."));
		
		JPanel alternateSuggestionPanel = new JPanel();
		alternateSuggestionPanel.add(new JTextField("There is an appropriate suggestion in your opinion, but the algorithm did not suggest it."));
		
		panel.add(tableHolderPanel);
		panel.add(noSuggestionPanel);
		panel.add(alternateSuggestionPanel);
		
		return panel;
	}
	
	private JPanel createMultiTablesPanel(){
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
	
	private JPanel createSelectablePanel(ResultTable table){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JCheckBox box = new JCheckBox();
		panel.add(table, BorderLayout.CENTER);
//		panel.add(box, BorderLayout.EAST);
		panel.add(new RatingPanel(), BorderLayout.EAST);
		return panel;
	}
	
	private void showSingleTable(){
		defaultTab.clearSelection();
		graphPanel.clear();
		cardLayout.last(cardPanel);
		showingMultiTables = false;
	}
	
	private void showMultiTables(){
		cardLayout.first(cardPanel);
		showingMultiTables = true;
	}
	
	private void showEquivalentSuggestions(NamedClass nc){
		messageLabel.setText(EQUIVALENTCLASSTEXT);
		
		if(owlEquivalenceStandardMap.get(nc) != null){
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
	
	private void showSuperSuggestions(NamedClass nc){
		messageLabel.setText(SUPERCLASSTEXT);
		
		if(owlSuperStandardMap.get(nc) != null){
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
	
	private void createCoverageWindow(){
		coverageWindow = new JWindow(this);
		graphPanel2 = new GraphicalCoveragePanel("");
		coverageWindow.add(graphPanel2);
		coverageWindow.pack();
//		coverageWindow.setLocationRelativeTo(classesTable);
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
	
	private void closeDialog(){
		setVisible(false);
		dispose();
	}
	
	
	private void showCoveragePanel(boolean visible){
		coverageWindow.setVisible(visible);
	}
	
	private void setCoverageLocationRelativeTo(Component component){
		Component parent = component.getParent();
		Point componentLocation = component.getLocationOnScreen();
		Point p;
		if(componentLocation.getX() < parent.getSize().width/2){
			p = new Point((int)(componentLocation.getX() + parent.getSize().width/2 + coverageWindow.getSize().getWidth()/2),componentLocation.y);
		} else {
			p = new Point((int)(componentLocation.getX() - parent.getSize().width/2 - coverageWindow.getSize().getWidth()/2),componentLocation.y);
		}
		
		coverageWindow.setLocation(p);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("next")){
			NamedClass nc = classesTable.getSelectedClass(currentClassIndex);
			if(showingMultiTables && showingEquivalentSuggestions){
				showSuperSuggestions(nc);
				showSingleTable();
			} else if(!showingMultiTables && showingEquivalentSuggestions){
				if(owlEquivalenceStandardMap.get(nc) != null){
					showMultiTables();
				} else {
					showSuperSuggestions(nc);
					showSingleTable();
					if(currentClassIndex + 1 >= defaultEquivalenceMap.keySet().size()){
						nextFinishButton.setText("Finish");
						nextFinishButton.setActionCommand("finish");
					}
				}
				
			} else if(!showingMultiTables && !showingEquivalentSuggestions){
				if(owlEquivalenceStandardMap.get(nc) != null){
					showMultiTables();
					if(currentClassIndex + 1 >= defaultEquivalenceMap.keySet().size()){
						nextFinishButton.setText("Finish");
						nextFinishButton.setActionCommand("finish");
					}
				} else {
					
					currentClassIndex++;
					classesTable.setSelectedClass(currentClassIndex);
					graphPanel.setConcept(classesTable.getSelectedClass(currentClassIndex));
					
					showEquivalentSuggestions(classesTable.getSelectedClass(currentClassIndex));
					showSingleTable();
				}
				
				
			} else {
				
				currentClassIndex++;
				classesTable.setSelectedClass(currentClassIndex);
				graphPanel.setConcept(classesTable.getSelectedClass(currentClassIndex));
				
				showEquivalentSuggestions(classesTable.getSelectedClass(currentClassIndex));
				showSingleTable();
			}

		} else if(e.getActionCommand().equals("finish")){
			
			closeDialog();
		}
		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting() && defaultTab.getSelectedRow() >= 0){
			graphPanel.setNewClassDescription(defaultTab.getSelectedValue());
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
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ComponentInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LearningProblemUnsupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(e.getSource() instanceof ResultTable){
			ResultTable result = ((ResultTable)e.getSource());
			int column = result.columnAtPoint(e.getPoint());
			int row = result.rowAtPoint(e.getPoint());
			
			if(column == 0 && row >=0 && row <= 9){
				EvaluatedDescriptionClass ec = result.getValueAtRow(row);
				graphPanel2.clear();
				graphPanel2.setNewClassDescription(ec);
				setCoverageLocationRelativeTo(result);
				showCoveragePanel(true);
				
			} else {
				showCoveragePanel(false);
			}
		} else {
			showCoveragePanel(false);
		}
		
		
	} 
	
	class RatingTablePanel extends JPanel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 7408917327199664584L;
		private ResultTable table;
		private RatingPanel rating;
		
		public RatingTablePanel(){
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			table = new ResultTable(); 
			add(table, BorderLayout.CENTER);
			rating = new RatingPanel();
			add(rating, BorderLayout.EAST);
			
		}
		
		public void addResults(List<EvaluatedDescriptionClass> resultList){
			table.addResults(resultList);
		}
		
		public void reset(){
			rating.clearSelection();
		}
		
		public int getRatingValue(){
			return rating.getSelectedValue();
		}
		
		public void addMouseMotionListener(MouseMotionListener mL){
			rating.addMouseMotionListener(mL);
			table.addMouseMotionListener(mL);
		}
		
	}
	
	class RatingPanel extends JPanel{

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
		
		public RatingPanel(){
			setLayout(new GridLayout(7,1));
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
		
		public int getSelectedValue(){
			if(rb1.isSelected()){
				return 1;
			} else if(rb2.isSelected()){
				return 2;
			} else if(rb3.isSelected()){
				return 3;
			} else if(rb4.isSelected()){
				return 4;
			} else {
				return 5;
			}
		}
		
		public void clearSelection(){
			rb1.setSelected(true);
		}
		
	}
		
		

}


