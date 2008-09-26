/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.utilities.statistics;

import java.io.File;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.utilities.Files;
import org.dllearner.utilities.StringFormatter;


/**
 * Class to collect results and output them as a latex table or other formats.
 * 
 * @author Sebastian Hellmann
 *
 */
public class Table implements Serializable{

	private static final long serialVersionUID = 0l;
    
    //used to give a good percentage output
    //private DecimalFormat df = new DecimalFormat( ".00%" ); 
    private List<TableColumn> columns = new ArrayList<TableColumn>();
    
    private String tableName = "";
    private String caption = "";
    private String label = "";
    
    public Table(String tableName){
    	this.tableName = tableName;
    } 
    
    public static void main(String[] args) {
		Table t = new Table("myTable");
		String tableFile = "results/table/myTable";
		TableColumn c1 = new TableColumn("col1", new String[]{"a","b"});
		TableColumn c2 = new TableColumn("col2", new String[]{"c","d"});
		t.addColumn(c1);
		System.out.println(t.getLatexString());
		
		serializeColumns(t, "results/table",tableFile );
		
		t = createTableFromSerializedColums("myTable", tableFile);
		System.out.println(t.getLatexString());
		
		t.addColumn(c2);
		serializeColumns(t, "results/table",tableFile );
		t = createTableFromSerializedColums("myTable", tableFile);
		System.out.println(t.getLatexString());
		
		
		
	}
    
    public String getLatexString(){
    	String tabular = "";
    	for (int i = 0; i < columns.size(); i++) {
			tabular+="l";
		}
    	
    	String headers = latexRow(getColumnHeaders());
    	  	
    	String table="";
    	
		table += "\\begin{table*}\n";
		table += "\t\\centering\n";
		table += "\t\t\\begin{tabular}{"+tabular+"}\n";
		table += "\\hline\n";
		table += headers;
		table += "\\hline\n";
		// add here
		for (int i = 0; i < getNumberOfRows(); i++) {
			table +=getRowInLatex(i);
		}
		table += "\\end{tabular}\n";
		table += "\t\\caption{"+caption+"}\n";
		table += "\t\\label{"+label+"}\n";
		table += "\\end{table*}\n\n";

		//List<String> myList = new ArrayList<String>({""});
		
		//List<String> list = Arrays.asList( "","" ); 
    	return table;
    	
    }
    
    public String getRowInLatex(int index){
    	List<String> l = new ArrayList<String>();
    	for(TableColumn c: columns){
    		l.add(c.getEntry(index));
    	}
    	return latexRow(l);
    }
    
    public int getNumberOfRows(){
    	if(columns.isEmpty())return 0;
    	else return columns.get(0).getSize();
    }
    
    public void removeColumn(String header){
    	for (int i = 0; i < columns.size(); i++) {
			if(columns.get(i).getHeader().equals(header)){
				columns.remove(i);
				return;
			}
		}
    }
    
    
    public List<String> getColumnHeaders(){
    	List<String> entries = new ArrayList<String>();
    	for (TableColumn c : columns) {
			 entries.add(c.getHeader());
		}
    	return entries;
    }
    
    public String latexRow(List<String> entries){
    	String ret="";
    	for (String one : entries) {
			ret+=" "+one+"\t& ";
		}
    	ret = ret.substring(0,ret.length()-3);
    	ret+="\t\\\\\n";
    	return ret;
    }
    
    public void addColumn(TableColumn c){
    	if(columns.isEmpty()){
    		columns.add(c);
    	}else{
    		if(getNumberOfRows()!=c.getSize()){
    			System.out.println("ERROR: size of columns doesn't match");
    			System.exit(0);
    		}else{
    			columns.add(c);
    		}
    	}
    }
    
    public static Table createTableFromSerializedColums(String tableName, String tableFile){
    	Table ret = new Table(tableName);
    	try{
    		
    		String[] columnFiles = Files.readFileAsArray(new File(tableFile));
    		for (String filename : columnFiles) {
    			TableColumn col = TableColumn.deSerialize(new File(filename));
    			//TableColumn col = (TableColumn) Files.readObjectfromFile(new File(filename));
				ret.addColumn(col);
			}
    	//	FileWriter fw =  new FileWriter ();
    	}catch (Exception e) {
			e.printStackTrace();
		}
    	return ret;
    }
    
    public static void serializeColumns(Table t, String dir, String tableFile){
    	String column = ".column";
    	String content = "";
    	dir = StringFormatter.checkIfDirEndsOnSlashAndRemove(dir);
    	Files.mkdir(dir);
    	try{
    		int i=0;
    		for(TableColumn c:t.getColumns()){
    			String header = URLEncoder.encode(c.getHeader(),"UTF-8");
    			String columnFileName = dir+File.separator+t.getTableName()+(i++)+header+column;
    			c.serialize(new File(columnFileName));
    			//Files.writeObjectToFile(c, new File(filename));
    			content += columnFileName+System.getProperty("line.separator");
    		}
    		Files.createFile(new File(tableFile), content);
    		//
    		//FileWriter fw =  new FileWriter ();
    	}catch (Exception e) {
    		e.printStackTrace();
		}
    }


	public List<TableColumn> getColumns() {
		return columns;
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
    
    
}
