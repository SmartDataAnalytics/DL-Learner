<?php
	/*
		File: css.php
		
		Test script that uses <xajaxResponse->includeCSS> and <xajaxResponse->removeCSS>
		to effect the active style information on the page dynamically.
	*/

	require_once("./options.inc.php");
	
	$objResponse = new xajaxResponse();
	
	class clsFunctions {
		function clsFunctions() {
		}
		
		function loadCSS1() {
			global $objResponse;
			$objResponse->includeCSS('css1.css');
			$objResponse->append('log', 'innerHTML', 'CSS1 loaded.<br />');
			return $objResponse;
		}
		
		function unloadCSS1() {
			global $objResponse;
			$objResponse->removeCSS('css1.css');
			$objResponse->append('log', 'innerHTML', 'CSS1 unloaded.<br />');
			return $objResponse;
		}
		
		function loadCSS2() {
			global $objResponse;
			$objResponse->includeCSS('css2.css');
			$objResponse->append('log', 'innerHTML', 'CSS2 loaded.<br />');
			return $objResponse;
		}
		
		function unloadCSS2() {
			global $objResponse;
			$objResponse->removeCSS('css2.css');
			$objResponse->append('log', 'innerHTML', 'CSS2 unloaded.<br />');
			return $objResponse;
		}
	}
	
	$functions = new clsFunctions();
	
	$aFunctions = $xajax->register(XAJAX_CALLABLE_OBJECT, $functions);
	
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
	
	$buttonLoadCSS1 = new clsButton(array(
		'attributes' => array(
			'class' => 'loadCSS1',
			'id' => 'loadCSS1'
			),
		'children' => array(new clsLiteral('Load CSS 1')),
		'event' => array('onclick', $aFunctions['loadcss1'])
		));

	$buttonUnloadCSS1 = new clsButton(array(
		'attributes' => array(
			'class' => 'initiallyHidden unloadCSS1',
			'id' => 'unloadCSS1'
			),
		'children' => array(new clsLiteral('Unload CSS 1')),
		'event' => array('onclick', $aFunctions['unloadcss1'])
		));
	
	$buttonLoadCSS2 = new clsButton(array(
		'attributes' => array(
			'class' => 'loadCSS2',
			'id' => 'loadCSS2'
			),
		'children' => array(new clsLiteral('Load CSS 2')),
		'event' => array('onclick', $aFunctions['loadcss2'])
		));

	$buttonUnloadCSS2 = new clsButton(array(
		'attributes' => array(
			'class' => 'initiallyHidden unloadCSS2',
			'id' => 'unloadCSS2'
			),
		'children' => array(new clsLiteral('Unload CSS 2')),
		'event' => array('onclick', $aFunctions['unloadcss2'])
		));

?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<title>Load / Unload CSS files</title>
		<?php $xajax->printJavascript(); ?>
		<script type='text/javascript'>
			/* <![CDATA[ */
			clearLog = function() {
				xajax.$('log').innerHTML = '';
			}
			/* ]]> */
		</script>
		<style type='text/css'>
			/* <![CDATA[ */
			.initiallyHidden {
				visibility: hidden;
			}
			.controls {
				width: 600px;
				border: 1px solid black;
			}
			.logger {
				margin-top: 3px;
				width: 600px;
				border: 1px solid black;
			}
			.log {
				padding: 2px;
			}
			.description {
				margin-top: 3px;
				padding: 2px;
				border: 1px solid #999999;
				font-size: smaller;
				width: 594px;
			}
			.clearLink {
				font-size: smaller;
			}
			/* ]]> */
		</style>
	</head>
	<body>
		<h1>Load / Unload CSS files</h1>
		
		<table cellspacing='0' cellpadding='0'>
			<tbody>
				<tr>
					<td>
						<table cellspacing='0' cellpadding='0' class='controls'>
							<thead>
								<tr>
									<th>Controls</th>
									<th>Effects</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td align='center' valign='top'>
										<div><?php $buttonLoadCSS1->printHTML(); ?><?php $buttonUnloadCSS1->printHTML(); ?></div>
										<div><?php $buttonLoadCSS2->printHTML(); ?><?php $buttonUnloadCSS2->printHTML(); ?></div>
									</td>
									<td valign='top'>
										<div class='frame'>
											<div class='headerText'>Header Text</div>
											<div class='bodyText'>This is the body text.</div>
											<div class='initiallyHidden tagline'>This is the tagline.</div>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<table cellspacing='0' cellpadding='0' class='logger'>
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
										<div class='log' id='log'>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<div class='description'>
							This test script demonstrates the ability to request the loading and unloading of CSS files from the server side. 
							Click the load button for either CSS1 or CSS2 and watch the browser apply the style changes to the page nearly 
							instantly.  Once loaded, the style changes can be removed with the unload button.
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</body>
</html>