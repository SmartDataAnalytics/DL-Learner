package org.dllearner.examples.pdb;



public class PDBProtein {
	
	private String _pdbID;
	private String _chainID;
	private String _species;
	private String _sequence;
	private String _rdfFileName = null;
	private String _confFileName = null;
	private String _arffFileName = null;
	private String _fastaFileName = null;
	
	public PDBProtein(String pdbID) {
		this(pdbID, "", "");
	}
	
	public PDBProtein(String pdbID, String chainID) {
		this(pdbID, chainID, "");
	}
	
	public PDBProtein(String pdbID, String chainID, String species) {
		this._pdbID = pdbID;
		this._chainID = chainID;
		this._species = species;
	}


	public String getPdbID() {
		return _pdbID;
	}
	public void setPdbID(String pdbID) {
		this._pdbID = pdbID;
	}
	public String getChainID() {
		return _chainID;
	}
	public void setChainID(String chain) {
		this._chainID = chain;
	}
	public String getSpecies() {
		return _species;
	}
	public void setSpecies(String species) {
		this._species = species;
	}
	
	public String getSequence() {
		return _sequence;
	}

	public void setSequence(String sequence) {
		this._sequence = sequence;
	}

	public String getRdfFileName(){
		if (_rdfFileName == null){
			if (this.getChainID().length() == 0){
				_rdfFileName =  this.getPdbID().toUpperCase() + ".rdf";
			}
			else
			{
				_rdfFileName =  this.getPdbID().toUpperCase() + "." 
				+ this.getChainID().toUpperCase() + ".rdf";
			}
		}
		return _rdfFileName;
	}

	public void setRdfFileName(String rdfFileName){
		this._rdfFileName  = rdfFileName;
	}
	
	public String getConfFileName() {
		if (_confFileName == null){
			if (this.getChainID().length() == 0){
				_confFileName =  this.getPdbID().toUpperCase() + ".conf";
			}
			else
			{
				_confFileName = this.getPdbID().toUpperCase() + "." 
				+ this.getChainID().toUpperCase() + ".conf";
			}
		}
		return _confFileName;
	}

	public void setConfFileName(String confFileName) {
		this._confFileName = confFileName;
	}

	public String getArffFileName(){
		if (_arffFileName == null){
			if (this.getChainID().length() == 0){
				_arffFileName =  this.getPdbID().toUpperCase() + ".arff";
			}
			else
			{
				_arffFileName = this.getPdbID().toUpperCase() + "." 
				+ this.getChainID().toUpperCase() + ".arff";
			}
		}
		return _arffFileName;
	}
	
	public void setArffFileName(String arffFileName){
		this._arffFileName = arffFileName;
	}
	
	public String getFastaFileName(){
		if (_fastaFileName == null){
			if (this.getChainID().length() == 0){
				_fastaFileName =  this.getPdbID().toUpperCase() + ".fasta";
			}
			else
			{
				_fastaFileName = this.getPdbID().toUpperCase() + "." 
				+ this.getChainID().toUpperCase() + ".fasta";
			}
		}
		return _fastaFileName;
	}
	
	public void setFastaFileName(String fastaFileName){
		this._fastaFileName = fastaFileName;
	}
}
