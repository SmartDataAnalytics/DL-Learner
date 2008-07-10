package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;

public class DescriptionLabel extends JLabel implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Description desc;
	private Individual ind;
	private ORE ore;
	private JPopupMenu menu;
	
	public DescriptionLabel(Description d){
		super(d.toString());
		this.desc = d;
		setForeground(Color.red);
		addMouseListener(this);
	
		
	}
	
	public void init(){
		menu = new JPopupMenu();
		if(!(desc instanceof Negation)){
			if(desc instanceof NamedClass){
				menu.add(new DescriptionMenuItem("remove class assertion " + desc.toString(), desc) );
				JMenu dme = new JMenu("move class assertion " + desc.toString() + " to ...");
				
				for(NamedClass nc : ore.getpossibleMoveClasses(ind)){
					MoveMenuItem move = new MoveMenuItem((NamedClass)desc, nc);
					dme.add(move);
					Set<NamedClass> complements = ore.getComplements(nc, ind);
					System.out.println("Größe" + complements.size());
					if(!(complements.size() <=1)){
						move.setEnabled(false);
						StringBuffer strBuf = new StringBuffer();
						strBuf.append("<html>class assertion not possible because individual<br> " +
									"is still asserted to its complements:<br><BLOCKQUOTE>");
						
						for(NamedClass n: complements)
							strBuf.append("<br><b>" + n + "</b>");
						strBuf.append("</BLOCKQUOTE></html>");

					
						move.setToolTipText(strBuf.toString());
					}
				}
				menu.add(dme);
			}
			else if(desc instanceof ObjectSomeRestriction){
				menu.add(new DescriptionMenuItem("remove complete property " + ((ObjectSomeRestriction)desc).getRole(), desc));
				if (!(desc.getChild(0) instanceof Thing)) 
					menu.add(new DescriptionMenuItem("remove all property assertions to " + ((ObjectSomeRestriction)desc).getChild(0), desc));
				
			}
			else if(desc instanceof ObjectAllRestriction){
				if (!(desc.getChild(0) instanceof Thing)) {
					JMenu dme = new JMenu("add property assertion " + ((ObjectAllRestriction) desc).getRole()
							+ " with object ...");
					for (Individual i : ore.getIndividualsNotOfPropertyRange((ObjectAllRestriction) desc, ind))
						dme.add(new DescriptionMenuItem(i.getName(), desc.getChild(0)));
					menu.add(dme);
				}
			}
	
		}
		else if(desc instanceof Negation){
			if(desc.getChild(0) instanceof NamedClass){
				DescriptionMenuItem item = new DescriptionMenuItem("add class assertion to " + desc.getChild(0).toString(), desc.getChild(0));
				menu.add(item);
				if(!ore.getComplements(desc, ind).isEmpty()){
					item.setEnabled(false);
					item.setToolTipText("<html>class assertion not possible because individual<br> is still asserted to its complement</html>");
				}
			}
			else if(desc.getChild(0) instanceof ObjectSomeRestriction){
				JMenu dme = new JMenu("add property " + desc.toString() + " with object ...");
				for(Individual i : ore.getIndividualsOfPropertyRange((ObjectSomeRestriction)desc.getChild(0), ind))
					dme.add(new DescriptionMenuItem(i.getName(), desc.getChild(0)));
				menu.add(dme);
			}
		}
	}
	
	public Description getDescription(){
		return desc;
	}
	
	public void addActionListeners(ActionListener aL){
		for(Component c : menu.getComponents()){
			if(c instanceof DescriptionMenuItem)
				((DescriptionMenuItem)c).addActionListener(aL);
			else if(c instanceof JMenu)
				for( int i = 0; i < ((JMenu)c).getItemCount(); i++)
					((JMenu)c).getItem(i).addActionListener(aL);
				
			
		}
	
		
	}
	
	public void setIndOre(ORE ore, Individual ind){
		this.ore = ore;
		this.ind = ind;
	}

	
	public void mouseClicked(MouseEvent e) {
		menu.show(this.getParent(),getLocation().x ,getLocation().y+50);
		
	}

	
	public void mouseEntered(MouseEvent e) {
		setText("<html><u>" + desc.toString() + "</u></html>");
		
	}

	
	public void mouseExited(MouseEvent e) {
		setText(desc.toString());
		
	}

	
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
