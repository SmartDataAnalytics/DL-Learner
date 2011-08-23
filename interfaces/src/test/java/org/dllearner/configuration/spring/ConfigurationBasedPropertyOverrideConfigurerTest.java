package org.dllearner.configuration.spring;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/19/11
 * Time: 5:52 PM
 * <p/>
 * Test For our ConfigurationBasedPropertyOverrideConfigurere.  It works as part of a ConfigurableApplicationContext
 * so we test it via a context.
 */
public class ConfigurationBasedPropertyOverrideConfigurerTest {


    private ApplicationContext context;

    @Before
    public void setUp() throws Exception {

        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();

        /** The DL-Learner Config File */
        Resource confFile = new ClassPathResource("/org/dllearner/configuration/spring/configurationBasedPropertyOverrideConfigurer.conf");

        /** Component Key Prefixes */
        List<String> componentKeyPrefixes = new ArrayList<String>();
        componentKeyPrefixes.add("component:");
        componentKeyPrefixes.add(":");

        /** Spring Config Files */
        List<Resource> springConfigResources = new ArrayList<Resource>();
        springConfigResources.add(new ClassPathResource("/org/dllearner/configuration/spring/configuration-based-property-override-configurer-configuration.xml"));

        /** Build The Application Context */
        context =  builder.buildApplicationContext(confFile,componentKeyPrefixes,springConfigResources);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testMethods() {
        TestBean testBean = context.getBean("testBean", TestBean.class);

        validateFirstBean(testBean);
        TestBean secondBean = testBean.getComponent();
        validateSecondBean(secondBean);
        validateThirdBean(secondBean.getComponent());
        validateFourthBean(context.getBean("fourthBean", TestBean.class));
        Assert.assertTrue(testBean.getComponentSet().size() == 3);
    }

    private void validateThirdBean(TestBean thirdBean) {
        Assert.assertEquals(thirdBean.getIntValue(), (Integer) 3);
        TestBean fourthBean = thirdBean.getComponent();
        validateFourthBean(fourthBean);
    }

    private void validateFirstBean(TestBean testBean) {
        Assert.assertEquals(testBean.getSimpleValue(), "simple value example");
        Assert.assertEquals(testBean.getIntValue(), (Integer) 23);
        Assert.assertEquals(testBean.getDoubleValue(), (Double) 78.5);
        Assert.assertTrue(testBean.getSetValue().contains("a"));
        Assert.assertTrue(testBean.getMapValue().get("a").equals("b"));
        Assert.assertTrue(testBean.getComponent() != null);
    }

    private void validateSecondBean(TestBean secondBean) {
        Assert.assertEquals(secondBean.getSimpleValue(), "second bean example");
        Assert.assertEquals(secondBean.getIntValue(), (Integer) 85);
        Assert.assertEquals(secondBean.getDoubleValue(), (Double) 178.5);
        Assert.assertTrue(secondBean.getSetValue().contains("e"));
        Assert.assertTrue(secondBean.getMapValue().get("f").equals("g"));
        Assert.assertTrue(secondBean.getComponent() != null);
        Assert.assertTrue(secondBean.getComponentSet().size() == 2);
    }


    private void validateFourthBean(TestBean fourthBean){
        Assert.assertEquals(fourthBean.getSimpleValue(), "Fourth Bean - not specified in xml");
    }

}
