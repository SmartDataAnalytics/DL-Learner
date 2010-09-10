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
  * Music recommender index page.
  * 
  * @author Jens Lehmann
  * @author Anita Janassary
  */

require_once '../music-recommender/xajax/xajax_core/xajax.inc.php';
require_once 'DLLearnerConnection.php';

$xajax = new xajax();

// set to debug during development if needed
$xajax->configureMany(array('debug'=>false));

// register functions
$xajax->registerFunction("doSearch");
$xajax->registerFunction("doSearchTitle");
$xajax->processRequest();

// search for songs matching the search string
function doSearch($searchString)
{
	// ToDo: execute a SPARQL query (find labels matching search string) by contacting DL-Learner web service
	/*$query = 'PREFIX geo:<http://www.geonames.org/ontology#>
	PREFIX wgs:<http://www.w3.org/2003/01/geo/wgs84_pos#>
	SELECT ?a ?place ?lat ?long
	WHERE {
		?a a mo:MusicArtist;
		foaf:based_near ?place.
		?place geo:name ?name;
		geo:population ?population;
		wgs:lat ?lat; 
		wgs:long ?long 
	}
	LIMIT 1';*/
	// unfortunately full text search does not work yet - I am in contact with Yves Raimond to find a solution
	// currently, we look for exact matches
	
	$query = '
	PREFIX map: <file:/home/moustaki/work/motools/musicbrainz/d2r-server-0.4/mbz_mapping_raw.n3#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
PREFIX rel: <http://purl.org/vocab/relationship/>
PREFIX lingvoj: <http://www.lingvoj.org/ontology#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>
PREFIX db: <http://dbtune.org/musicbrainz/resource/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX geo: <http://www.geonames.org/ontology#>
PREFIX bio: <http://purl.org/vocab/bio/0.1/>
PREFIX mo: <http://purl.org/ontology/mo/>
PREFIX vocab: <http://dbtune.org/musicbrainz/resource/vocab/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX mbz: <http://purl.org/ontology/mbz#>
SELECT ?homepage ?title ?image
	WHERE { 
		?artist a mo:MusicArtist .
		?artist foaf:name "'.$searchString.'" .
		?artist foaf:homepage ?homepage .
		?record foaf:maker ?artist .
		?record dc:title ?title .
		?record mo:image ?image .
				
	}';
	
	/*$query = ' //altes Select
	SELECT ?artist ?name ?image ?homepage ?title 
	WHERE {
		?artist a mo:MusicArtist .
		?artist foaf:name "'.$searchString.'" .
		?artist foaf:name ?name .
		?artist foaf:img ?image .
		?artist foaf:homepage ?homepage .
		?artist foaf:made ?album .
		?album dc:title ?title 
	}
	'; Ausgabe !
	$newContent .= '<img style="float:right" src="'.$bindings[0]->image->value.'" />';
		$newContent .= '<b>'.$bindings[0]->name->value.'</b><br />';
		$newContent .= '<a href="'.$bindings[0]->homepage->value.'">'.$bindings[0]->homepage->value.'</a><br />';
		$newContent .= 'TODO: make author name clickable such that people can get tracks from this artist and listen to them<br/>';
		
		foreach($bindings as $binding) {
		$speicherTitel[] .= $binding->title->value;
		}
		$newContent .= '<a href="">'.sizeof($speicherTitel).'</a><br />';
	*/

	/*PREFIX geo: <http://www.geonames.org/ontology#>
	PREFIX wgs: <http://www.w3.org/2003/01/geo/wgs84_pos#>
	SELECT DISTINCT ?an ?lat ?long ?name ?population
	WHERE { ?album mo:track ?track .
		?track dc:title ?title .
		?a a mo:MusicArtist; 
		foaf:based_near ?place; 
		foaf:name ?an;
		foaf:made ?alb.
		?alb tags:taggedWithTag <http://dbtune.org/jamendo/tag/punk>.
		?place 
			geo:name ?name; 
		geo:population ?population; 
		wgs:lat ?lat; 
		wgs:long ?long 
	}
	ORDER BY ?population';*/

	try {
		$connection = new DLLearnerConnection();
		$json = $connection->sparqlQuery($query);
		$result = json_decode($json);
		$bindings = $result->results->bindings;

		// preprocessing phase
		// found artists (currently all results are artists)
		/*
		$artists = array();
		$count = 0;
		foreach($bindings as $binding) {
			$artists[$count]['image'] = $binding->image;
			$count++;
		}*/

		// var_dump($bindings);
		// $newContent = 'searching for '.$searchString.' ... not implemented <pre>test</pre>';
		$newContent = '<h3>Search Results</h3>';
		//$speicher = $bindings->title->value;
		if (sizeof($bindings) == 0){ throw new Exception('kein Ergebnis');
		}
		
		$newContent .= '<b>'.$bindings[0]->name->value.'</b><br />';
		$newContent .= '<a href="'.$bindings[0]->homepage->value.'">'.$bindings[0]->homepage->value.'</a><br />';
		$newContent .= 'TODO: make author name clickable such that people can get tracks from this artist and listen to them<br/>';
		
		foreach($bindings as $binding) {
		$newContent .= '<div><b>'.$binding->title->value.'</b><br /></div>';
		$newContent .= '<div><img style="float:right" src="'.$binding->image->value.'" /><br /></div>';
		}
		//$newContent .= '<a href="">'.sizeof($speicherTitel).'</a><br />';
		
	} catch (Exception $e) {
    	$newContent = '<b>Suche ergibt: '.$e->getMessage().'</b>';
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchElement","innerHTML", $newContent);
	return $objResponse;
}
function doSearchTitle($searchTitle)
{
	// ToDo: execute a SPARQL query (find labels matching search string) by contacting DL-Learner web service
	
	$queryTitle = '
	PREFIX map: <file:/home/moustaki/work/motools/musicbrainz/d2r-server-0.4/mbz_mapping_raw.n3#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
PREFIX rel: <http://purl.org/vocab/relationship/>
PREFIX lingvoj: <http://www.lingvoj.org/ontology#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>
PREFIX db: <http://dbtune.org/musicbrainz/resource/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX geo: <http://www.geonames.org/ontology#>
PREFIX bio: <http://purl.org/vocab/bio/0.1/>
PREFIX mo: <http://purl.org/ontology/mo/>
PREFIX vocab: <http://dbtune.org/musicbrainz/resource/vocab/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX mbz: <http://purl.org/ontology/mbz#>
SELECT ?name ?recordname ?tracksname
	WHERE {
?track a mo:Track .
?track dc:title "'.$searchTitle.'" .
?track foaf:maker ?artist.
?artist foaf:name ?name.
?track dc:title ?tracksname.
 ?record mo:track ?track.
?record dc:title ?recordname.

	} 
';
	
	

	try {
		$connection = new DLLearnerConnection();
		$json = $connection->sparqlQuery($queryTitle);
		$result = json_decode($json);
		$bindings = $result->results->bindings;

		$newContent = '<h3>Search Results</h3>';
		//$speicher = $bindings->title->value;
		
		$newContent .= '<div style="float:left; width:50%;">'.Artist.'</div>';
		$newContent .= '<div style="float:left; width:50%;">'.Album.'</div><br />';
		//$newContent .= '<div style="float:right; width:30%;">'.Track.'</div></div>';
		
		foreach($bindings as $binding) {
		$newContent .= '<br /><div style="float:left; width:50%;">'.$binding->name->value.'</div>';
		$newContent .= '<div style="float:left; width:50%;">'.$binding->recordname->value.'</div><br /><br />';
		//$newContent .= '<div style="float:right; width:30%;">'.$binding->tracksname->value.'</div>';
		}
		//$newContent .= '<a href="">'.sizeof($speicherTitel).'</a><br />';
		if(sizeof($bindings) <= 0){throw new Exception('nichts gefunden');}
		
	} catch (Exception $e) {
    	$newContent = '<b>Suche ergibt: '.$e->getMessage().'</b>';
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchElement","innerHTML", $newContent);
	return $objResponse;
}

?>