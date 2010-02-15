package org.dllearner.utilities.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

public class ExperimentConfig {
	private static final Logger logger = Logger.getLogger(ExperimentConfig.class);

	public final String label;
	public final int iterations;
	
	protected List<MonitorComposite> mcs = new ArrayList<MonitorComposite>();
	protected Map<String,MonitorComposite> mcsMap = new HashMap<String, MonitorComposite>();
	protected Map<MonitorComposite,String> mcsMapRev = new HashMap<MonitorComposite,String>();
	
	public ExperimentConfig(String label){
		this(label,1);
	}
	public ExperimentConfig(String label, int iterations){
		this.label = label;
		this.iterations = iterations;
	}
	
	@Override
	public String toString(){
		return this.label+" with "+iterations+" iterations";
	}
	
	public List<TableRowColumn> getTableRows(){
		List<TableRowColumn> l = new ArrayList<TableRowColumn>();
		return l;
	}
	
	public void init(MonKeyImp monkey){
		Monitor[] marr = new Monitor[iterations];
		for (int i = 0; i < iterations; i++) {
			marr[i] = MonitorFactory.getMonitor(mon(monkey, i));
		}
		MonitorComposite m = new MonitorComposite(marr);
		mcs.add(m);
		mcsMap.put( monkey.getLabel(),m);
		mcsMapRev.put(m, monkey.getLabel());
	}

	
	
	protected MonKeyImp mon(MonKeyImp monkey){
		return (monkey.getLabel().startsWith(label))?monkey:new MonKeyImp(label+monkey.getLabel(), monkey.getUnits());
	}
	
	protected MonKeyImp mon(MonKeyImp monkey, int index){
		//narrensicher
		MonKeyImp l = mon(monkey);
		return (iterations==1)?monkey:new MonKeyImp(l.getLabel()+"_"+index, l.getUnits()) ;
	}
	
	public void add(MonKeyImp monkey, int index, double value){
		try{
			mcsMap.get(monkey.getLabel()).getMonitors()[index].add(value);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("index too big, max = "+index);
		}
		
	}
	public Monitor start(MonKeyImp monkey, int index){
		return mcsMap.get(monkey.getLabel()).getMonitors()[index].start();
	}
	
//	public static boolean higher(Monitor[] a, double current){
//		for (int i = 0; i < a.length; i++) {
//			if(current>a[i].getMax()){
//				return true;
//			}
//		}
//		return false;
//	}
	
}
