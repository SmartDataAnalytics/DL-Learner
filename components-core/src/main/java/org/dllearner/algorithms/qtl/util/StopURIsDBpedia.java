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
package org.dllearner.algorithms.qtl.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class StopURIsDBpedia {
	static final Set<String> uris = ImmutableSet.of(
			"http://dbpedia.org/ontology/abstract",
			"http://dbpedia.org/ontology/thumbnail",
			"http://www.w3.org/ns/prov#wasDerivedFrom",
			"http://dbpedia.org/ontology/wikiPageExternalLink",
			"http://dbpedia.org/ontology/wikiPageID",
			"http://dbpedia.org/ontology/wikiPageInLinkCount",
			"http://dbpedia.org/ontology/wikiPageOutLinkCount",
			"http://dbpedia.org/ontology/wikiPageRevisionID",
			"http://dbpedia.org/ontology/wikiPageEditLink",
			"http://dbpedia.org/ontology/wikiPageExtracted",
			"http://dbpedia.org/ontology/wikiPageHistoryLink",
			"http://dbpedia.org/ontology/wikiPageLength",
			"http://dbpedia.org/ontology/wikiPageModified",
			"http://dbpedia.org/ontology/wikiPageOutDegree",
			"http://dbpedia.org/ontology/wikiPageRevisionLink",
			"http://dbpedia.org/ontology/wikiPageDisambiguates",
			"http://dbpedia.org/ontology/wikiPageRedirects",
			"http://www.w3.org/ns/prov#Entity",
			"http://www.w3.org/2007/05/powder-s#describedby",
			"http://dbpedia.org/ontology/viafId",
			"http://www.opengis.net/gml/_Feature",
			"http://www.w3.org/2003/01/geo/wgs84_pos#lat",
			"http://www.w3.org/2003/01/geo/wgs84_pos#long",
			"http://www.georss.org/georss/point"
			);
	
	public static Set<String> get() {
		return uris;
	}
}
