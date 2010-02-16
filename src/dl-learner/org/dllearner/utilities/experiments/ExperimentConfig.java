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

	public final String experimentName;
	public final int sizeOfResultVector;
	
	protected List<MonitorComposite> mcs = new ArrayList<MonitorComposite>();
	protected Map<String,MonitorComposite> mcsMap = new HashMap<String, MonitorComposite>();
	protected Map<MonitorComposite,String> mcsMapRev = new HashMap<MonitorComposite,String>();
	
	public ExperimentConfig(String label){
		this(label,1);
	}
	public ExperimentConfig(String experimentName, int sizeOfResultVector){
		this.experimentName = experimentName;
		this.sizeOfResultVector = sizeOfResultVector;
	}
	
	@Override
	public String toString(){
		return this.experimentName+" with "+sizeOfResultVector+" iterations";
	}
	
	public List<TableRowColumn> getTableRows(){
		List<TableRowColumn> l = new ArrayList<TableRowColumn>();
		if(sizeOfResultVector == 1) {
			Monitor[] monitors = new Monitor[mcs.size()]; 
//			TableRowColumn trc =
			for (int i = 0; i < monitors.length; i++) {
				monitors[i] = mcs.get(i).getMonitors()[0];
			}
			 l.add(new TableRowColumn(monitors, experimentName, ""));
			 
		}else{
			for(MonitorComposite mc :mcs){
				 l.add(new TableRowColumn(mc.getMonitors(), experimentName, getRev(mc)));
			}
		}
		
		return l;
	}
	
	private MonitorComposite get(MonKeyImp m){
		return mcsMap.get(mon(m).getLabel());
	}
	private String getRev(MonitorComposite mc){
		return mcsMapRev.get(mc);
	}
	
	private void put(MonKeyImp m ,MonitorComposite mc  ){
		mcsMap.put(mon(m).getLabel(), mc);
	}
	private void putRev(MonitorComposite mc , MonKeyImp m  ){
		mcsMapRev.put( mc, m.getLabel());
	}
	
	public void init(List<MonKeyImp> monkeys){
		for (MonKeyImp monKeyImp : monkeys) {
			init(monKeyImp);
		}
//		JamonMonitorLogger.writeHTMLReport("/tmp/tiger.html");
	}
	
	public void init(MonKeyImp oldMonkey){
		Monitor[] marr = new Monitor[sizeOfResultVector];
		for (int i = 0; i < sizeOfResultVector; i++) {
			MonKeyImp newMonKey = mon(oldMonkey, i);
			if(newMonKey.getUnits().equals(Jamon.MS)){
				marr[i] = MonitorFactory.getTimeMonitor(newMonKey);
			}else{
				marr[i] = MonitorFactory.getMonitor(newMonKey);
			}
		}
		MonitorComposite m = new MonitorComposite(marr);
		mcs.add(m);
		put( oldMonkey,m);
		putRev(m, oldMonkey);
		
	}

	
	
	protected MonKeyImp mon(MonKeyImp monkey){
		MonKeyImp m = (monkey.getLabel().startsWith(experimentName))?monkey:new MonKeyImp(experimentName+"_"+monkey.getLabel(), monkey.getUnits());
		return m;
	}
	
	protected MonKeyImp mon(MonKeyImp oldMonkey, int index){
		//narrensicher
		MonKeyImp newMonkey = mon(oldMonkey);
		return new MonKeyImp(newMonkey.getLabel()+"_"+index, newMonkey.getUnits()) ;
	}
	
	public void add(MonKeyImp monkey, int index, double value){
		try{
			get(monkey).getMonitors()[index].add(value);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("index too big, max = "+index);
		}
		
	}
	public Monitor start(MonKeyImp monkey, int index){
		return get(monkey).getMonitors()[index].start();
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
