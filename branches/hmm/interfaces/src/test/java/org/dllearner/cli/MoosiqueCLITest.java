package org.dllearner.cli;

import junit.framework.Assert;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.learningproblems.PosOnlyLP;
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
public class MoosiqueCLITest {

    public ApplicationContext createApplicationContext(Resource confFile) throws IOException {
        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();

        //Spring Config Files
        List<Resource> springConfigResources = new ArrayList<Resource>();

        //DL-Learner Configuration Object
        IConfiguration configuration = new ConfParserConfiguration(confFile);
        //Build The Application Context

        ApplicationContext context = builder.buildApplicationContext(configuration, springConfigResources);
        return context;
    }


    @Test
    public void testMoosiqueConf() throws Exception {
        Resource confFile = new FileSystemResource("../examples/sparql/moosique_new.conf");
        ApplicationContext context = createApplicationContext(confFile);
        validateContext(context);
    }



    private void validateContext(ApplicationContext context) {
        PosOnlyLP lp = context.getBean("lp", PosOnlyLP.class);
        Assert.assertTrue(lp.getPositiveExamples().size() == 3);
//        Assert.assertTrue(lp.getNegativeExamples().size() == 4);
        Assert.assertNotNull(lp.getReasoner());

        CELOE algorithm = context.getBean("alg", CELOE.class);
        Assert.assertNotNull(algorithm);

        algorithm.start();
    }
}
