<?php
require_once ("xajax/xajax_core/xajax.inc.php");
$sid = session_id();
if (isset($_GET['path'])) $path=$_GET['path'];
else $path="";

$xajax = new xajax($path."ajaxfunctions.php?sid=$sid&path=".$path);
$xajax->configureMany(array('debug'=>true));

$xajax->register(XAJAX_FUNCTION, 'getsubjects', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->register(XAJAX_FUNCTION,'getarticle', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->register(XAJAX_FUNCTION,'learnConcept', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->register(XAJAX_FUNCTION,'getSubjectsFromConcept', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
    $xajax->registerFunction('toPositive');
$xajax->registerFunction('toNegative');
$xajax->registerFunction('clearPositives');
$xajax->registerFunction('clearNegatives');
$xajax->registerFunction('removePosInterest');
$xajax->registerFunction('removeNegInterest');
$xajax->registerFunction('stopServerCall');
?>