package org.dllearner.utilities.owl;

/**
 * Created by ailin on 16-1-28.
 */
public class OWLClassExpressionLengthMetric {
	public int classLength = 1;
	public int objectIntersectionLength = 1;
	public int objectUnionLength = 1;
	public int objectComplementLength = 1;
	public int objectSomeValuesLength = 1;
	public int objectAllValuesLength = 1;
	public int objectHasValueLength = 2;
	public int objectCardinalityLength = 2;
	public int objectOneOfLength = 1;
	public int dataSomeValuesLength = 1;
	public int dataAllValuesLength = 1;
	public int dataHasValueLength = 2;
	public int dataCardinalityLength = 2;
	public int objectProperyLength = 1;
	public int objectInverseLength = 2;
	public int dataProperyLength = 1;
	public int datatypeLength = 1;
	public int dataOneOfLength = 1;
	public int dataComplementLength = 1;
	public int dataIntersectionLength = 1;
	public int dataUnionLength = 1;

	public static OWLClassExpressionLengthMetric getDefaultMetric() {
		OWLClassExpressionLengthMetric metric = new OWLClassExpressionLengthMetric();
		return metric;
	}
}
