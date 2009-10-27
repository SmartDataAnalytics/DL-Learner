<?php
session_start();

require('classes/Config.php'); 
require('classes/DllearnerConnection.php');
require('classes/LastFM.php');
require('classes/SparqlQueryBuilder.php');
require('classes/DataHelper.php');
require('classes/View.php');
require('classes/RequestHandler.php');
require('classes/Recommendations.php');

$rqh = new RequestHandler();
$output = $rqh->getResponse();

echo $output;

?>