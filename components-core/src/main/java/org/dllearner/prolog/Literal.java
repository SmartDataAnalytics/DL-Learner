package org.dllearner.prolog;



/**
 * 
 * @author Sebastian Bader
 *
 */
public class Literal {
    private Atom atom;
    private boolean positive;
    
    public Literal(Atom atom, boolean state) {
        this.atom = atom;
        this.positive = state;
    }
    
    public Atom getAtom() {
        return atom;
    }
    
    public boolean isPositive() {
        return positive;
    }

    public boolean isGround() {
        return atom.isGround();
    }

    public Literal getInstance(Variable variable, Term term) {
        return new Literal(atom.getInstance(variable, term), positive);
    }
           
    @Override
	public String toString() {
        return (positive?"+":"-")+atom.toString();
    }
    
    public String toPLString() {
        return (positive?"":"not ")+atom.toPLString();
    }
    
    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
              
        Literal l;
        
        try {
            l = (Literal) obj;
        } catch (ClassCastException cce) {
            return false;
        }
        
        if (positive != l.positive)
            return false;
                    
        return atom.equals(l.atom);
    }

    @Override
	public int hashCode() {
        return atom.hashCode() * (positive?1:2);
    }
}
