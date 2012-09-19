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
public class ParCELCarcinoGenesisTest extends ParCELCLITestAbstract {

	@Override
	public String getConfigurationFilePath() {		
		///examples/carcinogenesis/carcinogenesis_parcel.conf
		return "../examples/carcinogenesis/carcinogenesis_parcel.conf";
	}
	
}
