<?php

class View extends Config {
  
  private $html = '<h2>Nothing found.</h2>';
  
  // view modes are debug or both, default is html view
  function __construct($searchType, $data) {
    parent::__construct(); // init config
        
    if (empty($data)) {
      $this->html = '<h2>Nothing found.</h2>';
    } else {
      $this->html = '';
      $this->createOutput($searchType, $data);
    }
  }
      
  /**
   *
   */ 
  private function createOutput($searchType, $data) {
    $mergedArray = array();

    switch ($searchType) {
      case 'artistSearch' :
        $mergedArray = $this->mergeArray($data, 'artist');      
        // remove double-values recursively
        $mergedArray = $this->arrayUnique($mergedArray);
        // create the HTML-Output
        $this->artistSearchHTML($mergedArray);
      break;
      
      case 'tagSearch' :
        $mergedArray = $this->mergeArray($data, 'tag');
        $this->tagSearchHTML($mergedArray);
      break;
      
      case 'songSearch' :
        $mergedArray = $this->mergeArray($data, 'track');
        $mergedArray = $this->arrayUnique($mergedArray);
        $this->debugger->log($mergedArray, "MERGEDSONGARRAY");
        $this->songSearchHTML($mergedArray);
      break;
      
      case 'albumPlaylist' :
        $playlistObject = simplexml_load_file($data['playlist']);
        $mergedArray = $this->object2array($playlistObject);
        // prepend the album stream-information
        $mergedArray['albumID'] = $data['albumID'];
        $this->albumPlaylistHTML($mergedArray);
      break;

      case 'trackPlaylist' :
        $this->debugger->log($data, 'DATA');
        $playlistObject = simplexml_load_file($data['playlist']);
        $mergedArray = $this->object2array($playlistObject);
        $this->debugger->log($mergedArray, "PLAYLIST");
        $mergedArray['albumID'] = $data['albumID'];
        $this->trackPlaylistHTML($mergedArray);

      break;
    }
  }


