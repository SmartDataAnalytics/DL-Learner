/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
 */
package org.dllearner.algorithms.schema;

import com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann 
 * @since Oct 25, 2014
 */
public class SchemaGeneratorTest {
	
	SyntheticDataGenerator dataGenerator = new SyntheticDataGenerator();
	
	@BeforeClass
	public static void setUp() throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		Logger.getLogger(SimpleSchemaGenerator.class).setLevel(Level.TRACE);
	}
	
	/**
	 * Test method for {@link org.dllearner.algorithms.schema.SchemaGenerator#generateSchema()}.
	 */
	@Test
	public void testGenerateSchema() {
		OWLOntology ontology = dataGenerator.createData(3, 1000);
		
		Model model = OwlApiJenaUtils.getModel(ontology);
		
		SimpleSchemaGenerator schemaGenerator = new SimpleSchemaGenerator(model);
		schemaGenerator.setAxiomTypes(Sets.<AxiomType<? extends OWLAxiom>>newHashSet(AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE));
		schemaGenerator.setNrOfIterations(4);
		schemaGenerator.generateSchema();
	}

}
