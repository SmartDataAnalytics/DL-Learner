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

include_once('PlaceTemplate.php');

/**
 * Template for all populated places.
 */
abstract class PopulatedPlaceTemplate extends PlaceTemplate {

	// return a nicely formatted string for the population of the place 
	function getPopulationString() {
		$population = $this->extractPropValue($triples, 'populationTotal'); 
		$number = number_format($population);
		$asOf = $this->extractPropValue('populationAsOf');
		
		return $number + ' (as of ' + $asOf + ')';
	}

}
?>