<?php
require_once ("xajax/xajax_core/xajax.inc.php");
$sid = session_id();

$xajax = new xajax("ajaxfunctions.php?sid=$sid");
$xajax->configureMany(array('debug'=>true));
$xajax->register(XAJAX_FUNCTION, 'showSubjects', array(
    'onResponseDelay' => 'showLoadingSubjects',
    'beforeResponseProcessing' => 'hideLoadingSubjects'
    ));
$xajax->registerFunction('getarticle');
$xajax->registerFunction('toPositive');
$xajax->registerFunction('toNegative');
$xajax->registerFunction('clearPositives');
$xajax->registerFunction('clearNegatives');
$xajax->registerFunction('showInterests');
$xajax->registerFunction('getAndShowArticle');
$xajax->register(XAJAX_FUNCTION, 'learnConcept', array(
    'onResponseDelay' => 'showLoadingConcept',
    'beforeResponseProcessing' => 'hideLoadingConcept'
    ));
$xajax->registerFunction('getSubjectsFromConcept');
$xajax->registerFunction('getAndShowSubjects');
$xajax->registerFunction('getsubjects');
$xajax->registerFunction('showArticle');
$xajax->registerFunction('removePosInterest');
$xajax->registerFunction('removeNegInterest');
$xajax->registerFunction('showConcept');
$xajax->registerFunction('learnAndShowConcept');
$xajax->registerFunction('showSubjectsFromConcept');
$xajax->registerFunction('getAndShowSubjectsFromConcept');
?>