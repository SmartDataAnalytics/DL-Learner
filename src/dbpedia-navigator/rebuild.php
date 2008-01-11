<?php
session_start();

session_unset();
ob_start();
require_once 'pear/HTTP_Request.php';
require_once 'DLLearnerConnection.php';
require_once 'Settings.php';
$settings=new Settings();

// download new WSDL file
DLLearnerConnection::loadWSDLfiles($settings->wsdluri);

// we need to make sure that PHP really uses the new WSDL file
// and does not cache, so we disable the cache and load it
ini_set("soap.wsdl_cache_enabled","0");
$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri);
//$sc->getIDs();
// TODO: does not work; 
// maybe try to set ttl of cache to 1 second,
// wait for 1 second and then redirect to start page

// redirect to index page
$index_uri = 'http://'.$_SERVER['SERVER_NAME'].dirname($_SERVER['SCRIPT_NAME']).'/index.php';
header('Location: ' . $index_uri);

?>