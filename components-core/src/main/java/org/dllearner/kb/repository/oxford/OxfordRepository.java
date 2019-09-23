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
package org.dllearner.kb.repository.oxford;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.kb.repository.SimpleRepositoryEntry;

/**
 * Oxford ontologies repository located at http://www.cs.ox.ac.uk/isg/ontologies
 *
 * @author Lorenz Buehmann
 */
public class OxfordRepository implements OntologyRepository{
	
	private static final Logger log = Logger.getLogger(OxfordRepository.class);
	
	private final String repositoryName = "Oxford";

    private final URI repositoryLocation = URI.create("http://www.cs.ox.ac.uk/isg/ontologies/UID/");

    private List<SimpleRepositoryEntry> entries = new ArrayList<>();

    int numberOfEntries = 797;
    
    private DecimalFormat df = new DecimalFormat("00000");

    @Override
    public void initialize() {
    	refresh();
    }

    @Override
	public String getName() {
        return repositoryName;
    }

    @Override
	public String getLocation() {
        return repositoryLocation.toString();
    }

    @Override
	public void refresh() {
        fillRepository();
    }

    @Override
	public Collection<OntologyRepositoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    private void fillRepository() {
    	entries.clear();
        for(int i = 1; i <= numberOfEntries; i++){
        	entries.add(new SimpleRepositoryEntry(URI.create(repositoryLocation + df.format(i) + ".owl")));
        }
        log.info("Loaded " + entries.size() + " ontology entries from Oxford.");
    }

    public static void main(String[] args) throws Exception {
		new OxfordRepository().fillRepository();
	}

}
