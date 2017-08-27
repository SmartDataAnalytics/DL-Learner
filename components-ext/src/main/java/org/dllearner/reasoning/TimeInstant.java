package org.dllearner.reasoning;

import java.time.Instant;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name=InfluxDBOWLTimeReasoner.instantTable)
public class TimeInstant {
	@Column(name=InfluxDBOWLTimeReasoner.timeColumn)
	Instant time;
	
	@Column(name=InfluxDBOWLTimeReasoner.classColumn, tag=true)
	String cls;
	
	@Column(name=InfluxDBOWLTimeReasoner.dummyColumn, tag=true)
	String dummy;
	
//	@Column(name=InfluxDBOWLTimeReasoner.individualColumn)
//	private Integer individualID;
	
	@Column(name=InfluxDBOWLTimeReasoner.individualColumn)
	String individual;
	
	@Override
	public String toString() {
		return time  + ": " + individual + " (" + cls + ")";
	}
}
