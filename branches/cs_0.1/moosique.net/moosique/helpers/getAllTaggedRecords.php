<?php
// This little script retrieves all albums from jamendo, 
// that are avaiable for listening and are tagged
$query = "
SELECT DISTINCT ?record WHERE {
?record rdf:type mo:Record ;
        mo:available_as ?playlist ;
        tags:taggedWithTag ?tag . 
}
";

require('../classes/Config.php'); 
require('../classes/DllearnerConnection.php');

$c = new DllearnerConnection();
$json = $c->sparqlQuery($query);
// convert to useable object
$result = json_decode($json);

echo '<pre>';

foreach($result->results->bindings as $record) {
  echo $record->record->value . "\n";
}

echo '</pre>';

?>