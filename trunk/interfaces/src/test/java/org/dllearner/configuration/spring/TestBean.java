package org.dllearner.configuration.spring;

import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/19/11
 * Time: 2:55 PM
 *
 * Test Bean for testing that we can store all the types we need.
 */
public class TestBean {

    private String simpleValue;
    private TestBean component;
    private Integer intValue;
    private Double doubleValue;
    private Set setValue;
    private Map mapValue;
    private Set positiveValues;
    private Set negativeValues;

    public String getSimpleValue() {
        return simpleValue;
    }

    public void setSimpleValue(String simpleValue) {
        this.simpleValue = simpleValue;
    }

    public TestBean getComponent() {
        return component;
    }

    public void setComponent(TestBean component) {
        this.component = component;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Set getSetValue() {
        return setValue;
    }

    public void setSetValue(Set setValue) {
        this.setValue = setValue;
    }

    public Map getMapValue() {
        return mapValue;
    }

    public void setMapValue(Map mapValue) {
        this.mapValue = mapValue;
    }

    public Set getPositiveValues() {
        return positiveValues;
    }

    public void setPositiveValues(Set positiveValues) {
        this.positiveValues = positiveValues;
    }

    public Set getNegativeValues() {
        return negativeValues;
    }

    public void setNegativeValues(Set negativeValues) {
        this.negativeValues = negativeValues;
    }
}
