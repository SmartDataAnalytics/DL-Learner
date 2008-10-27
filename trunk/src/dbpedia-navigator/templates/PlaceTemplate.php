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
 * Abstract template class. Templates are used for displaying information
 * about a particular entity in a user friendly way.
 * 
 * You can also use this class for convenience functions accessible by
 * all concrete templates.
 *  
 * @author Jens Lehmann
 */
abstract class PlaceTemplate extends AbstractTemplate {

	// returns a latitude string of the form 49°1′0″N or "unknown"
	public getLatitudeString($triples) {
		if(!areDBpediaPropertiesSet(array('latitudedegrees','latitudeminutes','latitudeseconds'))) {
			return "unknown";
		}
	
		$latitude = $triples['http://dbpedia.org/ontology/latitutedegrees'] + "° "
			+ $triples['http://dbpedia.org/ontology/latitudeminutes'] + "′"
			+ $triples['http://dbpedia.org/ontology/latitudeseconds'] + "″N";
		return $latitude;	
	}

	// returns a latitude string of the form 49°1′0″E or "unknown"
	public getLongitudeString($triples) {
		if(!areDBpediaPropertiesSet(array('longitudedegrees','longitudeminutes','longitudeseconds'))) {
			return "unknown";
		}
	
		$longitude = $triples['http://dbpedia.org/ontology/longitutedegrees'] + "° "
			+ $triples['http://dbpedia.org/ontology/longitudeminutes'] + "′"
			+ $triples['http://dbpedia.org/ontology/longitudeseconds'] + "″N";
		return $longitude;	
	}
	
}

?>