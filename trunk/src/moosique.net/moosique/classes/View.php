<?php
/**
 * This class handles all HTML-Output for all different kinds of requests. 
 * It also creates error messages shown in the frontend
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class View extends Config {
  
  private $html = ''; // the final HTML-Output is stored
  private $limit = 0; // limit for showing results
  
  /**
   * Creating a View automatically creates the HTML
   *
   * @param array $data The result-Data-array
   * @param string $type the type of search performed
   * @param mixed $search A string or array with the searchValues
   * @param int $limit The maximum number of results to show/create HTML for, optional
   * @author Steffen Becker
   */
  function __construct($data, $type, $search, $limit = false) {
    parent::__construct(); // init config
    
    if ($limit === false) { // if no special limit set, we use maxResults
      $this->limit = $this->getConfig('maxResults');
    } else {
      $this->limit = $limit;
    }
    $this->createOutput($data, $type, $search);
  }
  
  
  /**
   * Starts creating the HTML output. First checks if the data is fine, 
   * and gets sub-HTML-parts afterwards, if the data is not fine, an
   * error-message will be created
   *
   * @param array $data The result-Data-array
   * @param string $type the type of search performed
   * @param mixed $search A string or array with the searchValues
   * @author Steffen Becker
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
        case 'info' : $this->html = '<h2>Could not find any information about the currently playing song or artist.</h2>';
      }
    } else {
      // finally we are producing html, depending on the type of request
      // use the limits on the data before creating html, except for the playlist-html and more info
      if ($type != 'playlist' && $type != 'info') {
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
        case 'info' : 
          $this->infoHTML($data, $type);
        break;
        case 'playlist' : 
          $this->playlistHTML($data); 
        break;
      }
    }
  }


  /**
   * This lmits the result-data, to a given number of results stored
   * in the private $limit
   *
   * @param array $data The result-Data-Array
   * @return array The limited array
   * @author Steffen Becker
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
   * Returns the HTML for an artist serach, containing special stuff
   * like homepage-links, album-list etc.
   *
   * @param array $data The result-Array to create HTML from
   * @param string $type type of search to get the template
   * @return string HTML for a artistSearch
   * @author Steffen Becker
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
                    // remove the last part of the uri, defaults to mp3, because the yahooMediaPlayer can't play ogg
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
      $image = $this->getImage($artist, 'Image of ' . $artistName);
      $tags = $this->getTagList($artist['tag']);
      
      $homepage = ''; // homepagelink if avaiable
      if (!empty($artist['homepage'])) {
        $homepage = '<a href="' . $this->getValue($artist['homepage']) . '">(Homepage)</a>';
      }
      
      $this->html .= sprintf($template,
        $class, $artistName, $homepage, $image, $tags, $records
      );

      $i++;
    }
    $this->html .= '</ul></div>';
  }
  
  
  /**
   * Builds the HTML for tagSearch results
   * This is somewhat special, we first create a list of found tags,
   * and then we list the albums found for those tags 
   *
   * @param array $data The result-Array to create HTML from
   * @param string $type type of search to get the template
   * @author Steffen Becker
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
          $j = $random[$i]; // randomizing the results for the tag
        }
        
        $template = $this->getTemplate($type);

        $record = $this->getValue($tag['record'], $j);
        $artistName = $this->getValue($tag['artistName'], $j);
        $albumTitle = $this->getValue($tag['albumTitle'], $j);
        $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($tag['playlist'], $j)); 
        
        $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                       . $artistName . ' - ' . $albumTitle . '">Click here to add this album to your playlist</a></li>';
                       
        $image = $this->getImage($tag, 'Cover for ' . $artistName. ' - ' .  $albumTitle, $j, 'cover');
        
        $this->html .= sprintf($template,
          $class, $image, $artistName, $albumTitle, $addToPlaylist
        );
      }
      $this->html .= '</ul>';
    }
    $this->html .= '</div>';
  }
  
  
  /**
   * Builds the HTML for albumSearch results
   *
   * @param array $data The result-Array to create HTML from
   * @param string $type type of search to get the template
   * @author Steffen Becker
   */
  private function albumSearchHTML($data, $type) {
    $this->html .= '<div class="albumSearch"><ul class="clearfix">';
    $i = 0; // counter variable for alternating li-elements
    foreach($data as $album) {
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; } else { $class = ''; }
      
      $template = $this->getTemplate($type);
      // $index = 0, we always want a single value and no array here
      $record = $this->getValue($album['record'], 0);
      $albumTitle = $this->getValue($album['albumTitle'], 0);
      $artistName = $this->getValue($album['artistName'], 0);
      $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($album['playlist'], 0));
      
      $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                     . $artistName . ' - ' . $albumTitle . '">Click here to add this album to your playlist</a></li>';
      
      $image = $this->getImage($album, 'Cover for ' . $artistName . ' - ' .  $albumTitle, 0, 'cover');
      $tags = $this->getTagList($album['tag']);
      
      $this->html .= sprintf($template,
        $class, $artistName . ' - ' . $albumTitle, $image, $tags, $addToPlaylist
      );
      
      $i++;
    }
    $this->html .= '</ul></div>';
  }
  
  
  /**
   * Builds the HTML for songSearch results
   *
   * @param array $data The result-Array to create HTML from
   * @param string $type type of search to get the template
   * @author Steffen Becker
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
      $image = $this->getImage($song, 'Image of ' . $artistName);
      
      $this->html .= sprintf($template,
        $class, $artistName . ' - ' . $songTitle, $image, $tags, $addToPlaylist
      );
      
      $i++;
    }
    $this->html .= '</ul></div>';
  }


  /**
   * Builds the HTML for recommendations
   * TOOD enhance display: more info
   *
   * @param array $data The result-Array to create HTML from
   * @param string $type type of search to get the template
   * @author Steffen Becker
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
          
          $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                         . $artistName . ' - ' . $albumTitle . '">Click here to add this album to your playlist</a></li>';

          $image = $this->getImage($result, 'Cover for ' . $artistName . ' - ' . $albumTitle, 0, 'cover');

          $this->html .= sprintf($template,
            $class, $image, $artistName, $albumTitle, $addToPlaylist
          );
          $j++;
        }        
        $this->html .= '</ul>';
      }
    } else {
      $this->html = '<h2>Could not create any recommendations.</h2>';
      $this->html .= '<p>Listen to some more music or reset your recently listened to list.</p>';
    }
  }

  /**
   * Builds the HTML for additional information for and artist
   *
   * @param array $data An array of additional information
   * @author Steffen Becker
   */
  private function infoHTML($data, $type) {
    $template = $this->getTemplate($type);
    $albumTemplate = $this->getTemplate('tagSearch');
    $artist = $this->getValue($data['artist']);
    $artistName = $this->getValue($data['artistName']);
    $image = $this->getImage($data, $artistName);
    $tags = $this->getTagList($data['tag']);
    $homepage = '';
    if (isset($data['homepage'])) {
      $homepage = '<p>Homepage: <a href="' . $this->getValue($data['homepage']) . '">' 
                . $this->getValue($data['homepage']) . '</a></li>';
    }
    $albumList = '';
    $records = $this->getValue($data['record']);
    if (is_array($records)) {
      // if there is more than one record...    
      $j = 0;
      foreach($records as $key => $record) {
        if (($j % 2) == 0) { $class = 'odd'; } else { $class = ''; }
        $albumTitles = $this->getValue($data['albumTitle']);
        $playlists = $this->getValue($data['playlist']);
        $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($data['playlist'], $key));
        $cover = $this->getImage($data, 'Cover for '. $albumTitles[$key], $key, 'cover');
        $addToPlaylist = '<li><a rel="' . $record . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                       . $artistName . ' - ' . $albumTitles[$key] . '">Click here to add this album to your playlist</a></li>';
        // use the template
        $albumList .= sprintf($albumTemplate,
          $class, $cover, $artistName, $albumTitles[$key], $addToPlaylist
        );
        $j++;
      }
    } else { // only one record
      $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $this->getValue($data['playlist']));
      $addToPlaylist = '<li><a rel="' . $records . '" class="addToPlaylist" href="' . $playlist . '" title="'  
                     . $artistName . ' - ' . $this->getValue($data['albumTitle']) . '">Click here to add this album to your playlist</a></li>';
      
      $albumList .= sprintf($albumTemplate, 
        '', $this->getImage($data, 'Cover for '. $this->getValue($data['albumTitle']), 0, 'cover'),
        $artistName, $this->getValue($data['albumTitle']), $addToPlaylist
      );
    }
  
    $additionalInfo = '';
    if (isset($data['location'])) {
      $additionalInfo .= '<li><a href="' . $this->getValue($data['location']) . '">Geonames Location</a></li>';
    }
    if (isset($data['sameAs'])) {
      // woot! we have a musicbrainz-ID, fetch more information, and link profiles
      $link = $this->getValue($data['sameAs']);
      $mbid = str_replace('http://zitgist.com/music/artist/', '', $link);
      
      $additionalInfo .= '<li><a href="' . $link . '">Zitgist-Dataviewer</a></li>';
      // append musicbrainz-link
      $mbLink = 'http://musicbrainz.org/show/artist/?mbid=' . $mbid;
      $additionalInfo .= '<li><a href="' . $mbLink . '">Musicbrainz profile</a></li>';

      // append last-fm information, if avaiable
      $last = new LastFM();
      $lastfmLink = $last->getArtistPage($mbid);
      if ($lastfmLink) {
        $additionalInfo .= '<li><a href="' . $lastfmLink . '">Artist page on last.fm</a></li>';
      }
    }
    // show the frame if we have additional information
    if (isset($data['location']) || isset($data['sameAs'])) {
      $additionalInfo = '<h3>Information from external sources</h3><ul class="linkList">' . $additionalInfo;
      $additionalInfo .= '</ul><iframe src=""></iframe>';
    }
        
    $this->html .= sprintf($template,
      $artistName, $image, $tags, $homepage, $albumList, $additionalInfo
    );
    
  }
  

  /**
   * Builds a list of <li>s with playlist-entries, no surrounding <ul> 
   *
   * @param array $data An array of playlist-items 
   * @author Steffen Becker
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
   * Returns an <img>-HTML-Tag for a given result. If the image is empty
   * (can happen because of OPTIONAL search in SPARQL), the linked img in
   * src is an empty default img
   *
   * @param array $image The complete result-array
   * @param string $altText The alt-Text the image will have
   * @param int $index optional, used for getValue for specific value-retrieval
   * @param string $type optional, default is image, can be set to 'cover'
   * @return string The <img>-Tag
   * @author Steffen Becker
   */
  private function getImage($image, $altText, $index = 0, $type = 'image') {
    // in most cases the image is optional, so it could be empty
    $img = '<img src="img/noimage.png" alt="No image found..." />';
    if (isset($image[$type])) {
      $image = $image[$type];
      if ($this->getValue($image, $index)) {
        $img = '<img src="' . $this->getValue($image, $index) . '" alt="' . $altText . '" />';
      }
    } 
    return $img;
  }
  
  
  /**
   * Returns a comma-seperated list of tags for a given array of 
   * Tag-URLs like http://dbtune.org/jamendo/tag/sometag
   *
   * @param array $tagsArray An array with tag-URLs
   * @return string A comma seperated tag-list, sth like rock, metal, stoner, classic
   * @author Steffen Becker
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
   * Returns a template-html-subpart for usage in sprintf-php-function
   *
   * @param string $type The type of search we build the HTML for
   * @return string The template subpart containing some %s's
   * @author Steffen Becker
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
            <strong>Avaiable albums:</strong><br />
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
    if ($type == 'info') {
      $template = '
        <h2>More Information for &raquo;%s&laquo;</h2>
        <div class="image">%s</div>
        <p>Tags: %s</p>
        %s
        <h3>Avaiable albums</h3>
        <div class="results">
          <ul>
            %s
          </ul>
        </div>
        %s
      ';
    }
    return $template;
  }
  
  
  /**
   * This helper-function returns an array or a string with the value
   * if the optional $index is set, it will always be a string, else it
   * could be an array
   * 
   * @param array $data The Array to get Values from
   * @param int $index optional, get a specific value from the $data-array
   * @return mixed string or array with the value(s)
   * @author Steffen Becker
   */
  private function getValue($data, $index = false) {
    $value = $data['value'];
    if (!empty($value)) {
      if (is_array($value)) {
        if (count($value) == 1) {
          return $value[0];
        } 
        if (count($value) > 1 && $index !== false) {
          if (isset($value[$index])) { // the index could not be set
            return $value[$index];
          } else {
            return '';
          }
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
   * Returns the final HTML-Code produced
   *
   * @return void
   * @author Steffen Becker
   */
  public function getHTML() {
    return $this->html;
  }
  
}

?>