<?php

$array = explode("\n", file_get_contents('tagsWeUse.txt'));

$output = '';
$tagsOnly = array();

foreach ($array as $entry) {
  
  $parts = explode(" : ", $entry);
  $tag = str_replace('http://dbtune.org/jamendo/tag/', 'tag:', $parts[0]);
  
  // Build basics owl file
  $output .= '###  ' . $parts[0] . "\n";
  $output .= $tag . ' rdf:Type owl:Class ;' . "\n" ;
  
  // length of string for formatting
  $length = strlen($tag);
  for($i = 0; $i <= $length; $i++) {
    $output .= ' ';
  }
  
  $output .= 'rdfs:subClassOf mo:Record .';
  $output .= "\n" . "\n";
}

echo $output;


?>