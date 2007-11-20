<?php
	/*
		File: alert_confirm.php
		
		Test script for the following <xajaxResponse> commands:
			- <xajaxResponse->alert>
			- <xajaxResponse->confirmCommands>
	*/
	
	require_once("./options.inc.php");

	/*
		Function: sendAlert
		
		See <clsPage->sendAlert>
	*/
	function sendAlert($sValue) {
		global $page;
		return $page->sendAlert($sValue);
	}
	
	/*
		Function: sendConfirmCommands
		
		See <clsPage->sendAlert>
	*/
	function sendConfirmCommands() {
		global $page;
		return $page->sendConfirmCommands();
	}

	/*
		Class: clsPage
		
		Contains functions that will be registered and called from the browser.
	*/		
	class clsPage {
		/*
			Function: clsPage
			
			Constructor
		*/
		function clsPage() {
		}
		
		/*
			Function: sendAlert
			
			Generates a <xajaxResponse->alert> command that displays a message.
		*/
		function sendAlert($sValue) {
			$objResponse = new xajaxResponse();
			$objResponse->alert("Message from the sendAlert handler [{$sValue}].");
			return $objResponse;
		}
		
		/*
			Function: sendConfirmCommands
			
			Generates a <xajaxResponse->confirmCommands> command that will prompt
			the user if they want to see the alert, then a <xajaxResponse->alert>
			command.
		*/
		function sendConfirmCommands() {
			$objResponse = new xajaxResponse();
			$objResponse->confirmCommands(1, 'Do you want to see an alert next?');
			$objResponse->alert("Here is the alert!");
			return $objResponse;
		}
	}
	
	$page = new clsPage();
	$aPageRequests =& $xajax->register(XAJAX_CALLABLE_OBJECT, $page);
	$aPageRequests["sendalert"]->addParameter(XAJAX_QUOTED_VALUE, 'from callable object');
	
	$requestSendAlert =& $xajax->register(XAJAX_FUNCTION, 'sendAlert');
	$requestSendAlert->addParameter(XAJAX_QUOTED_VALUE, 'from function at global scope');
	$requestSendConfirmCommands =& $xajax->register(XAJAX_FUNCTION, 'sendConfirmCommands');
	
	$requestShowFormValues =& $xajax->register(
		XAJAX_FUNCTION, 
		new xajaxUserFunction('showFormValues', 'alert_confirm_external.inc.php'), 
		array('mode'=>'"synchronous"'));
	$requestShowFormValues->addParameter(XAJAX_FORM_VALUES, 'theForm');
	
	$requestSendBothEvent =& $xajax->register(XAJAX_EVENT, 'sendBoth');
	$requestSendBothEvent->addParameter(XAJAX_QUOTED_VALUE, 'from event handler');
	
	$xajax->register(XAJAX_EVENT_HANDLER, 'sendBoth', 'sendAlert');
	$xajax->register(XAJAX_EVENT_HANDLER, 'sendBoth', 'sendConfirmCommands');
	
	$xajax->processRequest();
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<title>xajax Test Suite</title>
		<?php $xajax->printJavascript('../../'); ?>
	</head>
	<body>
		<div>
			<div>Using callable object:</div>
			<a href='#' onclick='<?php $aPageRequests['sendalert']->printScript(); ?>; return false;'>Send Alert</a><br />
			<a href='#' onclick='<?php $aPageRequests['sendconfirmcommands']->printScript(); ?>; return false;'>Send Confirm Commands</a><br />
		</div>
		<div>
			<div>Using functions at global scope:</div>
			<a href='#' onclick='<?php $requestSendAlert->printScript(); ?>; return false;'>Send Alert</a><br />
			<a href='#' onclick='<?php $requestSendConfirmCommands->printScript(); ?>; return false;'>Send Confirm Commands</a><br />
		</div>
		<div>
			<div>Using event handler:</div>
			<a href='#' onclick='<?php $requestSendBothEvent->printScript(); ?>; return false;'>Send Both</a><br />
		</div>
		<div>
			<div>Using function at global scope from an external (include) file :</div>
			<a href='#' onclick='<?php $requestShowFormValues->printScript(); ?>; return false;'>Send Alert</a><br />
		</div>
		
		<form id="theForm" method="post" action="#">
			<input name="test" value="test value" />
			<input name="other" value="other" />
		</form>
	</body>
</html>

