<?php
// this small script retrieves all avaiable tags from jamendo
$query = "
SELECT DISTINCT ?tag WHERE {
?record rdf:type mo:Record ;
              mo:available_as ?playlist ;
              tags:taggedWithTag ?tag .
} ORDER BY ?tag
";


require('../classes/Config.php'); 
require('../classes/DllearnerConnection.php');

$c = new DllearnerConnection();
$json = $c->sparqlQuery($query);
// convert to useable object
$result = json_decode($json);

echo '<pre>';
foreach($result->results->bindings as $tag) {
  echo $tag->tag->value . "\n";
}
echo '</pre>';

?>