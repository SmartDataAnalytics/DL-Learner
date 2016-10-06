package org.dllearner.cli.DocumentationGeneratorMeta;

import java.util.Map;

import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.confparser.PostProcessor;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.config.ConfigOption;

@ComponentAnn(name = "GLOBAL OPTIONS", version = 0, shortName = "")
public abstract class GlobalDoc {
	// Implemented in: 
	Class<PostProcessor> prefixesImpl = PostProcessor.class;
	
	@ConfigOption(description = "Mapping of prefixes to replace inside other configuration file entries", exampleValue = "[ (\"ex\",\"http://example.com/father#\") ]")
	Map<String,String> prefixes;
	
	// Implemented in:
	Class<ConfParserConfiguration> renderingImpl = ConfParserConfiguration.class;
	@ConfigOption(description = "The string renderer for any OWL expression output, can be \"dlsyntax\" or \"manchester\"", defaultValue = "manchester", exampleValue = "dlsyntax")
	String rendering;
}
