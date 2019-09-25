/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
package org.dllearner.confparser3;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser.ConfParser;
import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.confparser.ConfParserLegacy;
import org.dllearner.confparser.ParseException;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Conf parser tests.
 * 
 * @author Jens Lehmann
 *
 */
public class ParseTest {

	@Test
	public void test() throws FileNotFoundException, ParseException, UnsupportedEncodingException {
		ConfParser parser = ConfParserLegacy.parseFile(new File("../examples/family/father.conf"));
		for(ConfFileOption option : parser.getConfOptions()) {
			System.out.print(option.getBeanName() + "." + option.getPropertyName() + " = " + option.getValue().toString());
			if(option.isBeanRef()) {
				System.out.println("    (bean reference)");
			} else {
				System.out.println();
			}	
		}
	}

	@Test(expected = org.springframework.beans.factory.UnsatisfiedDependencyException.class)
	public void testBrokenConf() throws Throwable {
		try {
			Resource confFileR = new FileSystemResource(new File("../test/father_broken.conf"));
			List<Resource> springConfigResources = new ArrayList<>();
			IConfiguration configuration = new ConfParserConfiguration(confFileR);

			ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
			builder.buildApplicationContext(configuration, springConfigResources);
		} catch (IOException e) {
			e.printStackTrace();
		} catch(RuntimeException e) {
			throw e.getCause();
		}
	}
	
}
