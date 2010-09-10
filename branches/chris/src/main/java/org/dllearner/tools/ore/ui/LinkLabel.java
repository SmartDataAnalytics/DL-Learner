package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.protege.editor.core.PropertyUtil;
import org.protege.editor.core.ProtegeProperties;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 26-May-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class LinkLabel extends JLabel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7632670052248388791L;

	private Color linkColor;

    private Color hoverColor;

    private Cursor oldCursor;

    private ActionListener linkListener;


    public LinkLabel(String text) {
        super(text);
       
        linkColor = PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.PROPERTY_COLOR_KEY),
                                          Color.GRAY);
        hoverColor = PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.CLASS_COLOR_KEY),
                                           Color.GRAY);

        setForeground(linkColor);

        addMouseListener(new MouseAdapter() {
            @Override
			public void mouseEntered(MouseEvent e) {
                setHoverMode(true);
            }


            @Override
			public void mouseExited(MouseEvent e) {
                setHoverMode(false);
            }


            @Override
			public void mouseReleased(MouseEvent e) {
                activateLink();
            }
        });
        setFont(getFont().deriveFont(Font.BOLD, 12.0f));
    }

    public void addLinkListener(ActionListener linkListener){
    	this.linkListener = linkListener;
    	
    }

    public void setLinkColor(Color color) {
        linkColor = color;
    }


    public void setHoverColor(Color color) {
        hoverColor = color;
    }


    private void setHoverMode(boolean b) {
        if (b && isEnabled()) {
            setForeground(hoverColor);
            oldCursor = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else {
            setForeground(linkColor);
            if (oldCursor != null) {
                setCursor(oldCursor);
            }
        }
    }


    private void activateLink() {
        if (isEnabled()) {
            linkListener.actionPerformed(new ActionEvent(this, 0, getText()));
        }
    }


    
}
