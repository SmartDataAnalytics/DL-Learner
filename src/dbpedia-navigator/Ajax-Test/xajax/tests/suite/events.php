<?php
	/*
		File: events.php
		
		Test script that uses <xajaxResponse->addHandler> and <xajaxResponse->removeHandler>
		to effect the active event handlers on the browser.
	*/

	require_once("./options.inc.php");
	
	function addHandler($sId, $sHandler)
	{
		$objResponse = new xajaxResponse();
		$objResponse->addHandler($sId, "click", $sHandler);
		$objResponse->append('handlers', 'innerHTML', '<div id="handler'.$sHandler.'">*-- '.$sHandler.' (attached)</div>');
		return $objResponse;
	}
	
	function removeHandler($sId, $sHandler)
	{
		$objResponse = new xajaxResponse();
		$objResponse->removeHandler($sId, "click", $sHandler);
		$objResponse->remove('handler'.$sHandler);
		return $objResponse;
	}
	
	function toggleButtons()
	{
		$objResponse = new xajaxResponse();

		$aArgs = func_get_args();
		
		if (3 < count($aArgs))
		{
			$aDisable = $aArgs[2];
			$aEnable = $aArgs[3];
			
			foreach ($aDisable as $sDisableId)
				$objResponse->assign($sDisableId, 'disabled', 'disabled');
			foreach ($aEnable as $sEnableId)
				$objResponse->assign($sEnableId, 'disabled', '');
		}
		
		return $objResponse;
	}
	
	$requestAdd = $xajax->register(XAJAX_EVENT, 'add_clicked');
	$xajax->register(XAJAX_EVENT_HANDLER, 'add_clicked', 'addHandler');
	$xajax->register(XAJAX_EVENT_HANDLER, 'add_clicked', 'toggleButtons');
	
	$requestRemove = $xajax->register(XAJAX_EVENT, 'remove_clicked');
	$xajax->register(XAJAX_EVENT_HANDLER, 'remove_clicked', 'removeHandler');
	$xajax->register(XAJAX_EVENT_HANDLER, 'remove_clicked', 'toggleButtons');
	
	$xajax->processRequest();
	
	$sRoot = dirname(dirname(dirname(__FILE__)));

	if (false == class_exists('xajaxControl')) {
		$sCore = '/xajax_core';
		include_once($sRoot . $sCore . '/xajaxControl.inc.php');
	}

	/*
		Class: clsMutuallyExclusiveButton
		
		Only one button can be disabled at a given time.
	*/
	class clsMutuallyExclusiveButton extends xajaxControl
	{
		var $aPartners;
		
		function clsMutuallyExclusiveButton($sId, $sName, $sValue)
		{
			xajaxControl::xajaxControl('input', array(
				'attributes' => array(
					'id' => $sId,
					'type' => 'submit',
					'name' => $sName,
					'value' => $sValue
					)
				));
			
			$this->aPartners = array();
		}
		
		function getId()
		{
			if (isset($this->aAttributes['id']))
				return $this->aAttributes['id'];
				
			trigger_error('An ID is requred for a mux. button.', E_USER_ERROR);
		}
		
		function addPartner(&$objPartnerNew)
		{
			if (false == is_a($objPartnerNew, 'clsMutuallyExclusiveButton'))
				trigger_error('Invalid partner specified; should be a clsMutuallyExclusiveButton', E_USER_ERROR);
			
			foreach (array_keys($this->aPartners) as $sKey)
			{
				$objPartner =& $this->aPartners[$sKey];
				$objPartner->privateAddPartner($objPartnerNew);
				$objPartnerNew->privateAddPartner($objPartner);
			}
			
			$this->privateAddPartner($objPartnerNew);
			$objPartnerNew->privateAddPartner($this);
		}
		
		function privateAddPartner(&$objPartner)
		{
			$this->aPartners[] =& $objPartner;
		}
		
		function setAttribute($sName, $sValue)
		{
			if ('disabled' == $sName)
			{
				if ('disabled' == $sValue)
				{
					foreach (array_keys($this->aPartners) as $sKey)
					{
						$objPartner =& $this->aPartners[$sKey];
						$objPartner->setAttribute('disabled', 'false');
					}
				}
			}
			
			xajaxControl::setAttribute($sName, $sValue);
		}
		
		function printHtml($sIndent='')
		{
			$sEnable = '[';
			$sSeparator = '"';
			$sTerminator = ']';
			foreach (array_keys($this->aPartners) as $sKey)
			{
				$objPartner =& $this->aPartners[$sKey];
				$sEnable .= $sSeparator;
				$sEnable .= $objPartner->getId();
				$sSeparator = '", "';
				$sTerminator = '"]';
			}
			$sEnable .= $sTerminator;
			
			$sDisable = '["' . $this->getId() . '"]';
			
			$aRequest =& $this->aEvents['onclick'];
			$objRequest =& $aRequest[0];
			$objRequest->setParameter(0, XAJAX_QUOTED_VALUE, '');
			$objRequest->setParameter(1, XAJAX_QUOTED_VALUE, '');
			$objRequest->setParameter(2, XAJAX_JS_VALUE, $sDisable);
			$objRequest->setParameter(3, XAJAX_JS_VALUE, $sEnable);
			
			xajaxControl::printHTML($sIndent);
		}
	}

	// one
	$inputAddOne =& new clsMutuallyExclusiveButton('add_one', 'add_one', 'Add');
	$inputAddOne->setEvent('onclick', 
		$requestAdd, array(
			array(0, XAJAX_QUOTED_VALUE, 'clicker'),
			array(1, XAJAX_QUOTED_VALUE, 'clickHandlerOne')
			)
		);
	
	$inputRemoveOne =& new clsMutuallyExclusiveButton('remove_one', 'remove_one', 'Remove');
	$inputRemoveOne->setEvent('onclick', 
		$requestRemove, array(
			array(0, XAJAX_QUOTED_VALUE, 'clicker'),
			array(1, XAJAX_QUOTED_VALUE, 'clickHandlerOne')
			)
		);
	
	$inputAddOne->addPartner($inputRemoveOne);
	$inputRemoveOne->setAttribute('disabled', 'disabled');

	// two
	$inputAddTwo =& new clsMutuallyExclusiveButton('add_two', 'add_two', 'Add');
	$inputAddTwo->setEvent('onclick', 
		$requestAdd, array(
			array(0, XAJAX_QUOTED_VALUE, 'clicker'),
			array(1, XAJAX_QUOTED_VALUE, 'clickHandlerTwo')
			)
		);
	
	$inputRemoveTwo =& new clsMutuallyExclusiveButton('remove_two', 'remove_two', 'Remove');
	$inputRemoveTwo->setEvent('onclick', 
		$requestRemove, array(
			array(0, XAJAX_QUOTED_VALUE, 'clicker'),
			array(1, XAJAX_QUOTED_VALUE, 'clickHandlerTwo')
			)
		);
	
	$inputAddTwo->addPartner($inputRemoveTwo);
	$inputRemoveTwo->setAttribute('disabled', 'disabled');

