package org.dllearner.algorithm.tbsl.sem.drs;

import java.io.StringReader;

import org.dllearner.algorithm.tbsl.sem.drs.reader.DRSParser;
import org.dllearner.algorithm.tbsl.sem.drs.reader.ParseException;
import org.dllearner.algorithm.tbsl.sem.drs.DRS;

public class DRS_Constructor {
	
	public DRS construct(String string)
	{
		DRS drs = null;
		DRSParser parser =  new DRSParser(new StringReader(new String(string)));
		parser.ReInit(new StringReader(new String(string)));		
		try {
			drs = parser.DRS();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	
		return drs;
	}
	
}
