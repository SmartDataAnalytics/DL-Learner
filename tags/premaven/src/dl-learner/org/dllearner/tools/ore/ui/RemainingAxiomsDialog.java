package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class RemainingAxiomsDialog extends JDialog implements ActionListener{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 8019086232940177286L;
	
		private JComponent explanationsPanel;
		private Set<RemainingAxiomsTable> tables;
		
		public static final int CANCEL_RETURN_CODE = 0;
		public static final int OK_RETURN_CODE = 1;
		
		private int returnCode;
		
		@SuppressWarnings("unused")
		private JButton okButton = null;
		@SuppressWarnings("unused")
		private JButton cancelButton = null;
		
		private List<OWLOntologyChange> changes;
		
		private ExplanationManager expMan;
		
		private OWLOntology ontology;
		
		
		public RemainingAxiomsDialog(OWLAxiom laconicAxiom, OWLOntology ont){
			super(TaskManager.getInstance().getDialog(), "Selected part of axioms in ontology", true);
			setLayout(new BorderLayout());
			JTextPane info = new JTextPane();
			info.setBackground(getParent().getBackground());
			info.setEditable(false);
			info.setContentType("text/html");
			info.setText("<html>You selected an axiom, which is only part of some axioms in the ontology." +
					" To retain the remaining parts, you can select them below.</html>");
			add(info, BorderLayout.NORTH);
			createControls();
			
			this.ontology = ont;
			
			tables = new HashSet<RemainingAxiomsTable>();
			explanationsPanel = new Box(1);
			
			expMan = ExplanationManager.getInstance(OREManager.getInstance());
			
			changes = new ArrayList<OWLOntologyChange>();
			
			Map<OWLAxiom, Set<OWLAxiom>> sourceAxiom2RemainingAxiomPartsMap = expMan.getRemainingAxiomParts(laconicAxiom);
			for(OWLAxiom source : sourceAxiom2RemainingAxiomPartsMap.keySet()){
				
				changes.add(new RemoveAxiom(ont, source));
				explanationsPanel.add(createRemainingAxiomsPanel(source, sourceAxiom2RemainingAxiomPartsMap.get(source)));
				explanationsPanel.add(Box.createVerticalStrut(5));
			}
			JPanel p = new JPanel(new BorderLayout());
			p.add(explanationsPanel, BorderLayout.NORTH);
			add(p, BorderLayout.CENTER);
			
	
		}
		
		private JPanel createRemainingAxiomsPanel(OWLAxiom source, Set<OWLAxiom> remainingParts){
			JPanel panel = new JPanel(new BorderLayout());
			
			JPanel p = new JPanel(new GridLayout(3, 1));
			p.add(new JLabel("<html><b>Axiom:</b></html>"));
			JTextPane sourceAxiomPane = new JTextPane();
			sourceAxiomPane.setContentType("text/html");
			sourceAxiomPane.setText(ManchesterSyntaxRenderer.render(source, false, 0));
			sourceAxiomPane.setEditable(false);
//			sourceAxiomPane.setBackground(getParent().getBackground());
			p.add(sourceAxiomPane);
			p.add(new JLabel("<html><b>Remaining parts:</b></html>"));
			panel.add(p, BorderLayout.NORTH);
			
			RemainingAxiomsTable table = new RemainingAxiomsTable(new ArrayList<OWLAxiom>(remainingParts));
			panel.add(table);
			
			panel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);
			
			tables.add(table);
			
			return panel;
		}
		
		private void createControls() {
			Box buttonBox = Box.createHorizontalBox();

			// Create a panel to hold a box with the buttons in it - to give it the
			// right space around them
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonBox);
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			// Create the buttons and add them to the box (leading strut will give
			// the dialog box its width)
			buttonBox.add(okButton = createButton("Ok", 'o'));
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(Box.createHorizontalStrut(4));
			buttonBox.add(cancelButton = createButton("Cancel", 'c'));
			buttonBox.add(Box.createHorizontalStrut(10));

			// Add the button panel to the bottom of the BorderLayout
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		}

	private JButton createButton(String label, char mnemonic) {
		// Create the new button object
		JButton newButton = new JButton(label);
		newButton.setActionCommand(label);

		newButton.setPreferredSize(new Dimension(90, 30));
		newButton.setMargin(new Insets(2, 2, 2, 2));

		if (mnemonic != '\0') {
			// Specify the button's mnemonic
			newButton.setMnemonic(mnemonic);
		}

		// Setup the dialog to listen to events
		newButton.addActionListener(this);

		return newButton;
	}
	
	 public int showDialog(){
		 setSize(700, 400);
		 setVisible(true);
		 setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);	 
		 return returnCode;
	 }

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
	
	public List<OWLOntologyChange> getChanges(){
	
		for(RemainingAxiomsTable table : tables){
			for(OWLAxiom ax : table.getSelectedAxioms()){
				changes.add(new AddAxiom(ontology, ax));
			}
		}
		return changes;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Ok")) {
			returnCode = OK_RETURN_CODE;
			closeDialog();
		} else {
			returnCode = CANCEL_RETURN_CODE;
			closeDialog();

		}
	}

}
