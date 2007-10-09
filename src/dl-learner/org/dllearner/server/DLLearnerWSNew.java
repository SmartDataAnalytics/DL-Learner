/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.server;

import java.util.HashMap;
import java.util.Random;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpoint;

/**
 * DL-Learner web service interface.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *
 */
@WebService(name = "DLLearnerWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DLLearnerWSNew {

	private HashMap<Long, State> clients;
	private Random rand=new Random();
	private ComponentManager cm = ComponentManager.getInstance();
	
	/**
	 * Generates a unique ID for the client and initialises a session. 
	 * Using the ID the client can call the other web service methods. 
	 * Two calls to this method are guaranteed to return different results. 
	 * 
	 * @return A session ID.
	 */
	@WebMethod
	public long generateID() {
		long id;
		do {
			id = rand.nextLong();
		} while(clients.containsKey(id));
		return id;
	}
	
	// returns session state or throws client not known exception
	private State getState(long id) throws ClientNotKnownException {
		State state = clients.get(id);
		if(state==null)
			throw new ClientNotKnownException(id);
		return state;
	}
	
	@WebMethod
	public void addKnowledgeSource(long id, String type, String url) throws ClientNotKnownException {
		State state = getState(id);
		Class<? extends KnowledgeSource> ksClass;
		if(type.equals("sparql"))
			ksClass = SparqlEndpoint.class;
		else
			ksClass = OWLFile.class;
		KnowledgeSource ks = cm.knowledgeSource(ksClass);
		cm.applyConfigEntry(ks, "url", url);
		state.addKnowledgeSource(ks);
	}
	
	@WebMethod
	public void setLearningAlgorithm(long id, String algorithm) throws ClientNotKnownException {
		State state = getState(id);
		// ...
	}
	
}
