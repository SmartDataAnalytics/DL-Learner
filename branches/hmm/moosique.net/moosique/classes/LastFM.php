<?php
/**
 * This class handles all connections to the last.fm-API for retrieving
 * tags for a given username. If the user has no tags, the topTags for
 * topArtists are used as tags
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class LastFM extends Config {
  
  /**
   * Initializing class requires the last.fm username
   *
   * @param string $user The last.fm Username
   * @author Steffen Becker
   */
  function __construct() {
    parent::__construct(); // init config
  }


  /**
   * Gets the Top-Tags for a last.fm user. The number of Tags to retrieve
   * is set in config.ini
   *
   * @param string $user The last.fm username
   * @return array An array with the top tags for $user
   * @author Steffen Becker
   */
  public function getTags($user) {
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
    
    return $allTags;
  }
  
  
  /**
   * This is called if the user has no Tags. This function tries to get the
   * topArtists from the user and then tries to get the topTags for the
   * artists -- the most listened to artists are a good base for useful tags
   *
   * @param string $user The last.fm Username
   * @return array An array with the topTags for the Topartists from $user
   * @author Steffen Becker
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
   * Returns the last.fm artist-page-URL vor a given musicbrainz-ID
   *
   * @param string $mbid The musicbrainz-ID
   * @return string The URL to the last.fm-page of the artist, or false
   * @author Steffen Becker
   */
  public function getArtistPage($mbid) {
    // get the top artists for the user
    $requestUrl = $this->getConfigLastFM('artistInfoUrl')
                . '&mbid=' . $mbid
                . '&api_key=' . $this->getConfigLastFM('apiKey');
                
    $artistInfo = @simplexml_load_file($requestUrl);
    
    if ($artistInfo) {
      return $artistInfo->artist->url;
    } else {
      return false;
    }
  }

}

?>