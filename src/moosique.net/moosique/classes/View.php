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
    if (empty($data)) {
      switch($type) {
        case 'artistSearch' : $this->html = '<h2>No Artists found for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>'; break;
        case 'tagSearch' : $this->html = '<h2>No Tags found for &raquo;' . $_GET['searchValue'] . '&laquo;</h2>'; break;
        case 'songSearch' : $this->html = '<h2>No Songs found for &raquo;' . $_GET['searchValue'] . '&laquo;.</h2>'; break;
        case 'lastFM' : $this->html = ''; break;
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
        
        case 'albumPlaylist' : $this->html = $this->albumPlaylistHTML($data); break;
        case 'trackPlaylist' : $this->html = $this->trackPlaylistHTML($data); break;
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
        $image = '<img src="img/noimage.png" alt="No image found..." />';
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
      
      // Artist-Image is optional             
      $image = '<img src="img/noimage.png" alt="No image found..." />';
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
      } 
      
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
   */
  private function albumPlaylistHTML($data) {
    $albumID = $data['albumID'];
    $output = '';
    foreach($data['trackList'] as $tracks) {
      foreach($tracks as $track) {
        $output .= '<li><a rel="' . $albumID . '" href="' . $track['location'] 
                 . '" class="htrack">'
                 . $track['creator'] . ' - ' . $track['title']
                 . '</a></li>';  
      }
    }
    return $output;
  }
  
  /**
   *
   *
   *
   */
  private function trackPlaylistHTML($data) {
    $albumID = $data['albumID'];
    $output = '';
    foreach($data['trackList'] as $track) {
      $output .= '<li><a rel="' . $albumID . '" href="' . $track['location'] 
               . '" class="htrack">'
               . $track['creator'] . ' - ' . $track['title']
               . '</a></li>';  
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
   * Returns the HTML
   * 
   * @return The produced HTML-output of this Class 
   */
  public function getHTML() {
    return $this->html;
  }

}

?>