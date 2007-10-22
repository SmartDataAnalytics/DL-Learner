<?php
	/*
		File: server_events.php
		
		Test script that uses <xajax->register> to register an event and some event
		handlers to process requests from the client.
	*/

	require_once("./options.inc.php");
	
	function eventHandlerOne()
	{
		$objResponse = new xajaxResponse();
		$objResponse->append('output', 'innerHTML', 'Message from event handler one.<br />');
		return $objResponse;
	}
	
	function eventHandlerTwo()
	{
		$objResponse = new xajaxResponse();
		$objResponse->append('output', 'innerHTML', 'Message from event handler two.<br />');
		return $objResponse;
	}
	
	class clsEventHandlers
	{
		function eventHandlerThree()
		{
			$objResponse = new xajaxResponse();
			$objResponse->append('output', 'innerHTML', 'Message from event handler three.<br />');
			$objResponse->setReturnValue('return value from event handler three.');
			return $objResponse;
		}
	}
	
	$objEventHandlers = new clsEventHandlers();
	$objEventHandlerThree = new xajaxUserFunction(array(&$objEventHandlers, 'eventHandlerThree'));
	
	$requestEvent = $xajax->register(XAJAX_EVENT, 'theOneAndOnly', array("mode" => "synchronous"));
	
	$xajax->register(XAJAX_EVENT_HANDLER, 'theOneAndOnly', 'eventHandlerOne');
	$xajax->register(XAJAX_EVENT_HANDLER, 'theOneAndOnly', 'eventHandlerTwo');
	$xajax->register(XAJAX_EVENT_HANDLER, 'theOneAndOnly', $objEventHandlerThree);
	
	$xajax->processRequest();
	
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Server-side Events</title>

		<?php $xajax->printJavascript() ?>

		<script type="text/javascript">
			/* <![CDATA[ */
			/* ]]> */
		</script>

		<style type='text/css'>
			/* <![CDATA[ */
			/* ]]> */
		</style>

	</head>
	<body>
		<h1>Server-side Events</h1>
		
		<button onclick='alert("The event handlers returned ["+<?php $requestEvent->printScript(); ?>+"]");'>Fire event</button>
		
		<div id='output'>
		</div>
		
		<div class='description'>
		This script demonstrates xajax's ability to register multiple 'handler' functions that will be called (in the order they are registered)
		in response to a single request.  A variety of call options can be set for the event request including mode, method and context.
		</div>
	</body>
</html>
