<?php
/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

/**
 * DBpedia navigator settings.
 * 
 * @author Sebastian Knappe
 * @author Jens Lehmann
 */
class Settings{

	public $wsdluri='http://localhost:8181/services?wsdl';
	
	// local OpenLink SPARQL endpoint
	// public $dbpediauri="http://localhost:8890/sparql";
	// public DBpedia SPARQL endpoint
	public $dbpediauri='http://dbpedia.openlinksw.com:8890/sparql';
	// public DBpedia mirror
	// public $dbpediauri='http://dbpedia2.openlinksw.com:8890/isparql';
	
	public $sparqlttl=60;
	
	public $language="en";
	
	public $googleMapsKey="ABQIAAAAWwHG9WuZ8hxFSPjRX2-D-hSOxlJeL3USfakgDtFzmQkGhQTW0xTFM1Yr38ho8qREnjt-6oLs37o4xg";
}

?>