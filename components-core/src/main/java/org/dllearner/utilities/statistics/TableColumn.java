package org.dllearner.utilities.statistics;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dllearner.utilities.Files;


public class TableColumn implements Serializable {

	private static final long serialVersionUID = 1L;
	private String header;
	private List<String> entries = new ArrayList<>();

public TableColumn() {
	super();
	
}	

public TableColumn(String header) {
	super();
	this.header = header;
}


public TableColumn( String[] entries) {
	this.entries = Arrays.asList(entries);
}

	
public TableColumn(String header, String[] entries) {
	this(header);
	this.entries = Arrays.asList(entries);
}

public TableColumn( List<String> entries) {
	this.entries = entries;
}


public String getHeader() {
	return header;
}

public void setHeader(String header) {
	this.header = header;
}

/**
 * entires should be in Latex, if the target is latex
 * @param entry
 */
public void addEntry(String entry){
	entries.add(entry);
}

public int getSize(){
	return entries.size();
}

public String getEntry(int index){
	return entries.get(index);
}

public void serialize(File file){
	String content = header+System.getProperty("line.separator");
	for (String entry : entries) {
		content += entry+System.getProperty("line.separator");
	}
	Files.createFile(file, content);
}

public static TableColumn deSerialize(File f){
	TableColumn ret = null;
	try{
		String[] c = Files.readFileAsArray(f);
		ret =  new TableColumn();
		boolean first = true;
		for (String line : c) {
			if(first){
				first = false;
				ret.setHeader(line);
				
			}else{
				ret.addEntry(line);
			}
			
		}
	}catch (Exception e) {
		 e.printStackTrace();
	}
	
	return ret;
}


}
