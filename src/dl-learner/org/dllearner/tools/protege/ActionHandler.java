package org.dllearner.tools.protege;



import java.awt.event.*;
import java.util.Observable;

public class ActionHandler extends Observable implements ActionListener, ItemListener, MouseListener {
	private DLLearnerModel model;
	private SuggestEquivalentClassView view;
	private Thread dlLearner;
	public ActionHandler(ActionHandler a,DLLearnerModel m, SuggestEquivalentClassView s)
	{
		view = s;
		model = m;
	}
	public void actionPerformed(ActionEvent z){
		
		if(z.getActionCommand().equals("RUN"))
		{
			model.setDLLearnerModel(view.getPositiveVector(), view.getNegativeVector(),view.getUri());
			dlLearner = new Thread(model);
			view.getStartButton().setEnabled(false);
			view.getStopButton().setEnabled(true);
			dlLearner.start();
		}
		
		if(z.getActionCommand().equals("Cancel"))
		{
			model.getLearningAlgorithm().stop();
			view.getStartButton().setEnabled(true);
			view.getStopButton().setEnabled(false);
			String error = "Learning aborted";
			dlLearner.interrupt();
			view.renderErrorMessage(error);
		}
		
		if(z.getActionCommand().equals("ADD"))
		{
			String suggest=view.getSuggestionList().getSelectedValue().toString();
			for(int i = 0;i<model.getSolutions().length;i++)
			{
				if(model.getSolutions()[i].toString().equals(suggest))
				{
					model.changeDLLearnerDescriptionsToOWLDescriptions(model.getSolutions()[i]);
					System.out.println(model.getSolutions()[i].toString());
				}
			}
			
			String message ="Concept added";
			view.renderErrorMessage(message);
		}
    }

	
	public void itemStateChanged(ItemEvent i)
	{
		
	}
	
	public void mouseReleased(MouseEvent m)
	{
		
	}
	
	public void mouseEntered(MouseEvent m)
	{
		
	}
	
	public void mouseClicked(MouseEvent m)
	{
		System.out.println("mouseClicked: ");
	}
	
	public void mouseExited(MouseEvent m)
	{
		
	}
	
	public void mousePressed(MouseEvent m)
	{
		
	}
	public void textValueChanged(TextEvent t)
	{

	}
	

}
