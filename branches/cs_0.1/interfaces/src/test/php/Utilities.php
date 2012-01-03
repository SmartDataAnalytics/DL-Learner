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

// Pear HTTP_Request class required
include ('HTTP/Request.php');

/**
 * Collection of static utility functions. 
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 */

class Utilities {

	/**
	 * Loads WSDL and imported XSD files from web service and stores them
	 * locally. 
	 */
	public static function loadWSDLfiles($wsdluri) {
		$main = self :: getRequest($wsdluri);
//		echo "loaded";
		$other = self :: getXSDImports($main);
//		print_r($other);
		$newMain = self :: changeXSDImports($main);
		self :: writeToFile("main.wsdl", $newMain);
		$x = 0;
		foreach ($other as $o) {
			self :: writeToFile("def" . ($x++) . ".xsd", self :: getRequest($o));
		}
	}

	/**
	 * Change XSD imports in WSDL file to point to local imports.
	 */
	public static function changeXSDImports($wsdlFileContent) {
		$before = "<xsd:import schemaLocation=\"";
		$after = "\" namespace=\"";
		$newWSDL = "";
		$desca = "def";
		$descb = ".xsd";
		$x = 0;
		while ($posstart = strpos($wsdlFileContent, $before)) {
			$posstart += strlen($before);
			$newWSDL .= substr($wsdlFileContent, 0, $posstart);
			$wsdlFileContent = substr($wsdlFileContent, $posstart);
			$newWSDL .= $desca . ($x++) . $descb;
			$posend = strpos($wsdlFileContent, $after);
			$wsdlFileContent = substr($wsdlFileContent, $posend);
		}
		return $newWSDL . $wsdlFileContent;
	}

	/**
	 * Extracts XSD imports from WSDL file.
	 */
	public static function getXSDImports($wsdlFileContent) {
		/*
		preg_match_all('/\"[^\"\?]*?\?xsd=[^\"]*?\"/',$wsdlFileContent, $matches);
		return $matches;
		*/
		$before = "<xsd:import schemaLocation=\"";
		$after = "\" namespace=\"";
		$ret = array ();
		while ($posstart = strpos($wsdlFileContent, $before)) {
			$posstart += strlen($before);
			$wsdlFileContent = substr($wsdlFileContent, $posstart);
			$posend = strpos($wsdlFileContent, $after);
			$tmp = substr($wsdlFileContent, 0, $posend);
			$ret[] = $tmp;
			$wsdlFileContent = substr($wsdlFileContent, $posend +strlen($after));
		}
		return $ret;
	}

	/**
	 * Peforms a GET request and returns body of result.
	 */
	public static function getRequest($uri) {
		$req = & new HTTP_Request($uri);
		$req->setMethod(HTTP_REQUEST_METHOD_GET);
		$req->sendRequest();
		$ret = $req->getResponseBody();
		return $ret;
	}

	/**
	 * Writes $content to file $filename.
	 */
	public static function writeToFile($filename, $content) {
		$fp = fopen($filename, "w");
		fwrite($fp, $content);
		fclose($fp);
	}

	/**
	 * Prints a list of all Web Service components and their configuration options.
	 */
	public static function printWebserviceComponents($client) {
		echo '<h1>Web Service Information</h1>';

		echo '<h2>Knowledge Sources</h2>';
		Utilities :: printComponentsInfo($client, $client->getKnowledgeSources()->item);

		echo '<h2>Reasoners</h2>';
		Utilities :: printComponentsInfo($client, $client->getReasoners()->item);

		echo '<h2>Learning Problems</h2>';
		Utilities :: printComponentsInfo($client, $client->getLearningProblems()->item);

		echo '<h2>Learning Algorithms</h2>';
		Utilities :: printComponentsInfo($client, $client->getLearningAlgorithms()->item);
	}

	/**
	 * Print information about all given components.
	 */
	public static function printComponentsInfo($client, $components) {
		foreach ($components as $component)
			Utilities :: printComponentInfo($client, $component);
	}

	/**
	 * Print information about a component.
	 */
	public static function printComponentInfo($client, $component) {
		echo '<h3>component: ' . $component . '</h3>';

		$options = $client->getConfigOptions($component, true)->item;
		if (!is_array($options))
			$options = array (
				$options
			);

		foreach ($options as $option)
			Utilities :: printOption($option);
	}

	/**
	 * Prints information about an option.
	 * 
	 * @param String Option as returned by the DL-Learner web service 
	 * getConfigOption() method.
	 */
	public static function printOption($option) {
		$parts = split('#', $option);
		echo 'option name: <b>' . $parts[0] . '</b><br />';
		echo 'option description: ' . $parts[1] . '<br />';
		echo 'option class: ' . $parts[2] . '<br />';
		if ($parts[3] != 'null')
			echo 'option name: ' . $parts[3] . '<br />';
		echo '<br />';
	}
}
?>
