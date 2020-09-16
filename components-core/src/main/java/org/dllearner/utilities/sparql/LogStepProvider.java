package org.dllearner.utilities.sparql;

import org.apache.jena.riot.system.StreamRDF;

public interface LogStepProvider {
    StreamRDF getLogStream();

    String getStepUri();
    String getRequestLogBaseUri();
}
