<?php
session_start();

require('../classes/Config.php'); 
require('../classes/DllearnerConnection.php');

$c = new DllearnerConnection();

// some stoner records, should give nice results
$posExamples = array(
  "http://dbtune.org/jamendo/record/1128",
  "http://dbtune.org/jamendo/record/8620",
  "http://dbtune.org/jamendo/record/8654",
  "http://dbtune.org/jamendo/record/10031"
);

$instances = array(  
  "http://dbtune.org/jamendo/record/1128",
  "http://dbtune.org/jamendo/record/8620",
  "http://dbtune.org/jamendo/record/8654",
  "http://dbtune.org/jamendo/record/10031", /* the first 4 are posExamples */
  "http://dbtune.org/jamendo/record/1465",
  "http://dbtune.org/jamendo/record/1568",
  "http://dbtune.org/jamendo/record/1668"
);

echo '<pre>';
print_r($instances);
print_r($posExamples);

$res = $c->learn($instances, $posExamples);
$res = json_decode($res);

foreach ($res as $solution) {
  echo round($solution->scoreValue*100, 2) . '% --- ' . $solution->descriptionKBSyntax . "\n";
  echo htmlentities($c->kbToSqarql($solution->descriptionKBSyntax)) . "\n" . "\n";
}


echo '</pre>';
?>