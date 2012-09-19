<?php
session_start();

// Instances are the positive Examples mixed up with some
// other examples, thus random records
$instances = array(
  "http://dbtune.org/jamendo/record/1819",
  "http://dbtune.org/jamendo/record/1372",
  "http://dbtune.org/jamendo/record/3929",
  "http://dbtune.org/jamendo/record/977",
  "http://dbtune.org/jamendo/record/395"
);

$posExamples = array(
  "http://dbtune.org/jamendo/record/1819",
  "http://dbtune.org/jamendo/record/1372"
);


require('../classes/Config.php'); 
require('../classes/DllearnerConnection.php');
require('../classes/SparqlQueryBuilder.php');

$c = new DllearnerConnection();
$s = new SparqlQueryBuilder('salsa', 'tagSearch');
$q = $s->getQuery();

$json = $c->sparqlQuery($q);
// convert to useable object
$result = json_decode($json);


echo '<pre>';
echo "SESSION-ID" . $_SESSION['sessionID'] . "\n" . "\n";

print_r($result);
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