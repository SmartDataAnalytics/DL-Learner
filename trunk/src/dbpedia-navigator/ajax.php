<?php
require_once ("xajax/xajax_core/xajax.inc.php");
$sid = session_id();

$xajax = new xajax("ajaxfunctions.php?sid=$sid");
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
$xajax->registerFunction('showInterests');
$xajax->registerFunction('removePosInterest');
$xajax->registerFunction('removeNegInterest');
$xajax->registerFunction('stopServerCall');
?>