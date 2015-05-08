/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristic;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristicSimple;
import org.dllearner.learningproblems.Heuristics.HeuristicType;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

/**
 * @author Lorenz Buehmann
 *
 */
public class Diagrams {
	
	public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/qtl-eval-config.properties"));

		String url = config.getProperty("url");
		String username = config.getProperty("username");
		String password = config.getProperty("password");
		Class.forName("com.mysql.jdbc.Driver").newInstance();
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
		
		HeuristicType[] measures = {
				HeuristicType.PRED_ACC, 
				HeuristicType.FMEASURE, 
				HeuristicType.MATTHEWS_CORRELATION};
		
		File dir = new File("/tmp/qtl/");
		dir.mkdirs();
		
		// |E| vs fscore
		PreparedStatement ps = conn.prepareStatement("SELECT nrOfExamples,avg_fscore_best_returned from eval_overall WHERE heuristic_measure = ? && noise = ? ORDER BY nrOfExamples");
		for (double noise : noiseIntervals) {
			String s = "";
			s += "\t";
			s += Joiner.on("\t").join(Ints.asList(nrOfExamplesIntervals));
			s += "\n";
			for (HeuristicType measure : measures) {
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
		ps = conn.prepareStatement("SELECT noise,avg_fscore_best_returned from eval_overall WHERE heuristic_measure = ? && nrOfExamples = ?");
		for (int nrOfExamples : nrOfExamplesIntervals) {
			String s = "";
			s += "\t";
			s += Joiner.on("\t").join(Doubles.asList(noiseIntervals));
			s += "\n";
			for (HeuristicType measure : measures) {
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
