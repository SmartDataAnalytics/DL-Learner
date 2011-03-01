<?php
session_start();

require('../classes/Config.php'); 
require('../classes/DllearnerConnection.php');

$c = new DllearnerConnection();

// Instances are the positive Examples mixed up with some
// other examples, thus random records
$instances = array(
  "http://dbtune.org/jamendo/record/2821",
  "http://dbtune.org/jamendo/record/6419",
  "http://dbtune.org/jamendo/record/4435",
  "http://dbtune.org/jamendo/record/9087"
);

$posExamples = array(
  "http://dbtune.org/jamendo/record/2821"
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