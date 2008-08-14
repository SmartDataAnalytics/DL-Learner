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
package org.dllearner.tools.protege;

import org.protege.editor.owl.ui.view.AbstractOWLClassViewComponent;
import org.semanticweb.owl.model.OWLClass;
import org.protege.editor.owl.ui.framelist.OWLFrameList2;
import javax.swing.*;
import java.awt.*;

/**
 * This is the class that must be implemented to get the plugin integrated
 * in protege.
 * @author Christian Koetteritzsch
 *
 */
public class ProtegePlugin  extends AbstractOWLClassViewComponent {
private static final long serialVersionUID = 728362819273927L;
/**
 * List of the lists for equibvalent classes and so on.
 */
private OWLFrameList2<OWLClass> list;

	@Override
	/**
	 * This method initializes the view of the plugin.
	 */
	public void initialiseClassView() throws Exception {
		list = new OWLFrameList2<OWLClass>(getOWLEditorKit(), new ButtonList(getOWLEditorKit()));
		setLayout(new BorderLayout());
		JScrollPane hallo = new JScrollPane(list);
		add(hallo);
		
		
	}
	
	@Override
	/**
	 * updates the view if somthing changes
	 */
	protected OWLClass updateView(OWLClass selectedClass) {
        list.setRootObject(selectedClass);
        return selectedClass;
    }

	@Override
	/**
	 * destroys every listener when protege is closed
	 */
    public void disposeView() {
        list.dispose();
    }
}  