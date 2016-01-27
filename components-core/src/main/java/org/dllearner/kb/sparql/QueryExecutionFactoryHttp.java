/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackString;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:47 PM
 */
public class QueryExecutionFactoryHttp
    extends QueryExecutionFactoryBackString
{
    private String service;

    private List<String> defaultGraphs = new ArrayList<>();

    public QueryExecutionFactoryHttp(String service) {
        this(service, Collections.<String>emptySet());
    }

    public QueryExecutionFactoryHttp(String service, String defaultGraphName) {
        this(service, Collections.singleton(defaultGraphName));
    }

    public QueryExecutionFactoryHttp(String service, Collection<String> defaultGraphs) {
        this.service = service;
        this.defaultGraphs = new ArrayList<>(defaultGraphs);
        Collections.sort(this.defaultGraphs);
    }

    @Override
    public String getId() {
        return service;
    }

    @Override
    public String getState() {
        return Joiner.on("|").join(defaultGraphs);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryEngineHTTP result = new QueryEngineHTTP(service, queryString);
        result.setDefaultGraphURIs(defaultGraphs);

        //QueryExecution result = QueryExecutionWrapper.wrap(engine);
        
        return result;
    }
}

