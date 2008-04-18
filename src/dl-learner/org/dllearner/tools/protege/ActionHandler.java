package org.dllearner.tools.protege;



import java.awt.event.*;
import java.util.Observable;
public class ActionHandler extends Observable implements ActionListener, ItemListener {
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
			dlLearner = new Thread(model);
			System.out.println("test");
			view.getStartButton().setEnabled(false);
			view.getStopButton().setEnabled(true);
			dlLearner.start();
		}
		
		if(z.getActionCommand().equals("Cancel"))
		{
			System.out.println(dlLearner.isInterrupted());
			model.getLearningAlgorithm().stop();
			view.destroyListener();
			view.getStartButton().setEnabled(true);
			view.getStopButton().setEnabled(false);
			System.out.println(dlLearner.isInterrupted());
			String error = "Learning aborted";
			dlLearner.interrupt();
			System.out.println(dlLearner.isInterrupted());
			view.renderErrorMessage(error);
		}
		
		if(z.getActionCommand().equals("ADD"))
		{
			String message ="Ausgezeichnet *Mr.Burns*";
			view.renderErrorMessage(message);
		}
    }

	
	public void itemStateChanged(ItemEvent i)
	{
		
	}
	
	public void textValueChanged(TextEvent t)
	{
		
		
	}
	
	public void destroyThread()
	{
		view.getStartButton().setEnabled(true);
		view.getStopButton().setEnabled(false);
	}
}
