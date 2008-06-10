package org.dllearner.tools.protege;



//import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;

import org.dllearner.core.owl.Description;
/**
 * 
 * @author Heero Yuy
 *
 */
public class ActionHandler implements ActionListener, ItemListener, MouseListener{
	/**
	 * 
	 */
	private DLLearnerModel model;
	/**
	 * 
	 */
	private String id;
	/**
	 * 
	 */
	private boolean toggled;
	/**
	 * 
	 */
	private Thread dlLearner;
	/**
	 * 
	 */
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	/**
	 * 
	 * @param a
	 * @param m
	 * @param view
	 * @param i
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
			if(view.getOptionPanel().getComponentCount()<=2)
			{
				view.renderErrorMessage("Could not start learning. No Examples where available");
			}
			else{
			view.renderErrorMessage("Learning started");
			this.dlLearner = new Thread(model);
			view.getRunButton().setEnabled(false);
			view.getCancelButton().setEnabled(true);
			dlLearner.start();
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
			model.changeDLLearnerDescriptionsToOWLDescriptions((Description)view.getSuggestionList().getSelectedValue());
			String message ="Concept added";
			view.renderErrorMessage(message);
		}
		
		if(z.getActionCommand().equals("?"))
		{
			if(z.getSource().toString().contains("PosHelpButton"))
			{
				String hilfe="A Instance that follows from the classdescription.\nPer Default all that belongs to the class.";
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
		//System.out.println(i.getItem());
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
	public void textValueChanged(TextEvent t)
	{

	}
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
