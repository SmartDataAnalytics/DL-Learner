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
package org.dllearner.sparqlquerygenerator.datastructures;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public interface QueryTree<N> {
	
	 /**
     * Gets the "content" of this tree node.
     * @return The user content of this node.
     */
    N getUserObject();
    
    void setUserObject(N userObject);

    QueryTree<N> getParent();
    
    List<QueryTree<N>> getChildren();
    
    List<QueryTree<N>> getChildren(Object edge);

    Object getEdge(QueryTree<N> child);
    
    void addChild(QueryTreeImpl<N> child);
    
    void addChild(QueryTreeImpl<N> child, Object edge);
    
    void removeChild(QueryTreeImpl<N> child);
    
    Set<Object> getEdges();
    
    void sortChildren(Comparator<QueryTree<N>> comparator);

    int getChildCount();

    boolean isRoot();

    boolean isLeaf();
    
    boolean isSubsumedBy(QueryTree<N> tree);
    
    boolean isSubsumedBy(QueryTree<N> tree, boolean stopAfterError);
    
    boolean isSameTreeAs(QueryTree<N> tree);
    
    void tag();
    
    boolean isTagged();

    QueryTree<N> getRoot();
    
    List<QueryTree<N>> getLeafs();

    List<QueryTree<N>> getPathToRoot();

    List<N> getUserObjectPathToRoot();
    
    void dump();

    void dump(PrintWriter writer);

    void dump(PrintWriter writer, int indent);

    Set<N> getUserObjectClosure();

    List<N> fillDepthFirst();
    
    String toSPARQLQueryString();
    
    String toSPARQLQueryString(int cnt);

}
