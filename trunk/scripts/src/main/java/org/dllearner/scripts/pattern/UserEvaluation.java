/**
 * 
 */
package org.dllearner.scripts.pattern;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coode.owlapi.latex.LatexWriter;
import org.dllearner.utilities.statistics.FleissKappa;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;


/**
 * @author Lorenz Buehmann
 *
 */
public class UserEvaluation {
	
	private static List<Integer> categories = Lists.newArrayList(1, 2, 0);
	private static DecimalFormat format = new DecimalFormat("#0.0");
	
	
	
	public static void main(String[] args) throws Exception{
		new File("pattern-threshold").mkdir();
		File mainDir = new File(args[0]);
		
		Map<File, short[][]> ratings = getRatings(mainDir);
		short[][] totalRating = new short[0][];
		Map<Double, Map<String, List<Integer>>> allIntervals = getAllIntervals();
		Map<String, short[][]> axiomType2Rating = new HashMap<String, short[][]>();
		Map<String, Float> axiomType2Kappa = new HashMap<String, Float>();
		for (Entry<File, short[][]> entry : ratings.entrySet()) {
			Map<Double, List<Integer>> intervals = getIntervals();
			StringBuilder sb = new StringBuilder();
			File axiomFile = entry.getKey();
			String axiomType = axiomFile.getName().replace("-instantiations-sample.csv", "").replace("_", " ").replace("Kopie von ","").replace("-", "");
			short[][] rating = entry.getValue();
			for (int entryId = 0; entryId < rating.length; entryId++) {
				short[] categories = rating[entryId];
				double score = getScore(axiomFile, entryId);
				//correct if there is more than 2 times 1 as rating
				int val;
				if(categories[1] >= 2){
					val = 1;
				} else {//incorrect
					val = 0;
				}
				add(intervals, score, val);
				sb.append(score + "," + val + "\n");
			}
//			printMatrix(rating);
//			Files.write(sb.toString(), new File("pattern-threshold/" + axiomFile.getName().replace("Kopie von ", "")), Charsets.UTF_8);
			float kappa = FleissKappa.computeKappa(rating);
			axiomType2Kappa.put(axiomType, kappa);
//			System.out.println("<tr><td>" + axiomType + "</td><td align=\"right\">" + rating.length + "</td><td align=\"right\">" + Math.round(kappa * 100)/100d + "</td></tr>");
//			System.out.println(axiomType + ":" + Math.round(kappa * 100)/100d);
//			System.out.println("Axioms:" + rating.length);
			totalRating = concat(totalRating, rating);
			printThresholdResults(intervals, axiomFile.getName().replace("Kopie von ", ""));
			add(allIntervals, intervals, axiomType);
			axiomType2Rating.put(axiomType, rating);
		}
		printThresholdResults(allIntervals);
		printUserResults(axiomType2Rating ,axiomType2Kappa);
			
			
//			printMatrix(mat);
			float kappa = FleissKappa.computeKappa(totalRating);
			System.out.println("Total kappa: " + kappa);
		
	}
	
	private static void printUserResults(Map<String, short[][]> axiomType2Rating, Map<String, Float> axiomType2Kappa){
		StringWriter sw = new StringWriter();
		LatexWriter w = new LatexWriter(sw);
		LatexObjectVisitor renderer = new LatexObjectVisitor(w, new OWLDataFactoryImpl());
		
		StringBuilder sb = new StringBuilder();
		
		short[][] totalRating = new short[0][];
		for (Entry<String, short[][]> entry : axiomType2Rating.entrySet()) {
			String axiomType = entry.getKey();
			short[][] rating = entry.getValue();
			int totalRatings = rating.length * rating[0].length;
			//get for each category the frequency
			Multiset<Integer> multiset = HashMultiset.create();
			for (int i = 0; i < rating.length; i++) {
				for (int j = 0; j < rating[i].length; j++) {
					multiset.add(j,(int)rating[i][j]);
				}
			}
			sb.append("<tr><td>" + axiomType + "</td><td align=\"right\">" + rating.length + "</td>");
			String latex = axiomType + " & " + rating.length;
			for (Integer category : categories) {
				double fraction = (double)multiset.count(category)/totalRatings;
				sb.append("<td align=\"right\">" + format.format(fraction*100) + "</td>");
				latex += " & " + format.format(fraction*100);
			}
			latex += " & " + format.format(axiomType2Kappa.get(axiomType) * 100);
			System.out.println(latex);
			//add kappa column value
			sb.append("<td align=\"right\">" + format.format(axiomType2Kappa.get(axiomType) * 100) + "</td>");
			
			sb.append("</tr>\n");
			totalRating = concat(totalRating, rating);
		}
		//the summarized row
		int totalRatings = totalRating.length * totalRating[0].length;
		Multiset<Integer> multiset = HashMultiset.create();
		for (int i = 0; i < totalRating.length; i++) {
			for (int j = 0; j < totalRating[i].length; j++) {
				multiset.add(j,(int)totalRating[i][j]);
			}
		}
		sb.append("<tr><td>Total</td><td align=\"right\">" + totalRating.length + "</td>");
		for (Integer category : categories) {
			System.err.println(multiset.count(category));
			double fraction = (double)multiset.count(category)/totalRatings;
			sb.append("<td align=\"right\">" + format.format(fraction*100) + "</td>");;
		}
		sb.append("</tr>\n");
		System.out.println(sb.toString());
	}
	
