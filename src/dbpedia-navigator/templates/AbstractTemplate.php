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
abstract class AbstractTemplate {

	abstract function printTemplate($triples,$subjecttriples);
	
	function getTableHeader() {
		return '<table border="0" style="width:100%;overflow:hidden"><tr><td><b>Predicate</b></td><td><b>Object/Subject</b></td></tr>';
	}
	
	// function to be called after all "special" actions have been taken;
	// it displays all remaining triples
	function printRemainingTriples($triples,$subjecttriples) {
		$table = '';
		if ((is_array($triples)&&count($triples)>0)||(is_array($subjecttriples)&&count($subjecttriples)>0)){
			$i=1;
			if (is_array($triples)&&count($triples)>0) foreach($triples as $predicate=>$object) {
				$number=count($object);
				if ($i>0) $backgroundcolor="eee";
				else $backgroundcolor="ffffff";
				$table .= '<tr style="background-color:#'.$backgroundcolor.';"><td><a href="'.$predicate.'" target="_blank">'.nicePredicate($predicate).'</a></td>';
				$table .= '<td>';
				if ($number>1) $table.='<ul>';
				$k=1;
				foreach($object as $element) {
					if ($k>3) $display=" style=\"display:none\"";
					else $display="";
					if ($element['type']=="uri"){
						if (strpos($element['value'],"http://dbpedia.org/resource/")===0&&substr_count($element['value'],"/")==4&&strpos($element['value'],"Template:")!=28){
							$label=str_replace('_',' ',substr($element['value'],28));
							if (strlen($label)>60) $label=substr($label,0,60).'...';
							if ($number>1) $table.='<li'.$display.'>';
							$table .= '<a href="#" onclick="get_article(\'label='.$element['value'].'&cache=-1\');">'.urldecode($label).'</a>';
							if ($number>1) $table.='</li>';
						}
						else{
							if ($number>1) $table.='<li'.$display.'>';
							$label=urldecode($element['value']);
							if (strlen($label)>60) $label=substr($label,0,60).'...';
							$table .= '<a href="'.$element['value'].'" target="_blank">'.$label.'</a>';
							if ($number>1) $table.='</li>';
						}
					}
					else{
						if ($number>1) $table.='<li'.$display.'>';
						$table .= $element['value'];
						if ($number>1) $table.='</li>';
					}
					$k++;
				}
				if ($number>3) $table.='<a href="javascript:none()" onclick="toggleAttributes(this)"><img src="images/arrow_down.gif"/>&nbsp;show</a>';
				if ($number>1) $table.='</ul>';
				$table .= '</td>';
				$i*=-1;
			}
			if (is_array($subjecttriples)&&count($subjecttriples)>0) foreach($subjecttriples as $predicate=>$object) {
				$number=count($object);
				if ($i>0) $backgroundcolor="eee";
				else $backgroundcolor="ffffff";
				$table .= '<tr style="background-color:#'.$backgroundcolor.';"><td><a href="'.$predicate.'" target="_blank">is '.nicePredicate($predicate).' of </a></td>';
				$table .= '<td>';
				if ($number>1) $table.='<ul>';
				$k=1;
				foreach($object as $element) {
					if ($k>3) $display=" style=\"display:none\"";
					else $display="";
					if ($element['type']=="uri"){
						if (strpos($element['value'],"http://dbpedia.org/resource/")===0&&substr_count($element['value'],"/")==4&&strpos($element['value'],"Template:")!=28){
							$label=str_replace('_',' ',substr($element['value'],28));
							if (strlen($label)>60) $label=substr($label,0,60).'...';
							if ($number>1) $table.='<li'.$display.'>';
							$table .= '<a href="#" onclick="get_article(\'label='.$element['value'].'&cache=-1\');">'.urldecode($label).'</a>';
							if ($number>1) $table.='</li>';
						}
						else{
							if ($number>1) $table.='<li'.$display.'>';
							$label=urldecode($element['value']);
							if (strlen($label)>60) $label=substr($label,0,60).'...';
							$table .= '<a href="'.$element['value'].'" target="_blank">'.$label.'</a>';
							if ($number>1) $table.='</li>';
						}
					}
					else{
						if ($number>1) $table.='<li'.$display.'>';
						$table .= $element['value'];
						if ($number>1) $table.='</li>';
					}
					$k++;
				}
				if ($number>3) $table.='<a href="javascript:none()" onclick="toggleAttributes(this)"><img src="images/arrow_down.gif"/>&nbsp;show</a>';
				if ($number>1) $table.='</ul>';
				$table .= '</td>';
				$i*=-1;
			}
			$table .= '</table>';
		}
		else $table="No triple left.";
		return $table;
	}
	
	// utility method, which checks whether the given DBpedia ontology properties exists in the triples
	// is they exist, the method returns true and false otherwise;
	// TODO: use $dbpediaOntologyPrefix in $settings (how do we access those settings in all scripts?)
	function areDBpediaPropertiesSet($triples, $properties) {
		foreach($properties as $property) {
			if(!isset($triples['http://dbpedia.org/ontology/'.$property])) {
				return false;
			}
		}
		return true;
	}
	
	// gets the value of the property
	function getPropValue($triples, $property) {
		return $triples['http://dbpedia.org/ontology/'.$property];
	}
	
	// gets the value of the property and removes it from the triple array
	// (this means you cannot access this information anymore afterwards)
	function extractPropValue($triples, $property) {
		$value = $triples['http://dbpedia.org/ontology/'.$property];
		unset($triples['http://dbpedia.org/ontology/'.$property]);
		return $value;
	}
	
	// displays as list of values for this property;
	// TODO: add toggle button here (as in remaining triples)
	function displayMultipleValues($triples, $property) {
		$objects = $triples[$property];
		foreach($objects as $object) {
			// ...
		}
	}
	
}

?>