package org.dllearner.algorithm.tbsl.ltag.agreement;

/**
 * implements the unification of two Feature objects. This is used by
 * earleyParser.SubstPredictor() to determine if a tree can be substituted into
 * a SubstNode if the SubstNode has Feature requirements.
 * 
 * @author felix
 * 
 */
public class Unification {

	/**
	 * @param a
	 *            FeatureConstraints from the SubstNode
	 * @param b
	 *            Feature from the RootNode of the Tree candidate
	 */
	public static boolean isUnifiable(Feature a, Feature b) {

		if (a == null && b == null) {
			return true;
		}
		else if (a == null && b != null) {
			if (b.getP() == null) { return true;}
			else { return false; }
		}
		else if (b == null && a != null) {
			if (a.getP() == null) { return true; }
			else { return false; }
		} else {
			if (a.equals(b)) {
				return true;
			} else {
				if (unify(a.getC(), b.getC()) && unify(a.getN(), b.getN())
						&& unify(a.getG(), b.getG())
						&& unify(a.getP(), b.getP())) {
					return true;
				}
				return false;
			}
		}
	}

	private static boolean unify(MorphologicalProperty a,
			MorphologicalProperty b) {
		if (a == null || b == null) {
			return true;
		} else {
			if (a.equals(b)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private static boolean unify(LexicalSelection a, LexicalSelection b) {
		if (a == null && b == null) {
			return true;
		} else if (a.equals(b)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		Feature a = Feature.construct("{c:nom,n:pl,p:durch}");
		Feature b = Feature.construct("{c:nom,p:durch}");

		System.out.println(a);
		System.out.println(b);
		System.out.println(Unification.isUnifiable(a, b));
	}

}
