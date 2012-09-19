<?php
session_start();
// use config.ini too to exclude predList and enable caching
// if you use a lot of runs (>20) be sure to have enough execution
// time for your php script when running on a web server 
// set max_execution_time = 1200 or even higher for really long runs

// set some config values for this performance script:
$useRandomInstances = true; // set to false if you want to use the predefined instances
$numberOfPosExamples = 10; // use values up to ten, more than ten wont be realworld
$numberOfRuns = 100; // the number of runs for better average values choose sth. >= 10

/*==========================================================*/
/* create a connection to the dllearner and initiate the
   recommendations helper class */
require('../classes/Config.php'); 
require('../classes/DllearnerConnection.php');
require('../classes/Recommendations.php');
$r = new Recommendations();
$c = new DllearnerConnection();

// a small script-timer function
function getTime() { 
  $a = explode (' ',microtime()); 
  return(double) $a[0] + $a[1]; 
} 

// new empty arrays for the ones we use
$instances = array();
$posExamples = array();

// some stoner records, should give nice results
// the actual number of items used from this array
// is defined in $numberOfPosExamples
$preDefPosExamples = array(
  "http://dbtune.org/jamendo/record/4812",
  "http://dbtune.org/jamendo/record/1128",
  "http://dbtune.org/jamendo/record/8620",
  "http://dbtune.org/jamendo/record/8654",
  "http://dbtune.org/jamendo/record/10031",
  "http://dbtune.org/jamendo/record/3763",
  "http://dbtune.org/jamendo/record/4256",
  "http://dbtune.org/jamendo/record/9097",
  "http://dbtune.org/jamendo/record/5661",
  "http://dbtune.org/jamendo/record/5878",
);
// predefined instances used, if not random
$preDefInstances = array(
  "http://dbtune.org/jamendo/record/1465",
  "http://dbtune.org/jamendo/record/1568",
  "http://dbtune.org/jamendo/record/1668",
  "http://dbtune.org/jamendo/record/11391"
);

// set the posExamples
if ($numberOfPosExamples < 10) {
  $posExamples = array_slice($preDefPosExamples, 0, $numberOfPosExamples);
} else {
  $posExamples = $preDefPosExamples;
}
$r->setPosExamples($posExamples);
// set the instances from preDefInstances
$r->setInstances($preDefInstances); 
$instances = $r->getInstances();

// reset total-timer
$total = 0;

// start the output
echo '<pre>';

// start the learning runs
for ($i = 1; $i <= $numberOfRuns; $i++) {
  // using random instances, overwrite preDef
  if ($useRandomInstances === true) { 
    $r->setInstances(); 
    $instances = $r->getInstances();
  }
  
  echo 'Positive Examples are:' . "\n";
  print_r($posExamples);
  echo "\n" . 'All Instances are:' . "\n";
  print_r($instances);
  echo "\n" . 'Starting ' . $numberOfRuns . ' runs...' . "\n";

 
  // start timer
  $start = getTime();
  $res = $c->learn($instances, $posExamples);
  $end = getTime(); 
  // stop timer right after learning, we dont want the other php-stuff 
  // to manipulate the actual learning-runtime

  $res = json_decode($res);
  echo "\n" . 'Results for run ' . $i . ':' . "\n";
  foreach ($res as $solution) {
    echo round($solution->scoreValue*100, 2) . '% --- ' . $solution->descriptionKBSyntax . "\n";
  }

  // print time used
  $timeTaken = number_format(($end - $start), 3);
  echo 'Time taken: ' . $timeTaken . ' seconds' . "\n";
  $total += $timeTaken;
}

echo "\n" . 'Total time: ' . $total . ' seconds';
echo "\n" . 'Avg. time: ' . $total / $numberOfRuns . ' seconds';
echo '</pre>';
// Done. Look at output in your browser. Reload for another run.
?>