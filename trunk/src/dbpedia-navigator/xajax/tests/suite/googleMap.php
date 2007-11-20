<?php

$sBaseFolder = dirname(dirname(dirname(__FILE__)));
$sXajaxPlugins = $sBaseFolder . '/xajax_plugins';

$xajax->configure('requestURI', basename(__FILE__));
$xajax->configure('javascript URI', '../../');

require $sXajaxPlugins . '/response/googleMap.inc.php';

$requestCreateMap =& $xajax->register(XAJAX_FUNCTION, 'createMap');
$requestZoom5 =& $xajax->register(XAJAX_FUNCTION, 'zoom');
$requestZoom5->setParameter(0, XAJAX_QUOTED_VALUE, 'myMap');
$requestZoom5->setParameter(1, XAJAX_JS_VALUE, 5);
$requestZoom10 =& $xajax->register(XAJAX_FUNCTION, 'zoom');
$requestZoom10->setParameter(0, XAJAX_QUOTED_VALUE, 'myMap');
$requestZoom10->setParameter(1, XAJAX_JS_VALUE, 10);
$requestZoom14 =& $xajax->register(XAJAX_FUNCTION, 'zoom');
$requestZoom14->setParameter(0, XAJAX_QUOTED_VALUE, 'myMap');
$requestZoom14->setParameter(1, XAJAX_JS_VALUE, 14);
$requestSetMarker =& $xajax->register(XAJAX_FUNCTION, 'setMarker');
$requestSetMarker->setParameter(0, XAJAX_QUOTED_VALUE, 'myMap');
$requestSetMarker->setParameter(1, XAJAX_FORM_VALUES, 'marker');

$xajax->processRequest();

$objPluginManager =& xajaxPluginManager::getInstance();
$objGoogleMapPlugin =& $objPluginManager->getPlugin('clsGoogleMap');
$objGoogleMapPlugin->setGoogleSiteKey(
	'INSERT_YOUR_SITE_KEY_HERE'
	);

?>
<html>
	<head>
		<?php $xajax->printJavascript(); ?>
	</head>
	<body>
		<a href='#' onclick='<?php $requestCreateMap->printScript(); ?>; return false;'>Create Map</a>
		<br />
		<a href='#' onclick='<?php $requestZoom5->printScript(); ?>; return false;'>Zoom 5</a>
		<br />
		<a href='#' onclick='<?php $requestZoom10->printScript(); ?>; return false;'>Zoom 10</a>
		<br />
		<a href='#' onclick='<?php $requestZoom14->printScript(); ?>; return false;'>Zoom 14</a>
		<br />
		<form id='marker' action='#' method='post' onsubmit='return false;'>
			Lat: <input type='text' name='lat' value='0'>
			<br />
			Lon: <input type='text' name='lon' value='10'>
			<br />
			Text: <input type='text' name='text' value='Test marker with <br />embedded <b>html</b>.'>
			<br />
			<a href='#' onclick='<?php $requestSetMarker->printScript(); ?>; return false;'>Set Marker</a>
		</form>
		
		<div id='myMapPlaceholder' style='position: relative; width: 500px; height: 300px;'>
		</div>
	</body>
</hmtl>
<?php

function createMap()
{
	$objResponse = new xajaxResponse();
	$objResponse->plugin('clsGoogleMap', 'create', 'myMap', 'myMapPlaceholder');
	return $objResponse;
}

function zoom($sMap, $nLevel)
{
	$objResponse = new xajaxResponse();
	$objResponse->plugin('clsGoogleMap', 'zoom', $sMap, $nLevel);
	return $objResponse;
}

function setMarker($sMap, $aFormValues)
{
	$nLat = $aFormValues['lat'];
	$nLon = $aFormValues['lon'];
	$sText = $aFormValues['text'];
	
	$objResponse = new xajaxResponse();
	$objResponse->plugin('clsGoogleMap', 'setMarker', $sMap, $nLat, $nLon, $sText);
	return $objResponse;
}

?>