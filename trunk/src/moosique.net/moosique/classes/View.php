<?php
/**
 * This class handles all HTML-Output for all different kinds of 
 * requests. No Templating system is used.
 *
 *
 */
class View extends Config {
  private $html = '';
  private $limit = 0;
  
  
  // view modes are debug or both, default is html view
  function __construct($data, $type, $search, $limit = false) {
    parent::__construct(); // init config
    
    if ($limit === false) { // no special limit set, we use the maxResultsLimit
      $this->limit = $this->getConfig('maxResults');
    } else {
      $this->limit = $limit;
    }
    $this->createOutput($data, $type, $search);
  }
      
      
  /**
   * 
   */
  private function createOutput($data, $type, $search) {
    // if we have an array for $search (Tag or lastFM-Search) we implode the searchString
    if (is_array($search)) {
      $searchText = implode(' ', $search);
    } else {
      $searchText = $search;
    }
    
    if (!is_array($data) || empty($data)) {
      switch($type) {
        case 'artistSearch' : $this->html = '<h2>No artists found for &raquo;' . $searchText . '&laquo;</h2>'; break;
        case 'tagSearch' : $this->html = '<h2>No tags found for &raquo;' . $searchText . '&laquo;</h2>'; break;
        case 'albumSearch' : $this->html = '<h2>No albums found for &raquo;' . $searchText . '&laquo;</h2>'; break;
        case 'songSearch' : $this->html = '<h2>No songs found for &raquo;' . $searchText . '&laquo;</h2>'; break;
        case 'lastFM' : $this->html = '<h2>The last.fm-user &raquo;' . $searchText . '&laquo; does not exist.</h2>'; break;
        case 'recommendations' : $this->html = '<h2>' . $data . '</h2>'; break;
      }
    } else {
      // finally we are producing html, depending on the type of request
      // use the limits on the data before creating html, except for the playlist-html
      if ($type != 'playlist') {
        $data = $this->limitData($data);
      }
      
      switch ($type) {
        case 'artistSearch' : 
          $this->html .= '<h2>Artist-results for &raquo;' . $searchText . '&laquo;</h2>';
          $this->artistSearchHTML($data, $type); 
        break;
        case 'tagSearch' : 
          if (is_array($search)) {
            $this->html .= '<h2>Tag-results for &raquo;' . $searchText . '&laquo;</h2>';
            // special case, use the albumSearch view for this one
            $this->albumSearchHTML($data, 'albumSearch'); 
          } else { // default case
            $this->html .= '<h2>Tag-results for &raquo;' . $searchText . '&laquo;</h2>';
            $this->tagSearchHTML($data, $type);
          }
        break;
        case 'albumSearch' : 
          $this->html .= '<h2>Album-results for &raquo;' . $searchText . '&laquo;</h2>';
          $this->albumSearchHTML($data, $type); 
        break;
        case 'songSearch' : 
          $this->html .= '<h2>Song-results for &raquo;' . $searchText . '&laquo;</h2>';
          $this->songSearchHTML($data, $type); 
        break;
        case 'recommendations' :
          $this->recommendationsHTML($data, $type); 
        break;
        case 'playlist' : 
          $this->playlistHTML($data); 
        break;
      }
    }
  }


  /**
   * 
   * 
   * 
   * 
   * 
   */
  private function limitData($data) {
    $count = count($data);
    if ($count > $this->limit && $this->limit > 0) {
      if ($this->getConfig('randomize') == 1) {
        $random = array_rand($data, $this->limit);
        $newData = array();
        foreach($random as $randRecord) {
          $newData[$randRecord] = $data[$randRecord];
        }
        $data = $newData;
        $this->html .= '<p><strong>Note:</strong> Found ' . $count . ' results, showing ' 
                     . $this->limit . ' random results.';
      } else { // first xx results are shown
        $data = array_slice($data, 0, $this->limit);
        $this->html .= '<p><strong>Note:</strong> Found ' . $count . ' results, showing the first ' 
                     . $this->limit . ' results.';
      }
    }
    return $data;
  }


