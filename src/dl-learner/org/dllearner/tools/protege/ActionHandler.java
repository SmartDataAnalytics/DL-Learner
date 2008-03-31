package org.dllearner.tools.protege;

import java.awt.event.*;

public class ActionHandler implements ActionListener, ComponentListener{
	private ActionHandler action;
   
	
	public ActionHandler(ActionHandler a)
	{
		action = a;
	}
	public void actionPerformed(ActionEvent z){
		System.out.println("hihihi: "+ z.getActionCommand());
    	}

	public void componentHidden(ComponentEvent e)
	{
		System.out.println("1: "+e.getID());
	}
	
	public void componentMoved(ComponentEvent e)
	{
		System.out.println("2: "+ e.getComponent().getClass().getName());
	}
	
	public void componentResized(ComponentEvent e)
	{
		//System.out.println("3: "+ e.getComponent().getClass().getName());
	}
	
	public void componentShown(ComponentEvent e)
	{
		System.out.println("4: "+ e.getID());
	}
	

}
