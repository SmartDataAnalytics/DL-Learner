package org.dllearner.algorithm.tbsl.ltag.agreement;

import java.util.regex.Pattern;

/**
 * Object to store the morphological properties of a TreeNode (root node) or the
 * morphological requirements of a SubstNode.
 * 
 * @author felix
 * 
 */
public class Feature {

	private Case c;
	private Numerus n;
	private Gender g;
	private Prep_de p;

	public Feature(Case c, Numerus n, Gender g, Prep_de p) {
		this.c = c;
		this.n = n;
		this.g = g;
		this.p = p;
	}

	/**
	 * used by the ltagParser to construct the Feature object from a string.
	 * 
	 * @param s
	 *            e.g. "{c:nom,g:g}"
	 * @return
	 */
	public static Feature construct(String s) {

		Case c = null;
		Numerus n = null;
		Gender g = null;
		Prep_de p = null;

		String[] parts = s.replaceAll("\\{|\\}", "").split(",");
		for (String x : parts) {
			if (Pattern.matches("(c:(nom|gen|dat|acc))", x)) {
				if (x.substring(2).equals("nom")) {
					c = Case.NOM;
				}
				if (x.substring(2).equals("gen")) {
					c = Case.GEN;
				}
				if (x.substring(2).equals("dat")) {
					c = Case.DAT;
				}
				if (x.substring(2).equals("acc")) {
					c = Case.ACC;
				}
			}
			if (Pattern.matches("(n:(sg|pl))", x)) {
				if (x.substring(2).equals("sg")) {
					n = Numerus.SG;
				}
				if (x.substring(2).equals("pl")) {
					n = Numerus.PL;
				}
			}
			if (Pattern.matches("(g:(m|f|n))", x)) {
				if (x.substring(2).equals("m")) {
					g = Gender.M;
				}
				if (x.substring(2).equals("f")) {
					g = Gender.F;
				}
				if (x.substring(2).equals("n")) {
					g = Gender.N;
				}
			}
			if (Pattern.matches("(p:(an|durch))", x)) {
				if (x.substring(2).equals("an")) {
					p = Prep_de.AN;
				}
				if (x.substring(2).equals("durch")) {
					p = Prep_de.DURCH;
				}
			}
		}
		if (c == null && g == null && n == null && p == null) {
			return null;
		} else {
			return new Feature(c, n, g, p);
		}

	}

	public String toString() {
		String cStr = "";
		String nStr = "";
		String gStr = "";
		String pStr = "";
		if (c != null) {
			cStr = "c:" + c.toString().toLowerCase() + " ";
		}
		if (n != null) {
			nStr = "n:" + n.toString().toLowerCase() + " ";
		}
		if (g != null) {
			gStr = "g:" + g.toString().toLowerCase();
		}
		if (p != null) {
			pStr = "p:" + p.toString().toLowerCase();
		}

		return ("{" + cStr + nStr + gStr + pStr + "}").trim().replaceAll(" ", ",");
	}

	public Case getC() {
		return c;
	}

	public void setC(Case c) {
		this.c = c;
	}

	public Numerus getN() {
		return n;
	}

	public void setN(Numerus n) {
		this.n = n;
	}

	public Gender getG() {
		return g;
	}

	public void setG(Gender g) {
		this.g = g;
	}
	
	public Prep_de getP() {
		return p;
	}
	
	public void setP(Prep_de p) {
		this.p = p;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + ((g == null) ? 0 : g.hashCode());
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Feature))
			return false;
		Feature other = (Feature) obj;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		if (g == null) {
			if (other.g != null)
				return false;
		} else if (!g.equals(other.g))
			return false;
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		return true;
	}

}
