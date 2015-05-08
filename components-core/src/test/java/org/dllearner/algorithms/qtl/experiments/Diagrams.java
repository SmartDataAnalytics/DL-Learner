/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristic;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristicSimple;
import org.dllearner.learningproblems.Heuristics.HeuristicType;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

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
				15,
				20, 
				25,
				30
				}; 
		
		double[] noiseIntervals = {
				0.0,
				0.1,
				0.2,
				0.3,
				0.4,
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
			Files.write(s, new File(dir, "examplesVsFscore-" + noise + ".tsv"), Charsets.UTF_8);
		}
		
		// noise vs fscore
		sql = "SELECT noise,%s from eval_overall WHERE heuristic_measure = ? && nrOfExamples = ?";
		for (int nrOfExamples : nrOfExamplesIntervals) {
			String s = "";
			s += "\t";
			s += Joiner.on("\t").join(Doubles.asList(noiseIntervals));
			s += "\n";
			for (HeuristicType measure : measures) {
				ps = conn.prepareStatement(String.format(sql, measure2ColumnName.get(measure)));
				ps.setString(1, measure.toString());
				ps.setInt(2, nrOfExamples);
				ResultSet rs = ps.executeQuery();
				 s+= measure;
				while(rs.next()) {
					double noise = rs.getDouble(1);
					double avgFscore = rs.getDouble(2);
					s += "\t" + avgFscore;
				}
				 s += "\n";
			}
			Files.write(s, new File(dir, "noiseVsFscore-" + nrOfExamples + ".tsv"), Charsets.UTF_8);
		}
		
	}

}
