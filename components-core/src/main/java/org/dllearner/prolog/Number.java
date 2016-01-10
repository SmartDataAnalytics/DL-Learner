package org.dllearner.prolog;


/**
 * 
 * @author Sebastian Bader
 *
 */
public class Number extends Constant {
    private double value;
    
    public Number(String src) {
        value = Double.parseDouble(src);
    }
    
    public Number(double value) {
        this.value = value;
    }

    public int getIntValue() {
        return (int) value;
    }
    public double getDoubleValue() {
        return value;
    }
    
    @Override
	public boolean isGround() {
        return true;
    }
    
    @Override
	public String toString() {
        return "C["+toPLString()+"]";
    }
    @Override
	public String toPLString() {
        if (((double)((int)value)) == value) 
            return ""+(int) value;
        return ""+value;
    }
    

    @Override
	public Term getInstance(Variable variable, Term term) {
        return new Number(value);
    }

    @Override
	public int hashCode() {
        return (int) value;
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        Number number;
        try {
        	number = (Number) obj;
        } catch (ClassCastException cce) {
            return false;
        }

        return value == number.value;
    }

    @Override
	public Object clone() {
        return new Number(value);
    }
}