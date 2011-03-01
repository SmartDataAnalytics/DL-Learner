package org.dllearner.examples;

import org.dllearner.core.owl.Individual;

public class KRKPiece {

	// REMEMBER
	// FILES are letters
	// RANKS are numbers
	public Individual id;
	public String file;
	public int rank;
	
	public KRKPiece(Individual id, String file, int rank) {
		super();
		this.id = id;
		this.file = file;
		this.rank = rank;
	}
	
	public int getRankDistance(KRKPiece p)
	{
		int otherrank=p.rank;
		if(this.rank>otherrank)
			return this.rank-otherrank;
		else if(this.rank==otherrank){
			return 0;
		}else return otherrank-this.rank;
		
	}
	public boolean meHasLowerRankThan(KRKPiece p){
		int otherrank=p.rank;
		if(this.rank<otherrank)
			return true;
		else return false;
	}
	
	public int getFileDistance(KRKPiece p)
	{
		int otherFile=letterToNumber(p.file);
		int meFile=letterToNumber(this.file);
		
		if(meFile>otherFile)
			return meFile-otherFile;
		else if(meFile==otherFile){
			return 0;
		}else return otherFile-meFile;
		
	}
	public boolean meHasLowerFileThan(KRKPiece p){
		int otherFile=letterToNumber(p.file);
		int meFile=letterToNumber(this.file);
		
		if(meFile<otherFile)
			return true;
		else return false;
	}
	
	private int letterToNumber(String s){
		if(s.equals("a"))
			return 1;
		else if(s.equals("b"))
			return 2;
		else if(s.equals("c"))
			return 3;
		else if(s.equals("d"))
			return 4;
		else if(s.equals("e"))
			return 5;
		else if(s.equals("f"))
			return 6;
		else if(s.equals("g"))
			return 7;
		else if(s.equals("h"))
			return 8;
		
		return 0;
	}
	
}
