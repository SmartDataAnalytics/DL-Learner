package org.dllearner.dl;

/**
 * TODO: nochmal ueberlegen, ob es sinnvoll ist, dass InverseRole von
 * Role erbt; ev. besser trennen oder ein eine gemeinsame Oberklasse
 * von beiden; genau wie Concept alle Konzeptkonstruktoren zusammenfasst,
 * koennte Role alle Rollenkonstruktoren zusammenfassen
 * 
 * Role => Unterklassen: AtomicRole, InverseRole
 * 
 * @author jl
 *
 */
public class InverseRole extends Role {
	
	public InverseRole(String name) {
		super(name);
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	@Override		
	public String toString() {
		return getName() + "-";
	}
}
