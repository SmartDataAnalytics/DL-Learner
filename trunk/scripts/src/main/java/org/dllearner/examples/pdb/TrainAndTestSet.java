package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TrainAndTestSet {
	
	private PdbProtein[] trainset;
	
	public PdbProtein[] getTrainset() {
		return trainset;
	}
	
	
	public TrainAndTestSet () {
		String pdbID = "";
		String chainID = "";
		String species = "";
		PdbProtein[] pdbProteins = {new PdbProtein(pdbID, chainID, species)};
		this.trainset = pdbProteins;
	}
	
	public TrainAndTestSet (String pdbID) {
		PdbProtein[] pdbProteins = {new PdbProtein(pdbID)};
		this.trainset = pdbProteins;
	}
	
	public TrainAndTestSet (String pdbID, String chainID) {
		PdbProtein[] pdbProteins = {new PdbProtein(pdbID, chainID)};
		this.trainset = pdbProteins;
	}
	
	public TrainAndTestSet (String pdbID, String chainID, String species) {
		PdbProtein[] pdbProteins = {new PdbProtein(pdbID, chainID, species)};
		this.trainset = pdbProteins;
	}
	
	public TrainAndTestSet (String[] pdbIDs) {
		PdbProtein pdbProt;
		PdbProtein[] pdbProteins = new PdbProtein[pdbIDs.length];
		for (int i = 0; i < pdbIDs.length; i++ )
		{
			pdbProt =  new PdbProtein(pdbIDs[i]);
			pdbProteins[i] = pdbProt;
		}
		this.trainset = pdbProteins;
	}

	public TrainAndTestSet (int setsize) {
		
		// we read in the online file with all PDB-entries
		URL pdbEntryType;
		try {
			pdbEntryType = new URL("ftp://ftp.wwpdb.org/pub/pdb/derived_data/pdb_entry_type.txt");
			LineNumberReader pdbproteins = new LineNumberReader(new InputStreamReader(pdbEntryType.openStream()));
			// read all lines in lines			
			ArrayList<String> lines = this.readInFile(pdbproteins); 
			pdbproteins.close();
			// get number of lines			
			int linenr = lines.size();
			System.out.println("PDB Prot File has "+linenr+" lines." );
			
			// handling of incorrect setsize values
			if ((2*setsize) >= linenr) {
				setsize = linenr / 2;
			}
			if (setsize < 0) {
				setsize = 0;
			}
			
			// lets create Train- and Testset
			this.trainset = this.createSet(setsize, linenr, lines);		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public TrainAndTestSet (File pdbIDlist) {
		try
		{
			LineNumberReader pdbproteins = new LineNumberReader(new FileReader(pdbIDlist));
			ArrayList<String> lines = this.readInFile(pdbproteins);
			pdbproteins.close();
			// get number of lines			
			int linenr = lines.size();
			System.out.println("File "+ pdbIDlist.getCanonicalPath() + " has " + linenr + " lines.");
			PdbProtein[] proteins = new PdbProtein[linenr];
			for (int i = 0; i < linenr; i++)
			{
				System.out.println("LINES element " + i + " contains " + lines.get(i));
				proteins[i] = new PdbProtein(getPdbID(i, lines), getChainID(i, lines), getSpecies(i, lines));
			}
			this.trainset = proteins;
		}
		catch (IOException e)
		{
			System.err.println("File " + pdbIDlist.getAbsolutePath() + " could not be read in!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private ArrayList<String> readInFile (LineNumberReader lnr) {
		ArrayList<String> arraylist = new ArrayList<String>();
		try {
			String line;
			while ((line = lnr.readLine()) != null) 
			{
				arraylist.add(line);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arraylist;
	}
	
	//creates Sets of PDB IDs equal to setsize
	private PdbProtein[] createSet(int setsize, int linenr, ArrayList<String> lines){
		
		PdbProtein[] set = new PdbProtein[setsize];
		HashMap<Integer,String> setmap = new HashMap<Integer,String>(2*setsize);

		Random gen = new Random();
		for (int i = 0; i < setsize; i++) {
			int lnr = gen.nextInt(linenr);
			while (setmap.containsKey(Integer.valueOf(lnr))) {
				lnr = gen.nextInt(linenr);
			}
			set[i].setPdbID(this.getPdbID(lnr, lines));
			setmap.put(Integer.valueOf(lnr), set[i].getPdbID());
		}
		return set;
	}
	
	
	private String getPdbID (int lineNumber, ArrayList<String> lines ) {
		// Initialize a LineNumberReader
		String line =(String) lines.get(lineNumber);
		String pdbID;
		if ( line.length() >= 4 )
		{
			pdbID = line.substring(0, line.indexOf("."));
		}
		else
		{
			pdbID = "";
		}
		return pdbID;
	}
	
	private String getChainID (int lineNumber, ArrayList<String> lines) {
		String line =(String) lines.get(lineNumber);
		String chainID;
		if (line.contains(".") )
		{
			chainID = line.substring(line.indexOf(".") + 1, line.lastIndexOf("."));
		}
		else
		{
			chainID = "";
		}
		return chainID;
	}
	
	private String getSpecies (int lineNumber, ArrayList<String> lines) {
		String line =(String) lines.get(lineNumber);
		String species;
		if (line.length() > 6)
		{
			species = line.substring(line.lastIndexOf("."));
		}
		else
		{
			species = "";
		}
		return species;
	}
}