  /**
   * 
   * @return 
   * @param object $array
   */
  private function artistSearchHTML($array) {
    $output = '<div class="artistSearch"><ul class="clearfix">';
    $i = 0; // counter variable for alternating li-elements
    foreach($array as $artist) {
      // TODO Template-Usage
      
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; }
      else { $class = ''; }
      $i++;
      
      $template = '
        <li class="clearfix %s">
          <h3>%s %s</h3>
          <div class="artistImage">%s</div>
          <strong>Tags:</strong><br/ > 
          <p>%s</p>
          <p>
            <strong>Avaiable records:</strong><br />
            Click an album to add it to your Playlist
          </p>
          <ul>%s</ul>
        </li>
      ';
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
      /* Optional Values could be empty */
      // artist-Image if avaiable
      $image = '';
      if (!empty($artist['artistImage'])) {
        $image = '<img src="' . $this->getValue($artist['artistImage']) . '" alt="' 
               . $this->getValue($artist['artistName']) . '" />';
      } else {
        $image = '<img src="img/noArtistImage.png" alt="' 
               . $this->getValue($artist['artistName']) . '" />';
      }
      // homepagelink if avaiable
      $homepage = '';
      if (!empty($artist['artistHomepage'])) {
        $homepage = '<a href="' . $this->getValue($artist['artistHomepage']) . '">(Homepage)</a>';
      }
      // tags if avaiable
      $tags = '';
      if (!empty($artist['tag'])) {
        if (is_array($this->getValue($artist['tag']))) {
          $tags = implode(', ', $this->getValue($artist['tag']));
        } else {
          $tags = $this->getValue($artist['tag']);
        }
        // remove the uri, we only want to have the tag-name
        $tags = str_replace('http://dbtune.org/jamendo/tag/', '', $tags);
      }
      $output .= sprintf($template,
        $class, $this->getValue($artist['artistName']), $homepage, $image, $tags, $records
      );
      
    }
    $output .= '</ul></div>';
    $this->html .= $output;
  }
  
  /**
   * 
   * @return 
   * @param object $array
   */
  private function tagSearchHTML($array) {
    $output = '<div class="tagSearch">';
    $output .= $this->displayLimitInfo();

    foreach ($array as $key => $tag) {
      $numberOfAlbums = sizeof($this->getValue($tag['record']));
      $output .= '<h3>Albums tagged with <em>' 
               . str_replace('http://dbtune.org/jamendo/tag/', '', $key) 
               . '</em> (' . $numberOfAlbums . ')</h3>';
      
      if ($numberOfAlbums > $this->getConfig('maxResults')) {
        $output .= '<p><strong>Note:</strong> Found ' . $numberOfAlbums 
                 . ' albums, only the first ' 
                 .   $this->getConfig('maxResults') . ' are shown.';
        $numberOfAlbums = $this->getConfig('maxResults');
      }
      // cycle through all albums and fill the list
      $output .= '<ul class="clearfix">';
      for ($i = 0; $i < $numberOfAlbums; $i++) {
        // alternating classes for li-elements
        if (($i % 2) == 0) { $class = 'odd'; } 
        else { $class = ''; }
        
        $template = ' 
          <li class="clearfix %s">
            <div class="cover">%s</div>
            <h4>%s - %s</h4>
            <ul>%s</ul>
          </li>
        ';
        // we have to do the if/else because if there is only one
        // result, the returned response wont contain a subarray
        $records = $this->getValue($tag['record']);
        if (is_array($records)) { $record = $records[$i]; } 
        else { $record = $records; }
        
        $artistNames = $this->getValue($tag['artistName']);
        if (is_array($artistNames)) { $artistName = $artistNames[$i]; } 
        else { $artistName = $artistNames; }
        
        $albumTitles = $this->getValue($tag['albumTitle']); 
        if (is_array($albumTitles)) { $albumTitle = $albumTitles[$i]; } 
        else { $albumTitle = $albumTitles; }
        
        $playlists = $this->getValue($tag['playlist']); 
        if (is_array($playlists)) {
          $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlists[$i]);
        } else {
          $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlists);
        }
        
        $addToPlaylist = '<li><a class="addToPlaylist" href="' . $playlist . '" '
                       . 'title="' . $artistName . ' - ' . $albumTitle . '" ' 
                       . 'rel="' . $record . '">Click here to add this album to your playlist.</a></li>';
                       
        /* The album cover is optional, so it could be empty */
        if (!empty($tag['cover'])) {
        $covers = $this->getValue($tag['cover']);
          if (is_array($covers)) {
            $image = '<img src="' . $covers[$i] . '" alt="' . $artistName. ' - ' .  $albumTitle. '" />';
          } else {
            $image = '<img src="' . $covers . '" alt="' . $artistName. ' - ' .  $albumTitle. '" />';
          }
        }
        
        $output .= sprintf($template,
          $class,
          $image,
          $artistName,
          $albumTitle,
          $addToPlaylist
        );
      }
      $output .= '</ul>';
    }
    $output .= '</div>';
    $this->html .= $output;
  }
  
  
  
  

  private function songSearchHTML($array) {
    $output = '<div class="songSearch"><ul class="clearfix">';
            
    $i = 0; // counter variable for alternating li-elements
    foreach($array as $song) {
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; } 
      else { $class = ''; }
      $i++;
      
      $template = '
        <li class="clearfix %s">
          <h3>%s</h3>
          <div class="artistImage">%s</div>
          <p>Tags: %s</p>
          <ul>%s</ul>
        </li>
      ';
      // we have to do the if/else because if there is only one
      // result, the returned response wont contain a subarray
      $tracks = $this->getValue($song['track']);
      if (is_array($tracks)) { $track = $tracks[$i]; } 
      else { $track = $tracks; }
      
      $records = $this->getValue($song['record']);
      if (is_array($records)) { $record = $records[$i]; } 
      else { $record = $records; }
    
      $songTitles = $this->getValue($song['songTitle']);
      if (is_array($songTitles)) { $songTitle = $songTitles[$i]; } 
      else { $songTitle = $songTitles; }
    
      $artistNames = $this->getValue($song['artistName']);
      if (is_array($artistNames)) { $artistName = $artistNames[$i]; } 
      else { $artistName = $artistNames; }
    
      $playlists = $this->getValue($song['playlist']); 
      if (is_array($playlists)) {
        $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlists[$i]);
      } else {
        $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlists);
      }
    
      $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                     . $artistName . ' - ' . $songTitle . '">Click here to add this song to your playlist</a></li>';
                   
      /* The album cover is optional, so it could be empty */
      if (!empty($song['artistImage'])) {
      $images = $this->getValue($song['artistImage']);
        if (is_array($images)) {
          $image = '<img src="' . $images[$i] . '" alt="' . $artistName. '" />';
        } else {
          $image = '<img src="' . $images . '" alt="' . $artistName. '" />';
        }
      }
    
      $tags = '';
      if (!empty($song['tag'])) {
        if (is_array($this->getValue($song['tag']))) {
          $tags = implode(', ', $this->getValue($song['tag']));
        } else {
          $tags = $this->getValue($song['tag']);
        }
        // remove the uri, we only want to have the tag-name
        $tags = str_replace('http://dbtune.org/jamendo/tag/', '', $tags);
      } else {
        $this->debugger->log($song['tag'], 'TAGS');
      }
    
      $output .= sprintf($template,
        $class, $artistName . ' - ' . $songTitle, $image, $tags, $addToPlaylist
      );
    }
    $output .= '</ul></div>';
    $this->html .= $output;
  }

   
  private function allSearchHTML($array) {      

  }
  
  
  /** 
   * If there is a global Limit for shown results set, this
   * function returns an information-String saying so
   * 
   * @return String Information about gloabl Search-Limit
   */
  private function displayLimitInfo() {
    $info = '';
    if ($this->getConfig('globalLimit') == 1) {
      $info .= '<p class="note">Please note: the maximum number of shown search results is '
            . $this->getConfig('maxResults') . '.</p>';
    }
    $info .= '<h2>Search results for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>';
    return $info;
  }
  
  
  
  
  
  private function trackPlaylistHTML($array) {
    $albumID = $array['albumID'];
    $output = '';
    foreach($array['trackList'] as $track) {
      $output .= '<li><a rel="' . $albumID . '" href="' . $track['location'] 
               . '" class="htrack">'
               . $track['creator'] . ' - ' . $track['title']
               . '</a>'
               . '<a href="#" class="moveUp" title="Move up">&uarr;</a>'
               . '<a href="#" class="moveDown" title="Move down">&darr;</a>'
               . '<a href="#" class="delete" title="Delete from playlist">X</a>'
               . '</li>';  
    }
    $this->html .= $output;
  }
  
  
  
  
  
  
  
  function albumPlaylistHTML($array) {
    $albumID = $array['albumID'];
    $output = '';
    foreach($array['trackList'] as $tracks) {
      foreach($tracks as $track) {
        $output .= '<li><a rel="' . $albumID . '" href="' . $track['location'] 
                 . '" class="htrack">'
                 . $track['creator'] . ' - ' . $track['title']
                 . '</a>'
                 . '<a href="#" class="moveUp" title="Move up">&uarr;</a>'
                 . '<a href="#" class="moveDown" title="Move down">&darr;</a>'
                 . '<a href="#" class="delete" title="Delete from playlist">X</a>'
                 . '</li>';  
      }
    }
    $this->html .= $output;
  }
  
  /**
   * Returns the HTML
   * 
   * @return The produced HTML-output of this Class 
   */
  public function getHTML() {
    return $this->html;
  }
  
  /**
   * This function returns an array or a string,
   * depending on the number of arrayItems for the 
   * given array, searches only in the 'value' subarray
   * returned by the Dllearner
   * 
   * @param Array $array The Array to get Values from
   * @return String or Array with the value 
   */
  private function getValue($array) {
    if (!empty($array['value'])) {
      if (is_array($array['value'])) {
        if (sizeof($array['value']) == 1) {
          return $array['value'][0];
        } else {
          return $array['value'];
        }
      } else {
        return $array['value'];
      }
    } else {
      return '';
    }
  }



  private function mergeArray($data, $type) {
    // convert the $data-response object to an array
    $array = $this->object2array($data);
    $combinedArray = array();
    
    foreach($array as $subArray) {
      if (!array_key_exists($subArray[$type]['value'], $combinedArray)) {
        $combinedArray[$subArray[$type]['value']] = $subArray;
      } else {
        // we already have an object with this tag? -> merge!
        $combinedArray[$subArray[$type]['value']] = array_merge_recursive(
          $combinedArray[$subArray[$type]['value']], $subArray
        );
      }
    }
    
    if (!empty($combinedArray)) {
      return $combinedArray;
    } else return false;
  }


  /**
   * Like the php-function array_unique, but for
   * multidimensional arrays, calls itself recursively
   * 
   * 
   * 
   * @return Array (Multidimensional) array without double entries 
   * @param Array $array The Array to clean up
   */
  private function arrayUnique($array) {
    $newArray = array();
    if (is_array($array)) {
      foreach($array as $key => $val) {
        if ($key != 'type' && $key != 'datatype') {
          if (is_array($val)) {
            $val2 = $this->arrayUnique($val);
          } else {
            $val2 = $val;
            $newArray = array_unique($array);
            break;
          }
          if (!empty($val2)) {
            $newArray[$key] = $val2;
          }
        }
      }
    }
    return $newArray;
  }
  
  /**
   * Converts a simple Object to an array
   * 
   * @return Array the Array created from the Object
   * @param object $obj The Object to convert
   */
  private function object2array($obj) { 
    $arr = array();
    $_arr = is_object($obj) ? get_object_vars($obj) : $obj; 
    foreach ($_arr as $key => $val) { 
      $val = (is_array($val) || is_object($val)) ? $this->object2array($val) : $val; 
      $arr[$key] = $val; 
    } 
    return $arr;
  } 
  
  

  
}

?>