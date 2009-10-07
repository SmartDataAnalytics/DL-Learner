<?php

class LastFM extends Config {
  
  private $topTags;

  function __construct($user) {
    parent::__construct(); // init config
    
    $this->getLastFMTags($user);
  }

  /**
   *
   *
   *
   */
  private function getLastFMTags($user) {
    $allTags = array();
    $requestUrl = $this->getConfigLastFM('topTagsUrl') 
                . '&user=' . $user 
                . '&api_key=' . $this->getConfigLastFM('apiKey');
    $lastFMTags = @simplexml_load_file($requestUrl);

    if ($lastFMTags) { // meaning the last.fm-username exists
      foreach($lastFMTags->toptags as $tags) {
        foreach($tags as $tag) {
          $allTags[] = (String)$tag->name;
        }
      }
      // we limit the array to the 10 most used tags
      $allTags = array_slice($allTags, 0, 10);

      // if there is a last-fm user, but he has tagged nothing?
      // we get the list of top-artists and get the topTags from the artists
      if (empty($allTags)) { 
        $allTags = $this->getTagsFromTopArtists($user);
      }
    } else {
      if ($this->debugger) $this->debugger->log($user, 'The last.fm-User does not exist. Please try again.');
    }
    $this->topTags = $allTags;
  }
  
  /**
   *
   *
   *
   */
  private function getTagsFromTopArtists($user) {
    $allArtists = array();
    $finalTags = array();
    
    // get the top artists for the user
    $requestUrl = $this->getConfigLastFM('topArtistsUrl')
                . '&user=' . $user 
                . '&api_key=' . $this->getConfigLastFM('apiKey');
                
    $lastFMArtists = @simplexml_load_file($requestUrl);

    foreach($lastFMArtists->topartists as $artists) {
      foreach($artists as $artist) {
        $allArtists[] = (String)$artist->name;
      }
    }
    // reduce top Artists to TOP 10
    $allArtists = array_slice($allArtists, 0, 10);
    
    // get the topTags for every artist

    foreach($allArtists as $artistName) {
      $requestUrl = $this->getConfigLastFM('artistsTopTagsUrl')
                  . '&artist=' . urlencode($artistName) 
                  . '&api_key=' . $this->getConfigLastFM('apiKey');
      $artistTags = @simplexml_load_file($requestUrl);
      
      // take only the first two tags, that should be enough
      foreach($artistTags->toptags as $tags) {
        $someCounter = 0;
        foreach($tags as $tag) {
          $finalTags[] = (String)$tag->name;
          $someCounter++;
          if ($someCounter == 2) break;
        }
      }
    }
    // remove double entries and limit the array to the TOP 10
    $finalTags = array_unique($finalTags);
    $finalTags = array_slice($finalTags, 0, 10);
    
    return $finalTags;
  }
  
  /**
   *
   *
   *
   */
  public function getTopTags() {
    return $this->topTags;
  }
  
  
}

?>