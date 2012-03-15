package org.dllearner.utilities.owl;

/**
 * Created by IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 3/13/12
 * Time: 6:28 PM
 * 
 * Test instance for a particular implementation.
 */
public class SimpleOntologyToByteConverterTest extends OntologyToByteConverterTest{

    @Override
    public OntologyToByteConverter getInstance() {
        return new SimpleOntologyToByteConverter();
    }
}
