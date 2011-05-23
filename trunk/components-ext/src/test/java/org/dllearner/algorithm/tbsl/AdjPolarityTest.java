package org.dllearner.algorithm.tbsl;

import java.io.BufferedReader;
import java.io.FileReader;

public class AdjPolarityTest {

	public static void main(String[] args) {
		
		String polarity = "POS";
		
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader("src/main/resources/tbsl/lexicon/adj_list.txt"));
			String line;
			while ((line = in.readLine()) != null ) {
				if (line.contains("small")) {
					polarity = line.split(" ")[0];
					break;
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(polarity);

	}

}
