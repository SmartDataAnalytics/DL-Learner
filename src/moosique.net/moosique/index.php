<?php
session_start();
// include all classes (except the Utilities and Debugger, these are
// included in SparqlQueryBuilder and Config if neccessary)
require('classes/Config.php'); 
require('classes/DllearnerConnection.php');
require('classes/LastFM.php');
require('classes/SparqlQueryBuilder.php');
require('classes/DataHelper.php');
require('classes/View.php');
require('classes/RequestHandler.php');
require('classes/Recommendations.php');

// initiate a new RequestHandler
$rqh = new RequestHandler();
// and return what happened
$output = $rqh->getResponse();
echo $output;

?>