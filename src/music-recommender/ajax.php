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
	SELECT ?artist ?name ?image ?homepage 
	WHERE {
		?artist a mo:MusicArtist .
		?artist foaf:name "'.$searchString.'" .
		?artist foaf:name ?name .
		?artist foaf:img ?image .
		?artist foaf:homepage ?homepage .
	}
	LIMIT 10';

	/*PREFIX geo: <http://www.geonames.org/ontology#>
	PREFIX wgs: <http://www.w3.org/2003/01/geo/wgs84_pos#>
	SELECT DISTINCT ?an ?lat ?long ?name ?population
	WHERE { 
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
		foreach($bindings as $binding) {
			$newContent .= '<img style="float:right" src="'.$binding->image->value.'" />';
			$newContent .= '<b>'.$binding->name->value.'</b><br />';
			$newContent .= '<a href="'.$binding->homepage->value.'">'.$binding->homepage->value.'</a><br />';
			$newContent .= 'TODO: make author name clickable such that people can get tracks from this artist and listen to them';
		}
	} catch (Exception $e) {
    	$newContent = '<b>Search aborted: '.$e->getMessage().'</b>';
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchElement","innerHTML", $newContent);
	return $objResponse;
}

?>