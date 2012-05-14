package org.dllearner.cli;

import java.io.File;
import java.io.IOException;

import org.dllearner.algorithms.PADCEL.PADCELAbstract;
import org.dllearner.algorithms.PADCEL.PADCELPosNegLP;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * JUnit test for PDCCEL
 * <ul>
 * <li>validate the configuration</li>
 * <li>execute the learning algorithm</li>
 * <li>validate the result</li>
 * </ul> 
 * 
 * @author An C. Tran
 *
 */
public abstract class PADCELCLITestAbstract {

	private CLI cli;

	public abstract String getConfigurationFilePath();
	
	/**
	 * Default constructor

	 */
	public PADCELCLITestAbstract() {	
		super();
	}
	
	/**
	 * Setup the testing
	 */
	@Before
	public void setup() {		
		try {
			
			System.out.println("---------------------------------------------------------------");
			System.out.println("Dataset: \"" + this.getConfigurationFilePath() + "\"");
			System.out.println("---------------------------------------------------------------");
			
			File carcinoConfFile = new File(this.getConfigurationFilePath());			
			cli = new CLI(carcinoConfFile);			
			cli.init();			
		}
		catch (Exception e) {
			//do nothing
		}		
	}

	
	/**
	 * Run validations
	 * 
	 * @throws IOException
	 */
	@Test
	public void runValidation() throws IOException {
		validateConfiguration();
		
		validateLearningResult();
	}
	
	/**
	 * Validate the configuration
	 * 
	 * @param context
	 */
	public void validateConfiguration() {
		//check CLI
		Assert.assertNotNull(cli);		
		
		//get application context for validating the configuration
		ApplicationContext context = cli.getContext();		
		Assert.assertNotNull(context);
		
		//learning problem	
		Assert.assertNotNull(context.getBean("lp", PADCELPosNegLP.class));
		
		//learning algorithm
		Assert.assertNotNull(context.getBean("alg", PADCELAbstract.class));
		
		//PDLL splitter (some algorithms do not have)
		//Assert.assertNotNull(context.getBean("splitter", PADCELDoubleSplitterAbstract.class));		
	}
	
	
	/**
	 * Validate the learning result
	 *  
	 * @param learner
	 * @throws IOException 
	 */
	public void validateLearningResult() throws IOException {
		Assert.assertNotNull(cli);

		//get application context for validating the configuration
		ApplicationContext context = cli.getContext();		
		Assert.assertNotNull(context);
		
		PADCELAbstract learner = context.getBean("alg", PADCELAbstract.class);
		
		//we shorten timeout for all testings to 30s for a faster validation		
		learner.setMaxExecutionTimeInSeconds(30);
		learner.start();	//start the learner instead to avoid cross validation in this test
		
		Assert.assertTrue(learner.getNumberOfPartialDefinitions() > 0);
		Assert.assertNotNull(learner.getUnionCurrenlyBestDescription());
	}
}
