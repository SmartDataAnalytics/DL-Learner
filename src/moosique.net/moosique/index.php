<?php
session_start();
require('classes/Config.php'); 
require('classes/DllearnerConnection.php');
require('classes/SparqlQueryBuilder.php');
require('classes/View.php');
require('classes/RequestHandler.php');

$rqh = new RequestHandler();
$output = $rqh->getResponse();

echo $output;

?>