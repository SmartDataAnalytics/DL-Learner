package org.dllearner.tools.protege;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.dllearner.core.owl.Description;
/**
 * 
 * @author Heero Yuy
 *
 */
public class ActionHandler implements ActionListener, ItemListener, MouseListener, ListSelectionListener{
	/**
	 * This is the DLLearnerModel.
	 */
	private DLLearnerModel model;
	/**
	 * This is the id that checks if the equivalent class or subclass button is 
	 * pressed in protege 
	 */
	private String id;
	/**
	 * this is a boolean that checked if the advanced button was pressed or not.
	 */
	private boolean toggled;
	/**
	 * This is the Tread of the DL-Learner
	 */
	private Thread dlLearner;
	/**
	 * This is the view of the DL-Learner tab.
	 */
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	/**
	 * This is the constructor for the action handler
	 * @param a ActionHandler
	 * @param m DLLearnerModel
	 * @param view DLlearner tab
	 * @param i id if it is a subclass oran equivalent class
	 */
	public ActionHandler(ActionHandler a,DLLearnerModel m,OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view ,String i)
	{
		this.view = view; 
		this.id=i;
		this.model = m;
		toggled = false;
		
		
	}
	/**
	 * 
	 */
	public void actionPerformed(ActionEvent z){
		
		if(z.getActionCommand().equals("Suggest "+id))
		{
			if(model.getAlreadyLearned()==true)
			{
				model.unsetListModel();
			}
			if(view.getPosAndNegSelectPanel().getPosAndNegSelectPanel().getComponentCount()<=2)
			{
				view.renderErrorMessage("Could not start learning. No Examples where available");
			}
			else{
			model.setKnowledgeSource();
			model.setReasoner();
			model.setPositiveAndNegativeExamples();
			model.setLearningProblem();
			model.setLearningAlgorithm();
			this.dlLearner = new Thread(model);
			dlLearner.start();
			view.getRunButton().setEnabled(false);
			view.getCancelButton().setEnabled(true);
			view.renderErrorMessage("Learning started");
			view.getPosAndNegSelectPanel().unsetCheckBoxes();
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
			model.setErrorMessage(error);
		}
		
		if(z.getActionCommand().equals("ADD"))
		{
			model.changeDLLearnerDescriptionsToOWLDescriptions((Description)view.getSuggestClassPanel().getSuggestList().getSelectedValue());
			String message ="Concept added";
			view.renderErrorMessage(message);
		}
		
		if(z.getActionCommand().equals("?"))
		{
			if(z.getSource().toString().contains("PosHelpButton"))
			{
				String hilfe="A Instance that follows from the classdescription.\nPer Default all that belongs to the class.";
				view.getPosAndNegSelectPanel().renderHelpMessage(hilfe);
			}
			
			if(z.getSource().toString().contains("NegHelpButton"))
			{
				String hilfe="A Instance tht doesn't follow from the classdescription.";
				view.getPosAndNegSelectPanel().renderHelpMessage(hilfe);
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
    
	/**	
	 * 
	 * @return
	 */
	public String getID()
	{
		return id;
	}
	/**
	 * 
	 */
	public void itemStateChanged(ItemEvent i)
	{
		if(i.getItem().toString().contains("Positive"))
		{
			for(int j = 0;j < model.getPosVector().size(); j++)
			{
				if(i.getItem().toString().contains(model.getPosVector().get(j).getText().toString()))
				{
					if(!model.getPosVector().get(j).isSelected())
					{
						model.getPosVector().get(j).setSelected(true);
						break;
					}
					if(model.getPosVector().get(j).isSelected())
					{
						model.getPosVector().get(j).setSelected(false);
						break;
					}
				}
			}
		}
		if(i.getItem().toString().contains("Negative"))
		{
			for(int j = 0;j < model.getNegVector().size(); j++)
			{
				if(i.getItem().toString().contains(model.getNegVector().get(j).getText().toString()))
				{
					if(!model.getNegVector().get(j).isSelected())
					{
						model.getNegVector().get(j).setSelected(true);
						break;
					}
					if(model.getNegVector().get(j).isSelected())
					{
						model.getNegVector().get(j).setSelected(false);
						break;
					}
				}
			}
		}
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
		
	}
	/**
	 * 
	 */
	public void mouseReleased(MouseEvent m)
	{
		
	}
	/**
	 * 
	 */
	public void mouseEntered(MouseEvent m)
	{
		
	}
	/**
	 * 
	 */
	public void mouseClicked(MouseEvent m)
	{
	           
	}
	/**
	 * 
	 */
	public void mouseExited(MouseEvent m)
	{
		
	}
	/**
	 * 
	 */
	public void mousePressed(MouseEvent m)
	{
		if(!view.getAddButton().isEnabled())
		{
			view.getAddButton().setEnabled(true);
		}
	}
	/**
	 * 
	 * @param t
	 */
	/*public void textValueChanged(TextEvent t)
	{

	}*/
	/**
	 * 
	 */
	public void destroyDLLearnerThread()
	{
		dlLearner =null;
	}
	/**
	 * 
	 */
	public void resetToggled()
	{
		toggled = false;
	}

}
