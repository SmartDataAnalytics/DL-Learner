package org.dllearner.tools.protege;



import java.awt.event.*;
//TODO: Concepte und errormessages aus model holen 
public class ActionHandler implements ActionListener, ItemListener, MouseListener{
	private DLLearnerModel model;

	private String id;
	private Thread dlLearner;
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	public ActionHandler(ActionHandler a,DLLearnerModel m,OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view ,String i)
	{
		this.view = view; 
		this.id=i;
		this.model = m;
		
		
	}
	public void actionPerformed(ActionEvent z){
		
		if(z.getActionCommand().equals("Suggest "+id))
		{
			this.dlLearner = new Thread(model);
			view.getRunButton().setEnabled(false);
			view.getCancelButton().setEnabled(true);
			dlLearner.start();
		}
		
		if(z.getActionCommand().equals("Cancel"))
		{
			view.getRunButton().setEnabled(true);
			view.getCancelButton().setEnabled(false);
			String error = "Learning aborted";
			view.renderErrorMessage(error);
			model.getLearningAlgorithm().stop();
			dlLearner.interrupt();

		}
		
		if(z.getActionCommand().equals("ADD"))
		{
			String suggest=view.getSuggestionList().getSelectedValue().toString();
			for(int i = 0;i<model.getSolutions().length;i++)
			{
				if(model.getSolutions()[i].toString().equals(suggest))
				{
					model.changeDLLearnerDescriptionsToOWLDescriptions(model.getSolutions()[i]);
				}
			}
			
			String message ="Concept added";
			view.renderErrorMessage(message);
		}
    }

	public String getID()
	{
		return id;
	}
	
	public void itemStateChanged(ItemEvent i)
	{
		//System.out.println(i.getItem());
	}
	
	public void mouseReleased(MouseEvent m)
	{
		
	}
	
	public void mouseEntered(MouseEvent m)
	{
		
	}
	
	public void mouseClicked(MouseEvent m)
	{
	           
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
	
	public void destroyDLLearnerThread()
	{
		dlLearner =null;
	}


}
