<?php
	/**
	 * iframe.php
	 * 
	 * xajax test script to test the ability to modify the contents
	 * of iframe documents within the main document.
	 */
	
	require_once("./options.inc.php");
	
	$aFunctions = $xajax->register(XAJAX_CALLABLE_OBJECT, new clsFunctions());
	$aFunctions['showformvalues']->addParameter(XAJAX_FORM_VALUES, 'theForm');
	
	$objResponse = new xajaxResponse();
	
	$xajax->processRequest();

?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<title>xajax Test Suite</title>
		<?php $xajax->printJavascript(); ?>
	</head>
	<body>
		<form id='theForm' method='post' action='#' onsubmit='<?php $aFunctions['showformvalues']->printScript(); ?>; return false;'>
			<input type='text' id='theText' name='theText' value='some text'><br />
			<input type='submit' id='theSubmit' name='theSubmit' value='Submit'>
		</form>
		<button onclick='<?php $aFunctions['clear']->printScript(); ?>'>Clear</button><br />
		<iframe id='theFrame' src='theFrame.php' style='width: 360px; height: 240px;'></iframe>
		<div id='outputDIV'></div>
		<br />
		<div>
		This test application demonstrates a number of features:
		<ul>
		<li>The ability to create and manipulate elements within an iframe.</li>
		<li>The ability to load a javascript file(s) inside an iframe</li>
		<li>The ability for iframes to have their own instance of xajax</li>
		<li>The ability for an iframe to communicate back to the parent</li>
		<li>The response command waitFor... which can wait for a custom condition to evaluate to true.</li>
		</ul>
		When you click the submit button, it will send a xajax request which in turn triggers the following:<br />
		The form values are displayed in the iframe and the main document.<br />
		The iframe is instructed to load a javascript file (iframe.js)<br />
		The main document frame waits until the javascript file is fully loaded (using waitFor)<br />
		The iframe.js invokes a xajax request (in the iframe to theFrame.php) which sleeps for 2 seconds, then returns 'iframe.js loaded'.<br />
		The iframe.js waits 4 seconds, then notifies the parent (main document) that the iframe.js file is fully loaded.<br />
		The main document displays that the iframe.js file is loaded in the iframe context.
		</div>
	</body>
</html>
<?php
	
	class clsContext {
		function begin($iframe) {
			global $objResponse;
			$objResponse->script("var theFrame = xajax.$('".$iframe."'); xajax.config.baseDocument = (theFrame.contentDocument || theFrame.contentWindow.document);");
		}
		function end() {
			global $objResponse;
			$objResponse->script("xajax.config.baseDocument = document;");
		}
	}
	
	class clsFunctions {
		function clsFunctions() {
		}
		
		function showIsLoaded() {
			global $objResponse;
			clsContext::begin("theFrame");
			$objResponse->script('try { if (iframe.code.loaded) xajax.$("outputDIV").innerHTML += "<br />iframe.js loaded"; } catch (e) { xajax.$("outputDIV").innerHTML += "<br />iframe.js *NOT* loaded"; }');
			clsContext::end();
			$objResponse->script('try { if (iframe.code.loaded) xajax.$("outputDIV").innerHTML += "<br />iframe.js loaded in iframe context"; } catch (e) { xajax.$("outputDIV").innerHTML += "<br />iframe.js *NOT* loaded in iframe context"; }');
		}
		function showFormValues($aFormValues) {
			global $objResponse;
			clsContext::begin("theFrame");
			$objResponse->assign("outputDIV", "innerHTML", print_r($aFormValues, true));
			$objResponse->includeScriptOnce("iframe.js");
			clsContext::end();
			$objResponse->assign("outputDIV", "innerHTML", print_r($aFormValues, true));
			$objResponse->waitFor("iframe.code.loaded", 90);
			$this->showIsLoaded();
			return $objResponse;
		}
		
		function clear() {
			global $objResponse;
			clsContext::begin('theFrame');
			$objResponse->assign('outputDIV', 'innerHTML', '');
			$objResponse->removeScript('iframe.js', 'iframe.code.unload();');
			clsContext::end();
			$objResponse->assign('outputDIV', 'innerHTML', '');
			$this->showIsLoaded();
			return $objResponse;
		}
	}

?>