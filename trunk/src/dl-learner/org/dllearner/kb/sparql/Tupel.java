package org.dllearner.kb.sparql;

public class Tupel {

	public String a;
	public String b;

	public Tupel(String a, String b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "<" + a + "|" + b + ">";
	}

	public boolean equals(Tupel t) {
		if (a.equals(t.a) && b.equals(t.b))
			return true;
		else
			return false;
	}

}
