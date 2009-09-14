package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.TaskManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

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
		private List<OWLAxiom> sourceAxioms;
		
		private ExplanationManager expMan;
		private ImpactManager impMan;
		
		private OWLOntology ontology;
		
		
		public RemainingAxiomsDialog(OWLAxiom laconicAxiom, OWLOntology ont){
			super(TaskManager.getInstance().getDialog(), "Selected part of axiom in ontology", true);
			setLayout(new BorderLayout());
			add(new JLabel("You selected an axiom which is only part of some axioms in the ontology"), BorderLayout.NORTH);
			createControls();
			
			this.ontology = ont;
			
			tables = new HashSet<RemainingAxiomsTable>();
			explanationsPanel = new Box(1);
			
			expMan = ExplanationManager.getInstance(OREManager.getInstance());
			impMan = ImpactManager.getInstance(OREManager.getInstance());
			
			changes = new ArrayList<OWLOntologyChange>();
			sourceAxioms = new ArrayList<OWLAxiom>();
			
			sourceAxioms.addAll(expMan.getSourceAxioms(laconicAxiom));
			
			for(OWLAxiom source : sourceAxioms){
				
				changes.add(new RemoveAxiom(ont, source));
				List<OWLAxiom> remainingAxioms = new ArrayList<OWLAxiom>(expMan.getRemainingAxioms(source, laconicAxiom));
				RemainingAxiomsTable table = new RemainingAxiomsTable(remainingAxioms);
				tables.add(table);
				explanationsPanel.add(table);
				
			}
			
			add(explanationsPanel, BorderLayout.CENTER);
			
	
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
			impMan.addSelection(sourceAxioms);
			closeDialog();
		} else {
			returnCode = CANCEL_RETURN_CODE;
			closeDialog();

		}
	}

}
