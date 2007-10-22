<?php
/*
	File: preFunctionTest.php

	Example which demonstrates the use of a preFunction.
	
	Title: preFunction test
	
	Please see <copyright.inc.php> for a detailed description, copyright
	and license information.
*/

/*
	@package xajax
	@version $Id: preFunctionTest.php 361 2007-05-24 12:48:14Z calltoconstruct $
	@copyright Copyright (c) 2005-2006 by Jared White & J. Max Wilson
	@license http://www.xajaxproject.org/bsd_license.txt BSD License
*/

require_once("../../xajax_core/xajax.inc.php");
require_once("../../xajax_core/legacy.inc.php");

function testRegularFunction($formData)
{
	$objResponse = new legacyXajaxResponse();
	$objResponse->addAlert("formData: " . print_r($formData, true));
	$objResponse->addAssign("submittedDiv", "innerHTML", nl2br(print_r($formData, true)));
	return $objResponse->getXML();
}

function myPreFunction($funcName, $args)
{
	$objResponse = new legacyXajaxResponse();
	if ($args[1] == 0) {
		$objResponse->addAlert("This is from the pre-function, which will now call " . $funcName);
		return $objResponse;
	}
	$objResponse->addAlert("This is from the pre-function, which will now end the request.");
	return array(false, $objResponse);
}

class myPreObject
{
	var $message = "This is from the pre-function object method";
	
	function preMethod($funcName, $args)
	{
		$objResponse = new legacyXajaxResponse();
		if ($args[1] == 0) {
			$objResponse->addAlert($this->message . ", which will now call " . $funcName);
			return $objResponse;
		}
		$objResponse->addAlert($this->message . ", which will now end the request.");
		return array(false, $objResponse);		
	}
}

$xajax = new legacyXajax();
$xajax->debugOn();
if (@$_GET['useObjects'] == "true") {
	$preObj = new myPreObject();
	$xajax->registerPreFunction(array("myPreFunction", &$preObj, "preMethod"));
}
else {
	$xajax->registerPreFunction("myPreFunction");
}
$xajax->registerFunction("testRegularFunction");
$xajax->processRequests();
?>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<title>Pre-function Test (Legacy) | xajax Tests</title>
	<?php $xajax->printJavascript("../../") ?>
</head>
<body>

	<h2><a href="index.php">xajax Legacy Mode Tests</a></h2>
	<h1>Pre-function Test (Legacy)</h1>

	<form id="testForm1" onsubmit="return false;">
	<p><input type="text" id="textBox1" name="textBox1" value="This is some text" /></p>
	<p><input type="submit" value="Normal request" onclick="xajax_testRegularFunction(xajax.getFormValues('testForm1'), 0); return false;" /></p>
	<p><input type="submit" value="Pre-function should end request" onclick="xajax_testRegularFunction(xajax.getFormValues('testForm1'), 1); return false;" /></p>
	</form>

	<p><a href="preFunctionTest.php?useObjects=true">Reload using object</a></p>

	<div id="submittedDiv"></div>

</body>
</html>