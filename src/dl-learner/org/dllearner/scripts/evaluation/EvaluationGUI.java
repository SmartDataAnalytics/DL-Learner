package org.dllearner.scripts.evaluation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ui.MarkableClassesTable;
import org.dllearner.tools.ore.ui.SelectableClassExpressionsTable;

public class EvaluationGUI extends JFrame implements ActionListener{
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3097551929270352556L;
	
	private SelectableClassExpressionsTable tab1;
	private SelectableClassExpressionsTable tab2;
	private SelectableClassExpressionsTable tab3;
	private SelectableClassExpressionsTable tab4;
	private SelectableClassExpressionsTable tab5;
	private SelectableClassExpressionsTable tab6;
	
	private MarkableClassesTable classesTable;
	private JButton nextFinishButton;
	private JLabel messageLabel;
	
	private static String SUPERCLASSTEXT = "Showing suggestions for super class";
	private static String EQUIVALENTCLASSTEXT = "Showing suggestions for equivalent class";
	
	private int currentClassIndex = 0;
	
	private boolean showingSuperclass = false;
	

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
	

	public EvaluationGUI(File input) throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException{
		super();
		loadResults(input);
		createUI();
		showNextClassSuggestions();
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void createUI(){
		
		setLayout(new BorderLayout());
		
		classesTable = new MarkableClassesTable();
		classesTable.addClasses(new TreeSet<NamedClass>(owlEquivalenceStandardMap.keySet()));
		JScrollPane classesScroll = new JScrollPane(classesTable);
		add(classesScroll, BorderLayout.WEST);
		
		JScrollPane suggestionsScroll = new JScrollPane(createTablesPanel());
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
        
	}
	
	private JPanel createTablesPanel(){
		JPanel messageTablesPanel = new JPanel();
		messageTablesPanel.setLayout(new BorderLayout());
		
		messageLabel = new JLabel();
		messageTablesPanel.add(messageLabel, BorderLayout.NORTH);
		
		JPanel tablesHolderPanel = new JPanel();
		tablesHolderPanel.setLayout(new GridLayout(5, 2));
		tab1 = new SelectableClassExpressionsTable();
		JPanel pan = new JPanel();pan.setBorder(BorderFactory.createBevelBorder(0));
		pan.add(tab1);
		tablesHolderPanel.add(tab1);
		tab2 = new SelectableClassExpressionsTable();
		tablesHolderPanel.add(tab2);
		tab3 = new SelectableClassExpressionsTable();
		tablesHolderPanel.add(tab3);
		tab4 = new SelectableClassExpressionsTable();
		tablesHolderPanel.add(tab4);
		tab5 = new SelectableClassExpressionsTable();
		tablesHolderPanel.add(tab5);
		tab6 = new SelectableClassExpressionsTable();
		tablesHolderPanel.add(tab6);
		
		messageTablesPanel.add(tablesHolderPanel, BorderLayout.CENTER);
		
		return messageTablesPanel;
	}
	
	private void showNextClassSuggestions(){
		classesTable.setSelectedClass(currentClassIndex);
		NamedClass nc = classesTable.getSelectedClass(currentClassIndex);
		if(!showingSuperclass){
			messageLabel.setText(EQUIVALENTCLASSTEXT);
			showEquivalentSuggestions(nc);
			showingSuperclass = true;
		} else {
			messageLabel.setText(SUPERCLASSTEXT);
			showSuperSuggestions(nc);
			showingSuperclass = false;
			currentClassIndex++;
			if(currentClassIndex >= owlEquivalenceStandardMap.keySet().size()){
				nextFinishButton.setText("Finish");
				nextFinishButton.setActionCommand("finish");
			}
		}
		
		
	}
	
	private void showEquivalentSuggestions(NamedClass nc){
		tab1.addResults(owlEquivalenceStandardMap.get(nc));
		tab2.addResults(owlEquivalenceFMeasureMap.get(nc));
		tab3.addResults(owlEquivalencePredaccMap.get(nc));
		tab4.addResults(fastEquivalenceStandardMap.get(nc));
		tab5.addResults(fastEquivalenceFMeasureMap.get(nc));
		tab6.addResults(fastEquivalencePredaccMap.get(nc));
	}
	
	private void showSuperSuggestions(NamedClass nc){
		tab1.addResults(owlSuperStandardMap.get(nc));
		tab2.addResults(owlSuperFMeasureMap.get(nc));
		tab3.addResults(owlSuperPredaccMap.get(nc));
		tab4.addResults(fastSuperStandardMap.get(nc));
		tab5.addResults(fastSuperFMeasureMap.get(nc));
		tab6.addResults(fastSuperPredaccMap.get(nc));
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
//			owlEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
			owlSuperStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperPredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			owlSuperJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();	
//			owlSuperGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
			fastEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
//			fastEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
			fastSuperStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperPredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			fastSuperJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
//			fastSuperGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("next")){
			showNextClassSuggestions();
		} else if(e.getActionCommand().equals("finish")){
			closeDialog();
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
		File input = new File(args[0]);
		new EvaluationGUI(input);
		
	}

	

}
