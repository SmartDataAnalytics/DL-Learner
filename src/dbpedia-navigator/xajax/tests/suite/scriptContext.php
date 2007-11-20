<?php
	/*
		File: scriptContext.php
		
		Test script that uses the context call option to provide an object (or array)
		that can be manipulated by the following commands via the 'this' keyword:
			- <xajaxResponse->script>
			- <xajaxResponse->call>
			- <xajaxResponse->waitFor>
			- <xajaxResponse->contextAssign>
			- <xajaxResponse->contextAppend>
			- etc...
	*/

	require_once("./options.inc.php");
		
	$objResponse = new xajaxResponse();
	
	function modifyValue() {
		$objResponse = new xajaxResponse();
		$objResponse->script('if (undefined == this.value) this.value = 1; else this.value += 1;');
		$objResponse->call('this.logValue');
		return $objResponse;
	}
	
	function callFunction() {
		$objResponse = new xajaxResponse();
		$objResponse->call('this.myFunction');
		return $objResponse;
	}
	
	$req_Mv = $xajax->register(XAJAX_FUNCTION, 'modifyValue');
	$req_Cf = $xajax->register(XAJAX_FUNCTION, 'callFunction');
	
	$req_Mv_Mo = $xajax->register(XAJAX_FUNCTION, 'modifyValue', array('context' => 'myObject', 'alias' => 'modifyValue_myObject'));
	$req_Cf_Mo = $xajax->register(XAJAX_FUNCTION, 'callFunction', array('context' => 'myObject', 'alias' => 'callFunction_myObject'));
	
	$xajax->processRequest();
	
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Javascript Functions</title>
		<?php $xajax->printJavascript() ?>
		<script type="text/javascript">
			/* <![CDATA[ */
			value = 0;
			logValue = function() {
				xajax.$('log').innerHTML += 'Value updated: ';
				xajax.$('log').innerHTML += value;
				xajax.$('log').innerHTML += '<br />';
			}
			myFunction = function() {
				xajax.$('log').innerHTML += 'Global myFunction called.<br />';
			}
			myObject = { value: 1000 };
			myObject.myFunction = function() {
				xajax.$('log').innerHTML += 'myObject.myFunction called.<br />';
			}
			myObject.logValue = function() {
				xajax.$('log').innerHTML += 'Value updated: ';
				xajax.$('log').innerHTML += this.value;
				xajax.$('log').innerHTML += '<br />';
			}
			myObject.callFunction = function() {
				xajax.request( { xjxfun: 'callFunction' }, { context: this } );
				return false;
			}
			clearLog = function() {
				xajax.$('log').innerHTML = '';
			}
			/* ]]> */
		</script>
		<style type='text/css'>
			/* <![CDATA[ */
			.controls {
				border: 1px solid black;
				width: 300px;
				margin-bottom: 3px;
			}
			.status {
				margin-left: 3px;
				border: 1px solid black;
				width: 300px;
			}
			.log {
				margin-top: 3px;
				border: 1px solid black;
				width: 603px;
			}
			.description {
				margin-top: 4px;
				margin-bottom: 4px;
				font-size: small;
				border: 1px solid #999999;
				padding: 3px;
			}
			#control {
				height: 25px;
			}
			#status {
				height: 25px;
			}
			/* ]]> */
		</style>
	</head>
	<body>
		<h1>Javascript Context</h1>
		<form id='mainForm' onsubmit='return false;'>
			<table cellspacing='0' cellpadding='0'>
				<tbody>
					<tr>
						<td valign='top'>
							<table class='controls'>
								<thead>
									<tr>
										<th colspan='6'>Controls</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td width='25%'>
											<div id='control'>&nbsp;</div>
										</td>
										<td align='center'>
											<input type='submit' value='Modify Global Value' 
											onclick='xajax.$("log").innerHTML += "Modifying global value<br />"; <?php $req_Mv->printScript(); ?>; return false;' />
										</td>
										<td align='center'>
											<input type='submit' value='Call Global Function' 
												onclick='xajax.$("log").innerHTML += "Calling global myFunction<br />"; <?php $req_Cf->printScript(); ?>; return false;' />
										</td>
										<td align='center'>
											<input type='submit' value='Modify Local Value' 
												onclick='xajax.$("log").innerHTML += "Modifying object value<br />"; <?php $req_Mv_Mo->printScript(); ?>; return false;' />
										</td>
										<td align='center'>
											<input type='submit' value='Call Local Function' 
												onclick='xajax.$("log").innerHTML += "Calling object myFunction<br />"; <?php $req_Cf_Mo->printScript(); ?>; return false;' />
										</td>
										<td align='center'>
											<input type='submit' value='Call Local Function (method 2)' 
												onclick='xajax.$("log").innerHTML += "Calling object myFunction (type 2)<br />"; myObject.callFunction(); return false;' />
										</td>
										<td width='25%'>
										</td>
									</tr>
								</tbody>
							</table>
						</td>
						<td valign='top'>
							<table class='status'>
								<thead>
									<tr>
										<th>
											Status
										</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td>
											<div id='status'></div>
										</td>
									</tr>
								</tbody>
							</table>
						</td>
					</tr>
					<tr>
						<td colspan='2' valign='top'>
							<table class='log'>
								<thead>
									<tr>
										<th>
											Log 
											<span class='clearLink'>(<a href='#' onclick='clearLog(); return false;'>Clear</a>)</span>
										</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td>
											<div id='log'></div>
										</td>
									</tr>
								</tbody>
							</table>
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		
		<div id='description' class='description'>
		</div>
		
	</body>
</html>
