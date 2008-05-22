package org.dllearner.tools.protege;



import java.awt.event.*;
//TODO: Concepte und errormessages aus model holen 
public class ActionHandler implements ActionListener, ItemListener, MouseListener{
	private DLLearnerModel model;

	private String id;
	private boolean toggled;
	private Thread dlLearner;
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	public ActionHandler(ActionHandler a,DLLearnerModel m,OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view ,String i)
	{
		this.view = view; 
		this.id=i;
		this.model = m;
		toggled = false;
		
		
	}
	public void actionPerformed(ActionEvent z){
		
		if(z.getActionCommand().equals("Suggest "+id))
		{
			if(view.getOptionPanel().getComponentCount()<=2)
			{
				view.renderErrorMessage("No Examples available");
			}
			else{
			view.renderErrorMessage("Learning started");
			this.dlLearner = new Thread(model);
			view.getRunButton().setEnabled(false);
			view.getCancelButton().setEnabled(true);
			dlLearner.start();
			//String error = "Learning succesful";
			//view.renderErrorMessage(error);
			}
		}
		
		if(z.getActionCommand().equals("Cancel"))
		{
			view.getRunButton().setEnabled(true);
			view.getCancelButton().setEnabled(false);
			String error = "Learning aborted";
			view.renderErrorMessage(error);
			dlLearner.interrupt();
			model.getLearningAlgorithm().stop();
		}
		
		if(z.getActionCommand().equals("ADD"))
		{
			String suggest=view.getSuggestionList().getSelectedValue().toString();
			for(int i = 0;i<model.getSolutions().length;i++)
			{
				if(model.getSolutions()[i].toString().equals(suggest))
				{
					System.out.println(model.getSolutions()[i]);
					model.changeDLLearnerDescriptionsToOWLDescriptions(model.getSolutions()[i]);
				}
			}
			
			String message ="Concept added";
			view.renderErrorMessage(message);
		}
		
		if(z.getActionCommand().equals("?"))
		{
			if(z.getSource().toString().contains("PosHelpButton"))
			{
				String hilfe="A Instance that follows from the classdescription. Per Default all that belongs to the class.";
				view.renderHelpMessage(hilfe);
			}
			
			if(z.getSource().toString().contains("NegHelpButton"))
			{
				String hilfe="A Instance tht doesn't follow from the classdescription.";
				view.renderHelpMessage(hilfe);
			}
			
			
		}
		if(z.getActionCommand().equals(""))
		{
			if(!toggled==true)
			{
				toggled=true;
				view.setExamplePanelVisible(toggled);
			}
			else
			{
				toggled=false;
				view.setExamplePanelVisible(toggled);
			}
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
	
	public void resetToggled()
	{
		toggled = false;
	}

}
