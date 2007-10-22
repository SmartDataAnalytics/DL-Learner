<?php
	/*
		File: delayEvents.php
		
		Test script that uses <xajaxResponse->sleep>, <xajaxResponse->call> and a variety
		of callback request options to show the progress of a series of requests.
	*/
	
	require_once("./options.inc.php");
	
	$objResponse = new xajaxResponse();
	
	class clsRequests
	{
		function clsRequests() {
		}
		
		function clearLog() {
			global $objResponse;
			$objResponse->clear('log', 'innerHTML');
			return $objResponse;
		}
		
		function shortDelay($sleepTimes) {
			global $objResponse;
			foreach ($sleepTimes AS $sleepTime)
				sleep((int)$sleepTime);
			$objResponse->append('log', 'innerHTML', 'Message from server: Request completed before abort.<br />');
			$objResponse->sleep(30);
			$objResponse->call('finished', $sleepTimes);
			return $objResponse;
		}
		
		function mediumDelay($sleepTimes) {
			global $objResponse;
			foreach ($sleepTimes AS $sleepTime)
				sleep((int)$sleepTime);
			$objResponse->append('log', 'innerHTML', 'Message from server: Request completed before abort.<br />');
			$objResponse->call('finished', $sleepTimes);
			return $objResponse;
		}
		
		function longDelay() {
			global $objResponse;
			sleep(15);
			$objResponse->append('log', 'innerHTML', 'Message from server: Request completed before abort.<br />');
			return $objResponse;
		}
		
		function shortDelayS() {
			global $objResponse;
			sleep(5);
			$objResponse->append('log', 'innerHTML', 'Message from server: Request completed before abort.<br />');
			$objResponse->setReturnValue('shortDelayS');
			return $objResponse;
		}
		
		function mediumDelayS() {
			global $objResponse;
			sleep(9);
			$objResponse->append('log', 'innerHTML', 'Message from server: Request completed before abort.<br />');
			$objResponse->setReturnValue('mediumDelayS');
			return $objResponse;
		}
		
		function longDelayS() {
			global $objResponse;
			sleep(15);
			$objResponse->append('log', 'innerHTML', 'Message from server: Request completed before abort.<br />');
			$objResponse->setReturnValue('longDelayS');
			return $objResponse;
		}
	}

	$configuration = array(
		'shortdelay' => array('callback' => 'local.callback'),
		'mediumdelay' => array(
			'callback' => '[local.callback, special.callback]', 
			'onRequest' => 'function() { xajax.$("log").innerHTML += "explicit callback onRequest called<br />"; }',
			'returnValue' => 'true'),
		'longdelay' => array('callback' => 'local.callback'), 
		'shortdelays' => array('callback' => 'local.callback', 'mode' => '"synchronous"'), 
		'mediumdelays' => array('callback' => 'local.callback', 'mode' => '"synchronous"'), 
		'longdelays' => array('callback' => 'local.callback', 'mode' => '"synchronous"')
		);

	$objRequests =& new clsRequests();
	$aRequests = $xajax->register(XAJAX_CALLABLE_OBJECT, $objRequests, $configuration);

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
	
	$shortDelay_Async = new clsAnchor();
	$shortDelay_Async->addChild(new clsLiteral("Short Delay (Async)"));
	$shortDelay_Async->setEvent('onclick', 
		$aRequests['shortdelay'], 
		array(array(0, XAJAX_JS_VALUE, '{a:1, b:2, c:"&amp;amp;nbsp;two>", "0":1, "ä":"ä"}'))
		);
	
	$mediumDelay_Async = new clsAnchor();
	$mediumDelay_Async->setAttribute('href', '#log');
	$mediumDelay_Async->addChild(new clsLiteral("Medium Delay (Async)"));
	$mediumDelay_Async->setEvent('onclick', 
		$aRequests['mediumdelay'],
		array(array(0, XAJAX_JS_VALUE, '[1,2,3,3]'))
		);
	
	$longDelay_Async = new clsAnchor();
	$longDelay_Async->addChild(new clsLiteral("Long Delay (Async)"));
	$longDelay_Async->setEvent('onclick', $aRequests['longdelay']);
	
	$shortDelay_Sync = new clsAnchor();
	$shortDelay_Sync->addChild(new clsLiteral("Short Delay (Sync)"));
	$shortDelay_Sync->setEvent('onclick', $aRequests['shortdelays'], array(), 'var oRet = ', '; xajax.$("log").innerHTML += "Function returned: " + oRet + "<br />"; return false;');
	
	$mediumDelay_Sync = new clsAnchor();
	$mediumDelay_Sync->addChild(new clsLiteral("Medium Delay (Sync)"));
	$mediumDelay_Sync->setEvent('onclick', $aRequests['mediumdelays'], array(), 'var oRet = ', '; xajax.$("log").innerHTML += "Function returned: " + oRet + "<br />"; return false;');
	
	$longDelay_Sync = new clsAnchor();
	$longDelay_Sync->addChild(new clsLiteral("Long Delay (Sync)"));
	$longDelay_Sync->setEvent('onclick', $aRequests['longdelays'], array(), 'var oRet = ', '; xajax.$("log").innerHTML += "Function returned: " + oRet + "<br />"; return false;');

	echo '<' . '?' . 'xml encoding=' . $xajax->getConfiguration('characterEncoding') . ' ?' . '>';
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html>
	<head>
		<title>xajax Test Suite</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<?php $xajax->printJavascript(); ?>
		<script type='text/Javascript' defer charset='UTF-8'>
			/* <![CDATA[ */
			clearCheckboxes = function() {
				var checkboxes = document.getElementsByTagName('INPUT');
				var cLen = checkboxes.length;
				for (var c = 0; c < cLen; ++c)
					if ('checkbox' == checkboxes[c].type)
						checkboxes[c].checked = '';
			}
			clearLog = function() {
				xajax.$('log').innerHTML = '';
			}
			clearAll = function() {
				clearCheckboxes();
				clearLog();
			}
			setupCallbacks = function() {
				xajax.callback.global.onRequest = function() {
					clearCheckboxes();
					xajax.$('global_onRequest').checked = 'checked';
				}
				xajax.callback.global.onResponseDelay = function() {
					xajax.$('global_onResponseDelay').checked = 'checked';
				}
				xajax.callback.global.onExpiration = function() {
					xajax.$('global_onExpiration').checked = 'checked';
				}
				xajax.callback.global.beforeResponseProcessing = function() {
					xajax.$('global_beforeResponseProcessing').checked = 'checked';
				}
				xajax.callback.global.onFailure = function() {
					xajax.$('global_onFailure').checked = 'checked';
				}
				xajax.callback.global.onRedirect = function() {
					xajax.$('global_onRedirect').checked = 'checked';
				}
				xajax.callback.global.onSuccess = function() {
					xajax.$('global_onSuccess').checked = 'checked';
				}
				xajax.callback.global.onComplete = function() {
					xajax.$('global_onComplete').checked = 'checked';
				}
				
				local = {};
				local.callback = xajax.callback.create(360, 12000);
				local.callback.onRequest = function() {
					xajax.$('local_onRequest').checked = 'checked';
				}
				local.callback.onResponseDelay = function() {
					xajax.$('local_onResponseDelay').checked = 'checked';
				}
				local.callback.onExpiration = function(oRequest) {
					xajax.$('log').innerHTML += 'request timed out...<br />';
					xajax.$('local_onExpiration').checked = 'checked';
					xajax.abortRequest(oRequest);
					if (1 < oRequest.retry) {
						--oRequest.retry;
						xajax.$('log').innerHTML += 'starting retry of current request<br />';
						clearCheckboxes();
						xajax.request(oRequest.functionName, oRequest);
					} else
						xajax.$('log').innerHTML += 'retry attempts exhausted, request failed<br />';
				}
				local.callback.beforeResponseProcessing = function() {
					xajax.$('local_beforeResponseProcessing').checked = 'checked';
				}
				local.callback.onFailure = function() {
					xajax.$('local_onFailure').checked = 'checked';
				}
				local.callback.onRedirect = function() {
					xajax.$('local_onRedirect').checked = 'checked';
				}
				local.callback.onSuccess = function() {
					xajax.$('local_onSuccess').checked = 'checked';
				}
				local.callback.onComplete = function() {
					xajax.$('local_onComplete').checked = 'checked';
				}
				
				special = {}
				special.callback = xajax.callback.create(6000, 15000);
				special.callback.onResponseDelay = function() {
					xajax.$('special_onResponseDelay').checked = 'checked';
				}
				special.callback.onComplete = function() {
					xajax.$('special_onComplete').checked = 'checked';
				}
			}
			
			function finished(aValues) {
				var newText = [];
				if ('object' == typeof (aValues)) {
					if (0 < aValues.length) {
						newText.push('Received array: ');
						for (var i=0; i < aValues.length; ++i) {
							if (0 < i)
								newText.push(', ');
							newText.push(aValues[i]);
						}
					}
					newText.push('<br />');
					newText.push('Received object: ');
					var i = 0;
					for (var key in aValues) {
						if (aValues[key]) {
							if (0 < i)
								newText.push(', ');
							newText.push(key);
							newText.push(":");
							newText.push(aValues[key]);
						}
						++i;
					}
				}
				newText.push(' Done.<br />');
				xajax.$('log').innerHTML = xajax.$('log').innerHTML + newText.join('');
			}
			/* ]]> */
		</script>
		<style type='text/css'>
			/* <![CDATA[ */
			.events {
				margin-left: 3px;
				border: 1px solid black;
				width: 330px;
			}
			.links {
				border: 1px solid black;
				width: 300px;
			}
			.log {
				margin-top: 3px;
				border: 1px solid black;
				width: 300px;
			}
			.logText {
				font-size: small;
			}
			.description {
				margin-top: 3px;
				font-size: small;
				border: 1px solid #999999;
				padding: 3px;
				width: 631px;
			}
			.clearLink {
				margin-left: 5px;
				font-weight: normal;
				font-size: smaller;
				vertical-align: center;
			}
			.checkColumn {
				text-align: center;
			}
			/* ]]> */
		</style>
	</head>
	<body onload='setupCallbacks();'>
		<h1>Callback Event Handlers</h1>
		<table cellspacing='0' cellpadding='0'>
			<tbody>
				<tr>
					<td valign='top'>
						<table class='links'>
							<thead>
								<tr>
									<th>Asynchronous</th>
									<th>Synchronous</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td valign='top' width='150px'>
										<?php $shortDelay_Async->printHTML(); ?>
										<br />
										<?php $mediumDelay_Async->printHTML(); ?>
										<br />
										<?php $longDelay_Async->printHTML(); ?>
									</td>
									<td valign='top' width='150px'>
										<?php $shortDelay_Sync->printHTML(); ?>
										<br />
										<?php $mediumDelay_Sync->printHTML(); ?>
										<br />
										<?php $longDelay_Sync->printHTML(); ?>
									</td>
								</tr>
							</tbody>
						</table>
						<table class='log'>
							<thead>
								<tr>
									<th width='306px'>
										Log 
										<span class='clearLink'>
											(<a href='#' onclick='clearLog(); return false;'>Clear</a>)
										</span>
									</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td>
										<div id='log' class='logText'></div>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
					<td valign='top'>
						<table class='events'>
							<thead>
								<tr>
									<th>
										Event 
										<span class='clearLink'>
											(<a href='#' onclick='clearCheckboxes(); return false;'>Clear</a>)
										</span>
									</th>
									<th>Global</th>
									<th>Local</th>
									<th>Special</th>
								</tr>
							</thead>
							<tfoot>
							</tfoot>
							<tbody>
								<tr>
									<td>onRequest</td>
									<td class='checkColumn'><input type='checkbox' id='global_onRequest'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onRequest'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onRequest'></td>
								</tr>
								<tr>
									<td>onResponseDelay</td>
									<td class='checkColumn'><input type='checkbox' id='global_onResponseDelay'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onResponseDelay'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onResponseDelay'></td>
								</tr>
								<tr>
									<td>beforeResponseProcessing</td>
									<td class='checkColumn'><input type='checkbox' id='global_beforeResponseProcessing'></td>
									<td class='checkColumn'><input type='checkbox' id='local_beforeResponseProcessing'></td>
									<td class='checkColumn'><input type='checkbox' id='special_beforeResponseProcessing'></td>
								</tr>
								<tr>
									<td>onSuccess</td>
									<td class='checkColumn'><input type='checkbox' id='global_onSuccess'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onSuccess'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onSuccess'></td>
								</tr>
								<tr>
									<td>onExpiration</td>
									<td class='checkColumn'><input type='checkbox' id='global_onExpiration'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onExpiration'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onExpiration'></td>
								</tr>
								<tr>
									<td>onFailure</td>
									<td class='checkColumn'><input type='checkbox' id='global_onFailure'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onFailure'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onFailure'></td>
								</tr>
								<tr>
									<td>onRedirect</td>
									<td class='checkColumn'><input type='checkbox' id='global_onRedirect'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onRedirect'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onRedirect'></td>
								</tr>
								<tr>
									<td>onComplete</td>
									<td class='checkColumn'><input type='checkbox' id='global_onComplete'></td>
									<td class='checkColumn'><input type='checkbox' id='local_onComplete'></td>
									<td class='checkColumn'><input type='checkbox' id='special_onComplete'></td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
			</tbody>
		</table>
		<div id='description' class='description'>
			This test shows how callback functions are invoked during the 
			call process.  The links along the top left represent various
			xajax enabled function calls.  Each function will sleep for a period
			of time on the php side, causing the browser to wait for the 
			response.  As the call progresses, xajax will call the various
			callback event handlers at the prescribed time.  The check boxes 
			along the top right show when each event handler is called.<p>
			During a synchronous call, you will notice that the browser will 
			block javascript events until after the response has been received. 
			This is due to the nature of a synchronous call.<p>
			(note: due to the nature of synchronous calls, xajax is not able to
			abort the request; only the browser can abort the request based on
			the browsers time-out settings)<p>
		</div>
	</body>
</html>
