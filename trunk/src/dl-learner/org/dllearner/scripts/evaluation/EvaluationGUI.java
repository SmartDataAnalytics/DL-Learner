package org.dllearner.scripts.evaluation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.tools.ore.ui.MarkableClassesTable;
import org.dllearner.tools.ore.ui.SelectableClassExpressionsTable;

public class EvaluationGUI extends JFrame implements ActionListener{
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3097551929270352556L;
	
	private SelectableClassExpressionsTable tab1;
	private MarkableClassesTable classesTable;
	
	private ReasonerComponent reasoner;
	
	private int classesCount = 0;
	private int currentClassIndex = 0;
	
	private static double minAccuracy = 0.85;
	
	private static double noisePercent = 5.0;

	private static int minInstanceCount = 3;

	private static int algorithmRuntimeInSeconds = 10;

	private static DecimalFormat df = new DecimalFormat();

	// for performance measurements and development
	private static boolean autoMode = false;
	private static boolean useFastInstanceChecker = true;
	private static boolean useApproximations = true;
	private static boolean computeApproxDiff = false;
	
	private Map<NamedClass, List<EvaluatedDescriptionClass>> namedClass2EquivalenceSuggestionsMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();
	private Map<NamedClass, List<EvaluatedDescriptionClass>> namedClass2SuperSuggestionsMap = new HashMap<NamedClass, List<EvaluatedDescriptionClass>>();

