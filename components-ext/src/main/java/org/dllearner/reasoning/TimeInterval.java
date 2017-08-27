package org.dllearner.reasoning;

import java.time.Instant;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name=InfluxDBOWLTimeReasoner.intervalTable)
public class TimeInterval {
	@Column(name=InfluxDBOWLTimeReasoner.timeColumn)
	Instant time;

	@Column(name=InfluxDBOWLTimeReasoner.individualColumn)
	String individual;
}
