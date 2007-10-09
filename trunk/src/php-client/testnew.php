<?php

include_once("LearnerClient.php");

ini_set("soap.wsdl_cache_enabled","0");

$wsdluri="http://localhost:8181/services?wsdl";

// always update WSDL
LearnerClient::loadWSDLfiles($wsdluri);

// test web service
$client = new SoapClient("main.wsdl");

$test = $client->hello();
echo $test;

$stringar = array('blub','blab');
$ret = $client->test($stringar);
print_r($ret);

?>