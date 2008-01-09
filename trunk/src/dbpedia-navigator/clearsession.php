<?php
session_start();

session_unset();
ob_start();
require_once 'pear/HTTP_Request.php';
require_once 'DLLearnerConnection.php';
require_once 'Settings.php';
$settings=new Settings();
DLLearnerConnection::loadWSDLfiles($settings->wsdluri);
$index_uri = 'http://'.$_SERVER['SERVER_NAME'].dirname($_SERVER['SCRIPT_NAME']).'/index.php';
header('Location: ' . $index_uri);
?>