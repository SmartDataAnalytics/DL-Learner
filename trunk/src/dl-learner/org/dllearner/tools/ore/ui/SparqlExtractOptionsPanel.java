package org.dllearner.tools.ore.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

public class SparqlExtractOptionsPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7870618540578215293L;
	
	private JSpinner recursionDepthSpinner;
	private JSpinner breakSuperClassRetrievalAfterSpinner;
	private JCheckBox useCacheCheckBox;
	private JCheckBox useImprovedSparqlTupelAquisitorCheckBox;
	private JCheckBox dissolveBlankNodesCheckBox;
	private JCheckBox getPropertyInformationCheckBox;
	private JCheckBox closeAfterRecursionCheckBox;
	private JCheckBox getAllSuperClassesCheckBox;
	private JCheckBox useLitsCheckBox;
	
	public SparqlExtractOptionsPanel(){
		createUI();
		setDefaults();
		setToolTips();	
	}
	
	private void createUI(){
		setBorder(new TitledBorder("Options"));
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.anchor = GridBagConstraints.LINE_END;	
		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel("Recursion depth:"), c);
		recursionDepthSpinner = new JSpinner();
		recursionDepthSpinner.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 0;
		add(recursionDepthSpinner, c);
		
		
		c.anchor = GridBagConstraints.LINE_END;	
		c.gridx = 0;
		c.gridy = 1;
		add(new JLabel("Break superclass retrieval after:"), c);
		breakSuperClassRetrievalAfterSpinner = new JSpinner();
		breakSuperClassRetrievalAfterSpinner.setModel(new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1));
		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 1;
		add(breakSuperClassRetrievalAfterSpinner, c);
		
		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 2;
		add(new JLabel("Use cache"), c);
		useCacheCheckBox = new JCheckBox();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.gridy = 2;
		add(useCacheCheckBox, c);
		
		
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 1;
		c.gridy = 3;
		add(new JLabel("Use improved Sparql tuple aquisitor"), c);
		useImprovedSparqlTupelAquisitorCheckBox = new JCheckBox();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.gridy = 3;
		add(useImprovedSparqlTupelAquisitorCheckBox, c);

		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 4;
		add(new JLabel("Dissolve blank nodes"), c);
		dissolveBlankNodesCheckBox = new JCheckBox();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.gridy = 4;
		add(dissolveBlankNodesCheckBox, c);
		
		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 5;
		add(new JLabel("Get property informations"), c);
		getPropertyInformationCheckBox = new JCheckBox();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.gridy = 5;
		add(getPropertyInformationCheckBox, c);
		
		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 6;
		add(new JLabel("Close after recursion"), c);
		closeAfterRecursionCheckBox = new JCheckBox();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.gridy = 6;
		add(closeAfterRecursionCheckBox, c);
		
		c.anchor = GridBagConstraints.LINE_START;	
		c.gridx = 1;
		c.gridy = 7;
		add(new JLabel("Get all superclasses"), c);
		getAllSuperClassesCheckBox = new JCheckBox();
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 0;
		c.gridy = 7;
		add(getAllSuperClassesCheckBox, c);
		
//		c.anchor = GridBagConstraints.LINE_START;	
//		c.gridx = 1;
//		c.gridy = 8;
//		add(new JLabel("Use literals"), c);
//		useLitsCheckBox = new JCheckBox();
//		c.anchor = GridBagConstraints.LINE_END;
//		c.gridx = 0;
//		c.gridy = 8;
//		add(useLitsCheckBox, c);
		
	}
	
	public int getRecursionDepthValue(){
		return ((Integer)recursionDepthSpinner.getValue()).intValue();
	}
	
	public int getBreakSuperClassRetrievalAfterValue(){
		return ((Integer)breakSuperClassRetrievalAfterSpinner.getValue()).intValue();
	}
	
	public boolean isUseCache(){
		return useCacheCheckBox.isSelected();
	}
	
	public boolean isUseImprovedSparqlTupelAquisitor(){
		return useImprovedSparqlTupelAquisitorCheckBox.isSelected();
	}
	
	public boolean isDissolveBlankNodes(){
		return dissolveBlankNodesCheckBox.isSelected();
	}
	
	public boolean isGetPropertyInformation(){
		return getPropertyInformationCheckBox.isSelected();
	}
	
	public boolean isCloseAfterRecursion(){
		return closeAfterRecursionCheckBox.isSelected();
	}
	
	public boolean isGetAllSuperClasses(){
		return getAllSuperClassesCheckBox.isSelected();
	}
	
	public boolean isUseLiterals(){
		return useLitsCheckBox.isSelected();
	}
	
	public void setDefaults(){
		recursionDepthSpinner.setValue(1);
		breakSuperClassRetrievalAfterSpinner.setValue(1000);
		useCacheCheckBox.setSelected(true);
		useImprovedSparqlTupelAquisitorCheckBox.setSelected(false);
		dissolveBlankNodesCheckBox.setSelected(false);
		getPropertyInformationCheckBox.setSelected(false);
		closeAfterRecursionCheckBox.setSelected(true);
		getAllSuperClassesCheckBox.setSelected(true);
//		useLitsCheckBox.setSelected(true);
	}
	
	public void setToolTips(){
		recursionDepthSpinner.setToolTipText("recursion depth of KB fragment selection");
		breakSuperClassRetrievalAfterSpinner.setToolTipText("stops a cyclic hierarchy after specified number of classes");
		useCacheCheckBox.setToolTipText("if true a Cache is used");
		useImprovedSparqlTupelAquisitorCheckBox.setToolTipText("uses deeply nested SparqlQueries, according to recursion depth, still EXPERIMENTAL");
		dissolveBlankNodesCheckBox.setToolTipText("Determines whether blanknodes are dissolved. This is a costly function.");
		getPropertyInformationCheckBox.setToolTipText("gets all types for extracted ObjectProperties");
		closeAfterRecursionCheckBox.setToolTipText("gets all classes for all instances");
		getAllSuperClassesCheckBox.setToolTipText("If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.");
//		useLitsCheckBox.setToolTipText("use Literals in SPARQL query");
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.add(new SparqlExtractOptionsPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(400, 400));
		frame.setVisible(true);
	}

}
