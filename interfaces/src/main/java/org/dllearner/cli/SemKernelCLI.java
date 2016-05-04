package org.dllearner.cli;

import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.utilities.semkernel.SemKernelWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * SemKernel command line interface
 *
 * @author Patrick Westphal
 */
public class SemKernelCLI extends CLIBase2 {
    private static Logger logger = LoggerFactory.getLogger(SemKernelCLI.class);

    private ApplicationContext context;
    private File confFile;
    private IConfiguration configuration;

    private SemKernelWorkflow semkernelWorkflow;

    public SemKernelCLI() {
    }

    public SemKernelCLI(File confFile) {
        this();
        this.setConfFile(confFile);
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("SemKernel command line interface");

        if(args.length == 0) {
            System.out.println("You need to give a conf file as argument.");
            System.exit(0);
        }

        // read file and print a message if it does not exist
        File file = new File(args[args.length - 1]);
        if(!file.exists()) {
            System.out.println("File \"" + file + "\" does not exist.");
            System.exit(0);
        }

        Resource confFile = new FileSystemResource(file);

        List<Resource> springConfigResources = new ArrayList<>();

        try {
            //SemKernel configuration object
            IConfiguration configuration = new ConfParserConfiguration(confFile);

            ApplicationContextBuilder builder =
                    new DefaultApplicationContextBuilder();
            ApplicationContext context = builder.buildApplicationContext(
                    configuration, springConfigResources);

            CLIBase2 cli = new SemKernelCLI();

            cli.setContext(context);
            cli.setConfFile(file);
            cli.run();

        } catch (Exception e) {e.printStackTrace();
            String stacktraceFileName = "log/error.log";

            //Find the primary cause of the exception.
            Throwable primaryCause = findPrimaryCause(e);

            // Get the Root Error Message
            logger.error("An Error Has Occurred During Processing.");
            logger.debug("Stack Trace: ", e);
            logger.error("Terminating DL-Learner...and writing stacktrace " +
                    "to: " + stacktraceFileName);
            FileOutputStream fos = new FileOutputStream(stacktraceFileName);
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
        }
    }

    public void run() {
        for(Entry<String, SemKernelWorkflow> entry : getContext().
                getBeansOfType(SemKernelWorkflow.class).entrySet()){

            semkernelWorkflow = entry.getValue();
            logger.info("Running SemKernel workflow \"" + entry.getKey() + "\" (" + semkernelWorkflow.getClass().getSimpleName() + ")");
            semkernelWorkflow.start();
        }
    }

}
