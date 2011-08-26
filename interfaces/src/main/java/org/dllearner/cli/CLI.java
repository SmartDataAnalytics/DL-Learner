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
package org.dllearner.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.Info;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser2.ParseException;
import org.dllearner.core.AbstractCELA;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * 
 * New commandline interface.
 * 
 * @author Jens Lehmann
 *
 */
public class CLI {



    public CLI(){

    }

    public void run(ApplicationContext context, String algorithmBeanName){
        AbstractCELA algorithm = context.getBean(algorithmBeanName, AbstractCELA.class);
        algorithm.start();
    }

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		
		System.out.println("DL-Learner " + Info.build + " [TODO: read pom.version and put it here (make sure that the code for getting the version also works in the release build!)] command line interface");
		
		// currently, CLI has exactly one parameter - the conf file
		if(args.length == 0) {
			System.out.println("You need to give a conf file as argument.");
			System.exit(0);
		}
		
		// read file and print and print a message if it does not exist
		File file = new File(args[args.length - 1]);
		Resource confFile = new FileSystemResource(args[args.length - 1]);
		if(!file.exists()) {
			System.out.println("File \"" + file + "\" does not exist.");
			System.exit(0);			
		}
		
		// build application context 
        List<String> componentKeyPrefixes = new ArrayList<String>();
        componentKeyPrefixes.add("component:");
        componentKeyPrefixes.add(":");

        List<Resource> springConfigResources = new ArrayList<Resource>();

        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
        ApplicationContext  context =  builder.buildApplicationContext(confFile,componentKeyPrefixes,springConfigResources);
        
        // start algorithm in conf file
        OCEL algorithm = context.getBean("alg",OCEL.class);
        algorithm.start();
		
	}

}
