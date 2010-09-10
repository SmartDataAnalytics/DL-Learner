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

import org.protege.editor.core.ui.error.ErrorLogPanel;


/**
 * This class reads the ontology in a separate thread.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class ReadingOntologyThread extends Thread {

	private DLLearnerView view;

	/**
	 * This is the constructor of the ReadingOntologyThread.
	 * 
	 * @param v
	 *            DL-Learner view
	 * 
	 */
	public ReadingOntologyThread(DLLearnerView v) {
		this.view = v;
	}

	

	@Override
	public void run() {
		Manager.getInstance().setIsPreparing(true);
		view.showStatusBar(true);
		view.setBusy(true);
		try {
			Manager.getInstance().initKnowledgeSource();
			Manager.getInstance().initReasoner();
			if(Manager.getInstance().canLearn()){
				view.setLearningEnabled();
			} else {
				view.showNoInstancesMessage();
			}
		} catch (Exception e) {
			ErrorLogPanel.showErrorDialog(e);
		}
		view.showStatusBar(false);
		view.setBusy(false);
		Manager.getInstance().setIsPreparing(false);
		
	}

}
