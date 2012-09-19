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
package org.dllearner.test;

import java.util.List;

import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.EvaluatedAxiom;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * 
 * Test of spring for new component system.
 * 
 * @author Jens Lehmann
 *
 */
public class SpringTest {

	public static void main(String[] args) {
		Resource resource = new FileSystemResource("../test/spring/example.xml");
		BeanFactory factory = new XmlBeanFactory(resource);
		AxiomLearningAlgorithm alg = (AxiomLearningAlgorithm) factory.getBean("learner");
		alg.start();
		List<EvaluatedAxiom> axioms = alg.getCurrentlyBestEvaluatedAxioms(10);
		for(EvaluatedAxiom axiom : axioms) {
			System.out.println(axiom.toString());
		}
	}
	
}
