/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;

import org.semanticweb.owl.model.OWLOntologyChange;

public class UndoLabel extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2058081574518973309L;

	private List<OWLOntologyChange> owlChanges;
	
	public UndoLabel(List<OWLOntologyChange> changes, MouseListener mL){
		super("Undo");
		setForeground(Color.RED);
		this.owlChanges = changes;
		addMouseListener(mL);
	}
	
	public List<OWLOntologyChange> getChanges(){
		return owlChanges;
	}
}
