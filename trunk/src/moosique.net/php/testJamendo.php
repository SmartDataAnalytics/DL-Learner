<?php
  
include('config.php');
// include('SparqlQueryBuilder.php');
include('DllearnerConnection.php');  

$connection = new DllearnerConnection($conf);

$query = '
SELECT ?artist ?album
WHERE
{ ?a 
     a mo:MusicArtist; 
     foaf:name ?artist;
     foaf:made ?album.
  ?album tags:taggedWithTag <http://dbtune.org/jamendo/tag/stonerrock>.
 }
';

/* TODO
$spargel = new SparqlQueryBuilder($conf, 'Low Earth Orbit', 'artist', array('artist', 'title', 'image'));
$query = $spargel->getQuery();
*/

$json = $connection->sparqlQuery($query);
$result = json_decode($json);
$bindings = $result->results->bindings;

echo '<pre>';
print_r($bindings);
echo '</pre>';

?>