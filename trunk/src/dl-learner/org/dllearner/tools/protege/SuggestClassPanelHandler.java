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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.EvaluatedDescription;
/**
 * This is the MouseListener for the Suggest Panel.
 * @author Christian Koetteritzsch
 *
 */
public class SuggestClassPanelHandler implements MouseListener, ListSelectionListener{
	private DLLearnerView view;
	private DLLearnerModel model;
	private ActionHandler action;
	private EvaluatedDescription evaluatedDescription;
	
	/**
	 * This is the constructor for the SuggestClassPanelHandler.
	 * @param v DLLearnerView
	 * @param m DLLearnerModel
	 */
	public SuggestClassPanelHandler(DLLearnerView v, DLLearnerModel m, ActionHandler a) {
		this.view = v;
		this.model = m;
		this.action = a;
	}


	@Override
	/**
	 * This methode sets the graphical coverage panel enable when a
	 * suggested class expression is selected.
	 */
	public void mouseClicked(MouseEvent e) {
		if (view.getSuggestClassPanel().getSuggestList().getSelectedValue() != null) {
			SuggestListItem item = (SuggestListItem) view
					.getSuggestClassPanel().getSuggestList().getSelectedValue();
			String desc = item.getValue();
			if (model.getEvaluatedDescriptionList() != null) {
				List<? extends EvaluatedDescription> evalList = model
						.getEvaluatedDescriptionList();
				Set<String> onto = model.getOntologyURIString();
				for (EvaluatedDescription eDescription : evalList) {
					for (String ont : onto) {
						if (desc.equals(eDescription.getDescription()
								.toManchesterSyntaxString(ont, null))) {
							evaluatedDescription = eDescription;
							action.setEvaluatedClassExpression(eDescription);
							break;
						}
					}
				}
			}
			view.getMoreDetailForSuggestedConceptsPanel().renderDetailPanel(evaluatedDescription);
			view.setGraphicalPanel();
		}
	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseEntered(MouseEvent e) {
	
	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	/**
	 * This methode sets the add button enable when 
	 * a suggested class expression is selected. 
	 */
	public void mousePressed(MouseEvent e) {
		if (view.getSuggestClassPanel().getSuggestList().getSelectedValue() != null) {
			if (!view.getAddButton().isEnabled()) {
				view.getAddButton().setEnabled(true);
			}
		}
	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseReleased(MouseEvent e) {
		
	}


	@Override
	/**
	 * Nothing happens here. 
	 */
	public void valueChanged(ListSelectionEvent e) {

	}

}
