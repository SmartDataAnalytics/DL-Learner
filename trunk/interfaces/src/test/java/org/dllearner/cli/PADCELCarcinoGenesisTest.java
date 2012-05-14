package org.dllearner.cli;

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
public class PADCELCarcinoGenesisTest extends PADCELCLITestAbstract {

	@Override
	public String getConfigurationFilePath() {		
		return "../examples/carcinogenesis/carcinogenesis_padcel.conf";
	}
	
}
