package org.dllearner.tools.protege;

import java.awt.event.*;
public class ActionHandler implements ActionListener, ItemListener{
	private DLLearnerModel model;
	
	public ActionHandler(ActionHandler a,DLLearnerModel m)
	{
		model = m;
	}
	public void actionPerformed(ActionEvent z){
		if(z.getActionCommand().equals("RUN"))
		{
			model.configDLLearner();
			model.DLLearnerStart();
			//setChanged();
	        //notifyObservers(model.getSolutions());
			System.out.println(model.getSolutions());
			//config.setDescriptionList(model.getSolutions());
		}
    	}

	
	public void itemStateChanged(ItemEvent i)
	{
		
	}
	

}
