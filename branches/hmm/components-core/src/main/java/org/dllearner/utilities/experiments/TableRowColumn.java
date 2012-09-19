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

import java.io.Serializable;
import java.text.DecimalFormat;

import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.Monitor;

public class TableRowColumn implements Serializable{
	private static final long serialVersionUID = 1252924374566004540L;

	enum Formats {
		LATEX, GNUPLOT
	}
	
	public enum Display {
		AVG, HITS, TOTAL
	}
	
	public static boolean useStdDevWithPercentageUnit = true;

	public static String latexSep = "\t&\t";
	public static String latexEnd = "\\\\";

	private final String label ;
	private final String experimentName;
	
//	final Monitor[] monitors;
	final FinalizedMonitor[] monitors;
	boolean useStdDev = false;
	
	Display display = Display.AVG;
	
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
		return latexFormat(monitors[i], getValue(i)) + " "+getLatexStdDev(i) ;
	}
	private String getLatexStdDev(int i){
		String tex = "(\\pm"+latexFormat(monitors[i], monitors[i].getStdDev()) + ") ";
		if(useStdDev){
			return tex;
		}
		
		if(useStdDevWithPercentageUnit && monitors[i].getUnits().equals(JamonMonitorLogger.PERCENTAGE)){
			return tex;
		}
	
		return "";
		
	}

	public String getGnuPlotEntry(int i) {
		return dfGnuPlotDefault.format(getValue(i)) + "";
	}
	
	public void setDisplay(Display d){
		display = d;
	}
	
	private double getValue(int i){
//		return monitors[i].getAvg();
		switch(display){
			case AVG: return monitors[i].getAvg();
			case HITS: return monitors[i].getHits();
			case TOTAL: return monitors[i].getTotal();
		}
		return monitors[i].getAvg();
	}
	
	private String latexFormat(FinalizedMonitor monitors, double value){
		if(monitors.getUnits().equals(JamonMonitorLogger.PERCENTAGE)){
			return dfPercentage.format(value).replace("%", "\\%").replace("_", "\\_");
		}else{
			return dfLatexDefault.format(value);
		}
	}
}
