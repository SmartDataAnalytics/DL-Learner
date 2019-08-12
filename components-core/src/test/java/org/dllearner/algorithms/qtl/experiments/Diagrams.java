/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.JavaPlot.Key;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import org.dllearner.learningproblems.Heuristics.HeuristicType;

/**
 * @author Lorenz Buehmann
 *
 */
public class Diagrams {
	
	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		dir.mkdirs();
		
		Properties config = new Properties();
		config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/qtl-eval-config.properties"));

		String url = config.getProperty("url");
		String username = config.getProperty("username");
		String password = config.getProperty("password");
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		
//		url = "jdbc:mysql://address=(protocol=tcp)(host=[2001:638:902:2010:0:168:35:138])(port=3306)(user=root)/qtl";
		
		Connection conn = DriverManager.getConnection(url, username, password);
		
		int[] nrOfExamplesIntervals = {
				5,
				10,
//				15,
				20, 
//				25,
				30
				}; 
		
		double[] noiseIntervals = {
				0.0,
				0.1,
				0.2,
				0.3,
//				0.4,
//				0.6
				};
		
		Map<HeuristicType, String> measure2ColumnName = Maps.newHashMap();
		measure2ColumnName.put(HeuristicType.FMEASURE, "avg_fscore_best_returned");
		measure2ColumnName.put(HeuristicType.PRED_ACC, "avg_predacc_best_returned");
		measure2ColumnName.put(HeuristicType.MATTHEWS_CORRELATION, "avg_mathcorr_best_returned");
		
		HeuristicType[] measures = {
				HeuristicType.PRED_ACC, 
				HeuristicType.FMEASURE, 
				HeuristicType.MATTHEWS_CORRELATION};
		
		String[] labels = {
				"A_1", 
				"F_1", 
				"MCC"};
		
		// get distinct noise intervals
		
		// |E| vs fscore
		String sql = "SELECT nrOfExamples,%s from eval_overall WHERE heuristic_measure = ? && noise = ? ORDER BY nrOfExamples";
		PreparedStatement ps;
		for (double noise : noiseIntervals) {
			String s = "";
			s += "\t";
			s += Joiner.on("\t").join(Ints.asList(nrOfExamplesIntervals));
			s += "\n";
			for (HeuristicType measure : measures) {
				ps = conn.prepareStatement(String.format(sql, measure2ColumnName.get(measure)));
				ps.setString(1, measure.toString());
				ps.setDouble(2, noise);
				ResultSet rs = ps.executeQuery();
				 s+= measure;
				while(rs.next()) {
					int nrOfExamples = rs.getInt(1);
					double avgFscore = rs.getDouble(2);
					s += "\t" + avgFscore;
				}
				 s += "\n";
			}
			org.dllearner.utilities.Files.writeToFile(s, new File(dir, "examplesVsScore-" + noise + ".tsv"));
		}
		
		// noise vs fscore
		sql = "SELECT noise,%s from eval_overall WHERE heuristic_measure = ? && nrOfExamples = ?";
		
