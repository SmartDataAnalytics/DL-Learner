package org.dllearner.algorithm.tbsl.sem.drs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.util.DomType;
import org.dllearner.algorithm.tbsl.sem.util.DominanceConstraint;
import org.dllearner.algorithm.tbsl.sem.util.Label;
import org.dllearner.algorithm.tbsl.sem.util.Position;
import org.dllearner.algorithm.tbsl.sem.util.SemanticRepresentation;


public class UDRS implements SemanticRepresentation {

	// this is the top element of the UDRS
	Label m_Top;
	
	// this is the bottom element of the UDRS
	Label m_Bottom;
	
	// contains the (DRT) conditions the UDRS consists of
	Set<DRS> m_Components; // TODO lists
	
	// the partial order is encoded as a set of DominanceConstraints
	Set<DominanceConstraint> m_DominanceConstraints;
	
	// constructors
	
	// trivial constructor
	
	public UDRS() {
		m_Components = new HashSet<DRS>();
		m_DominanceConstraints = new HashSet<DominanceConstraint>();	
	}
	
	// full constructor
	
	public UDRS(Label top, Label bottom, Set<DRS> components, Set<DominanceConstraint> constraints) {
		m_Top = top;
		m_Bottom = bottom;
		m_Components = components;
		m_DominanceConstraints = constraints;
	}
	
	// set and get methods
	
	public Set<DRS> getComponents() {
		return m_Components;
	}
	
	public DRS getComponent(Label label) {
		
		for (DRS component : m_Components) {
			if (component.getComponent(label) != null) {
				return component.getComponent(label);
			}
		}
		return null;
	}
	
	public DRS getUberComponent(Label label) {
		for (DRS component : m_Components ) {
			if ( component.getAllLabels().contains(label) ) {
				return component;
			}
		}
		System.out.println(" [!] didn't find UberComponent of " + label);
		return null;
	}
	
	public void setComponents(Set<DRS> components)
	{
		m_Components = components;
	}
	
	public Set<DominanceConstraint> getDominanceConstraints()
	{
		return m_DominanceConstraints;
	}
	
	
	public void setBottom(Label label)
	{
		m_Bottom = label;
	}
	
	public void setTop(Label label)
	{
		m_Top = label;
	}

	public Label getBottom()
	{
		return m_Bottom;
	}
	
	public Label getTop()
	{
		return m_Top;
	}

	// add methods
	
	public void addComponent(DRS component)
	{
		m_Components.add(component);
	}
	
	public DRS addCondition(DRS_Condition condition) {
		
		DRS component = new DRS();
		m_Components.add(component);
		return component;
	}
	
	public void addDominanceConstraint(DominanceConstraint constraint) {
		m_DominanceConstraints.add(constraint);
	}
	
	public void setDominanceConstraints(Set<DominanceConstraint> constraints) {
		m_DominanceConstraints = constraints;
	}
	
	
	
