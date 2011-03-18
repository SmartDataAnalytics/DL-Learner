package org.dllearner.algorithm.tbsl.sem.dudes.data;

import java.io.StringReader;

import org.dllearner.algorithm.tbsl.sem.dudes.reader.DUDE_Parser;
import org.dllearner.algorithm.tbsl.sem.dudes.reader.ParseException;




public class DUDE_Constructor {

	public Dude construct(String string) throws ParseException
	{
		Dude dude;
		DUDE_Parser parser =  new DUDE_Parser(new StringReader(new String(string)));
		parser.ReInit(new StringReader(new String(string)));		
		dude = parser.DUDE();
	
		return dude;
	}
	
	
}
