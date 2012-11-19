/** **/
package org.dllearner.common.index;

import static org.junit.Assert.*;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner3;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner3Test;
import org.junit.Test;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** @author konrad
 * */
public class SPARQLClassesIndexTest
{

	@Test public void test()
	{
		Model m = ModelFactory.createDefaultModel();
		m.read(SPARQLTemplateBasedLearner3Test.class.getClassLoader().getResourceAsStream("oxford/schema/LGD-Dump-110406-Ontology.nt"),null, "TURTLE");
		SPARQLClassesIndex index = new SPARQLClassesIndex(m);
		assertFalse(index.getResourcesWithScores("pharmacy").getItems().isEmpty());
	}

}
