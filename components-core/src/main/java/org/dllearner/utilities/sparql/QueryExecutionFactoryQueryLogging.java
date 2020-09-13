package org.dllearner.utilities.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.OutputStream;

public class QueryExecutionFactoryQueryLogging extends QueryExecutionFactoryDecorator
{
    private LogStepProvider provider;
    private long stepRequestCount;
    private OutputStream logStream;
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
        Resource logQuery = model.createResource(stepUri + "-query-" + stepRequestCount, model.createResource("logQuery"));
        logQuery.addLiteral(model.createProperty("query"), queryString);
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        logQuery.addProperty(model.createProperty("time"), fmt.print(dt), XSDDatatype.XSDdateTimeStamp);
        logQuery.addProperty(model.createProperty("step"), model.createResource(stepUri));
        RDFDataMgr.write(logStream,model, RDFFormat.NTRIPLES_UTF8);
        model.close();
        stepRequestCount++;
    }
}
