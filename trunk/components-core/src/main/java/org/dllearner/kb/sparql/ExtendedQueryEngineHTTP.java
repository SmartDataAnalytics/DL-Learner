/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.kb.sparql;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.Params;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.resultset.XMLInput;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;
import com.hp.hpl.jena.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Claus Stadler
 * Date: Oct 25, 2010
 * Time: 10:15:31 PM
 */
class DisconnectorThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DisconnectorThread.class);

    private HttpQuery connection;

    private long timeOut;

    private boolean canceled = false;

    public DisconnectorThread(HttpQuery connection, long timeOut) {
        this.connection = connection;
        this.timeOut = timeOut;
    }

    public void run() {
        synchronized (this) {

            while(!canceled && connection.getConnection() == null) {
                //logger.trace("Waiting for connection...");

                try {
                    this.wait(500l);
                } catch (InterruptedException e) {
                }
            }

            long startTime = System.currentTimeMillis();

            long remaining;
            while (!canceled && (remaining = (timeOut - (System.currentTimeMillis() - startTime))) > 0) {
                logger.trace("Forced disconnect in " + remaining + "ms");
                try {
                    this.wait(remaining);
                } catch (InterruptedException e) {
                }
            }

            if (!canceled && connection.getConnection() != null) {
                logger.warn("Disconnecting Http connection since a sparql query is taking too long");
                connection.getConnection().disconnect();
                canceled = true;
            }
        }
    }

    public void cancel() {
        synchronized (this) {
            if(!this.canceled) {
                logger.trace("Disconnect cancelled");
            }

            this.canceled = true;
            this.notify();
        }
    }
}

/**
 * A QueryEngineHTTP that is capable of closing connections after a given timeout.
 *
 * Jena now provides one on its own
 */
