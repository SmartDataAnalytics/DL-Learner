<?php
  
include('config.php');
// include('SparqlQueryBuilder.php');
include('DllearnerConnection.php');  

$connection = new DllearnerConnection($conf);

$queryWorking = '
select ?artist ?album where { 

?artistLink rdf:type mo:MusicArtist ; 
            foaf:name ?artist ; 
            foaf:made ?album ; 

} LIMIT 10
';


$queryNOTworking = '
select ?artist ?album where { 

?artistLink rdf:type mo:MusicArtist ; 
            foaf:name ?artist ; 
            foaf:made ?album ; 

FILTER (regex(str(?artist), "vin", "i")) . 

} LIMIT 10
';


/* TODO
$spargel = new SparqlQueryBuilder($conf, 'Low Earth Orbit', 'artist', array('artist', 'title', 'image'));
$query = $spargel->getQuery();
*/

$json = $connection->sparqlQuery($queryWorking);
// $json = $connection->sparqlQuery($queryNOTworking);
$result = json_decode($json);
$bindings = $result->results->bindings;

echo '<pre>';
print_r($bindings);
echo '</pre>';

?>

