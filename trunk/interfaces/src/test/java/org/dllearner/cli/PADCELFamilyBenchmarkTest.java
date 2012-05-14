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
public class PADCELFamilyBenchmarkTest extends PADCELCLITestAbstract {

	private String confFile;

	public PADCELFamilyBenchmarkTest(String confFile) {
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
				{"../examples/family-benchmark/Aunt_padcel.conf"},
				{"../examples/family-benchmark/Brother_padcel.conf"},				
				{"../examples/family-benchmark/Cousin_padcel.conf"},
				{"../examples/family-benchmark/Daughter_padcel.conf"},
				{"../examples/family-benchmark/Father_padcel.conf"},
				{"../examples/family-benchmark/Grandson_padcel.conf"},
				{"../examples/family-benchmark/Uncle_padcel.conf"}
				};
		return Arrays.asList(data);
	}

}
