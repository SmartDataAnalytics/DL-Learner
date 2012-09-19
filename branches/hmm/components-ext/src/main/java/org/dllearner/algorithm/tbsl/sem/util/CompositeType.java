package org.dllearner.algorithm.tbsl.sem.util;

public class CompositeType implements Type{

	Type argument;
	Type result;
	
	public CompositeType()
	{
	}
	
	public CompositeType(Type arg, Type res)
	{
		argument = arg;
		result = res;
	}
	
	public Type getArgumentType()
	{
		return argument;
	}
	
	public Type getResultType()
	{
		return result;
	}
	
	public void setArgumentType(Type type)
	{
		argument = type;
	}
	
	public void setResultType(Type type)
	{
		result = type;
	}
	
	public String toString()
	{
		return "<"+argument+","+result+">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositeType other = (CompositeType) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}
        
        public String toTex() {
            return "\\langle " + argument.toTex() + "," + result.toTex() + "\\rangle "; 
        }
	
}
