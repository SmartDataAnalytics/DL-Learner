<?php
	$xajaxCore = "../../xajax_core";
	$xajaxInclude = "/xajax.inc.php";
	
	if (isset($_GET['AIO']))
		if (0 != $_GET['AIO'])
			$xajaxInclude = "/xajaxAIO.inc.php";

	require $xajaxCore . $xajaxInclude;

	$xajax = new xajax();
	
	$xajax->configure('javascript URI', '../../');

	if (isset($_GET['debugging']))
		if (0 != $_GET['debugging'])
			$xajax->configure("debug", true);
	if (isset($_GET['verbose']))
		if (0 != $_GET['verbose'])
			$xajax->configure("verboseDebug", true);
	if (isset($_GET['status']))
		if (0 != $_GET['status'])
			$xajax->configure("statusMessages", true);
	if (isset($_GET['synchronous']))
		if (0 != $_GET['synchronous'])
			$xajax->configure("defaultMode", "synchronous");
	if (isset($_GET['useEncoding']))
		$xajax->configure("characterEncoding", $_GET['useEncoding']);
	if (isset($_GET['outputEntities']))
		if (0 != $_GET['outputEntities'])
			$xajax->configure("outputEntities", true);
	if (isset($_GET['decodeUTF8Input']))
		if (0 != $_GET['decodeUTF8Input'])
			$xajax->configure("decodeUTF8Input", true);
	if (isset($_GET['scriptDeferral']))
		if (0 != $_GET['scriptDeferral'])
			$xajax->configure('deferScriptGeneration', true);
	if (isset($_GET['lang_de']))
		if (0 != $_GET['lang_de'])
			$xajax->configure('language', 'de');
?>