<?php

$text = file_get_contents('allTags.txt');
$array = explode("\n", $text);


$countArray = array();

foreach($array as $tag) {
  if (empty($countArray[$tag])) {
    $countArray[$tag] = 1;
  } else {
    $countArray[$tag]++;
  }
}

arsort($countArray);

echo '<pre>';
print_r($countArray);
echo '</pre>';

?>