  /**
   * 
   * @param object $data
   */
  private function artistSearchHTML($data, $type) {
    $this->html .= '<div class="artistSearch"><ul class="clearfix">';
    $i = 0; // counter variable for alternating li-elements
    foreach($data as $artist) {
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; } else { $class = ''; }

      $template = $this->getTemplate($type);

      // avaiable Records
      $recordArray = $this->getValue($artist['record']);
      $records = '';
      if (is_array($recordArray)) {
        foreach($recordArray as $key => $record) {
          $records .= '<li><a class="addToPlaylist" href="' 
                    // remove the last part of the uri, defaults to mp3, because
                    // the yahooMediaPlayer can't play ogg
                    . str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $artist['playlist']['value'][$key]) 
                    . '" rel="' . $artist['record']['value'][$key] . '">'
                    . $this->getValue($artist['artistName'])  . ' - ' 
                    . $artist['albumTitle']['value'][$key] . '</a></li>';
        }
      } else {
        $records .= '<li><a class="addToPlaylist" href="' 
                  . str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($artist['playlist'])) 
                  . '" rel="' . $this->getValue($artist['record']) . '">'
                  . $this->getValue($artist['artistName'])  . ' - ' 
                  . $this->getValue($artist['albumTitle']) . '</a></li>';
      }

      $artistName = $this->getValue($artist['artistName']);
      $image = $this->getImage($artist, $artistName);
      $tags = $this->getTagList($artist['tag']);
      
      $homepage = ''; // homepagelink if avaiable
      if (!empty($artist['artistHomepage'])) {
        $homepage = '<a href="' . $this->getValue($artist['artistHomepage']) . '">(Homepage)</a>';
      }
      
      $this->html .= sprintf($template,
        $class, $artistName, $homepage, $image, $tags, $records
      );

      $i++;
    }
    $this->html .= '</ul></div>';
  }
  
  
  /**
   * 
   * @param object $array
   */
  private function tagSearchHTML($data, $type) {
    $this->html .= '<div class="tagSearch">';
    foreach ($data as $key => $tag) {
      $numberOfAlbums = count($this->getValue($tag['record']));
      $count = $numberOfAlbums; // used for the for-statement
      
      $this->html .= '<h3>Albums tagged with <em>' . str_replace('http://dbtune.org/jamendo/tag/', '', $key) . '</em></h3>';

      $random = array();
      // limit the number of shown albums per tag, too
      if ($numberOfAlbums > $this->limit) {
        if ($this->getConfig('randomize') == 1) {
          $this->html .= '<p><strong>Note:</strong> Found ' . $numberOfAlbums . ' albums, showing ' 
                       . $this->limit . ' random albums.';
          // get some random numbers for random albums, used later in the for-statement
          $random = array_rand($this->getValue($tag['record']), $this->limit);
        } else {
          $this->html .= '<p><strong>Note:</strong> Found ' . $numberOfAlbums . ' albums, showing the first ' 
                       . $this->limit . ' albums.';
        }
        $count = $this->limit;
      }
      
      $this->html .= '<ul class="clearfix">';
      // cycle through the albums
      for ($i = 0; $i < $count; $i++) {
        // alternating classes for li-elements
        if (($i % 2) == 0) { $class = 'odd'; } else { $class = ''; }
        
        $j = $i; // default -- non random, no limit
        // if there is limit set, and randomize is active, we use the random numbers
        if ($numberOfAlbums > $this->limit && $this->getConfig('randomize') == 1) {
          $j = $random[$i];
        }
        
        $template = $this->getTemplate($type);

        $record = $this->getValue($tag['record'], $j);
        $artistName = $this->getValue($tag['artistName'], $j);
        $albumTitle = $this->getValue($tag['albumTitle'], $j);
        $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($tag['playlist'], $j)); 
        
        $addToPlaylist = '<li><a class="addToPlaylist" href="' . $playlist . '" '
                       . 'title="' . $artistName . ' - ' . $albumTitle . '" ' 
                       . 'rel="' . $record . '">Click here to add this album to your playlist.</a></li>';
                       
        $image = $this->getImage($tag, $artistName. ' - ' .  $albumTitle, $j);
        
        $this->html .= sprintf($template,
          $class, $image, $artistName, $albumTitle, $addToPlaylist
        );
      }
      $this->html .= '</ul>';
    }
    $this->html .= '</div>';
  }
  
  
  /**
   *
   *
   */
  private function albumSearchHTML($data, $type) {
    $this->html .= '<div class="albumSearch"><ul class="clearfix">';
    $i = 0; // counter variable for alternating li-elements
    foreach($data as $album) {
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; } else { $class = ''; }
      
      $template = $this->getTemplate($type);
      
      $record = $this->getValue($album['record'], 0);
      $albumTitle = $this->getValue($album['albumTitle'], 0);
      $artistName = $this->getValue($album['artistName'], 0);
      $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($album['playlist'], 0));
      
      $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                     . $artistName . ' - ' . $albumTitle . '">Click here to add this album to your playlist</a></li>';
      
      // Artist-Image is optional   
      $image = $this->getImage($album, $artistName . ' - ' .  $albumTitle);
      $tags = $this->getTagList($album['tag']);
      
      $this->html .= sprintf($template,
        $class, $artistName . ' - ' . $albumTitle, $image, $tags, $addToPlaylist
      );
      
      $i++;
    }
    $this->html .= '</ul></div>';
  }
  
  
  /**
   * 
   * 
   * 
   * 
   * 
   * 
   */
  private function songSearchHTML($data, $type) {
    $this->html .= '<div class="songSearch"><ul class="clearfix">';
    $i = 0; // counter variable for alternating li-elements
    foreach($data as $song) {
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; } else { $class = ''; }
      
      $template = $this->getTemplate($type);
      
      $track = $this->getValue($song['track']);
      $record = $this->getValue($song['record']);
      $songTitle = $this->getValue($song['songTitle']);
      $artistName = $this->getValue($song['artistName']);
      $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($song['playlist']));
      
      $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                     . $artistName . ' - ' . $songTitle . '">Click here to add this song to your playlist</a></li>';
      
      $tags = $this->getTagList($song['tag']);
      $image = $this->getImage($song, $artistName);
      
      $this->html .= sprintf($template,
        $class, $artistName . ' - ' . $songTitle, $image, $tags, $addToPlaylist
      );
      
      $i++;
    }
    $this->html .= '</ul></div>';
  }


  /**
   * 
   * 
   * 
   * 
   * 
   * 
   */
  private function recommendationsHTML($data, $type) {
    $count = count($data['scores']); // doesnt matter if scores or results... 
    
    if ($count > 0) { // we have at least one usable result
      $scores = $data['scores'];
      $syntaxes = $data['kbSyntaxes'];
      $results = $data['results'];

      for ($i = 0; $i < $count; $i++) {
        $resultSet = $this->limitData($results[$i]);
        $syntax = str_replace('http://dbtune.org/jamendo/tag/', '', $syntaxes[$i]);
        $syntax = str_replace('"', '', $syntax);
        $this->html .= '<h3>&raquo;' . $syntax . '&laquo; (accuracy: ' . $scores[$i] . '%)</h3>';
        $this->html .= '<ul class="clearfix">';
        
        $j = 0;
        // cycle through all albums and fill the list
        foreach ($resultSet as $record => $result) {
          // alternating classes for li-elements
          if (($j % 2) == 0) { $class = 'odd'; } else { $class = ''; }
          
          $template = $this->getTemplate($type);

          $artistName = $this->getValue($result['artistName']);
          $albumTitle = $this->getValue($result['albumTitle']); 
          $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($result['playlist']));
          $addToPlaylist = '<li><a class="addToPlaylist" href="' . $playlist . '" '
                         . 'title="' . $artistName . ' - ' . $albumTitle . '" ' 
                         . 'rel="' . $record . '">Click here to add this album to your playlist.</a></li>';

          $image = $this->getImage($result, $artistName . ' - ' . $albumTitle);

          $this->html .= sprintf($template,
            $class, $image, $artistName, $albumTitle, $addToPlaylist
          );
          $j++;
        }        
        $this->html .= '</ul>';
      }
    } else {
      $this->html = '<h2>Could not create any recommendations...</h2>';
      $this->html .= '<p>Listen to some more music or reset your recently listened to list.</p>';
    }
  }


  /**
   *
   *
   *
   */
  private function playlistHTML($data) {
    $albumID = $data['albumID'];
    foreach($data['trackList'] as $tracks) {
      // if the tracks-list contains only one song, there are no sub-arrays
      if (isset($tracks[0]) && is_array($tracks[0])) { 
        foreach($tracks as $track) {
          $this->html .= '<li><a rel="' . $albumID . '" href="' . $track['location'] . '" class="htrack">'
                       . $track['creator'] . ' - ' . $track['title']
                       . '</a></li>';  
        }
      } else { // only one track, no sub-array, no foreach, same data
        $track = $tracks;
        $this->html .= '<li><a rel="' . $albumID . '" href="' . $track['location'] . '" class="htrack">'
                     . $track['creator'] . ' - ' . $track['title']
                     . '</a></li>';  
      }
    }
  }
  
  
  
  /**
   * 
   * 
   * 
   * 
   */
  private function getImage($image, $altText, $nr = 0) {
    // in most cases the image is optional, so it could be empty
    $img = '<img src="img/noimage.png" alt="No image found..." />';
    if (isset($image['image'])) {
      $image = $image['image'];
      if (!empty($image)) {
        $img = '<img src="' . $this->getValue($image, $nr) . '" alt="' . $altText . '" />';
      }
    } 
    return $img;
  }
  
  
  /**
   * 
   * 
   * 
   * 
   */
  private function getTagList($tagsArray) {
    $tags = '';
    $tempTag = $this->getValue($tagsArray);
    if (is_array($tempTag)) {
      $tags = implode(', ', $tempTag);
    } else {
      $tags = $tempTag;
    }
    $tags = str_replace('http://dbtune.org/jamendo/tag/', '', $tags);
    return $tags;
  }
  
  
  /**
   * 
   * 
   * 
   * 
   * 
   */
  private function getTemplate($type) {
    $template = '';
    if ($type == 'artistSearch') {
      $template = '
        <li class="clearfix %s">
          <h3>%s %s</h3>
          <div class="image">%s</div>
          <strong>Tags:</strong><br/ > 
          <p>%s</p>
          <p>
            <strong>Avaiable records:</strong><br />
          </p>
          <ul>%s</ul>
          <p>Click on an album to add it to your playlist.</p>
        </li>
      ';
    }
    if ($type == 'tagSearch' || $type == 'recommendations') {
      $template = ' 
        <li class="clearfix %s">
          <div class="image">%s</div>
          <h4>%s - %s</h4>
          <ul>%s</ul>
        </li>
      ';
    }
    if ($type == 'albumSearch' || $type == 'songSearch') {
      $template = '
          <li class="clearfix %s">
            <h3>%s</h3>
            <div class="image">%s</div>
            <p>Tags: %s</p>
            <ul>%s</ul>
          </li>
        ';
    } 
    return $template;
  }
  
  
  /**
   * This helper-function returns an array or a string, depending on the number of arrayItems for the 
   * given array and if the $index-var is set, searches only in the 'value' subarray (ignore type or uri)
   * 
   * @param Array $array The Array to get Values from
   * @return String or Array with the value 
   */
  private function getValue($data, $index = false) {
    $value = $data['value'];
    if (!empty($value)) {
      if (is_array($value)) {
        if (count($value) == 1) {
          return $value[0];
        } 
        if (count($value) > 1 && $index !== false) {
          return $value[$index];
        } else { // an array is requested, tagList for example
          return $value;
        }
      } else {
        return $value;
      }
    } else {
      return '';
    }
  }


  /**
   * Returns the HTML
   * 
   * @return The produced HTML-output of this Class 
   */
  public function getHTML() {
    return $this->html;
  }
  

}

?>