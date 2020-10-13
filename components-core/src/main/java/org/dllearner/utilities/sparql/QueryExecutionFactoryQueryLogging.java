package org.dllearner.utilities.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.dllearner.reasoning.SPARQLReasoner;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class QueryExecutionFactoryQueryLogging extends QueryExecutionFactoryDecorator
{
    private LogStepProvider provider;
    private long stepRequestCount;
    private StreamRDF logStream;
    private String lastStep;

    public QueryExecutionFactoryQueryLogging(QueryExecutionFactory decoratee, LogStepProvider prov) {
        super(decoratee);
        this.logStream = prov.getLogStream();
        this.provider = prov;
        this.stepRequestCount = 0L;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution qe = decoratee.createQueryExecution(query);
        logQuery(query);
        return qe;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryExecution qe = decoratee.createQueryExecution(queryString);
        logQuery(queryString);
        return qe;
    }

    @Override
    public void close() throws Exception {
        provider = null;
        super.close();
    }

    private void logQuery(Query query) {
        logQuery(query.serialize());
    }

    private void logQuery(String queryString) {
        Model model = ModelFactory.createDefaultModel();
        String stepUri = provider.getStepUri();
        if(!stepUri.equals(lastStep)){
            stepRequestCount = 0;
            lastStep = stepUri;
        }
        Resource logQuery = model.createResource(stepUri + "-query-" + stepRequestCount, model.createResource(SPARQLReasoner.REQUEST_LOG_NS + "logQuery"));
        logQuery.addLiteral(model.createProperty(SPARQLReasoner.REQUEST_LOG_NS + "query"), queryString);
        logQuery.addLiteral(model.createProperty(SPARQLReasoner.REQUEST_LOG_NS + "queryCount"), stepRequestCount);
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        logQuery.addProperty(model.createProperty(SPARQLReasoner.REQUEST_LOG_NS + "time"), fmt.print(dt), XSDDatatype.XSDdateTimeStamp);
        logQuery.addProperty(model.createProperty(SPARQLReasoner.REQUEST_LOG_NS + "step"), model.createResource(stepUri));
        StreamRDFOps.graphToStream(model.getGraph(), logStream);
        model.close();
        stepRequestCount++;
    }
}
