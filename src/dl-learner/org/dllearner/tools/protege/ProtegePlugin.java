package org.dllearner.tools.protege;

import org.protege.editor.owl.ui.view.AbstractOWLClassViewComponent;
import org.semanticweb.owl.model.OWLClass;
import org.protege.editor.owl.ui.framelist.OWLFrameList2;
import javax.swing.*;
import java.awt.*;


public class ProtegePlugin  extends AbstractOWLClassViewComponent
{
private static final long serialVersionUID = 728362819273927L;
private OWLFrameList2<OWLClass> list;
 
	public void initialiseClassView() throws Exception {
		list = new OWLFrameList2<OWLClass>(getOWLEditorKit(), new ButtonList(getOWLEditorKit()));
		setLayout(new BorderLayout());
		JScrollPane hallo = new JScrollPane(list);
		add(hallo);
		
		
	}
	
	
	protected OWLClass updateView(OWLClass selectedClass) {
        list.setRootObject(selectedClass);
        return selectedClass;
    }


    public void disposeView() {
        list.dispose();
    }
}  