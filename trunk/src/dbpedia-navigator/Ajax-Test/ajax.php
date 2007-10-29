<?php
require_once ("xajax/xajax_core/xajax.inc.php");

$xajax = new xajax("ajaxfunctions.php");
$xajax->register(XAJAX_FUNCTION, 'getsubjects', array(
    'onResponseDelay' => 'showLoadingSubjects',
    'beforeResponseProcessing' => 'hideLoadingSubjects'
    ));
$xajax->register(XAJAX_FUNCTION, 'getarticle', array(
    'onResponseDelay' => 'showLoadingArticle',
    'beforeResponseProcessing' => 'hideLoadingArticle'
    ));
$xajax->registerFunction('addPositive');
$xajax->registerFunction('addNegative');
$xajax->registerFunction('clearPositives');
$xajax->registerFunction('clearNegatives');
$xajax->register(XAJAX_FUNCTION, 'learnConcept', array(
    'onResponseDelay' => 'showLoadingConcept',
    'beforeResponseProcessing' => 'hideLoadingConcept'
    ));
?>