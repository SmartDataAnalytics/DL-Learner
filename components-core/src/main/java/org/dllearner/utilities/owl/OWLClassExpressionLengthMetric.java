package org.dllearner.utilities.owl;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.config.ConfigOption;

/**
 * Generic configurable length metric for class expression length calculation
 */
@ComponentAnn(name = "OWL Class Expression Length Metric", shortName = "cel_metric", version = 0.1)
public class OWLClassExpressionLengthMetric extends AbstractComponent {
	@ConfigOption(defaultValue = "1", description = "Class: \"C\"")
	public int classLength = 1;
	@ConfigOption(defaultValue = "1", description = "Intersection: A\"⨅\"B")
	public int objectIntersectionLength = 1;
	@ConfigOption(defaultValue = "1", description = "Union: A\"⨆\"B")
	public int objectUnionLength = 1;
	@ConfigOption(defaultValue = "1", description = "Complement: \"¬\"C")
	public int objectComplementLength = 1;
	@ConfigOption(defaultValue = "1", description = "Obj. Some Values: \"∃\" r.C")
	public int objectSomeValuesLength = 1;
	@ConfigOption(defaultValue = "1", description = "Obj. All Values: \"∀\" r.C")
	public int objectAllValuesLength = 1;
	@ConfigOption(defaultValue = "2", description = "Obj. Has Value: \"∃\" r.\"{I}\"")
	public int objectHasValueLength = 2;
	@ConfigOption(defaultValue = "2", description = "Obj. Cardinality restriction: \"≤n\" r.C")
	public int objectCardinalityLength = 2;
	@ConfigOption(defaultValue = "2", description = "Obj. Self restriction: \"∃\" r.Self")
	public int objectHasSelfLength = 1;
	@ConfigOption(defaultValue = "1", description = "Obj. One of: ∃ r.\"{X,Y,Z}\"")
	public int objectOneOfLength = 1;
	@ConfigOption(defaultValue = "1", description = "Data Some Values: \"∃\" p.t")
	public int dataSomeValuesLength = 1;
	@ConfigOption(defaultValue = "1", description = "Data All Values: \"∀\" p.t")
	public int dataAllValuesLength = 1;
	@ConfigOption(defaultValue = "2", description = "Data Has Value: \"∃\" p.\"{V}\"")
	public int dataHasValueLength = 2;
	@ConfigOption(defaultValue = "2", description = "Data Cardinality restriction: \"≤n\" r.t")
	public int dataCardinalityLength = 2;
	@ConfigOption(defaultValue = "1", description = "Obj. Property: ∃ \"r\".C")
	public int objectProperyLength = 1;
	@ConfigOption(defaultValue = "2", description = "Inverse property: ∃ \"r⁻\".C")
	public int objectInverseLength = 2;
	@ConfigOption(defaultValue = "1", description = "Data Property: ∃ \"p\".t")
	public int dataProperyLength = 1;
	@ConfigOption(defaultValue = "1", description = "Datatype: \"^^datatype\"")
	public int datatypeLength = 1;
	@ConfigOption(defaultValue = "1", description = "Data One of: ∃ p.\"{U,V,W}\"")
	public int dataOneOfLength = 1;
	@ConfigOption(defaultValue = "1", description = "Data Complement: \"¬\"datatype")
	public int dataComplementLength = 1;
	@ConfigOption(defaultValue = "1", description = "Data Intersection: datatype\"⨅\"datatype")
	public int dataIntersectionLength = 1;
	@ConfigOption(defaultValue = "1", description = "Data Union: datatype\"<u>⨆</u>\"datatype")
	public int dataUnionLength = 1;

	public static OWLClassExpressionLengthMetric getDefaultMetric() {
		OWLClassExpressionLengthMetric metric = new OWLClassExpressionLengthMetric();
		return metric;
	}

	public static OWLClassExpressionLengthMetric getOCELMetric() {
		OWLClassExpressionLengthMetric metric = new OWLClassExpressionLengthMetric();
		metric.dataHasValueLength = 1;
		metric.objectHasValueLength = 1;
		metric.dataSomeValuesLength = 0;
		metric.objectSomeValuesLength = 0;
		return metric;
	}

	public int getClassLength() {
		return classLength;
	}

