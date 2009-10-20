<?php
/**
 * This class handles all HTML-Output for all different kinds of 
 * requests. No Templating system is used.
 *
 *
 */
class View extends Config {
  private $html = '<h2>Nothing found.</h2>';
  
  // view modes are debug or both, default is html view
  function __construct($data, $type) {
    parent::__construct(); // init config
    $this->createOutput($data, $type);
  }
      
      
  /**
   * 
   */ 
  private function createOutput($data, $type) {
    if (!is_array($data) || empty($data)) {
      switch($type) {
        case 'artistSearch' : $this->html = '<h2>No Artists found for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>'; break;
        case 'tagSearch' : $this->html = '<h2>No Tags found for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>'; break;
        case 'songSearch' : $this->html = '<h2>No Songs found for &raquo;' . $_GET['searchValue'] . '&laquo;.</h2>'; break;
        case 'lastFM' : $this->html = '<h2>The last.fm-user &raquo;' . $_GET['searchValue'] . '&laquo; does not exist.</h2>'; break;
        case 'recommendations' : $this->html = '<h2>' . $data . '</h2>'; break;
      }
    } else {
      // finally we are producing html, depending on the type of request
      $this->html = '';
      switch ($type) {
        case 'artistSearch' : 
          $this->html .= '<h2>Artist-Results for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>';
          $this->html .= $this->artistSearchHTML($data); 
        break;
        case 'tagSearch' : 
          $this->html .= '<h2>Tag-Results for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>';
          $this->html .= $this->tagSearchHTML($data); 
        break;
        case 'songSearch' : 
          $this->html .= '<h2>Song-Results for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>';
          $this->html .= $this->songSearchHTML($data); 
        break;
        case 'lastFM' : 
          $this->html .= $this->tagSearchHTML($data); 
        break;
        case 'recommendations' :
          $this->html .= $this->recommendationsHTML($data); 
        break;
        case 'playlist' : 
          $this->html = $this->playlistHTML($data); 
        break;
      }
    }
  }


  /**
   * 
   * @return 
   * @param object $data
   */
  private function artistSearchHTML($data) {
    $output = '<div class="artistSearch"><ul class="clearfix">';
    $i = 0; // counter variable for alternating li-elements
    foreach($data as $artist) {
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
          </p>
          <ul>%s</ul>
          <p>Click on an album to add it to your playlist.</p>
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
      $image = '<img src="img/noimage.png" alt="No image found..." />';
      if (!empty($artist['artistImage'])) {
        $image = '<img src="' . $this->getValue($artist['artistImage']) . '" alt="' 
               . $this->getValue($artist['artistName']) . '" />';
      }
      
      // homepagelink if avaiable
      $homepage = '';
      if (!empty($artist['artistHomepage'])) {
        $homepage = '<a href="' . $this->getValue($artist['artistHomepage']) . '">(Homepage)</a>';
      }

      $tags = '';
      $tempTag = $this->getValue($artist['tag']);
      if (is_array($tempTag)) {
        $tags = implode(', ', $tempTag);
      } else {
        $tags = $tempTag;
      }
      // remove the uri, we only want to have the tag-name
      $tags = str_replace('http://dbtune.org/jamendo/tag/', '', $tags);

      $output .= sprintf($template,
        $class, $this->getValue($artist['artistName']), $homepage, $image, $tags, $records
      );
      
    }
    $output .= '</ul></div>';
    return $output;
  }
  
