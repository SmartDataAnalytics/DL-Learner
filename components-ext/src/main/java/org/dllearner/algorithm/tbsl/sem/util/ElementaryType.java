package org.dllearner.algorithm.tbsl.sem.util;

public class ElementaryType implements Type{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ElementaryType other = (ElementaryType) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	ElemType type;
	
	public ElementaryType(ElemType elemtype)
	{
		type = elemtype;
	}
	
	public String toString()
	{
		return type.toString();
	}
        public String toTex() {
            return type.toString();
        }
	
}