		NavigableMap<Integer, Map<HeuristicType, double[][]>> input = new TreeMap<>();
		for (int nrOfExamples : nrOfExamplesIntervals) {
			String s = "";
			s += "\t";
			s += Joiner.on("\t").join(Doubles.asList(noiseIntervals));
			s += "\n";
			
			String gnuplot = "";
			
			// F-score
			ps = conn.prepareStatement(
					"SELECT noise,avg_fscore_best_returned from eval_overall WHERE heuristic_measure = 'FMEASURE' && nrOfExamples = ?");
			ps.setInt(1, nrOfExamples);
			ResultSet rs = ps.executeQuery();
			gnuplot += "\"F_1\"\n";
			while (rs.next()) {
				double noise = rs.getDouble(1);
				double avgFscore = rs.getDouble(2);
				gnuplot += noise + "," + avgFscore + "\n";
			}
			
			// precision
			gnuplot += "\n\n";
			ps = conn.prepareStatement(
					"SELECT noise,avg_precision_best_returned from eval_overall WHERE heuristic_measure = 'FMEASURE' && nrOfExamples = ?");
			ps.setInt(1, nrOfExamples);
			rs = ps.executeQuery();
			gnuplot += "\"precision\"\n";
			while (rs.next()) {
				double noise = rs.getDouble(1);
				double avgFscore = rs.getDouble(2);
				gnuplot += noise + "," + avgFscore + "\n";
			}

			// recall
			gnuplot += "\n\n";
			ps = conn.prepareStatement(
					"SELECT noise,avg_recall_best_returned from eval_overall WHERE heuristic_measure = 'FMEASURE' && nrOfExamples = ?");
			ps.setInt(1, nrOfExamples);
			rs = ps.executeQuery();
			gnuplot += "\"recall\"\n";
			while (rs.next()) {
				double noise = rs.getDouble(1);
				double avgFscore = rs.getDouble(2);
				gnuplot += noise + "," + avgFscore + "\n";
			}
			
			// MCC
			gnuplot += "\n\n";
			ps = conn.prepareStatement(
					"SELECT noise,avg_mathcorr_best_returned from eval_overall WHERE heuristic_measure = 'MATTHEWS_CORRELATION' && nrOfExamples = ?");
			ps.setInt(1, nrOfExamples);
			rs = ps.executeQuery();
			gnuplot += "\"MCC\"\n";
			while (rs.next()) {
				double noise = rs.getDouble(1);
				double avgFscore = rs.getDouble(2);
				gnuplot += noise + "," + avgFscore + "\n";
			}
		
			
			// baseline F-score
			gnuplot += "\n\n";
			ps = conn.prepareStatement("SELECT noise,avg_fscore_baseline from eval_overall WHERE heuristic_measure = 'FMEASURE' && nrOfExamples = ?");
			ps.setInt(1, nrOfExamples);
			rs = ps.executeQuery();
			gnuplot += "\"baseline F_1\"\n";
			while(rs.next()) {
				double noise = rs.getDouble(1);
				double avgFscore = rs.getDouble(2);
				gnuplot += noise + "," + avgFscore + "\n";
			}
			
			// baseline MCC
			gnuplot += "\n\n";
			ps = conn.prepareStatement(
					"SELECT noise,avg_mathcorr_baseline from eval_overall WHERE heuristic_measure = 'MATTHEWS_CORRELATION' && nrOfExamples = ?");
			ps.setInt(1, nrOfExamples);
			rs = ps.executeQuery();
			gnuplot += "\"baseline MCC\"\n";
			while (rs.next()) {
				double noise = rs.getDouble(1);
				double avgFscore = rs.getDouble(2);
				gnuplot += noise + "," + avgFscore + "\n";
			}
			
			Files.write(gnuplot.trim(), new File(dir, "noiseVsScore-" + nrOfExamples + ".dat"), Charsets.UTF_8);
		}
		if(!input.isEmpty()) {
//			plotNoiseVsFscore(input);
		}
		
	}
	
	public static void plotNoiseVsFscore(NavigableMap<Integer, Map<HeuristicType, double[][]>> input) {
		JavaPlot p = new JavaPlot();
		p.set("xlabel", "'Noise'");
		p.set("ylabel", "'Objective Function'");
		p.set("xtics", "0,.1,.4");
		p.set("ytics", "0,.2,1");
		p.set("xrange", "[0:.4]");
        
        // last element 
        Entry<Integer, Map<HeuristicType, double[][]>> lastEntry = input.lastEntry();
        
        
		for (Entry<Integer, Map<HeuristicType, double[][]>> entry : input.entrySet()) {
			Integer nrOfExamples = entry.getKey();
			
			Map<HeuristicType, double[][]> h2data = entry.getValue();
			int pointStyle = 5;
			for (Entry<HeuristicType, double[][]> entry2 : h2data.entrySet()) {
				HeuristicType heuristic = entry2.getKey();
				double[][] data = entry2.getValue();
				
				PlotStyle myPlotStyle = new PlotStyle();
		        myPlotStyle.setStyle(Style.LINESPOINTS);
		        myPlotStyle.setLineWidth(1);
		        myPlotStyle.setPointType(pointStyle);
		        pointStyle += 2;
		        
				DataSetPlot s = new DataSetPlot(data);
				s.setPlotStyle(myPlotStyle);
				s.setTitle(heuristic.name());
				p.addPlot(s);
				
			}
			
			if(entry.equals(lastEntry)) {
				p.setKey(Key.OUTSIDE);
			} else {
				p.setKey(Key.OFF);
			}
			p.setTitle(nrOfExamples.toString());
			p.newGraph();
		}
		p.newGraph();
       
        p.plot();
	}

}
