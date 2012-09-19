package org.dllearner.cli;

import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.configuration.util.ConfigurationXMLBeanConverter;
import org.dllearner.configuration.util.SpringConfigurationXMLBeanConverter;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/23/11
 * Time: 5:21 AM
 * <p/>
 * Test for the CLI Class
 */
public class FatherCLITest {

    public ApplicationContext createApplicationContext(Resource confFile) throws IOException {
        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();

        //Spring Config Files
        List<Resource> springConfigResources = new ArrayList<Resource>();

        //DL-Learner Configuration Object
        IConfiguration configuration = new ConfParserConfiguration(confFile);

        ConfigurationXMLBeanConverter converter = new SpringConfigurationXMLBeanConverter();
        XmlObject object = converter.convert(configuration);
        System.out.println(object.toString());
        //Build The Application Context

        ApplicationContext context = builder.buildApplicationContext(configuration, springConfigResources);
        return context;
    }


    @Test
    public void testFatherConf() throws Exception {
        Resource confFile = new FileSystemResource("../examples/family/father.conf");
        ApplicationContext context = createApplicationContext(confFile);
        validateContext(context);
    }

    @Test
    public void testFatherAutoWiredConf() throws Exception {
        Resource confFile = new FileSystemResource("../examples/family/father_autowired.conf");
        ApplicationContext context = createApplicationContext(confFile);
        validateContext(context);
    }

    private void validateContext(ApplicationContext context) {
        PosNegLPStandard lp = context.getBean("lp", PosNegLPStandard.class);
        Assert.assertTrue(lp.getPositiveExamples().size() == 3);
        Assert.assertTrue(lp.getNegativeExamples().size() == 4);
        Assert.assertNotNull(lp.getReasoner());

        OCEL algorithm = context.getBean("alg", OCEL.class);
        Assert.assertNotNull(algorithm);

        algorithm.start();
    }
}
