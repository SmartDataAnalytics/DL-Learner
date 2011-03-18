package org.dllearner.algorithm.tbsl.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.drs.DRS;
import org.dllearner.algorithm.tbsl.sem.drs.UDRS;
import org.dllearner.algorithm.tbsl.sem.dudes.data.Dude;
import org.dllearner.algorithm.tbsl.sem.util.DomType;
import org.dllearner.algorithm.tbsl.sem.util.DominanceConstraint;
import org.dllearner.algorithm.tbsl.sem.util.Label;

public class DUDE2UDRS_Converter {
	
	public DUDE2UDRS_Converter() {
	}

	public UDRS convert(Dude dude) throws UnsupportedOperationException {
		
		UDRS udrs = new UDRS();
		
		// determining bottom and top
		
		Set<Label> bottoms = new HashSet<Label>();
		Set<Label> tops = new HashSet<Label>();
		
		for (DominanceConstraint constraint : dude.getDominanceConstraints()) {	
			if (!constraint.getType().equals(DomType.equal)) {
				tops.add(constraint.getSuper());
				bottoms.add(constraint.getSub());
			}
		}
		for (DominanceConstraint constraint : dude.getDominanceConstraints()) {	
			if (!constraint.getType().equals(DomType.equal)) {			
				tops.remove(constraint.getSub());
				bottoms.remove(constraint.getSuper());
			}	
		}
		
		if (tops.isEmpty()) { // then all constraints were equals
			tops.add(new Label("noTop"));
		}
		if (bottoms.isEmpty()) { // just to make sure...
			bottoms.add(new Label("noBottom"));
		}	
		
		//	precondition: tops and bottoms are singleton sets 
		Label bottomLabel = (new ArrayList<Label>(bottoms)).get(0);
		Label topLabel = (new ArrayList<Label>(tops)).get(0);
				
		udrs.setBottom(bottomLabel);
		udrs.setTop(topLabel);
		
		// copying components and dominance constraints
		
		for ( DRS component : dude.getComponents() ) 
		{
			udrs.addComponent(component.clone());
		}
		
		if (!topLabel.toString().equals("noTop")) {
			udrs.addComponent(dude.getComponent(topLabel).clone());
		}
		if (!bottomLabel.toString().equals("noBottom")) {
			udrs.addComponent(dude.getComponent(bottomLabel).clone());
		}
		
		for ( DominanceConstraint constraint : dude.getDominanceConstraints() )
		{
			udrs.addDominanceConstraint(constraint);
		}
				
		return udrs;
	}
	
}