	public EvaluationGUI(URL fileURL) throws ComponentInitException, MalformedURLException, LearningProblemUnsupportedException{
		super();
		loadOntology(fileURL);
		computeLearningResults();
		serializeResults();
		deserializeResults();
		createUI();
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void createUI(){
		
		setLayout(new BorderLayout());
		
		classesTable = new MarkableClassesTable();
		classesTable.addClasses(new TreeSet<NamedClass>(namedClass2EquivalenceSuggestionsMap.keySet()));
		JScrollPane classesScroll = new JScrollPane(classesTable);
		add(classesScroll, BorderLayout.WEST);
		
		JScrollPane suggestionsScroll = new JScrollPane(createTablesPanel());
		add(suggestionsScroll, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());       
        JSeparator separator = new JSeparator();
        buttonPanel.add(separator, BorderLayout.NORTH);
        Box buttonBox = new Box(BoxLayout.X_AXIS);
        JButton nextButton = new JButton("Next");
        nextButton.setActionCommand("next");
        nextButton.addActionListener(this);
        buttonBox.add(nextButton);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        
	}
	
	private JPanel createTablesPanel(){
		JPanel tablesHolder = new JPanel();
		tablesHolder.setLayout(new GridLayout(5, 2));
		tab1 = new SelectableClassExpressionsTable();
		
		
		
		tablesHolder.add(tab1);
		return tablesHolder;
	}
	
	private void showNextClassSuggestions(){
		NamedClass nc = classesTable.getSelectedClass(currentClassIndex);
		tab1.addResults(namedClass2EquivalenceSuggestionsMap.get(nc));
	}
	
	private void loadOntology(URL fileURL) throws ComponentInitException{
		ComponentManager cm = ComponentManager.getInstance();

		
		
		// load OWL in reasoner
		OWLFile ks = cm.knowledgeSource(OWLFile.class);
		ks.getConfigurator().setUrl(fileURL);
		ks.init();
		
		reasoner = null;
		reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		
		System.out.println("Loaded ontology " + fileURL + ".");
	}
	
	
	
	private void computeLearningResults() throws ComponentInitException, LearningProblemUnsupportedException, MalformedURLException{
		ComponentManager cm = ComponentManager.getInstance();
	
		String baseURI = reasoner.getBaseURI();
		Map<String, String> prefixes = reasoner.getPrefixes();
	
		// loop through all classes
		Set<NamedClass> classes = new TreeSet<NamedClass>(reasoner.getNamedClasses());
		classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
		// reduce number of classes for testing purposes
//		shrinkSet(classes, 20);
		for (NamedClass nc : classes) {
			// check whether the class has sufficient instances
			int instanceCount = reasoner.getIndividuals(nc).size();
			if (instanceCount < minInstanceCount) {
				System.out.println("class " + nc.toManchesterSyntaxString(baseURI, prefixes)
						+ " has only " + instanceCount + " instances (minimum: " + minInstanceCount
						+ ") - skipping");
			} else {
				System.out.println("\nlearning axioms for class "
						+ nc.toManchesterSyntaxString(baseURI, prefixes) + " with " + instanceCount
						+ " instances");
				

				TreeSet<EvaluatedDescriptionClass> suggestions;
				// i=0 is equivalence and i=1 is super class
				for (int i = 0; i <= 1; i++) {
					// learn equivalence axiom
					ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class,
							reasoner);
					lp.getConfigurator().setClassToDescribe(nc.getURI().toURL());
					if (i == 0) {
						System.out
								.println("generating suggestions for equivalent class (please wait "
										+ algorithmRuntimeInSeconds + " seconds)");
						lp.getConfigurator().setType("equivalence");
					} else {
						System.out.println("suggestions for super class (please wait "
								+ algorithmRuntimeInSeconds + " seconds)");
						lp.getConfigurator().setType("superClass");
					}
					lp.getConfigurator().setUseApproximations(useApproximations);
					lp.init();

					CELOE celoe = cm.learningAlgorithm(CELOE.class, lp, reasoner);
					CELOEConfigurator cf = celoe.getConfigurator();
					cf.setUseNegation(false);
					cf.setValueFrequencyThreshold(3);
					cf.setMaxExecutionTimeInSeconds(algorithmRuntimeInSeconds);
					cf.setNoisePercentage(noisePercent);
					cf.setMaxNrOfResults(10);
					celoe.init();

					celoe.start();
					// test whether a solution above the threshold was found
					EvaluatedDescription best = celoe.getCurrentlyBestEvaluatedDescription();
					double bestAcc = best.getAccuracy();
					
					if (bestAcc < minAccuracy || (best.getDescription() instanceof Thing)) {
						System.out
								.println("The algorithm did not find a suggestion with an accuracy above the threshold of "
										+ (100 * minAccuracy)
										+ "% or the best description is not appropriate. (The best one was \""
										+ best.getDescription().toManchesterSyntaxString(baseURI,
												prefixes)
										+ "\" with an accuracy of "
										+ df.format(bestAcc) + ".) - skipping");
					} else {

						

						suggestions = (TreeSet<EvaluatedDescriptionClass>) celoe
								.getCurrentlyBestEvaluatedDescriptions();
						List<EvaluatedDescriptionClass> suggestionsList = new LinkedList<EvaluatedDescriptionClass>(
								suggestions.descendingSet());
						if(i == 0){
							namedClass2EquivalenceSuggestionsMap.put(nc, suggestionsList);
						} else {
							namedClass2SuperSuggestionsMap.put(nc, suggestionsList);
						}
						
					}
				}
			}
		}
		
		
	}
	
	private void serializeResults() {
		OutputStream fos = null;
		File file = new File("test.ser");
		try {
			fos = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(namedClass2EquivalenceSuggestionsMap);
			o.writeObject(namedClass2SuperSuggestionsMap);
			o.flush();
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	private void deserializeResults() {
		InputStream fis = null;

		try {
			fis = new FileInputStream("test.ser");
			ObjectInputStream o = new ObjectInputStream(fis);
			namedClass2EquivalenceSuggestionsMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o
					.readObject();
			namedClass2SuperSuggestionsMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("next")){
			classesTable.setSelectedClass(currentClassIndex++);
			showNextClassSuggestions();
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
//		Logger.getRootLogger().setLevel(Level.WARN);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		if (args.length == 0) {
			System.out.println("You need to give an OWL file as argument.");
			System.exit(0);
		}
		URL fileURL = null;
		if(args[0].startsWith("http")){
			fileURL = new URL(args[0]);
		} else {
			fileURL = new File(args[0]).toURI().toURL();
		}
		new EvaluationGUI(fileURL);
		
	}

	

}
