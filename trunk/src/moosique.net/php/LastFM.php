<?php

/**
 * TODO: UGLY! Use Sparql-Querys instead?!
 *
 * Later.
 */
class LastFM {

  private $data;
  private $config;
  private $username;

  function __construct($config, $username) {
    $this->config = $config;
    $this->username = $username;
    $this->getData();
  }

  function getData() {
    include_once('arc/ARC2.php');
    $rdfParser = ARC2::getRDFParser();
    $lastfmResource = $this->config->getUrl('lastfm') . urlencode($this->username);
    $rdfParser->parse($lastfmResource);
    // parse, non simple array
    $index = $rdfParser->getSimpleIndex(0);
    $this->data = $index;
  }
  
  function getRecentTracks() {
    $playedTracks = array();
    $trackNodes = array();
    
    echo '<pre>';
    // print_r($this->data);
    
    if (is_array($this->data) && !empty($this->data)) {
      foreach($this->data as $rootItem => $rootValue) {
        // only process further if the rootitem ist no uri
        if (!preg_match('/http:\/\//i', $rootItem)) {
          foreach($rootValue as $childItem => $childValue) {
            // if there is a childitem :track_played, we can use the information
            if ($childItem == $this->config->getPrefix('played')) {
              $trackNodes[] = $childValue[0]['value'];              
            }
          }
        }
      }
    } else {
      echo 'Data-Array empty.';
    }

    if (!empty($trackNodes)) {
      foreach($trackNodes as $trackNode) {
        $track = $this->data[$trackNode][$this->config->getPrefix('title')][0]['value'];
        $artistNode = $this->data[$trackNode][$this->config->getPrefix('maker')][0]['value'];
        $artist = $this->data[$artistNode][$this->config->getPrefix('name')][0]['value'];
        $artistZitgist = $this->data[$artistNode][$this->config->getPrefix('same')][0]['value'];
        $album = '';
        $albumZitgist = '';
        
        $playedTracks[] = array($artist, $track, $album, $artistZitgist, $albumZitgist);
        
      }
    } else {
      echo "No recently played tracks avaiable from last.fm.";
    }
    
    print_r($trackNodes);
    print_r($playedTracks);
    echo '</pre>';
  }


}

include('config.php');

$lastfm = new LastFM($conf, 'nebelschwade');
$lastfm->getRecentTracks();

?>