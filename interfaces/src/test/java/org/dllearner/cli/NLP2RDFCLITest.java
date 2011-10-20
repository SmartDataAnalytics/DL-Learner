package org.dllearner.cli;

import java.io.File;
import java.io.IOException;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ReasonerComponent;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class NLP2RDFCLITest {

	@Test
	public void sampleTest() throws IOException {
		File f = new File("../examples/nlp2rdf/sample/sample1.conf");
		CLI cli = new CLI(f);
		cli.run();
		ApplicationContext context = cli.getContext();
		AbstractReasonerComponent rc = context.getBean(AbstractReasonerComponent.class);
	}
}
