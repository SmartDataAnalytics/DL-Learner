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
  * Small example showing how to use DL-Learner for learning class definitions. 
  * 
  * @author Jens Lehmann
  */
  
include('Utilities.php');

// load WSDL files (has to be done due to a Java web service bug)
ini_set("soap.wsdl_cache_enabled","0");
$wsdluri="http://localhost:8181/services?wsdl";
Utilities::loadWSDLfiles($wsdluri);

// specifiy ontology
$ontology = 'file:'.realpath("../../examples/family/father.owl");

// create DL-Learner client
$client = new SoapClient("main.wsdl");
// $client = new SoapClient($wsdluri);

// load owl file in DIG reasoner (you need a running DIG reasoner)
$id = $client->generateID();
$ksID = $client->addKnowledgeSource($id, "owlfile", $ontology);
$rID = $client->setReasoner($id, "owlapi");

// create a learning problem
$posExamples = array('http://example.com/father#stefan',
                     'http://example.com/father#markus',
                     'http://example.com/father#martin');
$negExamples = array('http://example.com/father#heinz',
                     'http://example.com/father#anna',
                     'http://example.com/father#michelle');
$client->setLearningProblem($id, "posNegDefinition");
$client->setPositiveExamples($id, $posExamples);
$client->setNegativeExamples($id, $negExamples);

// choose refinement operator approach
$client->setLearningAlgorithm($id, "refinement");

$client->initAll($id);

// learn concept
echo 'start learning ... ';
$concept = $client->learn($id, "manchester");
echo 'OK <br />';
echo 'solution: ' . $concept;

?>
