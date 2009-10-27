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
      // we limit the array to the most used tags
      $allTags = array_slice($allTags, 0, $this->getConfigLastFM('topTags'));

      // if there is a last-fm user, but he has tagged nothing?
      // we get the list of top-artists and get the topTags from the artists
      if (empty($allTags)) { 
        if ($this->debugger) $this->debugger->log($user, 'The last.fm-User has no tags, trying to fetch tags from top artists.');
        $allTags = $this->getTagsFromTopArtists($user);
      }
    } else {
      if ($this->debugger) $this->debugger->log($user, 'The last.fm-User does not exist. Please try again.');
    }
    if ($this->debugger) $this->debugger->log($allTags, 'Found these Tags for the last.fm-User' . $user);
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
    // reduce top Artists to topArtists
    $allArtists = array_slice($allArtists, 0, $this->getConfigLastFM('topArtists'));
    
    // get the topTags for every artist

    foreach($allArtists as $artistName) {
      $requestUrl = $this->getConfigLastFM('artistsTopTagsUrl')
                  . '&artist=' . urlencode($artistName) 
                  . '&api_key=' . $this->getConfigLastFM('apiKey');
      $artistTags = @simplexml_load_file($requestUrl);
      
      // take only the first tag, that should be enough
      foreach($artistTags->toptags as $tags) {
        foreach($tags as $tag) {
          $finalTags[] = (String)$tag->name;
          break;
        }
      }
    }
    // remove double entries and limit the array to the TOP Tags
    $finalTags = array_unique($finalTags);
    $finalTags = array_slice($finalTags, 0, $this->getConfigLastFM('topTags'));
    
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