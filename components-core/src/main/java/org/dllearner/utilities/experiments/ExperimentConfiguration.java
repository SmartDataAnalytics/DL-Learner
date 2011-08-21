/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

/**
 * An experiment has a certain configuration. In an iterated experiment or a
 * experiment row a parameter can be altered or influenced by previous
 * experiments with the same configuration. If you do not have an iterated
 * experiment (or better only 1 iteration) set sizeOfResultVector to 1
 * 
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * 
 */
public class ExperimentConfiguration {
	private static final Logger logger = Logger.getLogger(ExperimentConfiguration.class);

	public final String experimentName;
	public final int sizeOfResultVector;

	protected List<MonitorComposite> mcs = new ArrayList<MonitorComposite>();
	protected Map<String, MonitorComposite> mcsMap = new HashMap<String, MonitorComposite>();
	protected Map<MonitorComposite, String> mcsMapRev = new HashMap<MonitorComposite, String>();

	/**
	 * sets sizeOfResultVector to 1, meaning no iterated experiments
	 * 
	 * @param experimentName
	 */
	public ExperimentConfiguration(String experimentName) {
		this(experimentName, 1);
	}

	public ExperimentConfiguration(String experimentName, int sizeOfResultVector) {
		this.experimentName = experimentName;
		this.sizeOfResultVector = sizeOfResultVector;
	}

	@Override
	public String toString() {
		return this.experimentName + " with " + sizeOfResultVector + " iterations";
	}

	public List<TableRowColumn> getTableRows() {
		List<TableRowColumn> l = new ArrayList<TableRowColumn>();
		if (sizeOfResultVector == 1) {
			Monitor[] monitors = new Monitor[mcs.size()];
			for (int i = 0; i < monitors.length; i++) {
				monitors[i] = mcs.get(i).getMonitors()[0];
			}
			l.add(new TableRowColumn(monitors, experimentName, ""));

		} else {
			for (MonitorComposite mc : mcs) {
				l.add(new TableRowColumn(mc.getMonitors(), experimentName, getRev(mc)));
			}
		}

		return l;
	}

	private MonitorComposite get(MonKeyImp m) {
		return mcsMap.get(mon(m).getLabel());
	}

	private String getRev(MonitorComposite mc) {
		return mcsMapRev.get(mc);
	}

	private void put(MonKeyImp m, MonitorComposite mc) {
		mcsMap.put(mon(m).getLabel(), mc);
	}

	private void putRev(MonitorComposite mc, MonKeyImp m) {
		mcsMapRev.put(mc, m.getLabel());
	}

	public void init(List<MonKeyImp> monkeys) {
		for (MonKeyImp monKeyImp : monkeys) {
			init(monKeyImp);
		}
	}

	public void init(MonKeyImp oldMonkey) {
		Monitor[] marr = new Monitor[sizeOfResultVector];
		for (int i = 0; i < sizeOfResultVector; i++) {
			MonKeyImp newMonKey = mon(oldMonkey, i);
			if (newMonKey.getUnits().equals(JamonMonitorLogger.MS)) {
				marr[i] = MonitorFactory.getTimeMonitor(newMonKey);
			} else {
				marr[i] = MonitorFactory.getMonitor(newMonKey);
			}
		}
		MonitorComposite m = new MonitorComposite(marr);
		mcs.add(m);
		put(oldMonkey, m);
		putRev(m, oldMonkey);

	}

	protected MonKeyImp mon(MonKeyImp monkey) {
		MonKeyImp m = (monkey.getLabel().startsWith(experimentName)) ? monkey : new MonKeyImp(experimentName
				+ "_" + monkey.getLabel(), monkey.getUnits());
		return m;
	}

	protected MonKeyImp mon(MonKeyImp oldMonkey, int index) {
		// narrensicher
		MonKeyImp newMonkey = mon(oldMonkey);
		return new MonKeyImp(newMonkey.getLabel() + "_" + index, newMonkey.getUnits());
	}

	public void add(MonKeyImp monkey, int index, double value) {
		try {
			get(monkey).getMonitors()[index].add(value);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			logger.error("index too big, is: "+index+" max = " +  get(monkey).getMonitors().length);
		}

	}

	public Monitor start(MonKeyImp monkey, int index) {
		return get(monkey).getMonitors()[index].start();
	}

}
