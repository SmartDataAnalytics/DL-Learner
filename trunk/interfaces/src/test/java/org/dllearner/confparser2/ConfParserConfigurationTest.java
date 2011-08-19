package org.dllearner.confparser2;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/19/11
 * Time: 4:26 PM
 * <p/>
 * Basic Test
 */
public class ConfParserConfigurationTest {

    private ConfParserConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        Resource confFile = new ClassPathResource("/org/dllearner/confparser2/confParserConfigurationTest.conf");

        configuration = new ConfParserConfiguration(confFile);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testStringValueProperty() throws Exception {

        Object simpleValue = configuration.getObjectValue("testBean.simpleValue");
        Assert.assertTrue(simpleValue instanceof String);
        Assert.assertEquals("simple value example", simpleValue);

    }

    @Test
    public void testComponentValueProperty() throws Exception {

        Object value = configuration.getObjectValue("testBean.component");
        /** We expect a string back here */
        Assert.assertTrue(value instanceof String);
        Assert.assertEquals("component:test", value);

    }

    @Test
    public void testIntValueProperty() throws Exception {

        Object value = configuration.getObjectValue("testBean.intValue");
        /** We expect a string back here */
        Assert.assertTrue(value instanceof Integer);
        Assert.assertEquals(23, value);

    }

    @Test
    public void testDoubleValueProperty() throws Exception {

        Object value = configuration.getObjectValue("testBean.doubleValue");
        /** We expect a string back here */
        Assert.assertTrue(value instanceof Double);
        Assert.assertEquals(78.5d, (Double) value, .001);

    }

    @Test
    public void testSetValueProperty() throws Exception {

        Object value = configuration.getObjectValue("testBean.setValue");
        /** We expect a string back here */
        Assert.assertTrue(value instanceof Set);
        Set<String> set = (Set<String>) value;
        Assert.assertTrue(set.size() == 1);
        Assert.assertTrue(set.contains("a"));

    }

    @Test
    public void testMapValueProperty() throws Exception {

        Object value = configuration.getObjectValue("testBean.mapValue");
        /** We expect a string back here */
        Assert.assertTrue(value instanceof Map);
        Map<String, String> set = (Map<String, String>) value;
        Assert.assertTrue(set.size() == 2);
        Assert.assertEquals(set.get("a"), "b");
        Assert.assertEquals(set.get("c"), "d");
    }

    @Test
    public void testGetProperties() throws Exception {
        Properties properties = configuration.getProperties();
        Assert.assertTrue(properties.size() == 6);
        Assert.assertTrue(properties.get("testBean.intValue").equals("23"));

    }

    @Test
    public void testGetNegativeExamples(){
        Set<String> negativeExamples = configuration.getNegativeExamples();
        Assert.assertTrue(negativeExamples.size() == 1);
        Assert.assertTrue(negativeExamples.contains("http://example.org/neg1/"));
    }


    @Test
    public void testGetPositiveExamples(){
        Set<String> negativeExamples = configuration.getPositiveExamples();
        Assert.assertTrue(negativeExamples.size() == 1);
        Assert.assertTrue(negativeExamples.contains("http://example.org/pos1/"));
    }
}
