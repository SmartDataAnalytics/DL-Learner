package org.dllearner.tools.protege;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class PosAndNegSelectPanelHandler implements ActionListener, MouseListener {


// This is the DLLearnerModel.

private DLLearnerModel model;
private PosAndNegSelectPanel panel;
// This is the view of the DL-Learner tab.
private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;

/**
 * This is the constructor for the action handler.
 * 
 * @param a
 *            ActionHandler
 * @param m
 *            DLLearnerModel
 * @param view
 *            DLlearner tab
 * @param i
 *            id if it is a subclass or an equivalent class
 * @param editor OWLEditorKit
 */
public PosAndNegSelectPanelHandler(DLLearnerModel m, OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView v, PosAndNegSelectPanel p) {
	model = m;
	panel = p;
	view = v;
}

/**
 * When a Button is pressed this method select the right.
 * @param z ActionEvent 
 */
public void actionPerformed(ActionEvent action) {

		
	if (action.getActionCommand().equals("pos")) {
		panel.setExampleToOtherList(true, panel.getNegExampleList().getSelectedValue().toString());
	}

	if (action.getActionCommand().equals("neg")) {
		panel.setExampleToOtherList(false, panel.getPosExampleList().getSelectedValue().toString());
	}
	
	if (action.getActionCommand().equals("?")) {
		if (action.getSource().toString().contains("PosHelpButton")) {
			String help = "An individual that should be an instance of the learned class description.\n"
				+"Per Default all that belongs to the class.";
			view.getPosAndNegSelectPanel().renderHelpMessage(help);
		}

		if (action.getSource().toString().contains("NegHelpButton")) {
			String help = "A Instance tht doesn't follow from the classdescription.";
			view.getPosAndNegSelectPanel().renderHelpMessage(help);
		}

	}
}

/**
 * select/deselect the Check boxes.
 * @param i ItemEvent
 */
public void itemStateChanged(ItemEvent i) {
	if (i.getItem().toString().contains("Positive")) {
		for (int j = 0; j < model.getPosVector().size(); j++) {
			if (i.getItem().toString().contains(
					model.getPosVector().get(j).getText().toString())) {
				if (!model.getPosVector().get(j).isSelected()) {
					model.getPosVector().get(j).setSelected(true);
					break;
				}
				if (model.getPosVector().get(j).isSelected()) {
					model.getPosVector().get(j).setSelected(false);
					break;
				}
			}
		}
	}
	if (i.getItem().toString().contains("Negative")) {
		for (int j = 0; j < model.getNegVector().size(); j++) {
			if (i.getItem().toString().contains(
					model.getNegVector().get(j).getText().toString())) {
				if (!model.getNegVector().get(j).isSelected()) {
					model.getNegVector().get(j).setSelected(true);
					break;
				}
				if (model.getNegVector().get(j).isSelected()) {
					model.getNegVector().get(j).setSelected(false);
					break;
				}
			}
		}
	}
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
	if (panel.getPosExampleList().getSelectedValue() != null) {
		panel.getAddToNegPanelButton().setEnabled(true);
	} else {
		panel.getAddToNegPanelButton().setEnabled(false);
	}
	if (panel.getNegExampleList().getSelectedValue()!= null) {
		panel.getAddToPosPanelButton().setEnabled(true);
	} else {
		panel.getAddToPosPanelButton().setEnabled(false);
	}
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
