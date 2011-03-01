package org.dllearner.scripts.matching;

public class Pcollect implements Comparable<Pcollect>{

	String ldp;
	String dbp;
	int count=1;
	
	
	
	
	public Pcollect(String ldp, String dbp) {
		super();
		this.ldp = ldp.trim();
		this.dbp = dbp.trim();
	}


	@Override
	public String toString(){
		String ret = "count : "+count+"  : "+ldp+ " = "+dbp;
		
		return ret;
	}

	public int compareTo(Pcollect in){
		
		Pcollect other = (Pcollect) in;
		if(this.count==other.count)return 0;
		if( this.count>other.count){
			return -1;
		}else {return 1;}
	}
}
