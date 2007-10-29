<?php
session_start();

session_unset();
ob_start();
require_once 'pear/HTTP_Request.php';
require_once 'SparqlConnection.php';
require_once 'Settings.php';
$settings=new Settings();
SparqlConnection::loadWSDLfiles($settings->wsdluri);
header("Location: http://" . $_SERVER["HTTP_HOST"] . "/Ajax-Test/index.php");
?>