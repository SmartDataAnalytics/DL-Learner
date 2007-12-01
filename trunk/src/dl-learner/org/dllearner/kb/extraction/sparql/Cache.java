package org.dllearner.kb.extraction.sparql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;

public class Cache implements Serializable{
	// Object can be the cache itself
	// or a cache object(one entry)
	
	final static long serialVersionUID=104;
	transient String basedir="";
	transient String fileending=".cache";
	long timestamp;
	String content="";
	long daysoffreshness=15;
	long multiplier=24*60*60*1000;//h m s ms
	String sparqlquery="";
	
	//constructor for the cache itself
	public Cache(String path){
		this.basedir=path+File.separator;
		if(!new File(path).exists())
			{System.out.println(new File(path).mkdir());;}
	
	}
	
//	constructor for single cache object(one entry)
	public Cache(String c, String sparql){
		this.content=c;
		this.sparqlquery=sparql;
		this.timestamp=System.currentTimeMillis();
	}
	
	
	public String get(String key, String sparql){
		//System.out.println("get From "+key);
		String ret=null;
		try{
		Cache c =readFromFile(makeFilename(key))	;
		if(c==null)return null;
		//System.out.println(" file found");
		if(!c.checkFreshness())return null;
		//System.out.println("fresh");
		if(!c.validate(sparql))return null;
		//System.out.println("valid");
		ret=c.content;
		}catch (Exception e) {e.printStackTrace();}
		return ret;
	};
	public void put(String key, String content, String sparql){
		//System.out.println("put into "+key);
		Cache c=new Cache(content,sparql);
		putIntoFile(makeFilename(key), c);
	}
	
	
	String makeFilename(String key){
		String ret="";
		try{
		ret=basedir+URLEncoder.encode(key, "UTF-8")+fileending;
		}catch (Exception e) {e.printStackTrace();}
		return ret;
	}
	boolean checkFreshness(){
		if((System.currentTimeMillis()-this.timestamp)<=(daysoffreshness*multiplier))
			//fresh
			return true;
		else return false;
	}
	boolean validate(String sparql){
		if(this.sparqlquery.equals(sparql))
			//valid
			return true;
		else return false;
	}
	
	public void checkFile(String Filename){
		if(!new File(Filename).exists()){
			try{
				new File(Filename).createNewFile();
			}catch (Exception e) {e.printStackTrace();}
			
		}
		
	}
	
	public void putIntoFile(String Filename,Cache content){
		try{
		//FileWriter fw=new FileWriter(new File(Filename),true);
		FileOutputStream  fos = new FileOutputStream( Filename , false ); 
		ObjectOutputStream o = new ObjectOutputStream( fos ); 
		o.writeObject( content ); 
		fos.flush();
		fos.close();
		}catch (Exception e) {System.out.println("Not in cache creating: "+Filename);}
	}
	
	public Cache readFromFile(String Filename){
		Cache content=null;
		try{
		FileInputStream  fos = new FileInputStream( Filename ); 
		ObjectInputStream o = new ObjectInputStream( fos ); 
		content=(Cache)o.readObject();
		//FileReader fr=new FileReader(new File(Filename,"r"));
		//BufferedReader br=new BufferedReader(fr);
		}catch (Exception e) {}
		return content;
		
	}
}
