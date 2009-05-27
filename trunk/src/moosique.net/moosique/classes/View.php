<?php

class View extends Config {
  
  private $html = '<h2>Nothing found.</h2>';
  
  // view modes are debug or both, default is html view
  function __construct($typeOfSearch, $data) {
    parent::__construct(); // init config
        
    if (empty($data)) {
      $this->html = '<h2>Nothing found.</h2>';
    } else {
      $this->html = '';
      $this->createOutput($typeOfSearch, $data);
    }
  }
      
   
  function createOutput($typeOfSearch, $data) {

    $mergedArray = Array();

    switch ($typeOfSearch) {
      case 'artistSearch' :
        $mergedArray = $this->mergeArray($data, 'artist');      
        // remove double-values recursively
        $mergedArray = $this->arrayUnique($mergedArray);
        // create the HTML-Output
        $this->html .= $this->createHTMLCode($mergedArray, $typeOfSearch);
        
        
      break;
      
      case 'tagSearch' :
        $mergedArray = $this->mergeArray($data, 'tag');
        $this->debugger->log($mergedArray, "Merged");       
        $this->html .= $this->createHTMLCode($mergedArray, $typeOfSearch);
      break;
      
          
      case 'songSearch' :


      break;

    }
  }

  /**
   * 
   * @return String The HTML produced out of the given array and Type 
   * @param Array $data The Data-Array to convert in HTML
   * @param String $type The Type of array to convert
   */
  function createHTMLCode($array, $type) {
    
    $output = '<div class="' . $type . '">';    
    
    if ($this->getConfig('globalLimit') == 1) {
      $output .= '<p class="note">Please note: the maximum number of shown search results is ' .
                $this->getConfig('maxResults') . '.</p>';
    }
    
    switch($type) {
      case "artistSearch" :
        
        $output .= '<h2>Search results for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>';
        $output .= '<ul class="clearfix">';
                
        foreach($array as $artist) {
        
          // TODO Template-Usage
          $template = '
            <li rel="%s" class="clearfix">
              <h3>%s %s</h3>
              <div class="artistImage">%s</div>
              <strong>Tags:</strong><br/ > 
              <p>%s</p>
              <strong>Avaiable records:</strong> (Click to add to Playlist) 
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
                        . str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $artist['playlist']['value'][$key]) . '">' 
                        . $this->getValue($artist['artistName'])  . ' - ' 
                        . $artist['albumTitle']['value'][$key] . '</a></li>';
            }

          } else {
            $records .= '<li><a class="addToPlaylist" href="' 
                      . str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($artist['playlist'])) . '">'
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
          if (!empty($artist['tags'])) {
            if (is_array($this->getValue($artist['tags']))) {
              $tags = implode(', ', $this->getValue($artist['tags']));
            } else {
              $tags = $this->getValue($artist['tags']);
            }
            // remove the uri, we only want to have the tag-name
            $tags = str_replace('http://dbtune.org/jamendo/tag/', '', $tags);
          }
          
          
          $output .= sprintf($template,
            $this->getValue($artist['artist']), /* rel=" */
            $this->getValue($artist['artistName']),
            $homepage,
            $image, 
            $tags,
            $records
          );
        }
        
        $output .= '</ul>';
          
      break;
      
      
      case 'tagSearch' :
        $output .= '<h2>Search results for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>';
        
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
            $template = ' 
              <li rel="%s" class="clearfix">
                <div class="cover">%s</div>
                <h4>%s - %s</h4>
                <ul>%s</ul>
              </li>
            ';
          
          
            // we have to do the if/else because if there is only one
            // result, the returned response wont contain a subarray
            $records = $this->getValue($tag['record']);
            if (is_array($records)) {
              $record = $records[$i];
            } else {
              $record = $records;
            }
            
            $artistNames = $this->getValue($tag['artistName']);
            if (is_array($artistNames)) {
              $artistName = $artistNames[$i];
            } else {
              $artistName = $artistNames;
            }
            
            $albumTitles = $this->getValue($tag['albumTitle']); 
            if (is_array($albumTitles)) {
              $albumTitle = $albumTitles[$i];
            } else {
              $albumTitle = $albumTitles;
            }
            
            $playlists = $this->getValue($tag['playlist']); 
            if (is_array($playlists)) {
              $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlists[$i]);
            } else {
              $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlists);
            }
            
            $addToPlaylist = '<li><a class="addToPlaylist" href="' . $playlist . '" title="'  
                           . $artistName . ' - ' . $albumTitle . '">Click here to add this album to the playlist.</a></li>';
                           
            /* The album cover is optional, so it could be empty */
            if (!empty($tag['cover'])) {
            $covers = $this->getValue($tag['cover']);
              if (is_array($covers)) {
                $image = '<img src="' . $covers[$i] . '" alt="' 
                       . $artistName. ' - ' .  $albumTitle. '" />';
              } else {
                $image = '<img src="' . $covers . '" alt="' 
                       . $artistName. ' - ' .  $albumTitle. '" />';
              }
            }
            
            $output .= sprintf($template,
              $record,
              $image,
              $artistName,
              $albumTitle,
              $addToPlaylist
            );
          }
          $output .= '</ul>';
          
        }


      break;
      
      
      
      default :
        // TODO
      break;
      
    }
    
    $output .= '</div>';
    
    return $output;
    
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

  function mergeArray($data, $type) {
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
  function arrayUnique($array) {
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
  function object2array($obj) { 
    $_arr = is_object($obj) ? get_object_vars($obj) : $obj; 
    foreach ($_arr as $key => $val) { 
      $val = (is_array($val) || is_object($val)) ? $this->object2array($val) : $val; 
      $arr[$key] = $val; 
    } 
    return $arr;
  } 
  
  

  
}

?>