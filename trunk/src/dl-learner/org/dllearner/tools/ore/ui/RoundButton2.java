package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

public class RoundButton2 extends JComponent implements MouseListener{
    String title;
    ImageIcon ico;
    ActionListener a;
    String ac;
    boolean p,pressed;
    public RoundButton2(String title){
            this(title,new ImageIcon());
    }
    public RoundButton2(ImageIcon ico){
            this("",ico);
    }
    public RoundButton2(){
            this("",new ImageIcon());
    }
    public RoundButton2(String title,ImageIcon ico){
            this.title=title;
            this.ico=ico;
            p=pressed=false;
            getInsets().set(15,15,15,15);
            addMouseListener(this);
    }
    public Dimension getPreferredSize() {
            Rectangle bounds=getBounds();
            return(new Dimension(new Double(bounds.getWidth()).intValue()+
                    getInsets().left+getInsets().left,
                    new Double(bounds.getHeight()).intValue()+
                    getInsets().top+getInsets().bottom));
    }
    public Dimension getMinimumSize() {
            return(getPreferredSize());
    }
    public Dimension getMaximumSize() {
            return(getPreferredSize());
    }
    boolean onbutton(MouseEvent m){
            double
            a=getBounds().getWidth()/2,
            b=getBounds().getHeight()/2,
            x=new Integer(m.getX()).doubleValue()-a,
            y=new Integer(m.getY()).doubleValue()-b,
            w=b/a*Math.sqrt(( (a*a) - (x*x) ));
            return( y<= w && y>=-w );
    }
    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){
            p=true;
            if(pressed)repaint();
    }
    public void mouseExited(MouseEvent e){
            if(pressed)repaint();
    }
    public void mousePressed(MouseEvent e){
            pressed=true;if(onbutton(e)){p=true; repaint(); }
    }
    public void mouseReleased(MouseEvent e){
            pressed=false;
            if(onbutton(e)){repaint();}
            if(ac==null)ac="";
            if(a!=null) a.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,ac));
    }       
    protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            Rectangle bounds=getBounds();
            Color t=g.getColor();
            int j=0;
            g.setColor(UIManager.getColor("Button.background").brighter());
            if(p){
                    j=1;p=false;
                    g.setColor(UIManager.getColor("Button.background").darker().darker());
            }
            g.fillArc(
                    j,j,
                    new Double(bounds.getWidth()).intValue()-4,
                    new Double(bounds.getHeight()).intValue()-4,
                    0,
                    360);
            g.setColor(UIManager.getColor("Button.background").darker().darker());
            g.fillArc(
                    3,3,
                    new Double(bounds.getWidth()).intValue()-4,
                    new Double(bounds.getHeight()).intValue()-4,
                    45,
                    -180);
            g.setColor(UIManager.getColor("Button.background"));
            g.fillArc(
                    2+j,2+j,
                    new Double(bounds.getWidth()).intValue()-6,
                    new Double(bounds.getHeight()).intValue()-6,
                    0,
                    360);
            g.drawImage(
                    ico.getImage(),
                    new Double((double)bounds.getWidth()/8).intValue()+j,
                    new Double((double)bounds.getHeight()/8).intValue()+j,
                    new Double((double)bounds.getWidth()*.75).intValue(),
                    new Double((double)bounds.getHeight()*.75).intValue(),
                    ico.getImageObserver()
            );
            g.setColor(UIManager.getColor("Button.background").darker().darker());
            g.drawString(title.trim(),
                    new Double((double)bounds.getWidth()/2).intValue()+j,
                    new Double((double)bounds.getHeight()/2).intValue()+j
            );
            g.setColor(t);
    }
    public void addActionListener(ActionListener e){a=e;}
    public void removeActionListener(ActionListener e){a=null;}
    public void setActionCommand(String c){ac=c;}
}
