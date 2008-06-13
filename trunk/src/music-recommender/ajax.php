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

require_once '../dbpedia-navigator/xajax/xajax_core/xajax.inc.php';
require_once 'DLLearnerConnection.php';

$xajax = new xajax();

// register functions
$xajax->registerFunction("doSearch");


$xajax->processRequest();

// search for songs matching the search string
function doSearch($searchString)
{
	// ToDo: execute a SPARQL query (find labels matching search string) by contacting DL-Learner web service
	$query = '
	PREFIX geo: <http://www.geonames.org/ontology#>
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
	ORDER BY ?population';

	try {
		$connection = new DLLearnerConnection();
		$result = $connection->sparqlQuery($query);
		$newContent = 'searching for '.$searchString.' ... not implemented '.$result;
	} catch (Exception $e) {
    	$newContent = '<b>Search aborted: '.$e->getMessage().'</b>';
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchElement","innerHTML", $newContent);
	return $objResponse;
}

?>