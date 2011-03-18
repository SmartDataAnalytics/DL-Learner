package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Pair
{
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
		}
		return "";
	}

}