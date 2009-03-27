<?php

//
// TODO: 
// - move configuration for settings.ini
// - ignoredConcepts and ignoredRoles are not working yet

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
	
	//which predefined Endpoint to use
	public $endpoint="DBPEDIA";
	//public $endpoint="LOCALDBPEDIA";
	
	public $dbpediaPropertyPrefix = 'http://dbpedia.org/ontology/';
	public $dbpediaClassPrefix = 'http://dbpedia.org/ontology/';
	
	// in mikrosekunden
	public $sparqlttl=60000000;
	public $learnttl=5;
	
	public $language="en";
	
	//localhost
	public $googleMapsKey="ABQIAAAAWwHG9WuZ8hxFSPjRX2-D-hSOxlJeL3USfakgDtFzmQkGhQTW0xTFM1Yr38ho8qREnjt-6oLs37o4xg";
	//db.aksw.org
	//public $googleMapsKey="ABQIAAAAWwHG9WuZ8hxFSPjRX2-D-hRHWRcfpxRnIG10qrJMLnZO-_MKjRRpu2rZj8etMweqJES04ZL_eht1iQ";
	
	public $useCache=true;
	
	public $ignoredConcepts=array();
	public $ignoredRoles=array();

	//public $classSystem="YAGO";
	public $classSystem="DBpedia";
	//the name of the used database
	//public $database_name='navigator_db';
	public $database_name='navigator_db_new';

	//the type of database server
	public $database_type='mysql';
	//the server, where the mysql databank is located
	public $database_server='localhost';
	//the user, that has rights to access the navigator databank
	public $database_user='navigator';
	//the password of that user
	public $database_pass='dbpedia';
	
}

?>
