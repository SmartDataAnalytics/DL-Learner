<?php

// load WSDL files (has to be done due to a Java web service bug)
ini_set("soap.wsdl_cache_enabled","0");
$wsdluri="http://127.0.0.1:8181/services?wsdl";
if(!file_exists('main.wsdl')) {
	include('Utilities.php');
	Utilities::loadWSDLfiles($wsdluri);
}

$examples = array('http://dbpedia.org/resource/Leipzig', 'http://dbpedia.org/resource/Dresden');

$client = new SoapClient("main.wsdl",array('features' => SOAP_SINGLE_ELEMENT_ARRAYS));

$id = $client->generateID();

$ksID=$client->addKnowledgeSource($id,"sparqlfrag","http://dbpedia.org/sparql");
$client->applyConfigEntryInt($id, $ksID, "recursionDepth", 2);
$filterClasses=array("http://xmlns.com/foaf/","http://dbpedia.org/class/yago/","http://dbpedia.org/ontology/Resource");
$relatedInstances = array('http://dbpedia.org/resource/Berlin','http://dbpedia.org/resource/London');
$instances = array_merge($examples, $relatedInstances);
$client->applyConfigEntryStringArray($id, $ksID, "instances", $instances);
$client->applyConfigEntryString($id, $ksID, "predefinedFilter", "DBPEDIA-NAVIGATOR");
$client->applyConfigEntryString($id, $ksID, "predefinedEndpoint", "DBPEDIA");
$client->applyConfigEntryBoolean($id, $ksID, "saveExtractedFragment", true);
$client->applyConfigEntryBoolean($id, $ksID, "propertyInformation", true);

$rID = $client->setReasoner($id, "cwr");

$client->setLearningProblem($id, "posonlylp");
$client->setPositiveExamples($id, $examples);

$laID = $client->setLearningAlgorithm($id, "celoe");
$client->applyConfigEntryInt($id, $laID, "maxExecutionTimeInSeconds", 1);

$client->initAll($id);

echo '<p>Positive examples:<br />';
foreach($examples as $example) {
	echo $example.'<br />';
}
echo '</p>';

echo '<p>Additional instances:<br />';
foreach($relatedInstances as $related) {
	echo $related.'<br />';
}
echo '</p>';

echo '<p>start learning ... ';
$startTime = microtime(true);
$concepts = $client->learnDescriptionsEvaluated($id, 10);
$runTime = microtime(true) - $startTime;
echo 'OK ('.$runTime.' seconds)</p>';

$concepts = json_decode($concepts);

echo '<table border="1px"><tr><td><i>Manchester OWL Syntax</i></td><td><i>accuracy</i></td></tr>';
foreach($concepts as $concept) {
	// echo $natural . '(Manchester: ' . $concept->descriptionManchesterSyntax . ', acc. ' . $concept->scoreValue . ')<br />'; ;
	echo '<tr><td>'.$concept->descriptionManchesterSyntax.'</td><td>'.$concept->scoreValue.'</td></tr>';
}
echo '</table>';

?> 
