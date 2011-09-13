package org.dllearner.algorithm.tbsl.sem.dudes.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.drs.DRS;
import org.dllearner.algorithm.tbsl.sem.drs.Simple_DRS_Condition;
import org.dllearner.algorithm.tbsl.sem.util.DomType;
import org.dllearner.algorithm.tbsl.sem.util.DominanceConstraint;
import org.dllearner.algorithm.tbsl.sem.util.Label;
import org.dllearner.algorithm.tbsl.sem.util.SemanticRepresentation;
import org.dllearner.algorithm.tbsl.sem.util.Type;
import org.dllearner.algorithm.tbsl.sparql.BasicSlot;
import org.dllearner.algorithm.tbsl.sparql.Slot;

public class Dude implements SemanticRepresentation{

	String mainReferent;
	Label mainLabel;
	Type mainType;
	List<DRS> components;
	List<Argument> arguments;
	List<DominanceConstraint> dominanceConstraints;
	List<Slot> slots;
	
	
	public Dude()
	{
		arguments = new ArrayList<Argument>();
		components = new ArrayList<DRS>();
		dominanceConstraints = new ArrayList<DominanceConstraint>();
		slots = new ArrayList<Slot>();
	}
	
// set methods
	public void setReferent(String s) {
		mainReferent = s;		
	}
	public void setLabel(Label l) {
		mainLabel = l;		
	}
	public void setType(Type t) {
		mainType = t;		
	}
	public void setComponents(List<DRS> drss) {
		components = drss;		
	}
	public void setArguments(List<Argument> args) {
		arguments = args;
	}	
	public void setDominanceConstraints(List<DominanceConstraint> cs) {
		dominanceConstraints = cs;		
	}
	public void setSlots(List<Slot> ls) {
		slots = ls;
	}

// get methods
	public String getReferent() {
		return mainReferent;
	}
	public Type getType() {
		return mainType;
	}
	public Label getLabel() {
		return mainLabel;
	}
	public List<DRS> getComponents() {
		return components;
	}
	public List<Argument> getArguments() {
		return arguments;
	}
	public List<DominanceConstraint> getDominanceConstraints() {
		return dominanceConstraints;
	}
	public List<Slot> getSlots() {
		return slots;
	}
	
	public DRS getComponent(Label label) {
		
		DRS c = new DRS();
		
		for (DRS component : components) {
			if ( component.getLabel().equals(label) ) {
				c = component;
			}
		}
		return c;
	}
	public Argument getArgument(String index) {
		
		for ( Argument arg : arguments ) {
			if ( arg.anchor.equals(index) ) {
				return arg;
			}
		}
		return null;
	}
	
// print method
	public String toString()
	{
		String string = "<" + mainReferent + "," + mainLabel + "," + mainType + ", " 
					+ components + ", " + arguments + ", "+ dominanceConstraints
					+ slots + ">";
		
		return string;
	}

// removing an argument
	public void removeArgument(String index) {
		Argument argument = getArgument(index);
		arguments.remove(argument);
	}

// semantic operation corresponding to SUBSTITUTION
	public Dude apply(String index, Dude dude) throws UnsupportedOperationException {
		
		Argument argumentThis = this.getArgument(index);
		Argument argumentDude = dude.getArgument(index);
		
		if ( argumentThis != null && argumentThis.type.equals(dude.mainType) ) {
			return this.applyTo(argumentThis,dude);
		}	
		else {
			if ( argumentDude != null && argumentDude.type.equals(this.mainType) ) {
				return dude.applyTo(argumentDude,this);
			}
			else {
				throw new UnsupportedOperationException("Dude.apply failed because of type mismatch:\n Tried to merge " + this + " and " + dude 
						+ "\n Index is " + index + " and neither " + argumentThis + " and " + dude + " nor " + argumentDude + " and " + this + " fit.");
			}
		}
	}
	
	public Dude applyTo(Argument argument, Dude dude) {

		Dude output = cloneDude();
		Dude input  = dude.cloneDude();

		// first check for name clashes and rename if necessary

		output.avoidClash(input); 
		
		// then do the application
		
		input.replaceLabel(input.mainLabel,argument.label);
		
		String ref = argument.referent; 
		input.replaceReferent(input.mainReferent,ref);

		output.components.addAll(input.components); 		
		output.dominanceConstraints.addAll(input.dominanceConstraints); 
		output.arguments.remove(argument);
		output.arguments.addAll(dude.arguments); 
		output.slots.addAll(input.slots);
	
		return output;
	}
	
	// semantic operation corresponding to ADJOIN
	public Dude merge(Dude dude) {
		
		Dude output = cloneDude();
		Dude input  = dude.cloneDude();
			
		output.avoidClash(input); 

		// first unify designated referents 
		output.replaceReferent(output.mainReferent,input.mainReferent);
		
		// then put together both dudes (union of components, constraints, arguments, links, and meta variables)
		output.components.addAll(input.components); 
		output.dominanceConstraints.addAll(input.dominanceConstraints);
		output.arguments.addAll(input.arguments); 
		output.slots.addAll(input.slots);

		// finally add a constraint to link the main input-component to the bottom output-component (with DomType.equal)
		DominanceConstraint newConstraint = new DominanceConstraint(getBottomLabel(output),input.mainLabel);
		newConstraint.setType(DomType.equal);
		output.dominanceConstraints.add(newConstraint);

		return output;
	}
	
