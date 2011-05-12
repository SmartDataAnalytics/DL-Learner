package org.dllearner.examples.pdb;

public class PdbProtein {
	
	private String pdbID;
	private String chainID;
	private String species;
	
	public PdbProtein(String pdbID) {
		this.pdbID = pdbID;
	}
	
	public PdbProtein(String pdbID, String chainID) {
		this.pdbID = pdbID;
		this.chainID = chainID;
	}
	public PdbProtein() {
		this.pdbID = "";
		this.chainID = "";
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
