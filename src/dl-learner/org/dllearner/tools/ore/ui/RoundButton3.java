package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

class RoundButton3 extends JButton {
    public RoundButton3() {
        this(null, null);
    }
    public RoundButton3(Icon icon) {
        this(null, icon);
    }
    public RoundButton3(String text) {
        this(text, null);
    }
    public RoundButton3(Action a) {
        this();
        setAction(a);
    }
    public RoundButton3(String text, Icon icon) {
        setModel(new DefaultButtonModel());
        init(text, icon);
        if(icon==null) {
            return;
        }
        int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
        int sw = 1;
        setBorder(BorderFactory.createEmptyBorder(sw,sw,sw,sw));
        Dimension dim = new Dimension(iw+sw+sw, iw+sw+sw);
        setPreferredSize(dim);
        setMaximumSize(dim);
        setMinimumSize(dim);
        setBackground(Color.BLACK);
        setContentAreaFilled(false);
        setFocusPainted(false);
        //setVerticalAlignment(SwingConstants.TOP);
        setAlignmentY(Component.TOP_ALIGNMENT);
        initShape();
    }
    protected Shape shape, base;
    protected void initShape() {
        if(!getBounds().equals(base)) {
            Dimension s = getPreferredSize();
            base = getBounds();
            shape = new Ellipse2D.Float(0, 0, s.width-1, s.height-1);
        }
    }
    @Override
    protected void paintBorder(Graphics g) {
        initShape();
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        //g2.setStroke(new BasicStroke(1.0f));
        g2.draw(shape);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    @Override
    public boolean contains(int x, int y) {
        initShape();
        return shape.contains(x, y);
    }
}
