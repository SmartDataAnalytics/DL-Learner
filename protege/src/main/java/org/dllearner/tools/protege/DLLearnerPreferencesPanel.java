package org.dllearner.tools.protege;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

public class DLLearnerPreferencesPanel extends OWLPreferencesPanel{

    private static final long serialVersionUID = -943293597478204971L;

    private java.util.List<OWLPreferencesPanel> optionPages = new ArrayList<OWLPreferencesPanel>();

    private JTabbedPane tabPane;

    public static final String DEFAULT_PAGE = "General Options";

    
    public void applyChanges() {
        for (OWLPreferencesPanel optionPage : optionPages) {
            optionPage.applyChanges();
        }
    }


    public void initialise() throws Exception {
        setLayout(new BorderLayout());

        tabPane = new JTabbedPane();

        addOptions(new BasicOptionsPanel(), "General Options");

        add(tabPane, BorderLayout.NORTH);
    }


    public void dispose() throws Exception {
        for (OWLPreferencesPanel optionPage : optionPages) {
            optionPage.dispose();
        }
    }


    private void addOptions(OWLPreferencesPanel page, String tabName) throws Exception {
        // If the page does not exist, add it, and add the component
        // to the page.

        Component c = getTab(tabName);
        if(c == null) {
            // Create a new Page
            Box box = new Box(BoxLayout.Y_AXIS);
            box.add(page);
            box.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            tabPane.add(tabName, box);
            optionPages.add(page);
        }
        else {
            Box box = (Box) c;
            box.add(Box.createVerticalStrut(7));
            box.add(page);
            optionPages.add(page);
        }

        page.initialise();
    }

    protected Component getTab(String name) {
        for(int i = 0; i < tabPane.getTabCount(); i++) {
            if(tabPane.getTitleAt(i).equals(name)) {
                return tabPane.getComponentAt(i);
            }
        }
        return null;
    }
    
    class BasicOptionsPanel extends OWLPreferencesPanel {

        /**
         * 
         */
        private static final long serialVersionUID = -6685359718444062677L;
        private JCheckBox checkConsistencyCheckBox;

        public void initialise() throws Exception {
            setLayout(new BorderLayout(12, 12));
            setBorder(BorderFactory.createTitledBorder("Basic"));
            add(createUI(), BorderLayout.NORTH);
        }

        public void dispose() throws Exception {
            // do nothing
        }


        protected JComponent createUI() {
            Box panel = new Box(BoxLayout.LINE_AXIS);

            checkConsistencyCheckBox = new JCheckBox("Check consistency");
            panel.add(checkConsistencyCheckBox);

            return panel;
        }

        public void applyChanges() {
            DLLearnerPreferences.getInstance().setCheckConsistencyWhileLearning(checkConsistencyCheckBox.isSelected());
        }
    }




}
