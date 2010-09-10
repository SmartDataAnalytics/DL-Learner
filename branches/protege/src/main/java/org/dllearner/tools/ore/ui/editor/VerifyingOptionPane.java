package org.dllearner.tools.ore.ui.editor;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

public class VerifyingOptionPane extends JOptionPane {

    /**
     * 
     */
    private static final long serialVersionUID = -6308201481924625979L;

    private static final Logger logger = Logger.getLogger(VerifyingOptionPane.class);

    private JButton okButton;


    public VerifyingOptionPane(JComponent c) {
        super(c, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    }

    public VerifyingOptionPane(JComponent c, int messageType, int optionType) {
        super(c, messageType, optionType);
    }

    public void setOKEnabled(boolean enabled){
        if (okButton == null){
            okButton = getButtonComponent(this, JButton.class, (String)UIManager.get("OptionPane.okButtonText"));
        }
        if (okButton != null){
            okButton.setEnabled(enabled);
        }
        else{
            logger.warn("Cannot find OK button for this system. Please report this with details of your OS and language.");
        }
    }

    @SuppressWarnings("unchecked")
	private <T extends JComponent> T getButtonComponent(JComponent parent, Class<T> type, String name) {
        if (type.isAssignableFrom(parent.getClass())){
            if (parent instanceof JButton){
                if (name.equals(((JButton)parent).getText())){
                    return (T)parent;
                }
            }
        }
        for (Component c : parent.getComponents()){
            if (c instanceof JComponent){
                T target = getButtonComponent((JComponent)c, type, name);
                if (target != null){
                    return target;
                }
            }
        }
        return null;
    }
}
