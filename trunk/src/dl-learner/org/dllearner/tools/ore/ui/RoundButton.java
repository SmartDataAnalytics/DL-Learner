package org.dllearner.tools.ore.ui;

//Imports for the GUI classes.
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * RoundButton.java - 
 *   A custom JComponent that is a round button.  The round button
 *   will be empty until the mouse enters the circle.  When the
 *   mouse enters the circle it will be filled in with a specified
 *   color.  Then when the mouse is pressed the color will change
 *   to a second specified color.  Note that RoundButton "is a"
 *   MouseListener so it can handle its own MouseEvents.
 *
 * @author Grant William Braught
 * @author Dickinson College
 * @version 11/29/2000
 */
public class RoundButton 
    extends JComponent
    implements MouseListener {

    private Color mouseOverColor;
    private Color mousePressedColor;
    
    private boolean mouseOver;
    private boolean mousePressed;


    /**
     * Construct a new RoundButton with the specified
     * colors for mouse over and mouse pressed events.
     *
     * @param mouseOverColor the color the button should
     *                       be filled with when the mouse is
     *                       over the button.
     * @param mousePressedColor the color the button should be
     *                          filled with when the mouse 
     *                          is pressed on the button.
     */
    public RoundButton(Color mouseOverColor,
		       Color mousePressedColor) {
	this.mouseOverColor = mouseOverColor;
	this.mousePressedColor = mousePressedColor;
	mouseOver = false;
	mousePressed = false;

	// Make this object a MouseListener for itself.  This
	// ensures that the MouseEvents ocurring on this JComponet
	// will be routed to this object.  NOTE: "this" is a 
	// reference to a RoundButton and a RoundButton "is a" 
	// JComponent so it has an addMouseListener() method that
	// accepts a MouseListener object.  Since RoundButton 
	// implements MouseListener it "is a" MouseListener.
	this.addMouseListener(this);
    }

    /**
     * Default constructor that sets the mouse over color
     * to blue and the mouse pressed color to red.
     */
    public RoundButton() {
	this(Color.blue, Color.red);
    }

    /**
     * Paint the RoundButton on the screen each time the
     * window is redrawn.  Recall that the paint() method
     * of each JComponent in the content pane is called 
     * automatically when the window is redrawn.  This 
     * overrides paint() from JComponent so we have control
     * over what the RoundButton will look like when it is
     * painted.
     *
     * @param g the Graphics context on which to paint the
     *          button.
     */
    public void paint(Graphics g) {
	// Check mouse pressed first because if the mouse
	// is pressed it will also be in the button.
	if (mousePressed && mouseOver) {
	    g.setColor(mousePressedColor);
	    g.fillOval(0,0,100,100);
	}
	else if (mouseOver) {
	    g.setColor(mouseOverColor);
	    g.fillOval(0,0,100,100);
	}
	else {
	    g.setColor(Color.black);
	    g.drawOval(0,0,100,100);
	}
    }

    /**
     * Return the minimum size that our button would
     * like to be.  This overrides getMinimumSize from
     * JComponent which returns 0x0.
     *
     * @return the minimum size of the RoundButton.
     */
    public Dimension getMinimumSize() {
	return new Dimension(100,100);
    }

    /**
     * Return the ideal size that our button would
     * like to be.  This overrides the getPreferredSize
     * from JComponent which returns 0x0.
     *
     * @return the preferred size of the RoundButton.
     */
    public Dimension getPreferredSize() {
	return new Dimension(100,100);
    }

    // Methods from the MouseListener Interface.
    /**
     * Handler called when the mouse is clicked on
     * the RoundButton.
     *
     * @param e reference to a MouseEvent object describing 
     *          the mouse click.
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Handler called when the mouse enters the
     * RoundButton.  This is called when the mouse
     * enters the bounding rectangle of the JComponent
     * not when it enters the circle! (See RoundButton2
     * for a fix.)
     *
     * @param e reference to a MouseEvent object describing
     *          the mouse entry.
     */
    public void mouseEntered(MouseEvent e) {
	mouseOver = true;

	// Calling repaint() causes the component to be repainted.
	// repaint() makes a call to paint() but also does a few
	// other things related to the maintainence of the window
	// and layout.  Therefore, you should never call paint() 
	// directly.  If you want the component to be repainted,
	// call repaint().
	repaint();
    }

    /**
     * Handler called when the mouse exits  the
     * RoundButton.  Again, this is called when the
     * mouse exits the bounding rectangle.
     *
     * @param e reference to a MouseEvent object describing
     *          the mouse exit.
     */
    public void mouseExited(MouseEvent e) {
	mouseOver = false;
	repaint();
    }

    /**
     * Handler called when the mouse button is pressed 
     * over the RoundButton.  Again this is called if the
     * button is pressed anywhere within the bounding 
     * rectangle.
     *
     * @param e reference to a MouseEvent object describing
     *          the mouse press.
     */
    public void mousePressed(MouseEvent e) {
	mousePressed = true;
	repaint();
    }

    /**
     * Handler called when the mosue button is released
     * over the RoundButton.  Blah... Blah.. bounding 
     * rectangle.
     *
     * @param e reference to a MouseEvent object describing
     *          the mouse release.
     */
    public void mouseReleased(MouseEvent e) {
	mousePressed = false;
	repaint();
    }
}

