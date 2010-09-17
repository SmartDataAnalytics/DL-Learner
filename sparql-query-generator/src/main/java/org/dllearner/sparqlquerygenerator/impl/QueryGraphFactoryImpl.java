/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
 *
 */
package org.dllearner.sparqlquerygenerator.impl;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.QueryGraphFactory;
import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryGraphImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryGraphFactoryImpl implements QueryGraphFactory {

	@Override
	public QueryGraph getQueryGraph(Set<Node> nodes, Set<Edge> edges,
			Node rootNode) {
		return new QueryGraphImpl(nodes, edges, rootNode);
	}

	@Override
	public QueryGraph getQueryGraph(Node rootNode) {
		return new QueryGraphImpl(rootNode);
	}

	@Override
	public QueryGraph getQueryGraph() {
		return new QueryGraphImpl();
	}

}
