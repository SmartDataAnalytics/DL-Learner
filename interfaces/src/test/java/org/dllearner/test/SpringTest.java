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

import org.dllearner.configuration.spring.CustomPropertyEditorRegistrar;
import org.dllearner.core.LearningAlgorithm;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
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
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
		factory.addPropertyEditorRegistrar(new CustomPropertyEditorRegistrar());
		reader.loadBeanDefinitions(resource);

		LearningAlgorithm alg = factory.getBean(LearningAlgorithm.class);
		alg.start();

	}
}
