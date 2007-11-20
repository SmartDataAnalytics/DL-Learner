<?php
require_once("../xajax_core/xajax.inc.php");

$xajax = new xajax();
$xajax->configure('debug', true);
$xajax->registerFunction("myExternalFunction", dirname(__FILE__) . "/myExternalFunction.php");
$xajax->registerFunction(array("myFunction", "myExternalClass","myMethod"), dirname(__FILE__) . "/myExternalFunction.php");
$xajax->processRequest();
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/2000/REC-xhtml1-20000126/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>registerExternalFunction Test | xajax Tests</title>
<?php $xajax->printJavascript("../") ?>
</head>
<body>

<h2><a href="index.php">xajax Tests</a></h2>
<h1>registerExternalFunction Test</h1>

<form id="testForm1" onsubmit="return false;">
<p><input type="submit" value="Call External Function" onclick="xajax_myExternalFunction(); return false;" /></p>
<p><input type="submit" value="Call External Class Method" onclick="xajax_myFunction(); return false;" /></p>
</form>

<div id="submittedDiv"></div>

</body>
</html>