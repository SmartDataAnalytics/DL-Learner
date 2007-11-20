<?php
/*
	File: jsLoadingTest.php

	Test the ability to load javascript at the request of the server.
	
	Title: Javascript loading test
	
	Please see <copyright.inc.php> for a detailed description, copyright
	and license information.
*/

/*
	@package xajax
	@version $Id: jsLoadingTest.php 361 2007-05-24 12:48:14Z calltoconstruct $
	@copyright Copyright (c) 2005-2006 by Jared White & J. Max Wilson
	@license http://www.xajaxproject.org/bsd_license.txt BSD License
*/
require_once("../../xajax_core/xajax.inc.php");
require_once("../../xajax_core/legacy.inc.php");

function testForm($formData)
{
	sleep(4);
	$objResponse = new legacyXajaxResponse();
	$objResponse->addAlert("This is from the function");
	$objResponse->addAssign("submittedDiv", "innerHTML", nl2br(print_r($formData, true)));
	return $objResponse;
}
$xajax = new legacyXajax();
$xajax->registerFunction("testForm");
$xajax->processRequests();
?>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<title>Javascript Loading Function Test (Legacy) | xajax Tests</title>
	<?php $xajax->printJavascript("../../") ?>
	<script type="text/javascript">
	function setup() {
		xajax.loadingFunction = function() { alert("This is the loadingFunction...") }
		xajax.doneLoadingFunction = function() { alert("This is the doneLoadingFunction...") }
	}
	</script>
</head>
<body onload="setup()">

	<h2><a href="index.php">xajax Legacy Mode Tests</a></h2>
	<h1>Javascript Loading Function Test (Legacy)</h1>

	<form id="testForm1" onsubmit="return false;">
	<p><input type="text" id="textBox1" name="textBox1" value="This is some text" /></p>
	<p><input type="submit" value="Submit Normal" onclick="xajax.call('testForm', [xajax.getFormValues('testForm1')]); return false;" /></p>
	</form>

	<div id="submittedDiv"></div>

</body>
</html>