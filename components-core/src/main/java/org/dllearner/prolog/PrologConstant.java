package org.dllearner.prolog;


/**
 * 
 * @author Sebastian Bader
 *
 */
public class PrologConstant extends Constant {
    private String name;
    
    public PrologConstant(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
	public boolean isGround() {
        return true;
    }

    @Override
	public String toString() {
        return "C["+name+"]";
    }
    @Override
	public String toPLString() {
        return name;
    }

    @Override
	public Term getInstance(Variable variable, Term term) {
        return new PrologConstant(name);
    }
    
    @Override
	public boolean equals(Object obj) {
        return name.equals(obj);
    }

    @Override
	public int hashCode() {
        return name.hashCode();
    }

    @Override
	public Object clone() {
        return new PrologConstant(name);
    }
}