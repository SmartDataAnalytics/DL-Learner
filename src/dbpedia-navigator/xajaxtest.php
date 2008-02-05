<?php 
require_once ("xajax/xajax_core/xajax.inc.php");
$xajax = new xajax();
$xajax->configureMany(array('debug'=>true));
$xajax->registerFunction('learnConcept');
$xajax->processRequest();

function learnConcept()
{
	$start=microtime(true);
	$client=new SoapClient("main.wsdl");
	$id=$client->generateID();
	$objResponse=new xajaxResponse();
	$objResponse->append("articlecontent","innerHTML",microtime(true)-$start);
	return $objResponse;
}
?>