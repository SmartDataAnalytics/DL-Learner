/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
import org.dllearner.core.owl.ObjectCardinalityRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;

/**
 * Label that might have menu items when clicked on it.
 * @author Lorenz Buehmann
 *
 */
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
	private String descriptionLabel;

	private String baseURI;
	private Map<String, String> prefixes;

	/**
	 * constructor.
	 * 
	 * @param d
	 * @param mode
	 */
	public DescriptionLabel(Description d, String mode) {
		super();
		this.desc = d;
		this.mode = mode;
		setForeground(Color.red);
		addMouseListener(this);

	}
	
	/**
	 * initialize description label with solution.
	 */
	public void init(){
		baseURI = ore.getBaseURI();
		prefixes = ore.getPrefixes();
		
		setText(((Description) desc).toManchesterSyntaxString(ore.getBaseURI(), ore.getPrefixes()));
		menu = new JPopupMenu();
		ToolTipManager.sharedInstance().setDismissDelay(7000);
		//negative example solutions
		if(mode.equals("neg")){
			if(!(desc instanceof Negation)){
				if(desc instanceof NamedClass){																//1. description is a named class
					descriptionLabel = desc.toManchesterSyntaxString(baseURI, prefixes);
					menu.add(new DescriptionMenuItem(REMOVE_CLASS, descriptionLabel, desc));              //1.a remove class assertion
					
					JMenu dme = new JMenu("move class assertion " + descriptionLabel + " to ...");			//1.b move individual
					for(NamedClass nc : ore.getpossibleClassesMoveTo(ind)){
						DescriptionMenuItem move = new DescriptionMenuItem(MOVE_TO_CLASS, nc.toManchesterSyntaxString(baseURI, prefixes), (NamedClass) desc);
						move.setName(nc.toString());
						dme.add(move);
						Set<NamedClass> complements = ore.getComplements(nc, ind);							//check for complement error
						if(!(complements.isEmpty() || (complements.size() == 1 && complements.toArray()[0].toString().equals(desc.toString())))){
							move.setEnabled(false);
							StringBuffer strBuf = new StringBuffer();
							strBuf.append("<html>class assertion not possible because individual<br> " 
										+ "is still asserted to its complements:<br><BLOCKQUOTE>");
							
							for(NamedClass n: complements){
								strBuf.append("<br><b>" + n + "</b>");
							}
							strBuf.append("</BLOCKQUOTE></html>");
	
						
							move.setToolTipText(strBuf.toString());
						}
					}
					menu.add(dme);
				} else if(desc instanceof ObjectSomeRestriction){														//2. description is a object some restriction
					String propertyName = ((ObjectSomeRestriction) desc).getRole().toString(baseURI, prefixes);
					String propertyRange = ((ObjectSomeRestriction) desc).getChild(0).toManchesterSyntaxString(baseURI, prefixes);
					menu.add(new DescriptionMenuItem(DELETE_PROPERTY, propertyName , desc));						//2.a remove all property assertions
					if (!(desc.getChild(0) instanceof Thing)){ 
						menu.add(new DescriptionMenuItem(REMOVE_RANGE_PROPERTY, propertyRange, desc));				//2.b remove property assertions with objects in range
					}
				} else if(desc instanceof ObjectAllRestriction){														//3. description is a object all restriction
					if (!(desc.getChild(0) instanceof Thing)) {
						JMenu dme = new JMenu("add property assertion " + ((ObjectAllRestriction) desc).getRole().toKBSyntaxString(baseURI, prefixes)	//3.a add property assertion with object not in range
								+ " with object ...");
						for (Individual i : ore.getIndividualsNotInPropertyRange((ObjectAllRestriction) desc, ind)){
							dme.add(new DescriptionMenuItem(ADD_PROPERTY, i.toManchesterSyntaxString(baseURI, prefixes), desc));
						}
							menu.add(dme);
					}
				}
	
			} else if(desc instanceof Negation){
				if(desc.getChild(0) instanceof NamedClass){															//4. description is a negated named class
					DescriptionMenuItem addItem = new DescriptionMenuItem(ADD_CLASS, desc.getChild(0).toManchesterSyntaxString(baseURI, prefixes), desc.getChild(0));
					menu.add(addItem);																				//4.a add class assertion
					Set<NamedClass> complements = ore.getComplements(desc.getChild(0), ind);						//check for complement errors
					if(!complements.isEmpty()){
						addItem.setEnabled(false);
						StringBuffer strBuf = new StringBuffer();
						strBuf.append("<html>class assertion not possible because individual<br> " 
									  + "is still asserted to its complements:<br><BLOCKQUOTE>");
						
						for(NamedClass n: complements){
							strBuf.append("<br><b>" + n + "</b>");
						}
						strBuf.append("</BLOCKQUOTE></html>");

					
						addItem.setToolTipText(strBuf.toString());
					}
				}
			}
		} else if(mode.equals("pos")){//positive example solutions
			if(!(desc instanceof Negation)){
				if(desc instanceof NamedClass){
					DescriptionMenuItem add = new DescriptionMenuItem(ADD_CLASS, desc.toManchesterSyntaxString(baseURI, prefixes), desc);
					Set<NamedClass> complements = ore.getComplements(desc, ind);
					if(!(complements.isEmpty())){
						add.setEnabled(false);
						StringBuffer strBuf = new StringBuffer();
						strBuf.append("<html>class assertion not possible because individual<br> " 
									 + "is still asserted to its complements:<br><BLOCKQUOTE>");
						
						for(NamedClass n: complements){
							strBuf.append("<br><b>" + n + "</b>");
						}
						strBuf.append("</BLOCKQUOTE></html>");

					
						add.setToolTipText(strBuf.toString());
					}
					menu.add(add);
					
					Set<NamedClass> moveClasses = ore.getpossibleClassesMoveFrom(ind);
					if(moveClasses.size() > 0){
						JMenu move = new JMenu("move to " + desc.toManchesterSyntaxString(baseURI, prefixes) + " from ...");
						for (NamedClass m : moveClasses){System.out.println("hier" + m);
							DescriptionMenuItem item = new DescriptionMenuItem(MOVE_FROM_CLASS, m.toManchesterSyntaxString(baseURI, prefixes), desc);
							item.setName(m.toString());
							move.add(item);
							
							if(!(complements.isEmpty() || (complements.size() == 1 && complements.toArray()[0].toString().equals(m.toString())))){
								move.setEnabled(false);
								StringBuffer strBuf = new StringBuffer();
								strBuf.append("<html>moving class is not possible because individual<br> "
											 + "is still asserted to its complements:<br><BLOCKQUOTE>");
								
								for(NamedClass n: complements){
									strBuf.append("<br><b>" + n + "</b>");
								}
								strBuf.append("</BLOCKQUOTE></html>");
		
							
								move.setToolTipText(strBuf.toString());
							}
						}
						menu.add(move);
								
					}
					
					
				} else if(desc instanceof ObjectSomeRestriction){
					JMenu dme = new JMenu("add property assertion " + ((ObjectSomeRestriction) desc).getRole()
							+ " with object ...");
					for (Individual i : ore.getIndividualsInPropertyRange((ObjectSomeRestriction) desc, ind)){
						dme.add(new DescriptionMenuItem(ADD_PROPERTY, i.toManchesterSyntaxString(baseURI, prefixes), desc));
					}
						menu.add(dme);
					
				} else if(desc instanceof ObjectAllRestriction){
					if (!(desc.getChild(0) instanceof Thing)) {
						menu.add(new DescriptionMenuItem(REMOVE_NOT_RANGE_PROPERTY, ((ObjectAllRestriction) desc).getChild(0).toString(baseURI, prefixes), desc));
						menu.add(new DescriptionMenuItem(DELETE_PROPERTY, ((ObjectAllRestriction) desc).getRole().toString(baseURI, prefixes), desc));
					}
				}
					
				
			}
			
		} else if(desc instanceof Negation){
			if(desc.getChild(0) instanceof NamedClass){
				descriptionLabel = desc.toManchesterSyntaxString(baseURI, prefixes);
				menu.add(new DescriptionMenuItem(REMOVE_CLASS, descriptionLabel, desc));   
			}
		}
	}

	
	/**
	 * returns actual description.
	 * @return desc Description
	 */
	public Description getDescription(){
		return desc;
	}
	
	/**
	 * adds action listeners to menu items.
	 * @param aL ActionListener
	 */
	public void addActionListeners(ActionListener aL){
		for(Component c : menu.getComponents()){
			if(c instanceof DescriptionMenuItem){
				((DescriptionMenuItem) c).addActionListener(aL);
			} else if(c instanceof JMenu){
				for(int i = 0; i < ((JMenu) c).getItemCount(); i++){
					((JMenu) c).getItem(i).addActionListener(aL);
				}
			}
				
			
		}
	
		
	}

	public void setIndOre(ORE ore, Individual ind) {
		this.ore = ore;
		this.ind = ind;
	}

	public void mouseClicked(MouseEvent e) {
		menu.show(this.getParent(), getLocation().x, getLocation().y + 50);

	}

	/**
	 * Underlining label when mouse over.
	 */
	public void mouseEntered(MouseEvent e) {
		setText("<html><u>"
				+ ((Description) desc).toManchesterSyntaxString(ore
						.getBaseURI(), ore.getPrefixes()) + "</u></html>");
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		if(desc instanceof ObjectCardinalityRestriction){
			setToolTipText("ObjectCardinality repair not available at present");
		}

	}

	/**
	 * Removing underlining when mosue relased.
	 */
	public void mouseExited(MouseEvent e) {
		setText(((Description) desc).toManchesterSyntaxString(ore.getBaseURI(),
				ore.getPrefixes()));
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
