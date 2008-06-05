package org.dllearner.tools.protege;

import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.core.ui.view.ViewComponentPlugin;
import org.protege.editor.core.ui.view.ViewComponentPluginAdapter;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.owl.ui.clshierarchy.ToldOWLClassHierarchyViewComponent;
import org.protege.editor.owl.ui.selector.AbstractSelectorPanel;
import org.protege.editor.owl.OWLEditorKit;

public class DLLearnerViewPanel extends AbstractSelectorPanel {
	

	private ToldOWLClassHierarchyViewComponent viewComponent;
	private final static long serialVersionUID = 3546352435L;
	public DLLearnerViewPanel(OWLEditorKit editor)
	{
		super(editor);
	}
	
	@Override
	protected ViewComponentPlugin getViewComponentPlugin() {

        return new ViewComponentPluginAdapter() {
            public String getLabel() {
                return "OWL Asserted Class Hierarchy";
            }


            public Workspace getWorkspace() {
                return getOWLEditorKit().getOWLWorkspace();
            }


            public ViewComponent newInstance() throws ClassNotFoundException, IllegalAccessException,
                                                      InstantiationException {
                viewComponent = new ToldOWLClassHierarchyViewComponent();
                viewComponent.setup(this);
                return viewComponent;
            }

};
	}}
