<?php
  
include('config.php');
include('SparqlQueryBuilder.php');
include('DllearnerConnection.php');  


$connection = new DllearnerConnection($conf);
$connection->setEndpoint('http://arc.semsol.org/community/irc/sparql');

$query = '
SELECT ?s ?p ?o WHERE {
  ?s ?p ?o .
} LIMIT 10';


$json = $connection->sparqlQuery($query);
$result = json_decode($json);
$bindings = $result->results->bindings;

echo '<pre>';
print_r($bindings);
echo '</pre>';

?>