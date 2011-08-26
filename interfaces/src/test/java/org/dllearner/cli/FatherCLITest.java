package org.dllearner.cli;

import junit.framework.Assert;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.junit.BeforeClass;
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
 *
 * Test for the CLI Class
 */
public class FatherCLITest {

    private static ApplicationContext context;
    @BeforeClass
    public static void setUp() throws IOException{

        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();

        /** The DL-Learner Config File */
        Resource confFile = new FileSystemResource("../examples/family/father.conf");

//        confFile.getAbsoluteFile().getParent(
        /** Component Key Prefixes */
        List<String> componentKeyPrefixes = new ArrayList<String>();
        componentKeyPrefixes.add("component:");
        componentKeyPrefixes.add(":");

        /** Spring Config Files */
        List<Resource> springConfigResources = new ArrayList<Resource>();
//        springConfigResources.add(new ClassPathResource("/org/dllearner/configuration/spring/configuration-based-property-override-configurer-configuration.xml"));

        /** Build The Application Context */
        context =  builder.buildApplicationContext(confFile,componentKeyPrefixes,springConfigResources);

    }


    @Test
    public void testFatherConf(){

        PosNegLPStandard lp = context.getBean("lp", PosNegLPStandard.class);
        Assert.assertTrue(lp.getPositiveExamples().size() == 3);
        Assert.assertTrue(lp.getNegativeExamples().size() == 4);
        Assert.assertNotNull(lp.getReasoner());

        OCEL algorithm = context.getBean("alg",OCEL.class);
        Assert.assertNotNull(algorithm);

        algorithm.start();



    }
}
