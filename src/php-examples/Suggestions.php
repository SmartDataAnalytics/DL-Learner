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

$ksID=$client->addKnowledgeSource($id,"sparql","http://dbpedia.org/sparql");
$client->applyConfigEntryInt($id, $ksID, "recursionDepth", 1);
$filterClasses=array("http://xmlns.com/foaf/","http://dbpedia.org/class/yago/","http://dbpedia.org/ontology/Resource");
// $relatedInstances = $client->getNegativeExamples($id,$ksID,$examples,count($examples),"http://dbpedia.org/resource/",$filterClasses);
// $relatedInstances = $relatedInstances->item;
$relatedInstances = array('http://dbpedia.org/resource/Berlin','http://dbpedia.org/resource/London');
$instances = array_merge($examples, $relatedInstances);
$client->applyConfigEntryStringArray($id, $ksID, "instances", $instances);
// $client->applyConfigEntryString($id, $ksID, "predefinedFilter", "DBPEDIA-NAVIGATOR");
$client->applyConfigEntryString($id, $ksID, "predefinedEndpoint", "LOCALDBPEDIA");
$client->applyConfigEntryString($id, $ksID, "predefinedManipulator", "DBPEDIA-NAVIGATOR");
$client->applyConfigEntryBoolean($id, $ksID, "saveExtractedFragment", true);

$rID = $client->setReasoner($id, "fastInstanceChecker");

$client->setLearningProblem($id, "posOnlyLP");
$client->setPositiveExamples($id, $examples);

$laID = $client->setLearningAlgorithm($id, "celoe");

$client->initAll($id);

echo '<p>Positive examples:<br />';
foreach($examples as $example) {
	echo $example.'<br />';
}
echo '</p>';

echo '<p>Additional instances:<br />';
foreach($relatedinstances as $related) {
	echo $related.'<br />';
}
echo '</p>';

echo 'start learning ... ';
$concepts = $client->learnDescriptionsEvaluated($id, 5);
echo 'OK <br />';

$concepts = json_decode($concepts);

foreach($concepts as $concept) {
	echo $concept;
}

?> 
