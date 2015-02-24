/**
 * 
 */
package org.dllearner.algorithms.schema;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann 
 * @since Oct 25, 2014
 */
public class SchemaGeneratorTest extends TestCase{
	
	SyntheticDataGenerator dataGenerator = new SyntheticDataGenerator();
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
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