public class ExtendedQueryEngineHTTP
        implements QueryExecution {
    private static Logger log = LoggerFactory.getLogger(QueryEngineHTTP.class);

    public static final String QUERY_MIME_TYPE = "application/sparql-query";
    String queryString;
    String service;
    Context context = null;


    long timeOut = 0l;

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public long getTimeOut() {
        return timeOut;
    }


    //Params
    Params params = null;

    // Protocol
    List<String> defaultGraphURIs = new ArrayList<String>();
    List<String> namedGraphURIs = new ArrayList<String>();
    private String user = null;
    private char[] password = null;

    // Releasing HTTP input streams is important. We remember this for SELECT,
    // and will close when the engine is closed
    private InputStream retainedConnection = null;

    public ExtendedQueryEngineHTTP(String serviceURI, Query query) {
        this(serviceURI, query.toString());
    }

    public ExtendedQueryEngineHTTP(String serviceURI, String queryString) {
        this.queryString = queryString;
        service = serviceURI;
        // Copy the global context to freeze it.
        context = new Context(ARQ.getContext());
    }

//    public void setParams(Params params)
//    { this.params = params ; }

    // Meaning-less

    public void setFileManager(FileManager fm) {
        throw new UnsupportedOperationException("FileManagers do not apply to remote query execution");
    }

    public void setInitialBinding(QuerySolution binding) {
        throw new UnsupportedOperationException("Initial bindings not supported for remote queries");
    }

    public void setInitialBindings(ResultSet table) {
        throw new UnsupportedOperationException("Initial bindings not supported for remote queries");
    }

    /**
     * @param defaultGraphURIs The defaultGraphURIs to set.
     */
    public void setDefaultGraphURIs(List<String> defaultGraphURIs) {
        this.defaultGraphURIs = defaultGraphURIs;
    }

    /**
     * @param namedGraphURIs The namedGraphURIs to set.
     */
    public void setNamedGraphURIs(List<String> namedGraphURIs) {
        this.namedGraphURIs = namedGraphURIs;
    }

    public void addParam(String field, String value) {
        if (params == null)
            params = new Params();
        params.addParam(field, value);
    }

    /**
     * @param defaultGraph The defaultGraph to add.
     */
    public void addDefaultGraph(String defaultGraph) {
        if (defaultGraphURIs == null)
            defaultGraphURIs = new ArrayList<String>();
        defaultGraphURIs.add(defaultGraph);
    }

    /**
     * @param name The URI to add.
     */
    public void addNamedGraph(String name) {
        if (namedGraphURIs == null)
            namedGraphURIs = new ArrayList<String>();
        namedGraphURIs.add(name);
    }

    /**
     * Set user and password for basic authentication.
     * After the request is made (one of the exec calls), the application
     * can overwrite the password array to remove details of the secret.
     *
     * @param user
     * @param password
     */
    public void setBasicAuthentication(String user, char[] password) {
        this.user = user;
        this.password = password;
    }

    private InputStream doTimedExec(HttpQuery httpQuery) {
        DisconnectorThread stopTask = null;
        if (timeOut > 0) {
            stopTask = new DisconnectorThread(httpQuery, timeOut);
//            stopTask.start();
        }

        InputStream in;
        try {
            in = httpQuery.exec();
        }
        finally {
            if (stopTask != null) {
                stopTask.cancel();
            }
        }

        return in;
    }


    public ResultSet execSelect() {
        HttpQuery httpQuery = makeHttpQuery();
        // TODO Allow other content types.
        httpQuery.setAccept(HttpParams.contentTypeResultsXML);

        InputStream in = httpQuery.exec();


        ResultSet rs = ResultSetFactory.fromXML(in);
        retainedConnection = in; // This will be closed on close()
        return rs;
    }

    public Model execConstruct() {
        return execConstruct(GraphFactory.makeJenaDefaultModel());
    }

    public Model execConstruct(Model model) {
        return execModel(model);
    }

    public Model execDescribe() {
        return execDescribe(GraphFactory.makeJenaDefaultModel());
    }

    public Model execDescribe(Model model) {
        return execModel(model);
    }

    private Model execModel(Model model) {
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(HttpParams.contentTypeRDFXML);
        InputStream in = doTimedExec(httpQuery);
        model.read(in, null);
        return model;
    }

    public boolean execAsk() {
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(HttpParams.contentTypeResultsXML);
        InputStream in = doTimedExec(httpQuery);
        boolean result = XMLInput.booleanFromXML(in);
        // Ensure connection is released
        try {
            in.close();
        }
        catch (java.io.IOException e) {
            log.warn("Failed to close connection", e);
        }
        return result;
    }

    public Context getContext() {
        return context;
    }

    private HttpQuery makeHttpQuery() {
        HttpQuery httpQuery = new HttpQuery(service);
        httpQuery.setTimeOut((int)timeOut);
        httpQuery.addParam(HttpParams.pQuery, queryString);

        for (Iterator<String> iter = defaultGraphURIs.iterator(); iter.hasNext();) {
            String dft = iter.next();
            httpQuery.addParam(HttpParams.pDefaultGraph, dft);
        }
        for (Iterator<String> iter = namedGraphURIs.iterator(); iter.hasNext();) {
            String name = iter.next();
            httpQuery.addParam(HttpParams.pNamedGraph, name);
        }

        if (params != null)
            httpQuery.merge(params);

        httpQuery.setBasicAuthentication(user, password);
        return httpQuery;
    }

    public void abort() {
    }

    public void close() {
        if (retainedConnection != null) {
            try {
                retainedConnection.close();
            }
            catch (java.io.IOException e) {
                log.warn("Failed to close connection", e);
            }
            finally {
                retainedConnection = null;
            }
        }
    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTimeout(long timeout) {
       this.timeOut = timeout;
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

//    public boolean isActive() { return false ; }

    @Override
    public String toString() {
        HttpQuery httpQuery = makeHttpQuery();
        return "GET " + httpQuery.toString();
    }

    public Dataset getDataset() {
        return null;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

