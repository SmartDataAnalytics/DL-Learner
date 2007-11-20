<?php

$sBaseFolder = dirname(dirname(dirname(__FILE__)));

require_once("./options.inc.php");

$requestURI = $xajax->_detectURI();

$requestURI = str_replace('methodOne=1', '', $requestURI);
$requestURI = str_replace('methodTwo=1', '', $requestURI);
$requestURI = str_replace('methodThree=1', '', $requestURI);

$reqShowOutput =& $xajax->register(XAJAX_FUNCTION, "showOutput");

class testPlugin extends xajaxResponsePlugin
{
	function getName()
	{
		return "testPlugin";
	}
	
	function generateClientScript()
	{
		echo "\n<script type='text/javascript' charset='UTF-8'>\n"; // " . $this->sDefer . "
		echo "/* <![CDATA[ */\n";

		echo "xajax.commands['testPlg'] = function(args) { \n";
		echo "\talert('Test plugin command received: ' + args.data);\n";
		echo "}\n";

		echo "/* ]]> */\n";
		echo "</script>\n";
	}
	
	function testMethod()
	{
		$this->addCommand(array('n'=>'testPlg'), 'abcde]]>fg');	
	}
}

$objPluginManager =& xajaxPluginManager::getInstance();
$objPluginManager->registerPlugin(new testPlugin());

$xajax->processRequest();

$sRoot = dirname(dirname(dirname(__FILE__)));

if (false == class_exists('xajaxControl')) {
	$sCore = '/xajax_core';
	include_once($sRoot . $sCore . '/xajaxControl.inc.php');
}

$sControls = '/xajax_controls';
foreach (array(
	'/document.inc.php',
	'/content.inc.php',
	'/group.inc.php',
	'/structure.inc.php',
	'/misc.inc.php') as $sFile)
	include_once($sRoot . $sControls . $sFile);

$buttonShowOutput = new clsButton(array(
	'attributes' => array('id' => 'btnShowOutput'),
	'child' => new clsLiteral('Show Response XML'),
	'event' => array('onclick', $reqShowOutput)
	));

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/2000/REC-xhtml1-20000126/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Response Plugin Test</title>
<?php $xajax->printJavascript() ?>
</head>
<body>

<h2><a href="index.php">xajax Tests</a></h2>
<h1>Basic Plugin Test</h1>

<div>
<?php
	if (isset($_GET['methodOne']))
		echo 'Using plugin access method One.';
	else if (isset($_GET['methodTwo']))
		echo 'Using plugin access method Two.';
	else if (isset($_GET['methodThree']))
		echo 'Using plugin access method Three.';
	else
		$buttonShowOutput->setAttribute('disabled', 'disabled');
?>
</div>

<form id="testForm1" onsubmit="return false;">
<p><?php $buttonShowOutput->printHTML(); ?></p>
</form>

<div>
	<div>
		Please select a method appropriate to the version of php running on your host:
	<div>
<?php
	$sSeparator = '?';
	if (false !== strstr($requestURI, '?'))
		$sSeparator = '&';

	$newRequestURI = $requestURI . $sSeparator . 'methodOne=1';
	$newRequestURI = str_replace('?&', '?', str_replace('&&', '&', $newRequestURI));
	
	echo "<a href='" . $newRequestURI . "'>PHP4 & PHP5</a> ";

	$newRequestURI = $requestURI . $sSeparator . 'methodTwo=1';
	$newRequestURI = str_replace('?&', '?', str_replace('&&', '&', $newRequestURI));
	
	echo "<a href='" . $newRequestURI . "'>PHP5</a> ";

	$newRequestURI = $requestURI . $sSeparator . 'methodThree=1';
	$newRequestURI = str_replace('?&', '?', str_replace('&&', '&', $newRequestURI));
	
	echo "<a href='" . $newRequestURI . "'>PHP5 (type 2)</a>";
?>
</div>

<div id="submittedDiv"></div>

<div class='description'>
<p>This test script demonstrates the ability to declare and register response plugins then use
the registered plugin to send back new response commands to the client.
<p>To use this script, select an option (above) that matches your server configuration; all methods
can be used on a PHP5 based server.
<p>The plugin will register a javascript command handler, then, upon request, it will add
a response command that will be sent back to the client, invoking the command on the browser.
<p>In this case, the plugin simply shows an alert dialog beginning with the text 'Test plugin 
command received:' then some data sent from the server.
<p>The test script also outputs a preformatted string that represents a response object 
that includes the plugin response command.  NOTE:  This is not a copy of the response that IS sent
to the browser, just an example of one.
</div>

</body>
</html>
<?php

function showOutput()
{
	$testResponse = new xajaxResponse();
	$testResponse->alert("This is the text that would be displayed in an alert box.");
	
	// PHP4 & PHP5
	if (isset($_GET['methodOne']))
		eval('$testResponse->plugin("testPlugin", "testMethod");');
	
	// PHP5 ONLY - Uncomment to test
	if (isset($_GET['methodTwo']))
		eval('$testResponse->plugin("testPlugin")->testMethod();');
	
	// PHP5 ONLY - Uncomment to test
	if (isset($_GET['methodThree']))
		eval('$testResponse->testPlugin->testMethod();');
	
	$testResponseOutput = '<pre>' 
		. htmlspecialchars(str_replace("><", ">\n<", $testResponse->getOutput())) 
		. '</pre>';
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("submittedDiv", "innerHTML", $testResponseOutput);
	if (isset($_GET['methodOne']))
		eval('$objResponse->plugin("testPlugin", "testMethod");');
	if (isset($_GET['methodTwo']))
		eval('$objResponse->plugin("testPlugin")->testMethod();');
	if (isset($_GET['methodThree']))
		eval('$objResponse->testPlugin->testMethod();');
	return $objResponse;
}

?>