	public Label getBottomLabel(Dude dude) {
		
		List<Label> lowerLabels = new ArrayList<Label>();
		for (DominanceConstraint constraint : dude.getDominanceConstraints()) {	
			if (!constraint.getType().equals(DomType.equal)) {
				if (!lowerLabels.contains(constraint.getSub())) {
					lowerLabels.add(constraint.getSub());
				}
			}
		}
		for (DominanceConstraint constraint : dude.getDominanceConstraints()) {	
			if (!constraint.getType().equals(DomType.equal)) {
				lowerLabels.remove(constraint.getSuper());
			}
		}
		if (lowerLabels.size() == 1) {
			return lowerLabels.get(0);
		}
		else {
			if (lowerLabels.size() > 1) {
				System.out.println("Something is wrong: There is more than one bottom label. Using mainLabel instead.");
			}
			return dude.mainLabel;
		}
		
	}
	
	
// clone method	
	public Dude cloneDude() {
				
		List<DRS> cs = new ArrayList<DRS>();
		List<Argument> args = new ArrayList<Argument>();
		List<DominanceConstraint> dcs = new ArrayList<DominanceConstraint>();
		List<Slot> ls = new ArrayList<Slot>();
		List<BasicSlot> lbs = new ArrayList<BasicSlot>();
		
		for (DRS component : components) {
			cs.add(component.clone());
		}
		for (Argument argument : arguments) {
			args.add(argument.clone());
		}
		for (DominanceConstraint constraint : dominanceConstraints) {
			dcs.add(constraint.clone());
		}
		for (Slot slot : slots) {
			ls.add(slot.clone());
		}
		
		Dude dude = new Dude();
		
		dude.mainReferent = mainReferent;
		dude.mainLabel = mainLabel;
		dude.mainType = mainType;
		dude.components = cs;
		dude.dominanceConstraints = dcs;
		dude.arguments = args;
		dude.slots = ls;
		
		return dude;
	}
	
	public void avoidClash(Dude dude) {
		
		//System.out.println("Dudes before avoidClash:\n || " + this + "\n || " + dude); // DEBUG
		
		Set<String> variables = dude.collectVariables();
		Set<Label> labels = dude.collectLabels();
		
		Set<String> allVariables = new HashSet<String>();
		allVariables.addAll(variables);
		allVariables.addAll(collectVariables());
		Set<Label> allLabels = new HashSet<Label>();
		allLabels.addAll(labels);
		allLabels.addAll(collectLabels());
		
		variables.retainAll(collectVariables());
		labels.retainAll(collectLabels());
		
		for (String var : variables) {
			
			String freshbase;
			String varbase;
			if (var.charAt(0) == '?') { 
				freshbase = "?"; 
				varbase = ""+var.charAt(1); 
			}
			else { 
				freshbase = "";
				varbase = ""+var.charAt(0);
			}

			String fresh = freshbase + varbase + "0"; 

			for (int i = 0; (allVariables.contains(varbase+i) || allVariables.contains("?"+varbase+i)); i++) {	
				fresh = freshbase + varbase + (i+1);
			}			
			allVariables.add(fresh);
			dude.replaceReferent(var,fresh);
		}

		for (Label l : labels) {
			Label fresh = new Label("l1");
			for (int i=1; allLabels.contains(new Label("l"+i)); i++) {
				fresh = new Label("l" + (i+1));
			}
			allLabels.add(fresh);
			dude.replaceLabel(l,fresh);
		}
		
		//System.out.println("Dude after avoidClash:\n" + this); // DEBUG
	}
	
	public Set<String> collectVariables() {
		
		Set<String> variables = new HashSet<String>();
		
		variables.add(mainReferent);
		for ( DRS component : components ) {
			variables.addAll(component.collectVariables());
		}
		for ( Argument argument : arguments ) {
			variables.addAll(argument.collectVariables());
		}
		
		return variables;
	}
	
	public Set<Label> collectLabels() {
		
		Set<Label> labels = new HashSet<Label>();
		
		labels.add(mainLabel);
		for (DRS drs : components) {
			labels.addAll(drs.getAllLabels()); 
		}
		for (DominanceConstraint dc : dominanceConstraints) { // just to be sure (should be the same labels as in m_Components)
			labels.add(dc.getSuper());
			labels.add(dc.getSub());
		}
		for (Argument arg : arguments) {
			labels.add(arg.label);
		}
		
		return labels;
	}
	
	// method to collect all simple DRS conditions occurring in the DUDE
	public Set<Simple_DRS_Condition> collectPredicates() {
		
		Set<Simple_DRS_Condition> predicates = new HashSet<Simple_DRS_Condition>();

		for (DRS component : this.getComponents()) {
			predicates.addAll(component.collectPredicates());
		}
		return predicates;
	}
	
	public void replaceReferent(String ref1,String ref2) {
		
		if (mainReferent.equals(ref1)) {
			mainReferent = ref2;
		}
		for (DRS component : components) {
			component.replaceReferent(ref1,ref2);
		}
		for (Argument argument : arguments ) {
			argument.replaceReferent(ref1,ref2);
		}
		for (Slot slot : slots) {
			slot.replaceReferent(ref1.replace("?",""),ref2.replace("?",""));
		}
	}
	
	public void replaceLabel(Label l_old, Label l_new) {
		
		if (mainLabel.equals(l_old)) {
			mainLabel = l_new;
		}
		for (DRS drs : components) {
			drs.replaceLabel(l_old, l_new);
		}
		for (DominanceConstraint dc : dominanceConstraints) {
			if (dc.getSub().equals(l_old)) {
				dc.setSub(l_new);
			}
			if (dc.getSuper().equals(l_old)) {
				dc.setSuper(l_new);
			}
		}
		for (Argument arg : arguments) {
			if (arg.label.equals(l_old)) {
				arg.label = l_new;
			}
		}
	}
	
}