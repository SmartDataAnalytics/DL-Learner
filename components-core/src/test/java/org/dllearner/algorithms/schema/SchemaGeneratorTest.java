/**
 * 
 */
package org.dllearner.algorithms.schema;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann 
 * @since Oct 25, 2014
 */
public class SchemaGeneratorTest {
	
	SyntheticDataGenerator dataGenerator = new SyntheticDataGenerator();

	/**
	 * Test method for {@link org.dllearner.algorithms.schema.SchemaGenerator#generateSchema()}.
	 */
	@Test
	public void testGenerateSchema() {
		OWLOntology ontology = dataGenerator.createData(3, 20);
		
		Model model = OwlApiJenaUtils.getModel(ontology);
		
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(model);
		
		AbstractSchemaGenerator schemaGenerator = new SimpleSchemaGenerator(qef);
		schemaGenerator.setAxiomTypes(Sets.<AxiomType<? extends OWLAxiom>>newHashSet(AxiomType.OBJECT_PROPERTY_DOMAIN));
		schemaGenerator.generateSchema();
	}

}
