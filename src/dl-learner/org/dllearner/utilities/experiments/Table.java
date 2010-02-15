package org.dllearner.utilities.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class Table {
	private static final Logger logger = Logger.getLogger(Table.class);

	boolean replaceCommaByPoints = true;
	
	enum Formats {
		LATEX, GNUPLOT
	};

	private List<TableRowColumn> tableRowColumns = new ArrayList<TableRowColumn>();
	private int length;

	public static void main(String[] args) {
		int tablesize = 10;
		int nrOfMonitors = 10;
		Table t = new Table();
//		ITable st = (ITable) MonProxyFactory.monitor(new Table());
//		Table t = (Table)t;
			MonitorFactory.getMonitor(new MyMonKey("aa","aa"));
			MonitorFactory.getTimeMonitor("ss");
		
		Random r = new Random();
		for (int i = 0; i < tablesize; i++) {
			Monitor[] m = new Monitor[nrOfMonitors];
			for (int a = 0; a < nrOfMonitors; a++) {
				m[a] = JamonMonitorLogger.getStatisticMonitor("test" + i + "" + a,
						(a==0)?"":JamonMonitorLogger.PERCENTAGE);
				for (int b = 0; b < 2; b++) {
					m[a].add(r.nextDouble());

				}
//				System.out.println("avg: " + m[a].getAvg());
			}
			TableRowColumn trc = new TableRowColumn(m, "entry_" + i);
			trc.deleteAll();
			trc.useStdDev=false;
			t.addTableRowColumn(trc);
		}
		
//		System.out.println(t.getLatexAsColumn(true));
//		System.out.println(t.getLatexAsRows());
//		System.out.println(t.getGnuPlotAsColumn(true));
//		System.out.println(t.getGnuPlotAsRows());
//		System.out.println(MonProxyFactory.);
		System.out.println( MonitorFactory.getReport());
		JamonMonitorLogger.writeHTMLReport("log/tiger.html");
	}

	public void addTableRowColumn(TableRowColumn trc) {
		try{
			trc.toLatexRow();
		}catch (NullPointerException e) {
			logger.error("TableRowColumn was not initialized, ignoring it, label: "+trc.label);
			e.printStackTrace();
		}
		
		if (tableRowColumns.isEmpty()) {
			length = trc.size();
		}
		
		if (trc.size() != length) {
			
			logger.error("Added TableRowColumn does not match previous set length (" + length + ") but has size "
					+ trc.size() + "), \nignoring it, label: "+trc.label+", value: " + trc.toLatexRow());
			
			
		}
		tableRowColumns.add(trc);
	}

	public String getLatexAsRows() {
		return getAsRows(Formats.LATEX);
	}

	public String getLatexAsColumn() {
		return getLatexAsColumn(false);
	}
	public String getLatexAsColumn(boolean addNumbersInFront) {
		return getAsColums(Formats.LATEX, addNumbersInFront);
	}

	public String getGnuPlotAsRows() {
		return getAsRows(Formats.GNUPLOT);
	}

	public String getGnuPlotAsColumn() {
		return getGnuPlotAsColumn(false);
	}
	public String getGnuPlotAsColumn(boolean addNumbersInFront) {
		return getAsColums(Formats.GNUPLOT, addNumbersInFront);
	}

	private String getAsColums(Formats f, boolean addNumbersInFront) {
		String[] rows = new String[length+1];
		for (int i = 0; i < length+1; i++) {
			rows[i]="";
		}
		
		for (int a = 0; a < tableRowColumns.size(); a++) {
			TableRowColumn trc = tableRowColumns.get(a);
			boolean firstColumn = (a==0);
			for (int i = 1; i < length+1; i++) {
				boolean last = (a + 1 == tableRowColumns.size());
				boolean first = (i==1);
				switch (f) {
					case LATEX:
						rows[0] += (first?trc.label+TableRowColumn.latexSep:"");
						rows[i] += (firstColumn&&addNumbersInFront?i+TableRowColumn.latexSep:"");
						rows[i] += trc.getLatexEntry(i-1)
								+ ((last) ? TableRowColumn.latexSep : TableRowColumn.latexSep);
						break;
					case GNUPLOT:
						rows[0] += (first?"#"+trc.label+"\t":"");
						rows[i] += (firstColumn&&addNumbersInFront?i+"\t":"");
						rows[i] += trc.getGnuPlotEntry(i-1) + ((last) ?""  : "\t");
						break;
					}
			}
		}
		String ret = "";
		for (int i = 0; i < length+1; i++) {
			ret += rows[i]+"\n";
		}
		
		return (replaceCommaByPoints)?ret.replace(",","."):ret;
	}

	private String getAsRows(Formats f) {
		String ret = "";
		for (TableRowColumn trc : tableRowColumns) {
			switch (f) {
			case LATEX:
				ret += trc.toLatexRow()+"\n";
				break;
			case GNUPLOT:
				ret += trc.toGnuPlotRow()+"\n";
				break;
			default:
			}
		}
		return (replaceCommaByPoints)?ret.replace(",","."):ret;
	}

}
