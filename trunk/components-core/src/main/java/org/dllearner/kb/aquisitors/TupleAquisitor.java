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

package org.dllearner.kb.aquisitors;

import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

/**
 * 
 * Typed SPARQL query interface. The typing means that they all have the same
 * input and the same output: They are fn: resource -> ( a | b ) where a
 * normally is a predicate and b an object
 * 
 * @author Sebastian Hellmann
 * 
 */
public abstract class TupleAquisitor {
	

	private static Logger logger = Logger.getLogger(TupleAquisitor.class);
	protected final int NORMAL = 0;
	protected final int CLASSES_FOR_INSTANCES = 1;
	protected final int CLASS_INFORMATION = 2;
	
	protected int mode = 0;
	protected boolean uriDebugCheck = true;
	
	public boolean dissolveBlankNodes = false;

	
	/**
	 * TODO: this function is still used somewhere, but should be replaced
	 * @return
	 */
	public boolean isDissolveBlankNodes(){
		return dissolveBlankNodes;
	}
	
	public final SortedSet<RDFNodeTuple> getTupelForResource(String uri){
		checkURIforValidity(uri);
			
		try{
			if (mode == NORMAL) {
				return retrieveTupel(uri);
			} else if(mode == CLASSES_FOR_INSTANCES){
				return retrieveClassesForInstances(uri);
			}else if(mode == CLASS_INFORMATION){
				return retrieveTuplesForClassesOnly(uri);
			}else{
			      throw new RuntimeException("undefined mode in aquisitor");
			}
		}catch(Exception e){
			logger.warn("Caught exception in tupleaquisitor, ignoring it: "+e.toString());
			e.printStackTrace();
			return new TreeSet<RDFNodeTuple>();
			
		}
	}
	public abstract SortedSet<RDFNodeTuple> retrieveTupel(String uri);
	public abstract SortedSet<RDFNodeTuple> retrieveClassesForInstances(String uri);
	public abstract SortedSet<RDFNodeTuple> retrieveTuplesForClassesOnly(String uri);
	protected abstract void disambiguateBlankNodes(String uri, SortedSet<RDFNodeTuple> resultSet);
	public abstract  SortedSet<RDFNodeTuple> getBlankNode(int id);
	
	/*private void setMode(int mode) {
		this.mode = mode;
	}*/

	public int getMode() {
		return mode;
	}
	
	public void setNextTaskToNormal(){mode = NORMAL;}
	public void setNextTaskToClassesForInstances(){mode = CLASSES_FOR_INSTANCES;}
	public void setNextTaskToClassInformation(){mode = CLASS_INFORMATION;}
	
	protected boolean checkURIforValidity(String uri){
		if(uriDebugCheck) return true;
		try{
			new URI(uri);
		}catch (Exception e) {
			logger.warn("Exception while validating uri: "+uri);
			return false;
		}
		return true;
	}
}


