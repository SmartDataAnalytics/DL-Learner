package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

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
	
		
	private static final int MOVE_TO_CLASS = 0;
	private static final int MOVE_FROM_CLASS = 1;
	private static final int ADD_CLASS = 2;
	private static final int REMOVE_CLASS = 3;
	private static final int ADD_PROPERTY = 4;
	private static final int REMOVE_RANGE_PROPERTY = 5;
	private static final int DELETE_PROPERTY = 6;
	private static final int REMOVE_NOT_RANGE_PROPERTY = 7;
	
	
	
	
	private final Description desc;
	private Individual ind;
	private ORE ore;
	private JPopupMenu menu;
	private String mode;
	
	private String baseURI;
	private Map<String, String> prefixes;
	
	public DescriptionLabel(Description d, String mode){
		super();
		this.desc = d;
		this.mode = mode;
		setForeground(Color.red);
		addMouseListener(this);
	
		
	}
	
	public void init(){
		baseURI = ore.getBaseURI();
		prefixes = ore.getPrefixes();
		
		setText(((Description)desc).toManchesterSyntaxString(ore.getBaseURI(),ore.getPrefixes()));
		menu = new JPopupMenu();
		ToolTipManager.sharedInstance().setDismissDelay(7000);
		if(mode.equals("neg")){
			if(!(desc instanceof Negation)){
				if(desc instanceof NamedClass){
					menu.add(new DescriptionMenuItem(REMOVE_CLASS, desc.toManchesterSyntaxString(baseURI, prefixes), desc) );
					
					JMenu dme = new JMenu("move class assertion " + desc.toManchesterSyntaxString(baseURI, prefixes) + " to ...");
					for(NamedClass nc : ore.getpossibleClassesMoveTo(ind)){
						DescriptionMenuItem move = new DescriptionMenuItem(MOVE_TO_CLASS, nc.toManchesterSyntaxString(baseURI, prefixes), (NamedClass)desc);
						dme.add(move);
						Set<NamedClass> complements = ore.getComplements(nc, ind);
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
					String propertyName = ((ObjectSomeRestriction)desc).getRole().toString(baseURI, prefixes);
					String propertyRange = ((ObjectSomeRestriction)desc).getChild(0).toManchesterSyntaxString(baseURI, prefixes);
					menu.add(new DescriptionMenuItem(DELETE_PROPERTY, propertyName , desc));
					if (!(desc.getChild(0) instanceof Thing)) 
						menu.add(new DescriptionMenuItem(REMOVE_RANGE_PROPERTY,propertyRange , desc));
					
				}
				else if(desc instanceof ObjectAllRestriction){
					if (!(desc.getChild(0) instanceof Thing)) {
						JMenu dme = new JMenu("add property assertion " + ((ObjectAllRestriction) desc).getRole()
								+ " with object ...");
						for (Individual i : ore.getIndividualsNotInPropertyRange((ObjectAllRestriction) desc, ind))
							dme.add(new DescriptionMenuItem(ADD_PROPERTY,i.toManchesterSyntaxString(baseURI, prefixes), desc));
						menu.add(dme);
					}
				}
	
			}
			else if(desc instanceof Negation){
				if(desc.getChild(0) instanceof NamedClass){
					DescriptionMenuItem item = new DescriptionMenuItem(ADD_CLASS, desc.getChild(0).toManchesterSyntaxString(baseURI, prefixes), desc.getChild(0));
					menu.add(item);
					if(!ore.getComplements(desc, ind).isEmpty()){
						item.setEnabled(false);
						item.setToolTipText("<html>class assertion not possible because individual<br> is still asserted to its complement</html>");
					}
				}
			}
		}
		else if(mode.equals("pos")){
			if(!(desc instanceof Negation)){
				if(desc instanceof NamedClass){
					DescriptionMenuItem add = new DescriptionMenuItem(ADD_CLASS, desc.toManchesterSyntaxString(baseURI, prefixes), desc);
					Set<NamedClass> complements = ore.getComplements(desc, ind);
					if((complements.size() >0)){
						add.setEnabled(false);
						StringBuffer strBuf = new StringBuffer();
						strBuf.append("<html>class assertion not possible because individual<br> " +
									"is still asserted to its complements:<br><BLOCKQUOTE>");
						
						for(NamedClass n: complements)
							strBuf.append("<br><b>" + n + "</b>");
						strBuf.append("</BLOCKQUOTE></html>");

					
						add.setToolTipText(strBuf.toString());
					}
					menu.add(add);
					
					Set<NamedClass> moveClasses = ore.getpossibleClassesMoveFrom(ind);
					if(moveClasses.size() > 0){
						JMenu move = new JMenu("move to " + desc + " from ...");
						for (NamedClass m : moveClasses)
							move.add(new DescriptionMenuItem(MOVE_FROM_CLASS,m.toManchesterSyntaxString(baseURI, prefixes), desc));
						menu.add(move);
								
					}
					
					
				}
				else if(desc instanceof ObjectSomeRestriction){
					JMenu dme = new JMenu("add property assertion " + ((ObjectSomeRestriction) desc).getRole()
							+ " with object ...");
					for (Individual i : ore.getIndividualsOfPropertyRange((ObjectSomeRestriction) desc, ind))
						dme.add(new DescriptionMenuItem(ADD_PROPERTY,i.toManchesterSyntaxString(baseURI, prefixes), desc));
					menu.add(dme);
					
				}
				else if(desc instanceof ObjectAllRestriction){
					if (!(desc.getChild(0) instanceof Thing)) {
						menu.add(new DescriptionMenuItem(REMOVE_NOT_RANGE_PROPERTY,((ObjectAllRestriction) desc).getChild(0).toString(baseURI, prefixes), desc));
						menu.add(new DescriptionMenuItem(DELETE_PROPERTY,((ObjectAllRestriction) desc).getRole().toString(baseURI, prefixes), desc));
					}
				}
				
			}
			
		}
		
//		else if(desc instanceof Negation){
//			if(desc.getChild(0) instanceof NamedClass){
//				DescriptionMenuItem item = new DescriptionMenuItem("add class assertion to " + desc.getChild(0).toString(), desc.getChild(0));
//				menu.add(item);
//				if(!ore.getComplements(desc, ind).isEmpty()){
//					item.setEnabled(false);
//					item.setToolTipText("<html>class assertion not possible because individual<br> is still asserted to its complement</html>");
//				}
//			}
//			else if(desc.getChild(0) instanceof ObjectSomeRestriction){
//				JMenu dme = new JMenu("add property " + desc.toString() + " with object ...");
//				for(Individual i : ore.getIndividualsOfPropertyRange((ObjectSomeRestriction)desc.getChild(0), ind))
//					dme.add(new DescriptionMenuItem(i.getName(), desc.getChild(0)));
//				menu.add(dme);
//			}
//		}
	
	}
//		menu = new JPopupMenu();
//		ToolTipManager.sharedInstance().setDismissDelay(7000);
//		if(!(desc instanceof Negation)){
//			if(desc instanceof NamedClass){
//				menu.add(new DescriptionMenuItem("remove class assertion " + desc.toString(), desc) );
//				JMenu dme = new JMenu("move class assertion " + desc.toString() + " to ...");
//				
//				for(NamedClass nc : ore.getpossibleMoveClasses(ind)){
//					MoveMenuItem move = new MoveMenuItem((NamedClass)desc, nc);
//					dme.add(move);
//					Set<NamedClass> complements = ore.getComplements(nc, ind);
//					System.out.println("Größe" + complements.size());
//					if(!(complements.size() <=1)){
//						move.setEnabled(false);
//						StringBuffer strBuf = new StringBuffer();
//						strBuf.append("<html>class assertion not possible because individual<br> " +
//									"is still asserted to its complements:<br><BLOCKQUOTE>");
//						
//						for(NamedClass n: complements)
//							strBuf.append("<br><b>" + n + "</b>");
//						strBuf.append("</BLOCKQUOTE></html>");
//
//					
//						move.setToolTipText(strBuf.toString());
//					}
//				}
//				menu.add(dme);
//			}
//			else if(desc instanceof ObjectSomeRestriction){
//				menu.add(new DescriptionMenuItem("remove complete property " + ((ObjectSomeRestriction)desc).getRole(), desc));
//				if (!(desc.getChild(0) instanceof Thing)) 
//					menu.add(new DescriptionMenuItem("remove all property assertions to " + ((ObjectSomeRestriction)desc).getChild(0), desc));
//				
//			}
//			else if(desc instanceof ObjectAllRestriction){
//				if (!(desc.getChild(0) instanceof Thing)) {
//					JMenu dme = new JMenu("add property assertion " + ((ObjectAllRestriction) desc).getRole()
//							+ " with object ...");
//					for (Individual i : ore.getIndividualsNotOfPropertyRange((ObjectAllRestriction) desc, ind))
//						dme.add(new DescriptionMenuItem(i.getName(), desc.getChild(0)));
//					menu.add(dme);
//				}
//			}
//	
//		}
//		else if(desc instanceof Negation){
//			if(desc.getChild(0) instanceof NamedClass){
//				DescriptionMenuItem item = new DescriptionMenuItem("add class assertion to " + desc.getChild(0).toString(), desc.getChild(0));
//				menu.add(item);
//				if(!ore.getComplements(desc, ind).isEmpty()){
//					item.setEnabled(false);
//					item.setToolTipText("<html>class assertion not possible because individual<br> is still asserted to its complement</html>");
//				}
//			}
//			else if(desc.getChild(0) instanceof ObjectSomeRestriction){
//				JMenu dme = new JMenu("add property " + desc.toString() + " with object ...");
//				for(Individual i : ore.getIndividualsOfPropertyRange((ObjectSomeRestriction)desc.getChild(0), ind))
//					dme.add(new DescriptionMenuItem(i.getName(), desc.getChild(0)));
//				menu.add(dme);
//			}
//		}
//	}
	
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
		setText("<html><u>" + ((Description)desc).toManchesterSyntaxString(ore.getBaseURI(),ore.getPrefixes()) + "</u></html>");
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		
	}

	
	public void mouseExited(MouseEvent e) {
		setText(((Description)desc).toManchesterSyntaxString(ore.getBaseURI(),ore.getPrefixes()));
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
