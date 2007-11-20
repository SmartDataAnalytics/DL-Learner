<?php
/*
	File: catchAllFunctionTest.php

	Unit test page for the catch all function.
	
	Title: Catch-all function test.
	
	Please see <copyright.inc.php> for a detailed description, copyright
	and license information.
*/

/*
	@package xajax
	@version $Id: catchAllFunctionTest.php 361 2007-05-24 12:48:14Z calltoconstruct $
	@copyright Copyright (c) 2005-2006 by Jared White & J. Max Wilson
	@license http://www.xajaxproject.org/bsd_license.txt BSD License
*/

require_once("../../xajax_core/xajax.inc.php");
require_once("../../xajax_core/legacy.inc.php");

function test2ndFunction($formData, $objResponse)
{
	$objResponse->addAlert("formData: " . print_r($formData, true));
	$objResponse->addAssign("submittedDiv", "innerHTML", nl2br(print_r($formData, true)));
	return $objResponse->getXML();
}

function myCatchAllFunction($funcName, $args)
{
	$objResponse = new legacyXajaxResponse();
	$objResponse->addAlert("This is from the catch all function");
//	return $objResponse;
	return test2ndFunction($args[0], $objResponse);
}

function testForm($formData)
{
	$objResponse = new legacyXajaxResponse();
	$objResponse->addAlert("This is from the regular function");
	return test2ndFunction($formData, $objResponse);
}
$xajax = new legacyXajax();
$xajax->registerCatchAllFunction("myCatchAllFunction");
//$xajax->registerFunction("testForm");
$xajax->processRequests();
?>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<title>Catch-all Function Test (Legacy) | xajax Tests</title>
	<?php $xajax->printJavascript("../../") ?>
</head>
<body>

	<h2><a href="index.php">xajax Legacy Mode Tests</a></h2>
	<h1>Catch-all Function Test (Legacy)</h1>

	<form id="testForm1" onsubmit="return false;">
	<p><input type="text" id="textBox1" name="textBox1" value="This is some text" /></p>
	<p><input type="submit" value="Submit Normal" onclick="xajax.call('testForm', [xajax.getFormValues('testForm1')]); return false;" /></p>
	</form>

	<div id="submittedDiv"></div>

</body>
</html>