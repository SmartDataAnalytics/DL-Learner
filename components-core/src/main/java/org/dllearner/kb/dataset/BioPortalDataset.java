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
package org.dllearner.kb.dataset;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.dllearner.kb.repository.OntologyRepositoryEntry;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;

public class BioPortalDataset extends AbstractOWLOntologyDataset{
	
	private static final String name = "BioPortal";
	
	public BioPortalDataset(File datasetDirectory) {
		super(datasetDirectory, name);
	}

	@Override
	protected void addOntologyURLs() {
		BioPortalRepository bioportal = new BioPortalRepository();
		bioportal.initialize();
		for (OntologyRepositoryEntry entry : bioportal.getEntries()) {
			try {
				String name = URLEncoder.encode(entry.getOntologyShortName(), "UTF-8");
				super.ontologyURLs.put(entry.getPhysicalURI().toURL(), name);
			} catch (MalformedURLException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

}
