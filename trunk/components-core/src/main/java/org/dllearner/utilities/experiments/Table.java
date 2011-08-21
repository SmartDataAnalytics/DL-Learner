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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;

import com.jamonapi.Monitor;

public class Table implements Serializable{
	private static final long serialVersionUID = -7191672899557577952L;

	private static final Logger logger = Logger.getLogger(Table.class);

	boolean replaceCommaByPoints = true;
	
	enum Formats {
		LATEX, GNUPLOT
	};
	
	private SortedSet<String> experimentNames = new TreeSet<String>();
	private SortedSet<String> labels = new TreeSet<String>();
	
	private List<TableRowColumn> tableRowColumns = new ArrayList<TableRowColumn>();
	private int length;

	public static void main(String[] args) {
		int tablesize = 10;
		int nrOfMonitors = 10;
		Table t = new Table();
		
		Random r = new Random();
		for (int i = 0; i < tablesize; i++) {
			Monitor[] m = new Monitor[nrOfMonitors];
			for (int a = 0; a < nrOfMonitors; a++) {
				m[a] = JamonMonitorLogger.getStatisticMonitor("test" + i + "" + a,
						(a==0)?"":JamonMonitorLogger.PERCENTAGE);
				for (int b = 0; b < 2; b++) {
					m[a].add(r.nextDouble());

				}
			}
			TableRowColumn trc = new TableRowColumn(m, "Test","entry_" + i);
			trc.useStdDev=false;
			t.addTableRowColumn(trc);
		}
		
		for (int i = 0; i < tablesize; i++) {
			Monitor[] m = new Monitor[nrOfMonitors];
			for (int a = 0; a < nrOfMonitors; a++) {
				m[a] = JamonMonitorLogger.getStatisticMonitor("test" + i + "" + a,
						(a==0)?"":JamonMonitorLogger.PERCENTAGE);
				for (int b = 0; b < 2; b++) {
					m[a].add(r.nextDouble());
					
				}
			}
			TableRowColumn trc = new TableRowColumn(m, "Whatever","bentry_" + i);
			trc.useStdDev=false;
			t.addTableRowColumn(trc);
		}
		
		
		System.out.println(t.getLatexAsColumn(true));
		t.sortByLabel();
		System.out.println(t.getLatexAsColumn(true));
		t.sortByExperimentName();
		System.out.println(t.getGnuPlotAsColumn(true));
//		System.out.println(t.getLatexAsRows());
//		System.out.println(t.getGnuPlotAsColumn(true));
//		System.out.println(t.getGnuPlotAsRows());
//		System.out.println(MonProxyFactory.);
//		System.out.println( MonitorFactory.getReport());
//		JamonMonitorLogger.writeHTMLReport("log/tiger.html");
	}

	
	
	/**
	 * passes each it TableRowColumn one by one to addTableRowColumn
	 * @param t
	 */
	public void addTable(Table t){
		for (TableRowColumn  trc : t.tableRowColumns) {
			addTableRowColumn(trc);
		}
	}
	
	/**
	 * passes it one by one to addTableRowColumn
	 * @param trcs
	 */
	public void addTableRowColumns(List<TableRowColumn> trcs) {
		for (TableRowColumn tableRowColumn : trcs) {
			addTableRowColumn(tableRowColumn);
		}
	}
	
	public void addTableRowColumn(TableRowColumn trc) {
		labels.add(trc.getLabel());
		experimentNames.add(trc.getExperimentName());
		try{
			trc.toLatexRow();
		}catch (NullPointerException e) {
			logger.error("TableRowColumn was not initialized, ignoring it: "+trc);
			e.printStackTrace();
		}
		
		if (tableRowColumns.isEmpty()) {
			length = trc.size();
		}
		
		if (trc.size() != length) {
			
			logger.error("Added TableRowColumn does not match previous set length (" + length + ") but has size "
					+ trc.size() + "), \nignoring it: "+trc);
			
			
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
			String header = trc.getExperimentName()+" "+trc.getLabel();
			boolean firstColumn = (a==0);
			boolean lastColumn = (a + 1 == tableRowColumns.size());
			for (int i = 1; i < length+1; i++) {
				
				boolean firstRow = (i==1);
				switch (f) {
					case LATEX:
						rows[0] += ((firstColumn&&firstRow&&addNumbersInFront)?TableRowColumn.latexSep:"");
						rows[0] += (firstRow?header+TableRowColumn.latexSep:"");
						rows[i] += ((firstColumn&&addNumbersInFront)?i+TableRowColumn.latexSep:"");
						rows[i] += trc.getLatexEntry(i-1)+ ((lastColumn) ? TableRowColumn.latexSep : TableRowColumn.latexSep);
						break;
					case GNUPLOT:
						rows[0] += (firstRow?"#"+header+"\t":"");
						rows[i] += (firstColumn&&addNumbersInFront?i+"\t":"");
						rows[i] += trc.getGnuPlotEntry(i-1) + ((lastColumn) ?""  : "\t");
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
	
	public void sortByExperimentName(){
		_sortByLabel();
		_sortByExperimentName();
		
	}
	public void sortByLabel(){
		_sortByExperimentName();
		_sortByLabel();
	}
	
	private void _sortByLabel(){
		List<String> l = new ArrayList<String>(labels);
		List<TableRowColumn> newTrc = new ArrayList<TableRowColumn>();
		
		for (String s : l) {
			for (TableRowColumn trc : tableRowColumns) {
				if(trc.getLabel().startsWith(s) && trc.getLabel().contains(s)){
					newTrc.add(trc);
				}
			}
		}
		tableRowColumns = newTrc;
	}
	private void _sortByExperimentName(){
		List<String> l = new ArrayList<String>(experimentNames);
		List<TableRowColumn> newTrc = new ArrayList<TableRowColumn>();
		
		for (String s : l) {
			for (TableRowColumn trc : tableRowColumns) {
				if(trc.getExperimentName().equals(s)){
					newTrc.add(trc);
				}
			}
		}
		tableRowColumns = newTrc;
	}
	
	/**
	 * Writes all possible outputs to the given folder, i.e. 
	 * GNu plot rows and Columns and Latex tables
	 * @param folder
	 * @param fileprefix
	 */
	public void write(String folder, String fileprefix){
		logger.info("Writing results to "+folder+fileprefix);
		Files.mkdir(folder);
		Files.createFile(new File(folder+fileprefix+"_GNU_ROWS"), getGnuPlotAsRows());
		Files.createFile(new File(folder+fileprefix+"_GNU_COLUMNS_I"), getGnuPlotAsColumn(true));
		Files.createFile(new File(folder+fileprefix+"_GNU_COLUMNS"), getGnuPlotAsColumn());
		
		
		Files.createFile(new File(folder+fileprefix+"_LATEX_ROWS"), getLatexAsRows());
		Files.createFile(new File(folder+fileprefix+"_LATEX_COLUMNS"), getLatexAsColumn());
		Files.createFile(new File(folder+fileprefix+"_LATEX_COLUMNS_I"), getLatexAsColumn(true));
	
		serialize(folder+fileprefix+".ser");
		
	}
	
	public void serialize(String filename){
		Files.writeObjectToFile(this, new File(filename));
	}
	public static Table deserialize(String filename){
		return (Table)Files.readObjectfromFile(new File(filename));
	}

}
