package org.dllearner.tools.protege;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import java.awt.GridLayout;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
public class MoreDetailForSuggestedConceptsPanel extends JPanel{

	private static final long serialVersionUID = 785272797932584581L;

	private DLLearnerModel model;
	
	private JTextArea accuracy;
	private JPanel accuracyPanel;
	
	private JLabel coveredPositiveExamples;
	private JLabel coveredNegativeExamples;
	private JLabel notCoveredPositiveExamples;
	private JLabel notCoveredNegativeExamples;
	private JDialog detailPopup;
	private JPanel examplePanel;
	private JTextArea posCoveredText;
	private JTextArea posNotCoveredText;
	private JTextArea negCoveredText;
	private JTextArea negNotCoveredText;
	private JTextArea accuracyText;
	private JScrollPane detailScroll;
	
	private JPanel detailPanel;

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
	private EvaluatedDescription getSelectedConcept(Description eval)
	{
		List<EvaluatedDescription> evalDesc = model.getEvaluatedDescriptionList();
		EvaluatedDescription eDesc = null;
		
		for(Iterator<EvaluatedDescription> i = evalDesc.iterator(); i.hasNext();)
		{
			if(eDesc==null)
			{
			EvaluatedDescription e = i.next();
			System.out.println("Description: "+e.getDescription());
			System.out.println("Description2: "+eval);
			if(e.getDescription().toString().equals(eval.toString()))
			{
				eDesc = e;
			}
		
			}
		}
		return eDesc;
	}
	
	public void renderDetailPanel(Description desc)
	{
		EvaluatedDescription eval = getSelectedConcept(desc);
		System.out.println("Eval: "+eval);
		accuracyPanel = new JPanel(new GridLayout(0,2));
		accuracy = new JTextArea("Accuracy:");
		accuracy.setEditable(false);
		accuracyPanel.add(accuracy);
		posCoveredText = new JTextArea();
		posCoveredText.setEditable(false);
		posNotCoveredText = new JTextArea();
		posNotCoveredText.setEditable(false);
		negCoveredText = new JTextArea();
		negCoveredText.setEditable(false);
		negNotCoveredText = new JTextArea();
		negNotCoveredText.setEditable(false);
		accuracyText = new JTextArea();
		accuracyText.setEditable(false);
		if(eval!=null)
		{
		accuracyText.append(String.valueOf(eval.getAccuracy()));
		}
		accuracyPanel.add(accuracyText);
		accuracyPanel.setBounds(0,0,400,40);
		examplePanel = new JPanel(new GridLayout(0,2));
		detailPanel = new JPanel(new GridLayout(0,1));
		//detailPanel.setLayout(null);
		examplePanel.add(accuracy);
		examplePanel.add(accuracyText);
		coveredPositiveExamples = new JLabel("Covered Positive Examples:");
		notCoveredPositiveExamples = new JLabel("Not Covered Positive Examples");
		coveredNegativeExamples = new JLabel("Covered Negative Examples:");
		notCoveredNegativeExamples = new JLabel("Not Covered Negative Examples");
		detailPopup = new JDialog();
		detailPopup.setSize(400, 400);
		detailPopup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		detailPopup.setVisible(true);
		detailPopup.setResizable(false);
		detailScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		examplePanel.add(coveredPositiveExamples);
		examplePanel.add(notCoveredPositiveExamples);
		if(eval!=null)
		{
		for(Iterator<Individual> i = eval.getCoveredPositives().iterator(); i.hasNext();)
		{
			posCoveredText.append(i.next()+"\n");
		}
		examplePanel.add(posCoveredText);
		}
		if(eval!=null)
		{
			
		for(Iterator<Individual> i = eval.getNotCoveredPositives().iterator(); i.hasNext();)
		{
			posNotCoveredText.append(i.next()+"\n");
		}
		examplePanel.add(posNotCoveredText);
		}
		examplePanel.add(coveredNegativeExamples);
		examplePanel.add(notCoveredNegativeExamples);

		if(eval!=null)
		{
		for(Iterator<Individual> i = eval.getCoveredNegatives().iterator(); i.hasNext();)
		{
			negCoveredText.append(i.next()+"\n");
		}
		examplePanel.add(negCoveredText);
		}
		if(eval!=null)
		{
			
		for(Iterator<Individual> i = eval.getNotCoveredNegatives().iterator(); i.hasNext();)
		{
			negNotCoveredText.append(i.next()+"\n");
		}
		examplePanel.add(negNotCoveredText);
		}
		//detailPanel.add(accuracyPanel);
		detailPanel.add(examplePanel);
		detailScroll.setViewportView(detailPanel);
		
		detailPopup.add(detailScroll);
	}
}
