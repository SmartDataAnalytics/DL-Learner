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
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class StatsPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8418286820511803278L;
	
	private ORE ore;
	private OntologyModifier modifier;
	private Individual ind;
	private Set<NamedClass> oldClasses;
	private Set<ObjectPropertyAssertion> oldProperties;
	private Map<String, Set<String>> oldPropMap;
	
		
	private JXTaskPane indPane;
	private JXTaskPane classPane;
	private JXTaskPane propertyPane;
	
	private JXTaskPaneContainer container;
	
	private ImageIcon newIcon;
	
	private String baseURI;
	private Map<String, String> prefixes;
	
	public StatsPanel(ORE ore, Individual ind){
		super();
		this.ore = ore;
		this.modifier = ore.getModifier();
		this.ind = ind;
		
	}
	
	public void init(){
		prefixes = ore.getPrefixes();
		baseURI = ore.getBaseURI();
		
		newIcon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/new.gif");
		
		setLayout(new GridLayout());
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));		
		
		
		container = new JXTaskPaneContainer();
		
		
				
		indPane = new JXTaskPane();
		indPane.setTitle("Individual");
        indPane.add(new JLabel(ind.toManchesterSyntaxString(baseURI, prefixes)));
           
        classPane = new JXTaskPane();
        classPane.setTitle("Classes");
        oldClasses = ore.getOwlReasoner().getConcepts(ind);
       	for(NamedClass nc : oldClasses)
			classPane.add(new JLabel(nc.toManchesterSyntaxString(baseURI, prefixes)));
        
       	propertyPane = new JXTaskPane();
        propertyPane.setTitle("Properties");		
		
        oldProperties = modifier.getObjectProperties(ind);
        oldPropMap = new HashMap<String, Set<String>>();
        for(ObjectPropertyAssertion ob : oldProperties){
        	String role = ob.getRole().toString(baseURI, prefixes);
        	String ind = ob.getIndividual2().toManchesterSyntaxString(baseURI, prefixes);
        	if(oldPropMap.containsKey(role)){
        		Set<String> oldSet = oldPropMap.get(role);
        		oldSet.add(ind);
        		oldPropMap.put(role, oldSet);
        	}
        	else{
        		Set<String> newSet = new HashSet<String>();
        		newSet.add(ind);
        		oldPropMap.put(role, newSet);
        	}
        		
        }
        for(String key : oldPropMap.keySet()){
        	
        	JXTaskPane actionPane = new JXTaskPane();
            actionPane.setTitle(key);
            actionPane.setSpecial(true);
            Set<String> value = (Set<String>)oldPropMap.get(key);
			for(String i : value)
				actionPane.add(new JLabel(i));
			propertyPane.add(actionPane);
        }
      
        	
        container.add(indPane);
        container.add(classPane);
        container.add(propertyPane);
        
        add(container);
               
		
	}
	

	public void updatePanel(){
		
		//update classesPanel
		classPane.removeAll();
				
		Set<String> newClassesString = new HashSet<String>();
		for (NamedClass nc : ore.getOwlReasoner().getConcepts(ind))
			newClassesString.add(nc.toManchesterSyntaxString(baseURI, prefixes));
		Set<String> oldClassesString = new HashSet<String>();
		for (NamedClass nc : oldClasses)
			oldClassesString.add(nc.toManchesterSyntaxString(baseURI, prefixes));
		
		for (String nc : oldClassesString)
			if (!newClassesString.contains(nc)){
				classPane.add(new JLabel("<html><strike>" + nc + "</strike></html>"));
			}
			else{
				classPane.add(new JLabel(nc));
			}
			
		for (String nc : newClassesString)
			if (!oldClassesString.contains(nc)) {

				
				JLabel lab = new JLabel(nc);
				lab.setIcon(newIcon);
				lab.setHorizontalTextPosition(JLabel.LEFT);
				classPane.add(lab);
			}
		
		//update propertyPanel
		propertyPane.removeAll();
		
		Map<String, Set<String>> newPropMap = new HashMap<String, Set<String>>();
        for(ObjectPropertyAssertion ob : modifier.getObjectProperties(ind)){
        	String role = ob.getRole().toString(baseURI, prefixes);
        	String ind = ob.getIndividual2().toManchesterSyntaxString(baseURI, prefixes);
        	if(newPropMap.containsKey(role)){
        		Set<String> oldSet = newPropMap.get(role);
        		oldSet.add(ind);
        		newPropMap.put(role, oldSet);
        	}
        	else{
        		Set<String> newSet = new HashSet<String>();
        		newSet.add(ind);
        		newPropMap.put(role, newSet);
        	}
        		
        }
		
		for(String key : oldPropMap.keySet()){
			if (!newPropMap.keySet().contains(key)){
				JXTaskPane actionPane = new JXTaskPane();
				actionPane.setTitle("<html><strike>" + key + "</strike></html>");
				actionPane.setSpecial(true);
				Set<String> value = (Set<String>)oldPropMap.get(key);
				for(String i : value)
					actionPane.add(new JLabel("<html><strike>" + i + "</strike></html>"));
				actionPane.setExpanded(false);
				propertyPane.add(actionPane);
			}
			else if(newPropMap.keySet().contains(key)){
				JXTaskPane actionPane = new JXTaskPane();
				actionPane.setTitle(key);
				actionPane.setSpecial(true);
				for(String value : oldPropMap.get(key)){
					if(!newPropMap.get(key).contains(value)){
						actionPane.add(new JLabel("<html><strike>" + value + "</strike></html>"));
					}
					else{
						actionPane.add(new JLabel(value));
					}
				}
				for(String value : newPropMap.get(key)){
					if(!oldPropMap.get(key).contains(value)){
						JLabel newLab = new JLabel(value);
						newLab.setIcon(newIcon);
						newLab.setHorizontalTextPosition(JLabel.LEFT);
						actionPane.add(newLab);
					}
				}
				propertyPane.add(actionPane);
			}
		}
		for(String key : newPropMap.keySet()){
			if(!oldPropMap.keySet().contains(key)){
				JXTaskPane actionPane = new JXTaskPane();
				actionPane.setTitle(key);
				actionPane.setSpecial(true);
//				actionPane.setIcon(newIcon);
				for(String value : newPropMap.get(key)){
					JLabel newLab = new JLabel(value);
					newLab.setIcon(newIcon);
					newLab.setHorizontalTextPosition(JLabel.LEFT);
					actionPane.add(newLab);
				}
				propertyPane.add(actionPane);
			}
			
		}
			
       
	
	SwingUtilities.updateComponentTreeUI(this);
				
	}
}
