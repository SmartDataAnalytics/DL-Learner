package org.dllearner.examples.pdb;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TrainAndTestSet {
	
	private String[] trainset;
	private String[] testset;
	private HashMap<Integer,String> setentries;
	private ArrayList<String> pdbprotlines;
	
	public String[] getTrainset() {
		return trainset;
	}

	public String[] getTestset() {
		return testset;
	}


	public TrainAndTestSet (int setsize) {
		
		// we read in the online file with all PDB-entries
		URL pdbEntryType;
		try {
			pdbEntryType = new URL("ftp://ftp.wwpdb.org/pub/pdb/derived_data/pdb_entry_type.txt");
			LineNumberReader pdbproteins = new LineNumberReader(new InputStreamReader(pdbEntryType.openStream()));
		
			// we calculate the number of lines in that file and
			// read all lines into the global variable pdbprotlines 
			int linenr = this.getNumberOfLines(pdbproteins);
			pdbproteins.close();

		
			// System.out.println("PDB Prot File has "+linenr+" lines." );
			
			// handling of incorrect setsize values
			if ((2*setsize) >= linenr) {
				setsize = linenr / 2;
			}
			if (setsize < 0) {
				setsize = 0;
			}
			
			// lets create Train- and Testset
			this.trainset = this.create_set(setsize, linenr);
			this.testset = this.create_set(setsize, linenr);
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// this method counts the number of lines in the read in file and
	// fills pdbprotlines with content
	private int getNumberOfLines (LineNumberReader lnr) {
		try {
			int count = 0;
			ArrayList<String> arraylist = new ArrayList<String>();
			String line;

			
			while ((line = lnr.readLine()) != null) {
				arraylist.add(count, line);
				count++;
			}
			this.pdbprotlines = arraylist;

			return count;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
/*	private void createArrayList(int linenumber){
		try {
			ArrayList<String> arraylist = new ArrayList<String>();
			LineNumberReader lnr = new LineNumberReader(new FileReader(this.pdbproteins));
			for (int i = 0; i < linenumber; i++) {
				String line = lnr.readLine();
				arraylist.add(i, line);
				// System.out.println("Line "+ i +": "+ line);
			}
			this.pdbprotlines = arraylist;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	*/
	
	//creates Sets of PDB IDs equal to setsize
	private String [] create_set(int setsize, int linenr){
		String [] set = new String [setsize];
		if (this.setentries == null) {
			this.setentries = new HashMap<Integer,String>(2*setsize);
		}
		HashMap<Integer,String> setmap = this.setentries;
		Random gen = new Random();
		for (int i = 0; i < setsize; i++) {
			int lnr = gen.nextInt(linenr);
			while (setmap.containsKey(Integer.valueOf(lnr))) {
				lnr = gen.nextInt(linenr);
			}
			set[i] = this.getpdbid(lnr);
			setmap.put(Integer.valueOf(lnr), set[i]);
		}
		this.setentries = setmap;
		return set;
	}
	
	
	private String getpdbid (int lineNumber) {
		// Initialize a LineNumberReader
		ArrayList<String> arraylist = pdbprotlines;
		String line =(String) arraylist.get(lineNumber);
		String pdb_id = line.substring(0, 4);
		return pdb_id;
	}
}
