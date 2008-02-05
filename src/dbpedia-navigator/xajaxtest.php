<?php 
require_once ("xajax/xajax_core/xajax.inc.php");
$xajax = new xajax();
$xajax->configureMany(array('debug'=>true));
$xajax->registerFunction('learnConcept');
$xajax->processRequest();

function learnConcept()
{
	$client=new SoapClient("main.wsdl");
	$id=$client->generateID();
	$objResponse=new xajaxResponse();
	$objResponse->append("articlecontent","innerHTML","Test");
	return $objResponse;
}
?>