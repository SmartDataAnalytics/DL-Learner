package org.dllearner.cli;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base CLI class.
 */
public abstract class CLIBase2 {
	static {
		if (System.getProperty("log4j.configuration") == null)
			System.setProperty("log4j.configuration", "log4j.properties");
	}

	private static Logger logger = LoggerFactory.getLogger(CLIBase2.class);

	protected ApplicationContext context;
	protected File confFile;
	protected IConfiguration configuration;
	@ConfigOption(defaultValue = "INFO", description = "Configure logger log level from conf file. Available levels: \"FATAL\", \"ERROR\", \"WARN\", \"INFO\", \"DEBUG\", \"TRACE\". "
			+ "Note, to see results, at least \"INFO\" is required.")
	protected String logLevel = "INFO";

	protected static boolean createIfNotExists(File f) {
	    if (f.exists()) return true;

	    File p = f.getParentFile();
	    if (p != null && !p.exists()) p.mkdirs();

	    try {
	        f.createNewFile();
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	    return true;
	}

	/**
	 * Find the primary cause of the specified exception.
	 *
	 * @param e The exception to analyze
	 * @return The primary cause of the exception.
	 */
	protected static Throwable findPrimaryCause(Exception e) {
	    // The throwables from the stack of the exception
	    Throwable[] throwables = ExceptionUtils.getThrowables(e);

	    //Look For a Component Init Exception and use that as the primary cause of failure, if we find it
	    int componentInitExceptionIndex = ExceptionUtils.indexOfThrowable(e, ComponentInitException.class);

	    Throwable primaryCause;
	    if(componentInitExceptionIndex > -1) {
	        primaryCause = throwables[componentInitExceptionIndex];
	    }else {
	        //No Component Init Exception on the Stack Trace, so we'll use the root as the primary cause.
	        primaryCause = ExceptionUtils.getRootCause(e);
	    }
	    return primaryCause;
	}

	// separate init methods, because some scripts may want to just get the application
	// context from a conf file without actually running it
	public void init() throws IOException {
		Resource confFileR = new FileSystemResource(confFile);
		List<Resource> springConfigResources = new ArrayList<>();
		configuration = new ConfParserConfiguration(confFileR);

		ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
		context = builder.buildApplicationContext(configuration, springConfigResources);
	}

	public abstract void run();

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	public ApplicationContext getContext() {
		return context;
	}

	public File getConfFile() {
		return confFile;
	}

	public void setConfFile(File confFile) {
		this.confFile = confFile;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogLevel() {
		return logLevel;
	}

	protected AbstractReasonerComponent getMainReasonerComponent() {
		AbstractReasonerComponent rc = null;
		// there can be 2 reasoner beans
			Map<String, AbstractReasonerComponent> reasonerBeans = context.getBeansOfType(AbstractReasonerComponent.class);

			if (reasonerBeans.size() > 1) {
				for (Map.Entry<String, AbstractReasonerComponent> entry : reasonerBeans.entrySet()) {
					String key = entry.getKey();
					AbstractReasonerComponent value = entry.getValue();

					if (value instanceof ClosedWorldReasoner) {
						rc = value;
					}

				}
			} else {
				rc = context.getBean(AbstractReasonerComponent.class);
			}

			return rc;
	}
}
