package org.dllearner.algorithm.tbsl.sparql;

import java.io.Serializable;

public class SPARQL_Pair implements Serializable
{
	private static final long serialVersionUID = -1255754209857823420L;
	
	public SPARQL_Term a;
	public Object b;

	public SPARQL_PairType type;

	public SPARQL_Pair(SPARQL_Term a, Object b, SPARQL_PairType type)
	{
		super();
		this.a = a;
		this.b = b;
		this.type = type;
	}

	public SPARQL_Pair(SPARQL_Term a, SPARQL_PairType type)
	{
		super();
		this.a = a;
		this.type = type;
	}

	public String toString()
	{
		switch (type)
		{
		case B:
			return "BOUND(" + a + ")";
		case EQ:
			return a + " == " + b;
		case GT:
			return a + " > " + b;
		case GTEQ:
			return a + " >= " + b;
		case LT:
			return a + " < " + b;
		case LTEQ:
			return a + " <= " + b;
		case NB:
			return "!BOUND(" + a + ")";
		case NEQ:
			return a + "!=" + b;
		case REGEX:
			return "regex(" + a + "," + b + ",'i')";
		}
		return "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
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
		SPARQL_Pair other = (SPARQL_Pair) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	

}