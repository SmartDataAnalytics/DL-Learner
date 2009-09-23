package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

public class ExtractFromSparqlDialog extends JDialog implements ActionListener, PropertyChangeListener, DocumentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6379950242859458507L;
	
	public static final int CANCEL_RETURN_CODE = 0;
	public static final int OK_RETURN_CODE = 1;
	
	private int returnCode;
	
	private JButton okButton = null;
	@SuppressWarnings("unused")
	private JButton cancelButton = null;
	
	private JComboBox comboBox;
	private JTextField classField;
	private JTextField defaultGraphField;
	
	private JRadioButton asLabelButton;
	private JRadioButton asURLButton;
	
	private JButton extractButton;
	
	private JLabel message;
	
	private SparqlExtractOptionsPanel optionsPanel;
	private JToggleButton optionsButton;
	private ImageIcon toggledIcon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/toggled.gif");
	private ImageIcon untoggledIcon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/untoggled.gif");
	

	private SPARQLTasks task;
	private SparqlKnowledgeSource ks;
	private OntologyExtractingTask extractTask;
	private ProgressMonitor mon;
	
	private Map<URL, List<String>> endpointToDefaultGraph;
	
	public ExtractFromSparqlDialog(JFrame owner) {
		super(owner, "Extract fragment from SPARQL endpoint", true);

		// Create the controls
		createControls();
		//create main panel
		createSparqlPanel();
		//add predifined endpoints
		addPredifinedEndpoints();
		positionErrorDialog(owner);
	}
	 
	private void createControls() {
		getContentPane().setLayout(new BorderLayout());

		// Create the dialog buttons
		// Create a box to hold the buttons - to give the right spacing between
		// them
		Box buttonBox = Box.createHorizontalBox();

		// Create a panel to hold a box with the buttons in it - to give it the
		// right space around them
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonBox);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Create the buttons and add them to the box (leading strut will give
		// the dialog box its width)
		buttonBox.add(okButton = createButton("Ok", 'o'));
		okButton.setEnabled(false);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(Box.createHorizontalStrut(4));
		buttonBox.add(cancelButton = createButton("Cancel", 'c'));
		buttonBox.add(Box.createHorizontalStrut(10));

		// Add the button panel to the bottom of the BorderLayout
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	 
	private void createSparqlPanel() {
		JPanel panel = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		JPanel endPointHolderPanel = new JPanel();
		endPointHolderPanel.setLayout(new GridLayout(0, 1));
		endPointHolderPanel.setBorder(new TitledBorder("SPARQL endpoint"));
		comboBox = new JComboBox();
		comboBox.setEditable(true);
		comboBox.setActionCommand("endpoints");
		comboBox.addActionListener(this);
		
		((JTextComponent)comboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
		AutoCompleteDecorator.decorate(this.comboBox);
		endPointHolderPanel.add(new JLabel("URL"));
		endPointHolderPanel.add(comboBox);
		defaultGraphField = new JTextField();
		endPointHolderPanel.add(new JLabel("Default graph URI (optional)"));
		endPointHolderPanel.add(defaultGraphField);
		panel.add(endPointHolderPanel, c);

		JPanel classHolderPanel = new JPanel();
		classHolderPanel.setLayout(new GridLayout(0, 1));
		classHolderPanel.setBorder(new TitledBorder("Class to investigate"));
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
		panel.add(classHolderPanel, c);

		

		optionsButton = new JToggleButton(new AbstractAction("Advanced options") {

			/**
			 * 
			 */
			private static final long serialVersionUID = -4395104616001102604L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton) e.getSource();

				if (!button.getModel().isSelected()) {
					collapseOptionsPanel();
				} else {
					expandOptionsPanel();
				}

			}
		});
		optionsButton.setIcon(untoggledIcon);
		optionsButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		optionsButton.setHorizontalAlignment(JButton.LEADING); // optional
		optionsButton.setBorderPainted(false);
		optionsButton.setContentAreaFilled(false);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		panel.add(optionsButton, c);

		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		optionsPanel = new SparqlExtractOptionsPanel();
		panel.add(optionsPanel, c);
		optionsPanel.setVisible(false);
		
		extractButton = createButton("Extract", 'e');
		extractButton.setEnabled(false);
		c.fill = GridBagConstraints.NONE;
		panel.add(extractButton, c);
		
		message = new JLabel("");
		panel.add(message, c);
		
		JLabel padding = new JLabel();
		c.weighty = 1.0;
		panel.add(padding, c);
		getContentPane().add(panel, BorderLayout.CENTER);
	}
	
	private void addPredifinedEndpoints(){
		endpointToDefaultGraph = new HashMap<URL, List<String>>();
		for(SparqlEndpoint endpoint : SparqlEndpoint.listEndpoints()){
			endpointToDefaultGraph.put(endpoint.getURL(), endpoint.getDefaultGraphURIs());
		}		
		for(URL url : endpointToDefaultGraph.keySet()){
			comboBox.addItem(url.toString());
		}		
	}
	
	private void positionErrorDialog(JFrame owner) {
		if (owner == null || !owner.isVisible()) {
			Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation(screenDimension.width / 2 - getSize().width / 2, screenDimension.height / 2 - getSize().height / 2);
		}
	}
	 

	private void expandOptionsPanel(){
		optionsButton.setIcon(toggledIcon);
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
		optionsButton.setIcon(untoggledIcon);
		Dimension dialogSize = getSize ();
		Dimension detailSize = optionsPanel.getPreferredSize ();
		dialogSize.height -= detailSize.height;
		setSize (dialogSize);
		optionsPanel.setVisible(false);
		//  Cause the new layout to take effect
		invalidate ();
		validate ();		
	}
	 
	 private JButton createButton (String label, char mnemonic)  {
			//  Create the new button object
			JButton newButton = new JButton (label);
			newButton.setActionCommand(label);
			
			newButton.setPreferredSize (new Dimension (90, 30));
			newButton.setMargin (new Insets (2, 2, 2, 2));
			
			if (mnemonic != '\0')  {
				//  Specify the button's mnemonic
				newButton.setMnemonic (mnemonic);
			}
			
			//  Setup the dialog to listen to events
			newButton.addActionListener (this);
			
			return newButton;
	}
	 
	 public int showDialog(){
		 setSize(500, 400);
		 setVisible(true);
		 setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);	 
		 return returnCode;
	 }
	 	
	private void extract() {
		message.setText("Checking SPARQL endpoint availability");
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		mon = new ProgressMonitor(this, "Extracting fragment", "", 0, 100);
		
		extractTask = new OntologyExtractingTask(this, mon);
		extractTask.addPropertyChangeListener(this);
		extractTask.execute();
		
	}
	
	private boolean urlIsConnectable()
	{	
		
	
		URL url = null;
		try {
//			List<String> defaultGraphURIS = new ArrayList<String>(1);
//			defaultGraphURIS.add(defaultGraphField.getText());	
//			SparqlEndpoint endpoint = new SparqlEndpoint(new URL(comboBox.getSelectedItem().toString()), defaultGraphURIS, Collections.<String>emptyList());
//			url = new URL(endpoint.getHTTPRequest() + "SELECT * WHERE {?s ?p ?o} LIMIT 1");
			url = new URL((String)comboBox.getSelectedItem());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   try {
		if(url.openConnection().getContentLength() > 0){
			   return true;
		   } else {
			   return false;
		   }
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return false;
	}
	
	private boolean urlIsValid(){
		Document urlDoc = ((JTextComponent)comboBox.getEditor().getEditorComponent()).getDocument();
		String url = "";
		try {
			url = urlDoc.getText(0, urlDoc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(url.toLowerCase().startsWith("http://")){
			return true;
		} else {
			return false;
		}
	}
	
	private void setExtractButtonEnabledToValidInput(){		
		extractButton.setEnabled(urlIsValid() && !classField.getText().isEmpty());
	}
	
	private String getClassFromLabel(){
		
		String queryString = "SELECT DISTINCT ?class WHERE {" +
		"?class rdf:type owl:Class ." +
		"?class rdfs:label ?label . " +
		"FILTER(regex(?label, '" + classField.getText() + "')) }";
		SortedSet<String> classes = task.queryAsSet(queryString, "class");

		return classes.iterator().next();
	}
	
	public SparqlKnowledgeSource getKnowledgeSource(){
		return ks;
	}
	
	class OntologyExtractingTask extends SwingWorker<Void, Void>{
		
		private ProgressMonitor mon;
		private JDialog dialog;
		
		public OntologyExtractingTask(JDialog dialog, ProgressMonitor mon) {		
			this.mon = mon;
			this.dialog = dialog;
		}

		@Override
		public Void doInBackground() {
			if(urlIsConnectable()){
				message.setText("Successfully connected to SPARQL endpoint");
				
			} else {
				message.setText("<html><font color=\"red\">Could not connect to SPARQL endpoint</html>");
				cancel(true);
			}	
			
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
			String concept;
			if(asLabelButton.isSelected()){
				concept = getClassFromLabel();
			} else {
				concept = classField.getText();
			}
//			String classKBString;
//			if(asLabelButton.isSelected()){
//				classKBString = "\"" + getClassFromLabel() + "\"";
//			} else {
//				classKBString = "\"" + classField.getText() +"\"";
//			}
		
//			AutomaticPositiveExampleFinderSPARQL pos = new AutomaticPositiveExampleFinderSPARQL(
//					task);
//			pos.makePositiveExamplesFromConcept(classKBString);

			SortedSet<String> allPosExamples = getPosExamples(concept);//pos.getPosExamples();
			SortedSet<String> posExamples = SetManipulation.stableShrink(
					allPosExamples, 20);
			System.out.println(posExamples);
			SortedSet<String> instances = new TreeSet<String>(posExamples);

			ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
			ks.getConfigurator().setUrl(endpoint.getURL());
			ks.getConfigurator().setInstances(instances);
			ks.getConfigurator().setPredefinedFilter("YAGO");
			ks.getConfigurator().setBreakSuperClassRetrievalAfter(optionsPanel.getBreakSuperClassRetrievalAfterValue());
			ks.getConfigurator().setRecursionDepth(optionsPanel.getRecursionDepthValue());
			ks.getConfigurator().setUseCache(optionsPanel.isUseCache());
			ks.getConfigurator().setGetAllSuperClasses(optionsPanel.isGetAllSuperClasses());
			ks.getConfigurator().setDissolveBlankNodes(optionsPanel.isDissolveBlankNodes());
			ks.getConfigurator().setUseImprovedSparqlTupelAquisitor(optionsPanel.isUseImprovedSparqlTupelAquisitor());
			ks.getConfigurator().setUseLits(false);//optionsPanel.isUseLiterals());
			ks.getConfigurator().setGetPropertyInformation(optionsPanel.isGetPropertyInformation());
			ks.getConfigurator().setCloseAfterRecursion(optionsPanel.isCloseAfterRecursion());
			ks.addProgressMonitor(mon);
			ks.init();

			return null;
		}

		@Override
		public void done() {
			dialog.setCursor(null);
			if(!isCancelled() && ks != null){
				okButton.setEnabled(true);
				message.setText("<html><font color=\"green\">Fragment successfully extracted</html>");
				
			}
		}
		
		private SortedSet<String> getPosExamples(String concept){
			SortedSet<String> examples = new TreeSet<String>();
			SortedSet<String> superClasses = task.getSuperClasses(concept, 2);
			
			for (String sup : superClasses) {
				examples.addAll(task.retrieveInstancesForClassDescription("\""
						+ sup + "\"", 20));

			}
			return examples;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Extract")){
			extract();
		} else if(e.getActionCommand().equals("Ok")){
			returnCode = OK_RETURN_CODE;
			closeDialog();
		} else if(e.getActionCommand().equals("Cancel")){
			returnCode = CANCEL_RETURN_CODE;
			closeDialog();
		} else if(e.getActionCommand().equals("endpoints")){
			message.setText("");
			JComboBox cb = (JComboBox)e.getSource();
			if(cb.getSelectedIndex() >= 0){
		        URL endpointURL = null;
				try {
					endpointURL = new URL((String)cb.getSelectedItem());
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        List<String> defaultGraphs = endpointToDefaultGraph.get(endpointURL);
		        if(defaultGraphs != null && !defaultGraphs.isEmpty()){
		        	defaultGraphField.setText(defaultGraphs.iterator().next());
		        } else {
		        	defaultGraphField.setText("");
		        }
			}

		}
		
	}
	
	private void closeDialog(){
		setVisible(false);
		dispose();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if ("progress" == evt.getPropertyName() ) {
			 if(mon.isCanceled()){
				 extractTask.cancel(true);
			 }
		 }		
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		setExtractButtonEnabledToValidInput();		
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		setExtractButtonEnabledToValidInput();		
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		setExtractButtonEnabledToValidInput();	
	}
	
	public static void main(String[] args){
		ExtractFromSparqlDialog dialog = new ExtractFromSparqlDialog(null);
		dialog.showDialog();
	}		
}