  /**
   * 
   * @return 
   * @param object $array
   */
  private function tagSearchHTML($data) {
    $output = '<div class="tagSearch">';
    foreach ($data as $key => $tag) {

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
        $record = $this->getSingleValue($this->getValue($tag['record']), $i);
        $artistName = $this->getSingleValue($this->getValue($tag['artistName']), $i);
        $albumTitle = $this->getSingleValue($this->getValue($tag['albumTitle']), $i);
        $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getSingleValue($this->getValue($tag['playlist']), $i)); 
        
        $addToPlaylist = '<li><a class="addToPlaylist" href="' . $playlist . '" '
                       . 'title="' . $artistName . ' - ' . $albumTitle . '" ' 
                       . 'rel="' . $record . '">Click here to add this album to your playlist.</a></li>';
                       
        /* The album cover is optional, so it could be empty */
        $image = '<img src="img/noimage.png" alt="No image found..." />';
        if (!empty($tag['cover'])) {
          $image = '<img src="' . $this->getSingleValue($this->getValue($tag['cover']), $i) . '" alt="' . $artistName. ' - ' .  $albumTitle. '" />';
        }
        
        $output .= sprintf($template,
          $class, $image, $artistName, $albumTitle, $addToPlaylist
        );
      }
      $output .= '</ul>';
    }
    $output .= '</div>';
    return $output;
  }
  
  
  /**
   *
   *
   */
  private function songSearchHTML($data) {
    $output = '<div class="songSearch"><ul class="clearfix">';
            
    $i = 0; // counter variable for alternating li-elements
    foreach($data as $song) {
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

      $track = $this->getSingleValue($this->getValue($song['track']), $i);
      $record = $this->getSingleValue($this->getValue($song['record']), $i);
      $songTitle = $this->getSingleValue($this->getValue($song['songTitle']), $i);
      $artistName = $this->getSingleValue($this->getValue($song['artistName']), $i);
      $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getSingleValue($this->getValue($song['playlist']), $i));
    
      $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                     . $artistName . ' - ' . $songTitle . '">Click here to add this song to your playlist</a></li>';
      
      // Artist-Image is optional             
      $image = '<img src="img/noimage.png" alt="No image found..." />';
      if (!empty($song['artistImage'])) {
        $image = '<img src="' . $this->getSingleValue($this->getValue($song['artistImage']), $i) 
               . '" alt="' . $artistName. '" />';
      }
    
      $tags = '';
      $tempTag = $this->getValue($song['tag']);
      if (is_array($tempTag)) {
        $tags = implode(', ', $tempTag);
      } else {
        $tags = $tempTag;
      }
      $tags = str_replace('http://dbtune.org/jamendo/tag/', '', $tags);
      
      $output .= sprintf($template,
        $class, $artistName . ' - ' . $songTitle, $image, $tags, $addToPlaylist
      );
    }
    $output .= '</ul></div>';
    return $output;
  }


  /**
   * 
   * 
   * 
   * 
   * 
   * 
   */
  private function recommendationsHTML($data) {
    $output = '';
    $count = count($data['scores']); // doesnt matter if scores or results... 
    
    if ($count > 0) { // we have at least one usable result
      $scores = $data['scores'];
      $syntaxes = $data['kbSyntaxes'];
      $results = $data['results'];

      for ($i = 0; $i < $count; $i++) {
        $syntax = str_replace('http://dbtune.org/jamendo/tag/', '', $syntaxes[$i]);
        $syntax = str_replace('"', '', $syntax);
        $output .= '<h3>&raquo;' . $syntax . '&laquo; (accuracy: ' . $scores[$i] . '%)</h3>';
        $output .= '<ul class="clearfix">';

        $j = 0;
        // cycle through all albums and fill the list
        foreach ($results[$i] as $record => $result) {
          // alternating classes for li-elements
          if (($j % 2) == 0) { $class = 'odd'; } 
          else { $class = ''; }
          $j++;

          $template = ' 
            <li class="clearfix %s">
              <div class="cover">%s</div>
              <h4>%s - %s</h4>
              <ul>%s</ul>
            </li>
          ';
          $artistName = $this->getSingleValue($this->getValue($result['artistName']));
          $albumTitle = $this->getSingleValue($this->getValue($result['albumTitle'])); 
          $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getSingleValue($this->getValue($result['playlist'])));
          $addToPlaylist = '<li><a class="addToPlaylist" href="' . $playlist . '" '
                         . 'title="' . $artistName . ' - ' . $albumTitle . '" ' 
                         . 'rel="' . $record . '">Click here to add this album to your playlist.</a></li>';
          /* The album cover is optional, so it could be empty */
          $image = '<img src="img/noimage.png" alt="No image found..." />';
          if (!empty($result['cover'])) {
            $image = '<img src="' . $this->getSingleValue($this->getValue($result['cover'])) 
                   . '" alt="' . $artistName. ' - ' .  $albumTitle. '" />';
          }

          $output .= sprintf($template,
            $class, $image, $artistName, $albumTitle, $addToPlaylist
          );
        }        
        $output .= '</ul>';
      }
    } else {
      $output = '<h2>Could not create any recommendations...</h2>';
      $output .= '<p>Listen to some more music or reset your recently listened to list.</p>';
    }
    return $output;
  }













  /**
   *
   *
   *
   */
  private function playlistHTML($data) {
    $albumID = $data['albumID'];
    $output = '';
    foreach($data['trackList'] as $tracks) {
      // if the tracks-list contains only one song, there are no sub-arrays
      if (isset($tracks[0]) && is_array($tracks[0])) { 
        foreach($tracks as $track) {
          $output .= '<li><a rel="' . $albumID . '" href="' . $track['location'] 
                   . '" class="htrack">'
                   . $track['creator'] . ' - ' . $track['title']
                   . '</a></li>';  
        }
      } else { // only one track, no sub-array, no foreach, same data
        $track = $tracks;
        $output .= '<li><a rel="' . $albumID . '" href="' . $track['location'] 
                 . '" class="htrack">'
                 . $track['creator'] . ' - ' . $track['title']
                 . '</a></li>';  
      }
    }
    return $output;
  }
  
  
  
  
  /**
   * This helper-function returns an array or a string, depending on the number of arrayItems for the 
   * given array, searches only in the 'value' subarray returned by the Dllearner
   * 
   * @param Array $array The Array to get Values from
   * @return String or Array with the value 
   */
  private function getValue($data) {
    if (!empty($data['value'])) {
      if (is_array($data['value'])) {
        if (sizeof($data['value']) == 1) {
          return $data['value'][0];
        } else {
          return $data['value'];
        }
      } else {
        return $data['value'];
      }
    } else {
      return '';
    }
  }

  /**
   * 
   * 
   * 
   * 
   * 
   */
  private function getSingleValue($data, $index = 0) {
    if (is_array($data)) { 
      return $data[$index]; 
    } else { 
      return $data;
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