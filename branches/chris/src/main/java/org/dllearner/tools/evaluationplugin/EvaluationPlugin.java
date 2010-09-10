package org.dllearner.tools.evaluationplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class EvaluationPlugin extends AbstractOWLViewComponent implements ListSelectionListener {

	enum CompareMode{
		CompareWithTF,
		CompareWithFT,
		CompareWithTT
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static boolean in_compare_mode = false;
	private static CompareMode compareMode = CompareMode.CompareWithFT;
	
	private EvaluationTable evaluationTable;
	private EvaluationTable compareEvaluationTable;
	private GraphicalCoveragePanel coveragePanel;
	private JLabel inconsistencyLabel;
	private JButton nextSaveButton;
	private JButton backButton;
	private JLabel currentClassLabel;
	private JProgressBar progressBar;

	private List<NamedClass> classes = new ArrayList<NamedClass>();
	private int currentClassIndex = 0;
	
	private int lastSelectedRowIndex = -1;

	private final ConceptComparator comparator = new ConceptComparator();

	private static final String CURRENT_CLASS_MESSAGE = "<html>Showing equivalent class expressions for class ";
	private static final String INCONSISTENCY_WARNING = "<html>Warning. Selected class expressions leads to an inconsistent ontology!<br>"
			+ "(Often, suggestions leading to an inconsistency should still be added. They help to detect problems in "
			+ "the ontology elsewhere.<br>"
			+ " See http://dl-learner.org/files/screencast/protege/screencast.htm .)</html>";
	//TODO Add label
//	private static final String FOLLOWS_FROM_KB_WARNING = "<html>Selected class expressions follows already logically from ontology.</html>";

	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceStandardMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceFMeasureMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalencePredaccMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceGenFMeasureMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceJaccardMap;

	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceStandardMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceFMeasureMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalencePredaccMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceGenFMeasureMap;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceJaccardMap;

	private Map<NamedClass, List<EvaluatedDescriptionClass>> defaultEquivalenceMap;
	
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceStandardMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceFMeasureMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalencePredaccMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceGenFMeasureMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> fastEquivalenceJaccardMapComp;

	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceStandardMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceFMeasureMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalencePredaccMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceGenFMeasureMapComp;
	private Map<NamedClass, List<EvaluatedDescriptionClass>> owlEquivalenceJaccardMapComp;

	private Map<NamedClass, List<EvaluatedDescriptionClass>> defaultEquivalenceMapComp;
	
	private Hashtable<NamedClass, Map<EvaluatedDescriptionClass, Integer>> userInputMap = new Hashtable<NamedClass, Map<EvaluatedDescriptionClass,Integer>>();

	@Override
	protected void initialiseOWLView() throws Exception {
		System.out.println("Initializing DL-Learner Evaluation Plugin...");
		createUI();
		parseEvaluationFile();
		showClassExpressions(classes.get(currentClassIndex));
		backButton.setEnabled(false);
		
	}

	@Override
	protected void disposeOWLView() {
		evaluationTable.getSelectionModel().removeListSelectionListener(this);
		evaluationTable.dispose();
	}

	/**
	 * Create the user interface.
	 */
	private void createUI() {
		if(!in_compare_mode){
			setLayout(new BorderLayout());

			currentClassLabel = new JLabel();
			add(currentClassLabel, BorderLayout.NORTH);

			JPanel tableHolderPanel = new JPanel(new BorderLayout());
			evaluationTable = new EvaluationTable(getOWLEditorKit());
			evaluationTable.getSelectionModel().addListSelectionListener(this);
			JScrollPane sp = new JScrollPane(evaluationTable);
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			tableHolderPanel.add(sp);
			inconsistencyLabel = new JLabel(INCONSISTENCY_WARNING);
			inconsistencyLabel.setForeground(getBackground());
			tableHolderPanel.add(inconsistencyLabel, BorderLayout.SOUTH);
			add(tableHolderPanel);

			JPanel coverageHolderPanel = new JPanel(new BorderLayout());
			coveragePanel = new GraphicalCoveragePanel(getOWLEditorKit());
			coverageHolderPanel.add(coveragePanel);
			
			backButton = new JButton();
			backButton.setAction(new ShowPreviousClassAction());

			nextSaveButton = new JButton();
			nextSaveButton.setToolTipText("Show class expressions for next class to evaluate.");
			nextSaveButton.setAction(new ShowNextClassAction());
			
			progressBar = new JProgressBar();
	        progressBar.setValue(0);
	        progressBar.setStringPainted(true);
			
			JPanel buttonHolderPanel = new JPanel();
	        buttonHolderPanel.add(progressBar);
	        buttonHolderPanel.add(backButton);
			buttonHolderPanel.add(nextSaveButton);
			
			coverageHolderPanel.add(buttonHolderPanel, BorderLayout.SOUTH);
			add(coverageHolderPanel, BorderLayout.SOUTH);
		} else {
			setLayout(new BorderLayout());

			currentClassLabel = new JLabel();
			add(currentClassLabel, BorderLayout.NORTH);

			JPanel tableHolderPanel = new JPanel(new GridLayout(2, 1));
			evaluationTable = new EvaluationTable(getOWLEditorKit());
			compareEvaluationTable = new EvaluationTable(getOWLEditorKit());
			evaluationTable.getSelectionModel().addListSelectionListener(this);
			JScrollPane sp = new JScrollPane(evaluationTable);
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			tableHolderPanel.add(sp);
			tableHolderPanel.add(new JScrollPane(compareEvaluationTable));
			inconsistencyLabel = new JLabel(INCONSISTENCY_WARNING);
			
			add(tableHolderPanel);

			JPanel coverageHolderPanel = new JPanel(new BorderLayout());
			coveragePanel = new GraphicalCoveragePanel(getOWLEditorKit());
			
			backButton = new JButton();
			backButton.setAction(new ShowPreviousClassAction());

			nextSaveButton = new JButton();
			nextSaveButton.setToolTipText("Show class expressions for next class to evaluate.");
			nextSaveButton.setAction(new ShowNextClassAction());
			
			progressBar = new JProgressBar();
	        progressBar.setValue(0);
	        progressBar.setStringPainted(true);
			
			JPanel buttonHolderPanel = new JPanel();
	        buttonHolderPanel.add(progressBar);
	        buttonHolderPanel.add(backButton);
			buttonHolderPanel.add(nextSaveButton);
			
			coverageHolderPanel.add(buttonHolderPanel, BorderLayout.SOUTH);
			add(coverageHolderPanel, BorderLayout.SOUTH);
		}

	}

	
	private void showClassExpressions(NamedClass nc){
		showInconsistencyWarning(false);
		evaluationTable.setAllColumnsEnabled(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(nc).asOWLClass().
					getEquivalentClasses(getOWLModelManager().getActiveOntology().getImportsClosure()).size() > 0);
		if(in_compare_mode){
			compareEvaluationTable.setAllColumnsEnabled(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(nc).asOWLClass().
					getEquivalentClasses(getOWLModelManager().getActiveOntology().getImportsClosure()).size() > 0);
		}
		// show the name for the current class in manchester syntax
		String renderedClass = getOWLModelManager().getRendering(
				OWLAPIDescriptionConvertVisitor.getOWLClassExpression(nc));
		currentClassLabel.setText(CURRENT_CLASS_MESSAGE + "<b>" + renderedClass + "</b></html>");
		System.out.println("Showing evaluated descriptions for class " + nc.toString());

		// refresh coverage panel to the current class
		if(!in_compare_mode){
			coveragePanel.setConcept(nc);
		}
		
		//dencrement progress
		progressBar.setValue(currentClassIndex + 1);
		progressBar.setString("class " + (currentClassIndex + 1) + " of " + classes.size());
		
		//reset last selected row to -1
		lastSelectedRowIndex = -1;

		// necessary to set the current class to evaluate as activated entity
		OWLClassExpression desc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(nc);
		OWLEntity curEntity = desc.asOWLClass();
		getOWLEditorKit().getWorkspace().getOWLSelectionModel().setSelectedEntity(curEntity);
		
		//add the new disjoint descriptions to the table
		evaluationTable.setDescriptions(getMergedDescriptions(nc, false));
		if(in_compare_mode){
			compareEvaluationTable.setDescriptions(getMergedDescriptions(nc, true));
		}
		
		setInput(classes.get(currentClassIndex));
		
	}
	

	/**
	 * Load the computed DL-Learner results from a file, which name corresponds
	 * to the loaded owl-file.
	 */
	@SuppressWarnings("unchecked")
	private void parseEvaluationFile() {
		OWLOntology activeOnt = getOWLModelManager().getActiveOntology();
		URI uri = getOWLModelManager().getOntologyPhysicalURI(activeOnt);
		String resultFile = null;;
		InputStream fis = null;
		ObjectInputStream o = null;
		try {
			
			if(!in_compare_mode){
				resultFile = uri.toString().substring(0, uri.toString().lastIndexOf('.') + 1) + "res";
				fis = new FileInputStream(new File(URI.create(resultFile)));
				o = new ObjectInputStream(fis);
				
				owlEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				
				fastEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();

				defaultEquivalenceMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			} else {
				resultFile = uri.toString().substring(0, uri.toString().lastIndexOf('.')) + "_ff.res";
				fis = new FileInputStream(new File(URI.create(resultFile)));
				o = new ObjectInputStream(fis);
				owlEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceStandardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalencePredaccMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceJaccardMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceGenFMeasureMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				defaultEquivalenceMap = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				
				String ending = null;
				switch(compareMode){
					case CompareWithFT: ending = "_ft"; break;
					case CompareWithTF: ending = "_tf"; break;
					case CompareWithTT: ending = "_tt"; break;
				}
				resultFile = uri.toString().substring(0, uri.toString().lastIndexOf('.')) + ending + ".res";
				fis = new FileInputStream(new File(URI.create(resultFile)));
				o = new ObjectInputStream(fis);
				owlEquivalenceStandardMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceFMeasureMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalencePredaccMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceJaccardMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				owlEquivalenceGenFMeasureMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceStandardMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceFMeasureMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalencePredaccMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceJaccardMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				fastEquivalenceGenFMeasureMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
				defaultEquivalenceMapComp = (HashMap<NamedClass, List<EvaluatedDescriptionClass>>) o.readObject();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		classes.addAll(new TreeSet<NamedClass>(owlEquivalenceStandardMap.keySet()));
		progressBar.setMaximum(classes.size());
	}
	
	/**
	 * Add the evaluation for the current class to the result map.
	 * @param nc The class for which the user input is traced.
	 */
	private void traceInput(NamedClass nc){
		userInputMap.put(nc, evaluationTable.getUserInputMap());
	}
	
	private void setInput(NamedClass nc){
		Map<EvaluatedDescriptionClass, Integer> input = userInputMap.get(nc);
		if(input != null){
			evaluationTable.setUserInput(userInputMap.get(nc));
		}
	}

	/**
	 * Saves the user evaluation map object to disk. The file format is 'FILENAME'.inp .
	 */
	private void saveUserInputToFile() {
		OWLOntology activeOnt = getOWLModelManager().getActiveOntology();
		URI uri = getOWLModelManager().getOntologyPhysicalURI(activeOnt);
		String outputFile = uri.toString().substring(0, uri.toString().lastIndexOf('.') + 1) + "inp";
		OutputStream fos = null;
		File file = new File(URI.create(outputFile));
		try {
			fos = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(userInputMap);
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
		JOptionPane.showMessageDialog(this.getParent(), "Saved evaluation to ." + file);

	}

	/**
	 * Get a disjoint list of all computed evaluated descriptions.
	 * 
	 * @param nc
	 *            The class which is currently to evaluate.
	 * @return A List of disjoint evaluated descriptions - here disjointness
	 *         only by the description not the accuracy.
	 */
	private List<EvaluatedDescriptionClass> getMergedDescriptions(NamedClass nc, boolean compared) {
		
		Set<EvaluatedDescriptionClass> evaluatedDescriptions = new TreeSet<EvaluatedDescriptionClass>(
				new Comparator<EvaluatedDescriptionClass>() {

					public int compare(EvaluatedDescriptionClass o1, EvaluatedDescriptionClass o2) {
						return comparator.compare(o1.getDescription(), o2.getDescription());

					};
				});
		if(!compared){
			evaluatedDescriptions.addAll(owlEquivalenceStandardMap.get(nc));
			evaluatedDescriptions.addAll(owlEquivalenceJaccardMap.get(nc));
			evaluatedDescriptions.addAll(owlEquivalenceGenFMeasureMap.get(nc));
			evaluatedDescriptions.addAll(owlEquivalenceFMeasureMap.get(nc));
			evaluatedDescriptions.addAll(owlEquivalencePredaccMap.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceStandardMap.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceJaccardMap.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceGenFMeasureMap.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceFMeasureMap.get(nc));
			evaluatedDescriptions.addAll(fastEquivalencePredaccMap.get(nc));
			evaluatedDescriptions.addAll(defaultEquivalenceMap.get(nc));
		} else {
			evaluatedDescriptions.addAll(owlEquivalenceStandardMapComp.get(nc));
			evaluatedDescriptions.addAll(owlEquivalenceJaccardMapComp.get(nc));
			evaluatedDescriptions.addAll(owlEquivalenceGenFMeasureMapComp.get(nc));
			evaluatedDescriptions.addAll(owlEquivalenceFMeasureMapComp.get(nc));
			evaluatedDescriptions.addAll(owlEquivalencePredaccMapComp.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceStandardMapComp.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceJaccardMapComp.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceGenFMeasureMapComp.get(nc));
			evaluatedDescriptions.addAll(fastEquivalenceFMeasureMapComp.get(nc));
			evaluatedDescriptions.addAll(fastEquivalencePredaccMapComp.get(nc));
			evaluatedDescriptions.addAll(defaultEquivalenceMapComp.get(nc));
		}
		
		List<EvaluatedDescriptionClass> merged = new ArrayList<EvaluatedDescriptionClass>(evaluatedDescriptions);
		
//		OWLReasoner reasoner = getOWLModelManager().getReasoner();
//		SubsumptionTree tree = new SubsumptionTree(reasoner);
//		
//		List<EvaluatedDescription> list = new ArrayList<EvaluatedDescription>();
//		for(EvaluatedDescriptionClass ec : merged){
//			list.add((EvaluatedDescription)ec);
//		}
//		tree.insert(list);
//		System.out.println(tree.toString());
//
//		for(Node node : tree.getRoot().getSubClasses()){
//			System.out.println(node.getSubClasses());
//		}
		System.out.println("NC: " + nc);
		System.out.println("MERGED: " + merged);
		return merged;
	}

	/**
	 * Show a red colored warning, if adding the selected class expression would
	 * lead to an inconsistent ontology.
	 * 
	 * @param show
	 *            If true a warning is displayed, otherwise not.
	 */
	private void showInconsistencyWarning(boolean show) {
		if (show) {
			inconsistencyLabel.setForeground(Color.RED);
		} else {
			inconsistencyLabel.setForeground(getBackground());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selectedRow = evaluationTable.getSelectedRow();
		if (!e.getValueIsAdjusting() &&  selectedRow >= 0 && lastSelectedRowIndex != selectedRow) {
			coveragePanel.setNewClassDescription(evaluationTable.getSelectedEvaluatedDescription());
			showInconsistencyWarning(!evaluationTable.getSelectedEvaluatedDescription().isConsistent());
			lastSelectedRowIndex = selectedRow;
		}
	}
	
	private class ShowNextClassAction extends AbstractAction{
		private static final long serialVersionUID = 2739746405142077803L;
		
		public ShowNextClassAction(){
			super("Next");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			backButton.setEnabled(true);
			traceInput(classes.get(currentClassIndex));
			currentClassIndex++;
			showClassExpressions(classes.get(currentClassIndex));
			if (currentClassIndex == classes.size() - 1) {
				nextSaveButton.setAction(new SaveToDiskAction());
			}
		}
	}
	
	private class ShowPreviousClassAction extends AbstractAction{
		private static final long serialVersionUID = -7689154691704844040L;
		
		public ShowPreviousClassAction() {
			super("Back");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(currentClassIndex == classes.size() - 1){
				nextSaveButton.setAction(new ShowNextClassAction());
			}
			traceInput(classes.get(currentClassIndex));
			currentClassIndex--;
			showClassExpressions(classes.get(currentClassIndex));
			if(currentClassIndex == 0){
				backButton.setEnabled(false);
			}
		}
	}
	
	private class SaveToDiskAction extends AbstractAction{
		private static final long serialVersionUID = -7588319245317451924L;
		
		public SaveToDiskAction(){
			super("Save");
			nextSaveButton.setToolTipText("Save the evaluation results to disk.");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			traceInput(classes.get(currentClassIndex));
			saveUserInputToFile();
		}
	}

}
