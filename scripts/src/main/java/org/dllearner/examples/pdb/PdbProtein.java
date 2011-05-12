package org.dllearner.examples.pdb;

public class PdbProtein {
	
	private String pdbID;
	private String chainID;
	private String species;
	
	public PdbProtein() {
		this("", "", "");
	}
	
	public PdbProtein(String pdbID) {
		this(pdbID, "", "");
	}
	
	public PdbProtein(String pdbID, String chainID) {
		this(pdbID, chainID, "");
	}
	
	public PdbProtein(String pdbID, String chainID, String species) {
		this.pdbID = pdbID;
		this.chainID = chainID;
		this.species = species;
	}


	public String getPdbID() {
		return pdbID;
	}
	public void setPdbID(String pdbID) {
		this.pdbID = pdbID;
	}
	public String getChainID() {
		return chainID;
	}
	public void setChainID(String chain) {
		this.chainID = chain;
	}
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		this.species = species;
	}
}
