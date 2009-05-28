<?php

include('Utilities.php');

// load WSDL files (has to be done due to a Java web service bug)
ini_set("soap.wsdl_cache_enabled","0");
$wsdluri="http://localhost:8181/services?wsdl";
Utilities::loadWSDLfiles($wsdluri);

$examples = array('http://dbpedia.org/resource/Leipzig', 'http://dbpedia.org/resource/Dresden');

$client = new SoapClient("main.wsdl",array('features' => SOAP_SINGLE_ELEMENT_ARRAYS));

$id = $client->generateID();

$ksID=$client->addKnowledgeSource($id,"sparql","http://dbpedia.org/sparql");
$client->applyConfigEntryInt($id, $ksID, "recursionDepth", 1);
$relatedInstances = $client->getNegativeExamples($id,$ksID,$examples,count($examples),"http://dbpedia.org/resource/",array());
$instances = array_merge($examples, $relatedInstances->item);
$client->applyConfigEntryStringArray($id, $ksID, "instances", $instances);

// $this->client->applyConfigEntryString($this->id, $this->ksID, "predefinedFilter", "DBPEDIA-NAVIGATOR");
// $this->client->applyConfigEntryString($this->id, $this->ksID, "predefinedEndpoint", $this->endpoint);
// $this->client->applyConfigEntryString($this->id, $this->ksID, "predefinedManipulator", "DBPEDIA-NAVIGATOR");

$rID = $client->setReasoner($id, "fastInstanceChecker");

$client->setLearningProblem($id, "posOnlyLP");
$client->setPositiveExamples($id, $examples);

$laID = $client->setLearningAlgorithm($id, "celoe");

$client->initAll($id);

echo 'start learning ... ';
$concept = $client->learnDescriptionsEvaluated($id, 5);
echo 'OK <br />';

?> 
