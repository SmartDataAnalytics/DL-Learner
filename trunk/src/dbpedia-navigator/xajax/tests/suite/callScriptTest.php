<?php
/*
	File: callScriptTest.php
	
	Test script that uses the <xajaxResponse->call> command to execute
	a function call on the browser.
*/

require_once("./options.inc.php");

function callScript()
{
	$response = new xajaxResponse();
	$value2 = "this is a string";
	$response->call("myJSFunction", "arg1", 9432.12, array("myKey" => "some value", "key2" => $value2));
	return $response;
}

function callOtherScript()
{
	$response = new xajaxResponse();
	$response->call("myOtherJSFunction");
	return $response;
}

$requestCallScript =& $xajax->register(XAJAX_FUNCTION, "callScript");
$requestCallOtherScript =& $xajax->register(XAJAX_FUNCTION, "callOtherScript");

$xajax->processRequest();

$sRoot = dirname(dirname(dirname(__FILE__)));

if (false == class_exists('xajaxControl')) {
	$sCore = '/xajax_core';
	include_once($sRoot . $sCore . '/xajaxControl.inc.php');
}

$sControls = '/xajax_controls';
include_once($sRoot . $sControls . '/document.inc.php');
include_once($sRoot . $sControls . '/content.inc.php');
include_once($sRoot . $sControls . '/group.inc.php');
include_once($sRoot . $sControls . '/form.inc.php');
include_once($sRoot . $sControls . '/misc.inc.php');

$buttonCallScript = new clsButton(array(
	'attributes' => array('id' => 'call_script'),
	'children' => array(new clsLiteral('Click Me'))
	));
$buttonCallScript->setEvent('onclick', $requestCallScript);

$buttonCallOtherScript = new clsButton(array(
	'attributes' => array('id' => 'call_other_script'),
	'children' => array(new clsLiteral('or Click Me'))
	));
$buttonCallOtherScript->setEvent('onclick', $requestCallOtherScript);

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>call Script Test | xajax Tests</title>
	<?php $xajax->printJavascript() ?>
	<script type="text/javascript">
		function myJSFunction(firstArg, numberArg, myArrayArg)
		{
			var newString = firstArg + " and " + (+numberArg + 100) + "\n";
			newString += myArrayArg["myKey"] + " | " + myArrayArg.key2;
			alert(newString);
			xajax.$('myDiv').innerHTML = newString;
		}
		function myOtherJSFunction() {
			var newString = 'No parameters needed for this function.';
			alert(newString);
			xajax.$('myDiv').innerHTML = newString;
		}
	</script>
</head>
<body>

<h1>call Script Test</h1>

<p>Howdy.</p>
<p><?php $buttonCallScript->printHTML(); ?></p>
<p><?php $buttonCallOtherScript->printHTML(); ?></p>

<p>Result:</p>

<pre id="myDiv">[blank]</pre>

<p>Expecting:</p>

<pre>arg1 and 9532.12
some value | this is a string</pre>

<div class='description'>
	<p>This script demonstrates the ability for xajax to send data to and call javascript 
	functions on the client browser.
	<p>The first button, Click Me, will call a xajax function on the server, which will in
	turn send a response command back to the client to call myJSFunction.  myJSFunction accepts 
	three parameters which should match the text following 'Expecting:'.
	<p>The second button, or Click Me, will call a xajax function on the server which will return
	a response command back to the client to call myOtherJSFunction.  myOtherJSFunction does not
	accept any parameters and simply pops up and alert explaining so.
</div>

</body>
</html>