	public void setClassLength(int classLength) {
		this.classLength = classLength;
	}

	public int getObjectIntersectionLength() {
		return objectIntersectionLength;
	}

	public void setObjectIntersectionLength(int objectIntersectionLength) {
		this.objectIntersectionLength = objectIntersectionLength;
	}

	public int getObjectUnionLength() {
		return objectUnionLength;
	}

	public void setObjectUnionLength(int objectUnionLength) {
		this.objectUnionLength = objectUnionLength;
	}

	public int getObjectComplementLength() {
		return objectComplementLength;
	}

	public void setObjectComplementLength(int objectComplementLength) {
		this.objectComplementLength = objectComplementLength;
	}

	public int getObjectSomeValuesLength() {
		return objectSomeValuesLength;
	}

	public void setObjectSomeValuesLength(int objectSomeValuesLength) {
		this.objectSomeValuesLength = objectSomeValuesLength;
	}

	public int getObjectAllValuesLength() {
		return objectAllValuesLength;
	}

	public void setObjectAllValuesLength(int objectAllValuesLength) {
		this.objectAllValuesLength = objectAllValuesLength;
	}

	public int getObjectHasValueLength() {
		return objectHasValueLength;
	}

	public void setObjectHasValueLength(int objectHasValueLength) {
		this.objectHasValueLength = objectHasValueLength;
	}

	public int getObjectCardinalityLength() {
		return objectCardinalityLength;
	}

	public void setObjectCardinalityLength(int objectCardinalityLength) {
		this.objectCardinalityLength = objectCardinalityLength;
	}

	public int getObjectOneOfLength() {
		return objectOneOfLength;
	}

	public void setObjectOneOfLength(int objectOneOfLength) {
		this.objectOneOfLength = objectOneOfLength;
	}

	public int getDataSomeValuesLength() {
		return dataSomeValuesLength;
	}

	public void setDataSomeValuesLength(int dataSomeValuesLength) {
		this.dataSomeValuesLength = dataSomeValuesLength;
	}

	public int getDataAllValuesLength() {
		return dataAllValuesLength;
	}

	public void setDataAllValuesLength(int dataAllValuesLength) {
		this.dataAllValuesLength = dataAllValuesLength;
	}

	public int getDataHasValueLength() {
		return dataHasValueLength;
	}

	public void setDataHasValueLength(int dataHasValueLength) {
		this.dataHasValueLength = dataHasValueLength;
	}

	public int getDataCardinalityLength() {
		return dataCardinalityLength;
	}

	public void setDataCardinalityLength(int dataCardinalityLength) {
		this.dataCardinalityLength = dataCardinalityLength;
	}

	public int getObjectProperyLength() {
		return objectProperyLength;
	}

	public void setObjectProperyLength(int objectProperyLength) {
		this.objectProperyLength = objectProperyLength;
	}

	public int getObjectInverseLength() {
		return objectInverseLength;
	}

	public void setObjectInverseLength(int objectInverseLength) {
		this.objectInverseLength = objectInverseLength;
	}

	public int getDataProperyLength() {
		return dataProperyLength;
	}

	public void setDataProperyLength(int dataProperyLength) {
		this.dataProperyLength = dataProperyLength;
	}

	public int getDatatypeLength() {
		return datatypeLength;
	}

	public void setDatatypeLength(int datatypeLength) {
		this.datatypeLength = datatypeLength;
	}

	public int getDataOneOfLength() {
		return dataOneOfLength;
	}

	public void setDataOneOfLength(int dataOneOfLength) {
		this.dataOneOfLength = dataOneOfLength;
	}

	public int getDataComplementLength() {
		return dataComplementLength;
	}

	public void setDataComplementLength(int dataComplementLength) {
		this.dataComplementLength = dataComplementLength;
	}

	public int getDataIntersectionLength() {
		return dataIntersectionLength;
	}

	public void setDataIntersectionLength(int dataIntersectionLength) {
		this.dataIntersectionLength = dataIntersectionLength;
	}

	public int getDataUnionLength() {
		return dataUnionLength;
	}

	public void setDataUnionLength(int dataUnionLength) {
		this.dataUnionLength = dataUnionLength;
	}

	@Override
	public void init() {
		initialized = true;
	}

}