?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Event Handlers</title>
		<?php $xajax->printJavascript() ?>
		<script type="text/javascript">
			function writeOutput(msg) {
				xajax.$('output').innerHTML += msg;
			}
			function clickHandlerOne() {
				writeOutput('message from click handler one<br />');
			}
			function clickHandlerTwo() {
				writeOutput('message from click handler two<br />');
			}
			function clickDetected() {
				writeOutput('<br />');
				writeOutput('* click event triggered<br />');
				if ('undefined' != typeof clickTimeout)
					clearTimeout(clickTimeout);
				clickTimeout = setTimeout('clearClick();', 4000);
				return true;
			}
			function clearClick() {
				xajax.$('output').innerHTML = '';
				clickTimeout = undefined;
			}
		</script>
		<style>
		.clicker {
			padding: 3px;
			display: table;
			border: 1px outset black;
			font-size: large;
			margin-bottom: 10px;
			cursor: pointer;
		}
		.controls {
			margin-top: 6px;
			margin-bottom: 7px;
		}
		.description {
			margin-top: 4px;
			margin-bottom: 4px;
			font-size: small;
			border: 1px solid #999999;
			padding: 3px;
		}
		.single_line {
			white-space: nowrap;
		}
		</style>
	</head>
	<body>
		<h1>Client-side Event Handlers</h1>
		
		<div id='clicker' class='clicker' onclick='return clickDetected();'>Click here to trigger event</div>
		
		<form id='mainForm' onsubmit='return false;'>
			<div id='controls' class='controls'>
				<table>
					<tr>
						<td class='single_line'>Event handler one: </td>
						<td align='center'>
							<?php $inputAddOne->printHTML(); ?>
						</td>
						<td align='center'>
							<?php $inputRemoveOne->printHTML(); ?>
						</td>
						<td width='100%'></td>
					</tr>
					<tr>
						<td class='single_line'>Event handler two: </td>
						<td align='center'>
							<?php $inputAddTwo->printHTML(); ?>
						</td>
						<td align='center'>
							<?php $inputRemoveTwo->printHTML(); ?>
						</td>
						<td width='100%'></td>
					</tr>
				</table>
			</div>
		</form>
		
		<div id='handlers' class='handlers'>
		</div>

		<div id='output'></div>
		
		<div id='description' class='description'>
		This page tests the ability to attach event handlers to DOM objects.  The DIV above labeled 'Click Here' has an onclick
		event that simply appends *click* to the output area.  To attach additional even handlers, click 'Add' for either event
		handler One, Two or Both.  Event handlers attached to the DIV are listed below it.
		</div>
		
	</body>
</html>