	public Set<DRS> initResolve() { // resolving scope ambiguities (returns all possible scopings)
		
		// step 1: rename constraints with scope and res in it 
		renameScopeRes();
		
		// step 2: collect all equals-dominance constraint...
		ArrayList<DominanceConstraint> equalConstraints = new ArrayList<DominanceConstraint>();
		
		for (DominanceConstraint dc : m_DominanceConstraints) {
			if (dc.getType().equals(DomType.equal)) {
				equalConstraints.add(dc);
				}
		}
			
		// ...and then merge all components that are linked by these equals-constraints
		m_DominanceConstraints.removeAll(equalConstraints);
		mergeEquals(equalConstraints);

		// if there is no top (and thus also no bottom) and everything is fine, return the one component
			if (m_Components.size() == 1 && m_DominanceConstraints.isEmpty()) {
				return m_Components;
			} 
			else if (m_Components.size() == 1 && !m_DominanceConstraints.isEmpty()) { // if it is not fine, give up
				System.out.println(" [!] Something went terribly wrong: the dominance constraints are " + m_DominanceConstraints + " and the components are " + m_Components);
				return null;
			}
			else { // ...else (i.e. if there is a top and bottom), call resolve with the top label
				return resolve();
			}
	}
	
	
	private boolean mergeEquals(ArrayList<DominanceConstraint> constraints) {
		
		if (constraints.size() < 1) {
			return true;
		}
		else {		
			DominanceConstraint dc = constraints.get(0);
		
			constraints.remove(dc);
			
			if (!dc.getSub().equals(dc.getSuper())) {
							
				DRS drs1 = getUberComponent(dc.getSub());
				DRS drs2 = getUberComponent(dc.getSuper());
				
				m_Components.remove(drs1);
				m_Components.remove(drs2);
				
				Label winnerLabel = null;
				Label loserLabel = null;
								
				if (!drs1.m_Label.equals(dc.getSub())) {
					winnerLabel = drs2.m_Label;
					loserLabel = dc.getSub();
					m_Components.add(drs1.mergeIn(drs2,loserLabel,winnerLabel)); 
				}
				else if (!drs2.m_Label.equals(dc.getSuper())) {
					winnerLabel = drs1.m_Label;
					loserLabel = dc.getSuper();
					m_Components.add(drs2.mergeIn(drs1,loserLabel,winnerLabel)); 
					// rename winnerLabel if it is embedded 
					for (DRS c : m_Components) {
						if (c.getAllLabels().contains(winnerLabel)) {
							loserLabel = winnerLabel; // this is going to break the other cases!
							winnerLabel = c.getLabel();
							break;
						}
					}
				}
				else {
					winnerLabel = drs1.m_Label;
					loserLabel = drs2.m_Label;
					m_Components.add(drs1.mergeIn(drs2,winnerLabel,winnerLabel));
				}
									
				// since in merge one of the labels projects, we need to update the constraints and the top and bottom labels 
				// (presupposed winnerLabel and loserLabel are not null (if they are, something went wrong anyways...))
				if (loserLabel != null && winnerLabel != null) {
					for (DominanceConstraint dom : constraints) { 
						if (dom.getSub().equals(loserLabel)) {
							dom.setSub(winnerLabel);
						}
						if (dom.getSuper().equals(loserLabel)) {
							dom.setSuper(winnerLabel);
						}
					} 
					for (DominanceConstraint dom : m_DominanceConstraints) { 
						if (dom.getSub().equals(loserLabel)) {
							dom.setSub(winnerLabel);
						}
						if (dom.getSuper().equals(loserLabel)) {
							dom.setSuper(winnerLabel);
						}
					}
					if (m_Top.equals(loserLabel)) { m_Top = winnerLabel; }
					if (m_Bottom.equals(loserLabel)) { m_Bottom = winnerLabel; }
				}
			}
			
			return mergeEquals(constraints);
		}
	}
	
	
	public Set<DRS> resolve() {	
		
		Set<DRS> output = new HashSet<DRS>();
		
	// check whether only top and bottom are left 
		boolean noComponentsLeft = true;
		for (DRS component : m_Components) {
			if ( component.getLabel() != null && !component.getLabel().equals(m_Top) && !component.getLabel().equals(m_Bottom) ) { 
				noComponentsLeft = false; }
		}
		
	// in that case (the base case), merge them and add the result to output
		if ( noComponentsLeft ) { 

			DRS topDRS = getComponent(m_Top);
			DRS bottomDRS = getComponent(m_Bottom);
			
			Label toMergeWith = m_Top;
			for (DominanceConstraint c : m_DominanceConstraints) {
				if ( c.getSub().equals(m_Bottom) ) { toMergeWith = c.getSuper(); }
			}
			
			output.add(topDRS.clone().mergeIn(bottomDRS.clone(),toMergeWith,toMergeWith));
		}
		else { 
	// if more components are left (recursive step), build new UDRSs by merging the daughters of top with top
			
			// first, we collect the top label together with the labels of all scopes of complex conditions occurring in the top DRS
			List<Label> motherLabels = getComponent(m_Top).getAllLabels();
	
			// then we collect all constraints that contain the motherLabels as Super (to get all daughter DRSs later), except for bottom
			List<DominanceConstraint> daughters = new ArrayList<DominanceConstraint>();
			for (DominanceConstraint constraint : m_DominanceConstraints) {
				for (Label motherLabel : motherLabels) {
					if ( constraint.getSuper().equals(motherLabel) && !constraint.getSub().equals(m_Bottom)) { 
						daughters.add(constraint); 
					} 
				}
			}
			
			
			
			for (DominanceConstraint daughter : daughters) {
				
				Label loserLabel = daughter.getSub(); 
				Label winnerLabel = daughter.getSuper();
				
				DRS drs = getComponent(loserLabel); // i.e. "drs" is the daughter DRS	
				DRS oldTop = getComponent(m_Top);	
				
				UDRS udrs = new UDRS();
				
				Set<DRS> newComponents = new HashSet<DRS>();
				for (DRS component : m_Components) {
					if (!component.equalsModuloLabel(drs) && !component.equalsModuloLabel(oldTop)) {
						newComponents.add(component.clone());
					}
				}   						
//				newComponents.remove(drs);
//				newComponents.remove(oldTop);
				
				DRS newTop = oldTop.clone().mergeIn(drs.clone(),winnerLabel,winnerLabel); 
				newComponents.add(newTop);
							
				udrs.setTop(newTop.m_Label); // should actually always be the same as m_Top
				udrs.setBottom(m_Bottom);
				udrs.setComponents(newComponents); 
				
				Label scopeWinnerLabel = new Label(winnerLabel.getLabel());
				scopeWinnerLabel.setPosition(Position.scope);
				Set<DominanceConstraint> newConstraints = new HashSet<DominanceConstraint>();		
				for (DominanceConstraint dc : m_DominanceConstraints) { 

					if (dc.getSub().equals(loserLabel)) {
						// do nothing (i.e. do not keep the constraint)
						// this also comprises the case that dc.equals(daughter)
					}
					else if (dc.getSuper().equals(loserLabel)) {
						// TODO then what, he?
					}
					else {
						DominanceConstraint newdc = dc.clone();
						if ( dc.getSub().equals(m_Top) ) { newdc.setSub(winnerLabel); }
						if ( dc.getSuper().equals(m_Top) ) { newdc.setSuper(scopeWinnerLabel); }
						if ( dc.getSuper().equals(winnerLabel) ) { newdc.setSuper(scopeWinnerLabel); }
						newConstraints.add(newdc);
					}
				}
								
				udrs.setDominanceConstraints(newConstraints);
				udrs.renameScopeRes();
				
				// recursive call
				output.addAll(udrs.resolve()); 
			}

		}
		
		return output;
	}
	
	private void renameScopeRes() {
		
		for (DominanceConstraint dc : m_DominanceConstraints) {	
			if (dc.getSub().getPosition() == Position.scope) {
				dc.setSub(getComponent(dc.getSub()).getScopeLabel());
			}
			if (dc.getSuper().getPosition() == Position.scope) {
				dc.setSuper(getComponent(dc.getSuper()).getScopeLabel());
			}
			if (dc.getSub().getPosition() == Position.res) {
				dc.setSub(getComponent(dc.getSub()).getResLabel());
			}
			if (dc.getSuper().getPosition() == Position.res) {
				dc.setSuper(getComponent(dc.getSuper()).getResLabel());
			}
		}
	}

	
	public // printing methods
	
	String toString()
	{
		String string = "";
		
		for (DRS component : m_Components)
		{
			string += component +"\n";
		}
		
		for (DominanceConstraint constraint : m_DominanceConstraints)
		{
			string += constraint +"\n";
		}
		
		return string;
		
	}


	
}
