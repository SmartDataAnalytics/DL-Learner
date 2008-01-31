<?php
require_once ("xajax/xajax_core/xajax.inc.php");
$sid = session_id();

$xajax = new xajax("ajaxfunctions.php?sid=$sid");
$xajax->configureMany(array('debug'=>true));
$xajax->register(XAJAX_FUNCTION, 'showSubjects', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->registerFunction('getarticle');
$xajax->registerFunction('toPositive');
$xajax->registerFunction('toNegative');
$xajax->registerFunction('clearPositives');
$xajax->registerFunction('clearNegatives');
$xajax->registerFunction('showInterests');
$xajax->registerFunction('getAndShowArticle');
$xajax->registerFunction('learnConcept');
$xajax->registerFunction('getSubjectsFromConcept');
$xajax->registerFunction('getAndShowSubjects');
$xajax->registerFunction('getsubjects');
$xajax->register(XAJAX_FUNCTION,'showArticle', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->registerFunction('removePosInterest');
$xajax->registerFunction('removeNegInterest');
$xajax->register(XAJAX_FUNCTION,'showConcept', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->registerFunction('learnAndShowConcept');
$xajax->register(XAJAX_FUNCTION,'showSubjectsFromConcept', array(
    'onResponseDelay' => 'showLoading',
    'beforeResponseProcessing' => 'hideLoading'
    ));
$xajax->registerFunction('getAndShowSubjectsFromConcept');
?>