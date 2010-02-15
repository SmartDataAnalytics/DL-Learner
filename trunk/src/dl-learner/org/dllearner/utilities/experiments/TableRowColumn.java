package org.dllearner.utilities.experiments;

import java.text.DecimalFormat;

import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class TableRowColumn {

	enum Formats {
		LATEX, GNUPLOT
	}

	public static String latexSep = "\t&\t";
	public static String latexEnd = "\\\\";

	String label = "";
	
	Monitor[] monitors;
	boolean useStdDev = false;
	
	DecimalFormat dfGnuPlotDefault = new DecimalFormat("#######.######");
	
//	DecimalFormat dfStdDevLatex = new DecimalFormat("##.##%");
	DecimalFormat dfLatexDefault = new DecimalFormat("####.####");
	DecimalFormat dfPercentage = new DecimalFormat("##.##%");

	// public TableRowColumn(Monitor[] monitors){
	// this.monitors = monitors;
	// }
	public TableRowColumn(Monitor[] monitors, String label) {
		this.monitors = monitors;
		this.label = label;
	}

	
	
	public static void main(String[] args) {
		// double d = 0.05d;
		// System.out.println(new TableRowColumn().dfStdDevLatex.format(d));
	}

	public void deleteAll(){
		for (int i = 0; i < monitors.length; i++) {
			MonitorFactory.remove(monitors[i].getMonKey());
		}
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

	public String toGnuPlotRow() {
		return toRow(Formats.GNUPLOT);
	}

	private String toRow(Formats f) {
		String ret = label;
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
	
	private String latexFormat(Monitor m, double value){
		if(m.getUnits().equals(JamonMonitorLogger.PERCENTAGE)){
			return dfPercentage.format(value);
		}else{
			return dfLatexDefault.format(value);
		}
	}
}