	private static void printThresholdResults(Map<Double, Map<String, List<Integer>>> allIntervals) throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append(",");
		for (Entry<String, List<Integer>> entry : allIntervals.entrySet().iterator().next().getValue().entrySet()) {
			sb.append(entry.getKey() + ",");
		}
		sb.append("\n");
		for (Entry<Double, Map<String, List<Integer>>> entry : allIntervals.entrySet()) {
			Double minAccuracy = entry.getKey();
			sb.append(minAccuracy + ",");
			Map<String, List<Integer>> axiomType2Values = entry.getValue();
			double sum = 0;
			int total = 0;
			for (Entry<String, List<Integer>> entry2 : axiomType2Values.entrySet()) {
				String axiomType = entry2.getKey();
				List<Integer> values = entry2.getValue();
				int posCnt = 0;
				int negCnt = 0;
				for (Integer v : values) {
					if(v == 1){
						posCnt++;
					} 
				}
				if(values.size() < 5){
					sb.append(",");
				} else {
					double percentage = (double)posCnt/values.size();
					sum += percentage;
					total++;
					sb.append(percentage + ",");
				}
			}
			System.err.println(minAccuracy + "," + sum/total);
			sb.append("\n");
		}
		Files.write(sb.toString(), new File("pattern-threshold/all.csv"), Charsets.UTF_8);
		
