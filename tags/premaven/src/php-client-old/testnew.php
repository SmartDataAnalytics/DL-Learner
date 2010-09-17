<?php

include_once("LearnerClient.php");

ini_set("soap.wsdl_cache_enabled","0");

$wsdluri="http://localhost:8181/services?wsdl";
$ontology="file:/home/jl/promotion/dl-learner-svn/trunk/examples/father.owl";
// $ontology="file:/home/jl/programmierung/eclipse_workspace/DL-Learner-SVN/examples/father.owl";

$posExamples = array('http://example.com/father#stefan',
                     'http://example.com/father#markus',
                     'http://example.com/father#martin');
$negExamples = array('http://example.com/father#heinz',
                     'http://example.com/father#anna',
                     'http://example.com/father#michelle');

// always update WSDL
LearnerClient::loadWSDLfiles($wsdluri);

// test web service
$client = new SoapClient("main.wsdl");

$print_ws_info = false;

if($print_ws_info) {
	$components = $client->getComponents()->item;
	
	echo '<h1>Web Service Information</h1>';
	
	foreach($components as $component) {
		echo '<h2>component '.$component.'</h2>';	
	
		$options = $client->getConfigOptions($component, true)->item;
		if(!is_array($options))
			$options = array($options);
		
		foreach($options as $option)
			echo $option.'<br />';
	}
}

echo '<h1>Algorithm Run</h1>';

$id = $client->generateID();

$ksID = $client->addKnowledgeSource($id, "owlfile", $ontology);
// echo $client->getConfigOptionValueString($id, $ksID, "url");
$client->setReasoner($id, "dig");
$client->setLearningProblem($id, "posNegDefinition");
$client->setPositiveExamples($id, $posExamples);
$client->setNegativeExamples($id, $negExamples);
$client->setLearningAlgorithm($id, "refinement");

$start = microtime(true);

$client->initAll($id);

$learn_start = microtime(true);
$init = $learn_start - $start;
echo 'components initialised in '.$init.' seconds<br />';

$threaded = true;

if($threaded == false) {

	$concept = $client->learn($id);
	
	$learn = microtime(true) - $learn_start;
	echo 'concept learned in '.$learn.' seconds<br />';
	
	echo 'result: '.$concept;
	
} else {

	$client->learnThreaded($id);
	
	$i = 1;
	$sleeptime = 1;
	
	do {
		// sleep a while
		sleep($sleeptime);
		
		// see what we have learned so far
		$concept=$client->getCurrentlyBestConcept($id);
		$running=$client->isAlgorithmRunning($id);
		
		$seconds = $i * $sleeptime;
		
		echo 'result after '.$seconds.' seconds of sleep: '.$concept.'<br />';
		
		$i++;
	} while($running);
	
	echo 'algorithm finished';
}

?>