package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TrainAndTestSet {
	
	private File pdbproteins;
	private String[] trainset;
	private String[] testset;
	private HashMap setentries;
	private ArrayList pdbprotlines;
	
	private ArrayList getPdbprotlines() {
		return pdbprotlines;
	}

	private void setPdbprotlines(ArrayList pdbprotlines) {
		this.pdbprotlines = pdbprotlines;
	}

	private HashMap getSetentries() {
		return setentries;
	}

	private void setSetentries(HashMap setentries) {
		this.setentries = setentries;
	}

	private File getPdbproteins() {
		return pdbproteins;
	}

	private void setPdbproteins(File pdbproteins) {
		this.pdbproteins = pdbproteins;
	}

	public String[] getTrainset() {
		return trainset;
	}

	public void setTrainset(String[] trainset) {
		this.trainset = trainset;
	}

	public String[] getTestset() {
		return testset;
	}

	public void setTestset(String[] testset) {
		this.testset = testset;
	}

	public TrainAndTestSet (File file, int setsize) {
		this.setPdbproteins(file);
		int linenr = this.getNumberOfLines();
		// System.out.println("PDB Prot File has "+linenr+" lines." );
		if ((2*setsize) >= linenr) {
			setsize = linenr / 2;
		}
		if (setsize < 0) {
			setsize = 0;
		}
		this.setTrainset(this.create_set(setsize, linenr));
		this.setTestset(this.create_set(setsize, linenr));
	}
	
	private void createArrayList(int linenumber){
		try {
			ArrayList arraylist = new ArrayList();
			LineNumberReader lnr = new LineNumberReader(new FileReader(this.getPdbproteins()));
			for (int i = 0; i < linenumber; i++) {
				String line = lnr.readLine();
				arraylist.add(i, line);
				// System.out.println("Line "+ i +": "+ line);
			}
			this.setPdbprotlines(arraylist);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private String [] create_set(int setsize, int linenr){
		String [] set = new String [setsize];
		if (this.getSetentries() == null) {
			this.setSetentries(new HashMap(2*setsize));
		}
		HashMap setmap = this.getSetentries();
		Random gen = new Random();
		for (int i = 0; i < setsize; i++) {
			int lnr = gen.nextInt(linenr);
			while (setmap.containsKey(Integer.valueOf(lnr))) {
				lnr = gen.nextInt(linenr);
			}
			set[i] = this.getpdbid(lnr);
			setmap.put(Integer.valueOf(lnr), set[i]);
		}
		this.setSetentries(setmap);
		return set;
	}
	
	private int getNumberOfLines () {
		try {
			LineNumberReader lnr = new LineNumberReader(new FileReader(this.getPdbproteins()));
			int count = 0;
			while (lnr.readLine() != null) {
				count++;
			}
			this.createArrayList(count);
			return count;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	

	private String getpdbid (int lineNumber) {
		// Initialize a LineNumberReader
		ArrayList arraylist = this.getPdbprotlines();
		String line =(String) arraylist.get(lineNumber);
		String pdb_id = line.substring(0, 4);
		return pdb_id;
	}
}
