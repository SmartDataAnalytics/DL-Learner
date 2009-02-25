/**
 * Copyright (C) 2007-2009, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.tools.protege;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This class handles the commands for the example panel.
 * @author christian Koetteritzsch
 *
 */
public class PosAndNegSelectPanelHandler implements ActionListener, MouseListener {


// This is the DLLearnerModel.

//private DLLearnerModel model;
//private PosAndNegSelectPanel panel;
// This is the view of the DL-Learner tab.
//private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;

/**
 * This is the constructor for the PosAndNegSelectPanelHandler.
 * @param m
 *            DLLearnerModel
 * @param v
 *            OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView
 * @param p
 *            PosAndNegSelectPanel
 */
public PosAndNegSelectPanelHandler(DLLearnerModel m, OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView v, PosAndNegSelectPanel p) {
	//model = m;
	//panel = p;
	//view = v;
}

/**
 * When a Button is pressed this method select the right.
 * @param action ActionEvent 
 */
public void actionPerformed(ActionEvent action) {

	//System.out.println(action.getSource());
	//if (action.getSource().toString().contains("pos")) {
		//panel.setExampleToOtherList(true, panel.getNegExampleList().getSelectedValue().toString());
		//System.out.println("COUNT: " + panel.getPosExampleList().getModel().getSize());
		//if(panel.getPosExampleList().getModel().getSize()>0) {
		//	view.getRunButton().setEnabled(true);
		//}
	//}

	//if (action.getSource().toString().contains("neg")) {
		//panel.setExampleToOtherList(false, panel.getPosExampleList().getSelectedValue().toString());
	//}
	
	//if (action.getActionCommand().equals("?")) {
		//if (action.getSource().toString().contains("PosHelpButton")) {
			//String help = "An individual that should be an instance of the learned class description.\n"
			//	+"Per default all that belongs to the class.";
			//view.getPosAndNegSelectPanel().renderHelpMessage(help);
		//}

		//if (action.getSource().toString().contains("NegHelpButton")) {
			//String help = "An individual that should not be instance of the learned class description.\n" 
			//	+" By default, these are all individuals, which are not instances of the current class.";
			//view.getPosAndNegSelectPanel().renderHelpMessage(help);
		//}

	//}
}




/**
 * Nothing happens here.
 * @param m MouseEvent
 */
public void mouseReleased(MouseEvent m) {

}

	/**
 * Nothing happens here.
 * @param m MouseEvent
 */
public void mouseEntered(MouseEvent m) {

}

/**
 * Choses the right EvaluatedDescription object after a concept is chosen in the list.
 * @param m MouseEvent
 */
public void mouseClicked(MouseEvent m) {
	//if (!panel.getPosExampleList().isSelectionEmpty() && m.toString().contains("pos")) {
	//	panel.getAddToNegPanelButton().setEnabled(true);
	//	panel.getAddToPosPanelButton().setEnabled(false);
	//	panel.getNegExampleList().clearSelection();
	//}
	//if (!panel.getNegExampleList().isSelectionEmpty() && m.toString().contains("neg")) {
	//	panel.getAddToPosPanelButton().setEnabled(true);
	//	panel.getAddToNegPanelButton().setEnabled(false);
	//	panel.getPosExampleList().clearSelection();
	//} 
}

/**
 * Nothing happens here.
 * @param m MouseEvent
 */
public void mouseExited(MouseEvent m) {

}

/**
 * Sets the ADD button enable after a concept is chosen.
 * @param m MouseEvent
 */
public void mousePressed(MouseEvent m) {
}


}
