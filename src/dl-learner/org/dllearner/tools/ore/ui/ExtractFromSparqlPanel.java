package org.dllearner.tools.ore.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

public class ExtractFromSparqlPanel extends JPanel implements DocumentListener, ActionListener, PropertyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2164059829630727931L;
	
	
	private JComboBox comboBox;
	private JTextField classField;
	private JTextField defaultGraphField;
	
	private JRadioButton asLabelButton;
	private JRadioButton asURLButton;
	
	private JButton extractButton;
	
	private GridBagConstraints c;
	
	private SPARQLTasks task;
	private SparqlKnowledgeSource ks;
	private OntologyExtractingTask extractTask;
	private ProgressMonitor mon;
	private List<SparqlEndpoint> endpoints;
	
	private SparqlExtractOptionsPanel optionsPanel;
	
	public ExtractFromSparqlPanel(){
		
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		JPanel endPointHolderPanel = new JPanel();
		endPointHolderPanel.setLayout(new GridLayout(0, 1));
		endPointHolderPanel.setBorder(new TitledBorder("Sparql endpoint"));
		comboBox = new JComboBox();
		comboBox.setEditable(true);
		AutoCompleteDecorator.decorate(this.comboBox);
		endPointHolderPanel.add(new JLabel("URL"));
		endPointHolderPanel.add(comboBox);
		defaultGraphField = new JTextField();
		endPointHolderPanel.add(new JLabel("Default graph URI"));
		endPointHolderPanel.add(defaultGraphField);
		add(endPointHolderPanel, c);
		
		
		JPanel classHolderPanel = new JPanel();
		classHolderPanel.setLayout(new GridLayout(0, 1));
		classHolderPanel.setBorder(new TitledBorder("Class to learn"));
		asLabelButton = new JRadioButton("label");
		asURLButton = new JRadioButton("URI");
		asURLButton.setSelected(true);
		ButtonGroup bG = new ButtonGroup();
		bG.add(asLabelButton);
		bG.add(asURLButton);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new JLabel("input type:"));
		buttonPanel.add(asURLButton);
		buttonPanel.add(asLabelButton);
		
		classHolderPanel.add(buttonPanel);
		classField = new JTextField();	
		classField.getDocument().addDocumentListener(this);
		classHolderPanel.add(classField);
		add(classHolderPanel, c);
		
		extractButton = new JButton("extract");
		extractButton.addActionListener(this);
		c.fill = GridBagConstraints.NONE;
		add(extractButton, c);
		
		
//		JToggleButton button = new JToggleButton(new AbstractAction("Options") {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				JToggleButton button = (JToggleButton)e.getSource();
//				
//				if(!button.getModel().isSelected()){
//					
//					collapseOptionsPanel();
//				} else {
//					expandOptionsPanel();
//				}
//				
//				
//			}
//		});
//		
//		
//		add(button, c);
		
		
		optionsPanel = new SparqlExtractOptionsPanel();
		add(optionsPanel, c);
//		optionsPanel.setVisible(false);
		JLabel padding = new JLabel();
        c.weighty = 1.0;
        add(padding, c);
	
	}
	
	private void expandOptionsPanel(){
		Dimension dialogSize = getSize ();
		Dimension detailSize = optionsPanel.getPreferredSize ();
		dialogSize.height += detailSize.height;
		setSize (dialogSize);
		optionsPanel.setVisible(true);
		//  Cause the new layout to take effect
		invalidate ();
		validate ();
		
	}
	
	private void collapseOptionsPanel(){
		optionsPanel.setVisible(false);
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
                                                  new JScrollPane(panel),
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
			List<String> defaultGraphURIS = new ArrayList<String>(1);
			defaultGraphURIS.add(defaultGraphField.getText());
			SparqlEndpoint endpoint = new SparqlEndpoint(endpointURL, defaultGraphURIS, Collections.<String>emptyList());
			task = new SPARQLTasks(endpoint);

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
			ks.getConfigurator().setUrl(SparqlEndpoint.getEndpointDBpedia().getURL());
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
	
	private static class JOptionPaneEx extends JOptionPane {

	    /**
		 * 
		 */
		private static final long serialVersionUID = -8531422911783932819L;

		public  static int showConfirmDialog(JComponent parent, String title, JComponent content, int messageType,
	                                        int optionType, final JComponent defaultFocusedComponent) {

	        JOptionPane optionPane = new JOptionPane(content, messageType, optionType);
	        JDialog dlg = optionPane.createDialog(parent, title);
	        dlg.addWindowListener(new WindowAdapter() {
	            public void windowOpened(WindowEvent e) {
	                if (defaultFocusedComponent != null) {
	                    defaultFocusedComponent.requestFocusInWindow();
	                }
	            }
	        });
	        dlg.setSize(400, 300);
	        dlg.setResizable(true);
	        dlg.setVisible(true);
	        Object value = optionPane.getValue();
	        return (value != null) ? (Integer) value : JOptionPane.CLOSED_OPTION;
	    }
	}


	public static void main(String[] args){
		ExtractFromSparqlPanel.showDialog();
	}



}
