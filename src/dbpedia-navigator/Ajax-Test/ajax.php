<?php
require_once ("xajax/xajax_core/xajax.inc.php");

$xajax = new xajax("ajaxfunctions.php");
$xajax->register(XAJAX_FUNCTION, 'getsubjects', array(
    'onResponseDelay' => 'showLoading',
    'onComplete' => 'hideLoading'
    ));
$xajax->registerFunction("getarticle");
?>