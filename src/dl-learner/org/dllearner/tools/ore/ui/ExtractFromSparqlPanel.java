package org.dllearner.tools.ore.ui;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.protege.editor.core.ui.util.JOptionPaneEx;

public class ExtractFromSparqlPanel extends JPanel implements DocumentListener, ActionListener, PropertyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2164059829630727931L;
	
	
	private JComboBox comboBox;
	private JTextField classField;
	private JButton extractButton;
	
	private GridBagConstraints c;
	
	private SPARQLTasks task;
	private SparqlKnowledgeSource ks;
	private OntologyExtractingTask extractTask;
	private ProgressMonitor mon;
	
	private SparqlExtractOptionsPanel optionsPanel;
	
	public ExtractFromSparqlPanel(){
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JPanel comboBoxHolderPanel = new JPanel();
		comboBoxHolderPanel.setBorder(new TitledBorder("Sparql endpoint URI"));
		comboBox = new JComboBox(SparqlEndpoint.listEndpoints().toArray());
		comboBox.setEditable(true);
		AutoCompleteDecorator.decorate(this.comboBox);
		comboBoxHolderPanel.add(comboBox);
		c.gridx = 0;
		c.gridy = 0;
		add(comboBoxHolderPanel, c);
		
		
		classField = new JTextField();	
		classField.getDocument().addDocumentListener(this);
		c.gridx = 0;
		c.gridy = 1;
		add(classField, c);
		
		extractButton = new JButton("extract");
		extractButton.addActionListener(this);
		c.gridx = 0;
		c.gridy = 2;
		add(extractButton, c);
		
		optionsPanel = new SparqlExtractOptionsPanel();
		c.gridx = 0;
		c.gridy = 3;
		add(optionsPanel, c);
		

	}
	
	
	
	
	private void extract() {
		mon = new ProgressMonitor(this, "Extracting fragment", "", 0, 100);
		
		extractTask = new OntologyExtractingTask(mon);
		extractTask.addPropertyChangeListener(this);
		extractTask.execute();
		
		
	}
	
	private String getClassFromLabel(){
		
		String queryString = "SELECT DISTINCT ?class WHERE {" +
		"?class rdf:type owl:Class ." +
		"?class rdfs:label ?label . " +
		"FILTER(regex(?label, '" + classField.getText() + "')) }";
		SortedSet<String> classes = task.queryAsSet(queryString, "class");

		return classes.iterator().next();
	}
	private void autocomplete(){
		
		
		
		
		
	}
	
	public static SparqlKnowledgeSource showDialog() {
		ExtractFromSparqlPanel panel = new ExtractFromSparqlPanel();
        int ret = JOptionPaneEx.showConfirmDialog(null,
                                                  "Extract fragment from Sparql-Endpoint",
                                                  panel,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  panel.comboBox);
        if (ret == JOptionPane.OK_OPTION) {
            return panel.ks;
        }
        return null;
    }

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
//		autocomplete();
		
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		extract();
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if ("progress" == evt.getPropertyName() ) {
			 if(mon.isCanceled()){
				 extractTask.cancel(true);
			 }
		 }
		
	}
	
	class OntologyExtractingTask extends SwingWorker<Void, Void>{
		
		private ProgressMonitor mon;
		
		public OntologyExtractingTask(ProgressMonitor mon) {		
			this.mon = mon;			
		}

		@Override
		public Void doInBackground() {
			ComponentManager cm = ComponentManager.getInstance();
			URL endpointURL = null;
			try {
				endpointURL = new URL(comboBox.getSelectedItem().toString());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			task = new SPARQLTasks(new SparqlEndpoint(endpointURL));

			String exampleClassKBString = "\"" + getClassFromLabel() + "\"";
			AutomaticPositiveExampleFinderSPARQL pos = new AutomaticPositiveExampleFinderSPARQL(
					task);
			pos.makePositiveExamplesFromConcept(exampleClassKBString);

			SortedSet<String> allPosExamples = pos.getPosExamples();
			SortedSet<String> posExamples = SetManipulation.stableShrink(
					allPosExamples, 20);
			System.out.println(posExamples.size());
			System.out.println(posExamples);
			SortedSet<String> instances = new TreeSet<String>(posExamples);

			ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
			ks.getConfigurator().setUrl(endpointURL);
			ks.getConfigurator().setInstances(instances);
			ks.getConfigurator().setPredefinedFilter("YAGO");
			ks.getConfigurator().setBreakSuperClassRetrievalAfter(optionsPanel.getBreakSuperClassRetrievalAfterValue());
			ks.getConfigurator().setRecursionDepth(optionsPanel.getRecursionDepthValue());
			ks.getConfigurator().setUseCache(optionsPanel.isUseCache());
			ks.getConfigurator().setGetAllSuperClasses(optionsPanel.isGetAllSuperClasses());
			ks.getConfigurator().setDissolveBlankNodes(optionsPanel.isDissolveBlankNodes());
			ks.getConfigurator().setUseImprovedSparqlTupelAquisitor(optionsPanel.isUseImprovedSparqlTupelAquisitor());
			ks.getConfigurator().setUseLits(optionsPanel.isUseLiterals());
			ks.getConfigurator().setGetPropertyInformation(optionsPanel.isGetPropertyInformation());
			ks.getConfigurator().setCloseAfterRecursion(optionsPanel.isCloseAfterRecursion());
			ks.addProgressMonitor(mon);

			ks.init();

			return null;
		}

		@Override
		public void done() {
			mon.setProgress(0);
		}
	}

	


}
