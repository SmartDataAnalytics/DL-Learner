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
  * Small example showing how to use DL-Learner for learning 
  * a class definition using a separate learning thread. 
  * 
  * @author Jens Lehmann
  */
  
include('Utilities.php');

// load WSDL files (has to be done due to a Java web service bug)
ini_set("soap.wsdl_cache_enabled","0");
$wsdluri="http://localhost:8181/services?wsdl";
Utilities::loadWSDLfiles($wsdluri);

// specifiy ontology
$ontology = 'file:'.realpath("../../examples/family/uncle.owl");

// create DL-Learner client
$client = new SoapClient("main.wsdl");

// load owl file in reasoner
$id = $client->generateID();

$state = $client->getState($id);

var_dump($state);
echo 'STATE END';

$ksID = $client->addKnowledgeSource($id, "owlfile", $ontology);
$rID = $client->setReasoner($id, "owlapi");

// create a learning problem
$posExamples = array(
	'http://localhost/foo#heinrich',
	'http://localhost/foo#alfred',
	'http://localhost/foo#heinz',
	'http://localhost/foo#hans',
	'http://localhost/foo#alex');
$negExamples = array(
	'http://localhost/foo#jan',
	'http://localhost/foo#markus',
	'http://localhost/foo#susi',
	'http://localhost/foo#anna',
	'http://localhost/foo#maria',
	'http://localhost/foo#katrin',
	'http://localhost/foo#johanna',
	'http://localhost/foo#mandy',
	'http://localhost/foo#miriam',
	'http://localhost/foo#hanna');
$client->setLearningProblem($id, "posNegLPStandard");
$client->setPositiveExamples($id, $posExamples);
$client->setNegativeExamples($id, $negExamples);

// choose refinement operator approach
$client->setLearningAlgorithm($id, "refinement");

$client->initAll($id);

// start learning process in DL-Learner
$client->learnThreaded($id);
	
$sleeptime = 2;
$seconds = 0;
	
do {
	// sleep a while
	sleep($sleeptime);
		
	// see what we have learned so far
	$concept=$client->getCurrentlyBestConcept($id);
	$running=$client->isAlgorithmRunning($id);
		
	$seconds += $sleeptime;
	if($seconds == 10)
		$sleeptime = 5;
		
	echo 'result after '.$seconds.' seconds: '.$concept.'<br />';
	flush();	
	
} while($running);
	
// print best concepts found (not all of which are
// necessarily perfect solutions)
echo '<br />Algorithm finished. Best concepts: ';
echo '<pre>';
print_r($client->getCurrentlyBestConcepts($id, 10));
echo '</pre>';

?>
