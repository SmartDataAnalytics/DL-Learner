package org.dllearner.utilities;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class JenaResultSetConvenience {
	ResultSet rs;

	public JenaResultSetConvenience(ResultSet rs) {
		super();
		this.rs = rs;
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<String> getStringListForVariable(String var){
		SortedSet<String> result = new TreeSet<String>();
		
		List<ResultBinding> l =  ResultSetFormatter.toList(this.rs);
		
		for (ResultBinding resultBinding : l) {
				
			result.add(resultBinding.get(var).toString());
		
		}
		
		return result;
		
	}
	
	
}
