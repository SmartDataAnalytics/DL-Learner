package org.dllearner.utilities;

import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.SortedSet;

/**
 * Implicit class to interpret CoverageCount as tp/fp/fn/fp
 */
public class CoverageAdapter {
	public static class CoverageCountAdapter2 {
		private final ReasoningUtils.CoverageCount[] cc;

		public CoverageCountAdapter2(ReasoningUtils.CoverageCount[] cc) {
			this.cc = cc;
		}

		public int posAsPos() {return tp();}
		public void setPosAsPos(int tp){setTp(tp);}
		public int posAsNeg() {return fn();}
		public void setPosAsNeg(int fn){setFn(fn);}
		public int negAsPos() {return fp();}
		public void setNegAsPos(int fp) {setFp(fp);}
		public int negAsNeg() {return tn();}
		public void setNegAsNeg(int tn) {setTn(tn);}

		public int tp() { return cc[0].trueCount; }
		public void setTp(int tp) { cc[0].trueCount = tp; }

		public int fn() { return cc[0].falseCount; }
		public void setFn(int fn) { cc[0].falseCount = fn; }

		public int fp() { return cc[1].trueCount; }
		public void setFp(int fp) { cc[1].trueCount = fp; }

		public int tn() { return cc[1].falseCount; }
		public void setTn(int tn) { cc[1].falseCount = tn; }

		private String valueToString(int value) {
			return "#" + value;
		}

		@Override
		public String toString() {
			return "CoverageCount2{" +
					"tp" + valueToString(tp()) + "," +
					"fn" + valueToString(fn()) + "," +
					"fp" + valueToString(fp()) + "," +
					"tn" + valueToString(tn()) +
					'}';
		}
	}

	public static class CoverageAdapter2 {
		private final ReasoningUtils.Coverage[] cov;

		public CoverageAdapter2(ReasoningUtils.Coverage[] cov) {
			this.cov = cov;
		}

		public SortedSet<OWLIndividual> posAsPos() {return tp();}
		public void setPosAsPos(SortedSet<OWLIndividual> tp){setTp(tp);}
		public SortedSet<OWLIndividual> posAsNeg() {return fn();}
		public void setPosAsNeg(SortedSet<OWLIndividual> fn){setFn(fn);}
		public SortedSet<OWLIndividual> negAsPos() {return fp();}
		public void setNegAsPos(SortedSet<OWLIndividual> fp) {setFp(fp);}
		public SortedSet<OWLIndividual> negAsNeg() {return tn();}
		public void setNegAsNeg(SortedSet<OWLIndividual> tn) {setTn(tn);}

		public SortedSet<OWLIndividual> tp() { return cov[0].trueSet; }
		public void setTp(SortedSet<OWLIndividual> tp) { cov[0].trueSet = tp; cov[0].trueCount = tp.size(); }

		public SortedSet<OWLIndividual> fn() { return cov[0].falseSet; }
		public void setFn(SortedSet<OWLIndividual> fn) { cov[0].falseSet = fn; cov[0].falseCount = fn.size(); }

		public SortedSet<OWLIndividual> fp() { return cov[1].trueSet; }
		public void setFp(SortedSet<OWLIndividual> fp) { cov[1].trueSet = fp; cov[1].trueCount = fp.size(); }

		public SortedSet<OWLIndividual> tn() { return cov[1].falseSet; }
		public void setTn(SortedSet<OWLIndividual> tn) { cov[1].falseSet = tn; cov[1].falseCount = tn.size(); }

		private String setValueToString(SortedSet<OWLIndividual> set) {
			return "#" + set.size() + (set.size()>0?"(" + set.first() + (set.size()>1?"...":"")+")":"");
		}
		@Override
		public String toString() {
			return "Coverage2{" +
					"tp" + setValueToString(tp()) + "," +
					"fn" + setValueToString(fn()) + "," +
					"fp" + setValueToString(fp()) + "," +
					"tn" + setValueToString(tn()) +
					'}';
		}
	}
}
