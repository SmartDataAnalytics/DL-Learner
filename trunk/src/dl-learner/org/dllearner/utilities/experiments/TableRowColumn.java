package org.dllearner.utilities.experiments;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.Monitor;

public class TableRowColumn implements Serializable{
	private static final long serialVersionUID = 1252924374566004540L;

	enum Formats {
		LATEX, GNUPLOT
	}

	public static String latexSep = "\t&\t";
	public static String latexEnd = "\\\\";

	private final String label ;
	private final String experimentName;
	
//	final Monitor[] monitors;
	final FinalizedMonitor[] monitors;
	boolean useStdDev = false;
	
	boolean hits_instead_of_average = false;
	
	DecimalFormat dfGnuPlotDefault = new DecimalFormat("######0.00####");
	
//	DecimalFormat dfStdDevLatex = new DecimalFormat("##.##%");
	DecimalFormat dfLatexDefault = new DecimalFormat("####.####");
	DecimalFormat dfPercentage = new DecimalFormat("##.##%");

	// public TableRowColumn(Monitor[] monitors){
	// this.monitors = monitors;
	// }
	public TableRowColumn(Monitor[] monitors,  String experimentName, String label) {
		this.monitors = new FinalizedMonitor[monitors.length];
		for (int i = 0; i < monitors.length; i++) {
			this.monitors[i] = new FinalizedMonitor(monitors[i]);
		}
		this.label = label;
		this.experimentName = experimentName;
	}

	
	
	public static void main(String[] args) {
		// double d = 0.05d;
		// System.out.println(new TableRowColumn().dfStdDevLatex.format(d));
	}

	public void deleteAll(){
		for (int i = 0; i < monitors.length; i++) {
//			MonitorFactory.remove(monitors[i].getMonKey());
		}
	}
	
	@Override
	public String toString(){
		return experimentName+" "+label+" "+toGnuPlotRow();
	}
	
	public int size() {
		return monitors.length;
	}

	public String toLatexRow() {
		return toRow(Formats.LATEX);
	}

	public String getLabel() {
		return label;
	}
	
	public String getExperimentName() {
		return experimentName;
	}

	public String toGnuPlotRow() {
		return toRow(Formats.GNUPLOT);
	}

	private String toRow(Formats f) {
		String ret = experimentName+ " "+ label;
		for (int i = 0; i < monitors.length; i++) {
			boolean last = (i + 1 == monitors.length);
			switch (f) {
			case LATEX:
				ret += latexSep + getLatexEntry(i) + (last?latexEnd:"");
				break;
			case GNUPLOT:
				ret += "\t" + getGnuPlotEntry(i);
				break;
			default:
				break;
			}
		}
//		System.out.println(ret);
		return ret;

	}

	public String toGnuPlotRow(int rowNumber) {
		return rowNumber + "\t" + toGnuPlotRow();
	}

	public String getLatexEntry(int i) {
		return latexFormat(monitors[i], monitors[i].getAvg()) + " "+ (useStdDev ? "(\\pm"+latexFormat(monitors[i], monitors[i].getStdDev()) + ") " : "");
	}

	public String getGnuPlotEntry(int i) {
		return dfGnuPlotDefault.format(monitors[i].getAvg()) + "";
	}
	
	private String latexFormat(FinalizedMonitor monitors, double value){
		if(monitors.getUnits().equals(JamonMonitorLogger.PERCENTAGE)){
			return dfPercentage.format(value);
		}else{
			return dfLatexDefault.format(value);
		}
	}
}
