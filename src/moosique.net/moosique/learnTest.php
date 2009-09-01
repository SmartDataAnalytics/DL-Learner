<?php
session_start();

require('classes/Config.php'); 
require('classes/DllearnerConnection.php');

$c = new DllearnerConnection();

// Instances are the positive Examples mixed up with some
// other examples, thus random records
$instances = array();
$numberOfInstaces = 10;

// some stoner records
$posExamples = array(
  "http://dbtune.org/jamendo/record/1128",
  "http://dbtune.org/jamendo/record/8620",
  "http://dbtune.org/jamendo/record/8654",
  "http://dbtune.org/jamendo/record/10031"
);

// take the last 1/3 of total as positive examples
$howManyPosExamples = round($numberOfInstaces/3);
$totalPosExamples = count($posExamples);
for ($i = $totalPosExamples; $i > ($totalPosExamples - $howManyPosExamples); $i--) {
  $instances[] = $posExamples[$i-1];
}
// and then add some random Records _not_ in this list
$allRecords = file($c->getConfigUrl('allRecords'));
$howManyRandomRecords = $numberOfInstaces - $howManyPosExamples;
for ($i = 0; $i < $howManyRandomRecords; $i++) {
  // cutting of linebreak
  $randomRecord = trim($allRecords[array_rand($allRecords)]);
  if (!in_array($randomRecord, $instances)) {
    $instances[] = $randomRecord;
  }
  
}

// randomizing, just for fun :-)
shuffle($instances);


echo '<pre>';
print_r($instances);
echo '</pre>';


echo $c->learn($instances, $posExamples, 'http://localhost/moosique.net/moosique/stonerTest.owl');



/*
replacePredicate=[(
"http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type")];




// set the start class to the correct type (Record in this case) - not supported yet
// celoe.startClass = "http://purl.org/ontology/mo/Record";
// let it run for a short amount of time (we only want simple expressions)

celoe.maxExecutionTimeInSeconds = 2;

// use owl:hasValue if appropriate
// see: http://www.w3.org/TR/2008/WD-owl2-syntax-20081202/#Individual_Value_Restriction
// not sure whether this greatly influences the results


celoe.useHasValueConstructor = true;
celoe.valueFrequencyThreshold = 2;

*/

?>