package org.dllearner.algorithm.tbsl.exploration.Sparql;

public class Hypothesis {
private String variable;
private String uri;
private float rank;
public String getUri() {
	return uri;
}
public void setUri(String uri) {
	this.uri = uri;
}
public String getVariable() {
	return variable;
}
public void setVariable(String variable) {
	this.variable = variable;
}
public float getRank() {
	return rank;
}
public void setRank(float rank) {
	this.rank = rank;
}

public Hypothesis(String variable, String uri, float rank){
	setRank(rank);
	setVariable(variable);
	setUri(uri);
}
}
