package org.dllearner.utilities;

/**
 * Implicit class to interpret CoverageCount as tp/fp/fn/fp
 */
public class CoverageAdapter {
	public static class CoverageAdapter2 {
		private final ReasoningUtils.CoverageCount[] cc;

		public CoverageAdapter2(ReasoningUtils.CoverageCount[] cc) {
			this.cc = cc;
		}

		public int tp() {
			return cc[0].trueCount;
		}

		public void setTp(int tp) {
			cc[0].trueCount = tp;
		}

		public int fn() {
			return cc[0].falseCount;
		}

		public void setFn(int fn) {
			cc[0].falseCount = fn;
		}

		public int fp() {
			return cc[1].trueCount;
		}

		public void setFp(int fp) {
			cc[1].trueCount = fp;
		}

		public int tn() {
			return cc[1].falseCount;
		}

		public void setTn(int tn) {
			cc[1].falseCount = tn;
		}
	}
}
