package org.dllearner.prolog;

/**
 * 
 * @author Sebastian Bader
 *
 */
public class FunctionDefinition {
    public static int TYPE_USUAL = 0;
    public static int TYPE_INFIX = 1;
    public static int TYPE_POSTFIX = 2;
    public static int TYPE_PREFIX = 3;
    
    public static String[] TYPE_NAMES = new String[]{"usual", "infix", "postfix", "prefix"};
    
    private String name;
    private int arity;
    private int type;
    
    public FunctionDefinition(String name, int arity, int type) {
        super();
        this.name = name;
        this.arity = arity;
    }
    
    public FunctionDefinition(Function function) {
        this(function.getName(), function.getArity(), function.getType());
    }
    
    public int getArity() {
        return arity;
    }
    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }

    @Override
	public int hashCode() {
        return name.hashCode() * (arity + 1);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        try {
            FunctionDefinition fd = (FunctionDefinition) obj;
            if (fd.getArity() != getArity())
                return false;
            if (!fd.getName().equals(getName()))
                return false;
            if (fd.getType() != getType())
                return false;
        } catch (ClassCastException cce) {
            return false;
        }
        return true;
    }
    
    @Override
	public String toString() {
        return name+TYPE_NAMES[type]+"/"+arity;
    }
}
