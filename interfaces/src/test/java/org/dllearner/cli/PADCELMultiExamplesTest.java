package org.dllearner.cli;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Run validation for multiple examples
 * 
 * @author An C. Tran
 *
 */
@RunWith(value = Parameterized.class)
public class PADCELMultiExamplesTest extends PADCELCLITestAbstract {

	private String confFile;

	public PADCELMultiExamplesTest(String confFile) {
		super();
		this.confFile = confFile;
	}

	@Override
	public String getConfigurationFilePath() {
		return this.confFile;
	}

	@Parameters
	public static Collection<String[]> data() {
		String[][] data = new String[][] {
				// each row is an array of parameters that will be used to instantiate this class				
				{"../examples/moral_reasoner/moral_all_examples_simple_owl_padcel.conf"},
				{"../examples/poker/straight_owl_padcel.conf"},
				{"../examples/forte/uncle_owl_large_padcel.conf"},
				{"../examples/carcinogenesis/carcinogenesis_padcel.conf"},
				{"../examples/showering-duration/uca1_150_padcel.conf"}
				};
		return Arrays.asList(data);
	}

}
