package org.dllearner.tools.protege;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;

import java.awt.GridLayout;

import org.dllearner.core.EvaluatedDescription;

import org.dllearner.core.owl.Individual;
public class MoreDetailForSuggestedConceptsPanel extends JPanel{

	private static final long serialVersionUID = 785272797932584581L;

	private DLLearnerModel model;
	
	private JLabel accuracy;
	private JPanel accuracyPanel;
	private JPanel coveredExamplesPanel;
	
	private JLabel coveredExamples;
	
	private JDialog detailPopup;
	
	private JScrollPane detailScroll;
	
	private JPanel detailPanel;
	private EvaluatedDescription evalDescription;
	public MoreDetailForSuggestedConceptsPanel(DLLearnerModel model)
	{
		this.model = model;
	}
	
	public JDialog getMoreDialog()
	{
		return detailPopup;
	}
	
	public JScrollPane getDetailScrollPane()
	{
		return detailScroll;
	}
	
	public void renderDetailPanel(EvaluatedDescription eval)
	{
		this.evalDescription = eval;
		accuracy = new JLabel("Accuracy:");
		coveredExamples = new JLabel("Covered Examples:");
		detailPopup = new JDialog();
		detailPopup.setSize(300, 300);
		accuracyPanel = new JPanel(new GridLayout(0,1));
		coveredExamplesPanel = new JPanel(new GridLayout(0,1));
		detailScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		detailPanel = new JPanel(new GridLayout(0,2));
		accuracyPanel.add(new JLabel(String.valueOf(evalDescription.getAccuracy())));
		Set<Individual> posCovered = evalDescription.getCoveredPositives();
		for(Iterator<Individual> i = posCovered.iterator(); i.hasNext();)
		{
			coveredExamplesPanel.add(new JLabel(i.next().toString()));
		}
		detailPanel.add(accuracy);
		detailPanel.add(accuracyPanel);
		detailPanel.add(coveredExamples);
		detailPanel.add(coveredExamplesPanel);
		detailScroll.setViewportView(detailPanel);
		detailPopup.add(detailScroll);
	}
}
