package org.dllearner.confparser3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import org.dllearner.cli.ConfFileOption2;
import org.dllearner.confparser3.ConfParser;
import org.dllearner.confparser3.ParseException;
import org.junit.Test;

public class ParseTest {

	@Test
	public void test() throws FileNotFoundException, ParseException {
		ConfParser parser = ConfParser.parseFile(new File("../examples/family/father_new.conf"));
		for(ConfFileOption2 option : parser.getConfOptions()) {
			System.out.print(option.getBeanName() + "." + option.getPropertyName() + " = " + option.getValueObject());
			if((option.getPropertyType().equals(String.class) || option.getPropertyType().equals(Set.class)) && !option.isInQuotes()) {
				System.out.println("    (bean reference)");
			} else {
				System.out.println();
			}	
		}
	}
	
}