		sb = new StringBuilder();
		for (Entry<Double, Map<String, List<Integer>>> entry : allIntervals.entrySet()) {
			Double minAccuracy = entry.getKey();
			sb.append(minAccuracy + ",");
			Map<String, List<Integer>> axiomType2Values = entry.getValue();
			int posCnt = 0;
			int totalCnt = 0;
			for (Entry<String, List<Integer>> entry2 : axiomType2Values.entrySet()) {
				totalCnt++;
				List<Integer> values = entry2.getValue();
				for (Integer v : values) {
					if(v == 1){
						posCnt++;
					} 
				}
			}
			sb.append((double)posCnt/totalCnt + ",");
			sb.append("\n");
		}
		Files.write(sb.toString(), new File("pattern-threshold/aggregated.csv"), Charsets.UTF_8);
	}
	
	private static void printThresholdResults(Map<Double, List<Integer>> intervals, String filename) throws IOException{
		StringBuilder sb = new StringBuilder();
		for (Entry<Double, List<java.lang.Integer>> entry : intervals.entrySet()) {
			Double minAccuracy = entry.getKey();
			List<Integer> values = entry.getValue();
			int posCnt = 0;
			int negCnt = 0;
			for (Integer v : values) {
				if(v == 1){
					posCnt++;
				} else {
					negCnt++;
				}
			}
			if(values.isEmpty()){
				sb.append(minAccuracy + ",0,0\n");
			} else {
				sb.append(minAccuracy + "," + (double)posCnt/values.size() + "," + (double)negCnt/values.size() + "\n");
			}
			
		}
//		System.out.println(sb.toString());
		Files.write(sb.toString(), new File("pattern-threshold/" + filename), Charsets.UTF_8);
	}
	
	private static void add(Map<Double, Map<String, List<Integer>>> allIntervals, Map<Double, List<Integer>> intervals, String axiomType){
		for (Entry<Double, List<Integer>> entry1 : intervals.entrySet()) {
			Double minAccuracy1 = entry1.getKey();
			for (Entry<Double, Map<String,List<Integer>>> entry2 : allIntervals.entrySet()) {
				Double minAccuracy2 = entry2.getKey();
				if(minAccuracy1.equals(minAccuracy2)){
					Map<String,List<Integer>> values = entry2.getValue();
					values.put(axiomType, entry1.getValue());
				}
				
			}
		}
	}
	
	private static void add(Map<Double, List<Integer>> intervals, double accuracy, int value){
		for (Entry<Double, List<java.lang.Integer>> entry : intervals.entrySet()) {
			Double minAccuracy = entry.getKey();
			List<Integer> values = entry.getValue();
			if(accuracy - minAccuracy <= 0.1){
				values.add(value);
				break;
			}
		}
	}
	
	private static Map<Double, Map<String, List<Integer>>> getAllIntervals(){
		Map<Double, Map<String, List<Integer>>> allIntervals = new LinkedHashMap<Double, Map<String,List<Integer>>>();
		for (double i = 0.6; i < 1.0; i+=0.1) {
			allIntervals.put(i, new LinkedHashMap<String, List<Integer>>());
		}
		return allIntervals;
	}
	
	private static Map<Double, List<Integer>> getIntervals(){
		Map<Double, List<Integer>> intervals = new LinkedHashMap<Double, List<Integer>>();
		for (double i = 0.6; i < 1.0; i+=0.1) {
			intervals.put(i, new ArrayList<Integer>());
		}
		return intervals;
	}
	
	private static short[][] concat(short[][] a1, short[][] a2){
		short[][] result = new short[a1.length + a2.length][];

		System.arraycopy(a1, 0, result, 0, a1.length);
		System.arraycopy(a2, 0, result, a1.length, a2.length);
		
		return result;
	}
	
	private static double getScore(File file, int entry) throws IOException{
		List<String> lines = Files.readLines(file, Charsets.UTF_8);
		return Double.parseDouble(lines.get(entry).split(",")[1]);
	}
	
	private static Map<File, short[][]> getRatings(File baseDir) throws IOException {
		Map<File, short[][]> ratings = new HashMap<File, short[][]>();

		// list sub directories
		File[] subDirs = baseDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		
		int nrOfUsers = subDirs.length;

		List<List<Map<File, List<Short>>>> sameFiles = new ArrayList<List<Map<File, List<Short>>>>();
		// for each sub dir
		for (File dir : subDirs) {
			// read csv files
			File[] files = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File file, String name) {System.out.println(name);
					return name.endsWith(".csv");
				}
			});

			List<Map<File, List<Short>>> fileValues = new ArrayList<Map<File,List<Short>>>();
			for (File file : files) {System.out.println(file);
				List<Short> values = getValues(file);
				Map<File, List<Short>> map = new HashMap<File, List<Short>>();
				map.put(file, values);
				fileValues.add(map);
			}
			sameFiles.add(fileValues);

		}

		for (int i = 0; i < 11; i++) {
			System.out.println("########################");
			int nrOfEntries = sameFiles.get(0).get(i).entrySet().iterator().next().getValue().size();
			short[][] mat = new short[nrOfEntries][3];
			File axiomFile = sameFiles.get(0).get(i).keySet().iterator().next();
			for (int entry = 0; entry < nrOfEntries; entry++) {
				Multiset<Short> values = HashMultiset.create();
				for (int userId = 0; userId < nrOfUsers; userId++) {
					List<Map<File, List<Short>>> userData = sameFiles.get(userId);
					List<Short> data = userData.get(i).entrySet().iterator().next().getValue();
					Short value = data.get(entry);
					values.add(value);
				}
				for (short cat = 0; cat < 3; cat++) {
					mat[entry][cat] = (short) values.count(cat);
				}
			}
			ratings.put(axiomFile, mat);
		}

		return ratings;
	}
	
	private static List<Short> getValues(File file) throws IOException{
		List<Short> values = new ArrayList<Short>();
		List<String> lines = Files.readLines(file, Charsets.UTF_8);
		for (String line : lines) {
			values.add(Short.parseShort(line.split(",")[2]));
		}
		return values;
	}
	
	static void printMatrix(short[][] grid) {
	    for(int r=0; r<grid.length; r++) {
	       for(int c=0; c<grid[r].length; c++)
	           System.out.print(grid[r][c] + " ");
	       System.out.println();
	    }
	}
		

}
