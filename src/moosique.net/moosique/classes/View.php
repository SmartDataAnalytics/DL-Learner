<?php
/**
 * This class handles all HTML-Output for all different kinds of requests. 
 * It also creates error messages shown in the frontend
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class View extends Config {
  
  private $html = ''; // the final HTML-Output is stored in here
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
    
    $this->setLimit($limit);
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
    $searchText = $this->getSearchText($search);
    
    // create error-headline, if data empty or no array 
    if (!is_array($data) || empty($data)) {
      $this->html .= $this->createHeadline($data, $type, $searchText, true);
    } else {
      // finally we are producing html, depending on the type of request
      // use the limits on the data before creating html, except for the playlist-html and more info
      if ($type != 'playlist' && $type != 'info') {
        $data = $this->limitData($data);
      }

      $this->html .= $this->createHeadline($data, $type, $searchText);
      switch ($type) {
        case 'tagSearch' : 
          if (is_array($search)) {
            // special case, use the albumSearch view for this one
            $type = 'albumSearch';
            $this->html .= $this->createAlbumListHTML($data, $type);
          } else { // default case
            $this->tagSearchHTML($data, $type);
          }
        break;
        case 'artistSearch'    : $this->artistSearchHTML($data, $type); break;
        case 'albumSearch'     : $this->html .= $this->createAlbumListHTML($data, $type); break;
        case 'songSearch'      : $this->html .= $this->createAlbumListHTML($data, $type); break;
        case 'recommendations' : $this->recommendationsHTML($data, $type); break;
        case 'info'            : $this->infoHTML($data, $type); break;
        case 'playlist'        : $this->playlistHTML($data); break;
      }
      $this->wrapInDiv($type);
    }
  }


  /**
   * Set the global/userdefined limit 
   *
   * @param int $limit Optional, if nothing set, uses maxResults from config.ini
   * @author Steffen Becker
   */
  private function setLimit($limit) {
    if ($limit === false) { // if no special limit set, we use maxResults
      $this->limit = $this->getConfig('maxResults');
    } else {
      $this->limit = $limit;
    }
  }
  
  
  /**
   * Returns the string-representation of the serach-Value
   *
   * @param mixed $search If it is an array, it seperates the value with " "
   * @return string The searchText
   * @author Steffen Becker
   */
  function getSearchText($search) {
    if (is_array($search)) {
      return implode(' ', $search);
    } else {
      return $search;
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
    // dont limit anything if limit == 0
    if ($count > $this->limit && $this->limit > 0) {
      if ($this->getConfig('randomize')) {
        $random = array_rand($data, $this->limit);
        $newData = array();
        foreach($random as $randRecord) {
          $newData[$randRecord] = $data[$randRecord];
        }
        $data = $newData;
      } else { // first xx results are shown
        $data = array_slice($data, 0, $this->limit);
      }
      $this->html .= $this->showRandomLimitNote($count);
    }
    return $data;
  }


  /**
   * Creates an h2-Headline for different SearchTypes, also produces
   * h2-error messages if $error = true
   *
   * @param array $data The whole Data, sometimes the error-messages are in there
   * @param string $type The type of search: tagSearch, albumSearch etc.
   * @param string $searchText The prepared searchText
   * @param bool $error Optional, set to true, to produce error-messages
   * @return string <h2>-HTMl ready to use
   * @author Steffen Becker
   */
  private function createHeadline($data, $type, $searchText, $error = false) {
    $h2 = '';
    $typeNice = str_replace('Search', '', $type); 
    
    if ($error !== false) { // produce an error headline
      if (strpos($type, 'Search') > 0) {
        $h2 = '<h2>No ' . $typeNice . 's found for &raquo;' . $searchText . '&laquo;</h2>';
      } else if ($type == 'lastFM') {
        $h2 = '<h2>The last.fm-user &raquo;' . $searchText . '&laquo; does not exist.</h2>';
      } else if ($type == 'recommendations') {
        $h2 = '<h2>' . $data . '</h2>';
      } else if ($type == 'info') {
        $h2 = '<h2>Could not find any information about the currently playing song or artist.</h2>';
      }
    } else {
      if (strpos($type, 'Search') > 0) { // type == search
        $typeNice = ucfirst($typeNice);
        $h2 = '<h2>' . $typeNice . '-results for &raquo;' . $searchText . '&laquo;</h2>';
      } else { 
        // do nothing here, no h2 for recommendations, playlist, lastFM or info
      }
    }
    return $h2;
  }
  
  
  /**
   * Produces a Notification for limited results
   *
   * @param int $count The Limit of the Results
   * @return string The note as HTML
   * @author Steffen Becker
   */
  private function showRandomLimitNote($count) {
    if ($this->getConfig('randomize')) {
      return '<p><strong>Note:</strong> Found ' . $count . ' results, showing ' . $this->limit . ' random results.';
    } else {
      return '<p><strong>Note:</strong> Found ' . $count . ' results, showing the first ' . $this->limit . ' results.';
    }
  }
  
  
  /**
   * Wraps $this->html in a Div-Container if it is a Search
   *
   * @param string $type The searchType, such as artistSearch etc.
   * @author Steffen Becker
   */
  private function wrapInDiv($type) {
    if (strpos($type, 'Search') > 0) {
      $this->html = '<div class="' . $type . '">' . $this->html . '</div>';
    }
  }


  /**
   * Returns an HTML-Link that can be added to a playlist
   *
   * @param string $rel The related record, as URL
   * @param string $playlist The playlist-URL to an XSPF-File
   * @param string $title The text for the Link 
   * @return string The HTML-Link with addToPlaylist-class
   * @author Steffen Becker
   */
  private function wrapAddToPlaylist($rel, $playlist, $title) {
    $playlist = str_replace('?item_o=track_no_asc&aue=ogg2&n=all', '', $playlist);
    return '<a rel="' . $rel . '" class="addToPlaylist" href="' . $playlist . '" title="Click to add to your playlist">' . $title . '</a>';
  }


  /**
   * Returns the HTML for an artist serach, containing special stuff
   * like homepage-links, album-list etc.
   *
   * @param array $data The result-Array to create HTML from
   * @param string $type type of search to get the template
   * @author Steffen Becker
   */
  private function artistSearchHTML($data, $type) {
    $i = 0; // counter variable for alternating li-elements
    $this->html .= '<ul class="clearfix">';
    foreach($data as $result) {
      // alternating classes for li-elements
      if (($i % 2) == 0) { $class = 'odd'; } else { $class = ''; }
      $artistName = $this->getValue($result['artistName']);
      $template = $this->getTemplate($type);
      $addToPlaylist = '';

      // avaiable Records
      $records = $this->getValue($result['record']);
      // if there is only one entry, getValue will return a string,
      // but we want an array -- make it one
      if (!is_array($records)) $records = array(0 => $records);
      foreach($records as $key => $record) {
        $addToPlaylist .= '<li>' . $this->wrapAddToPlaylist(
          $record,
          $this->getValue($result['playlist'], $key), 
          $artistName  . ' &mdash; ' . $this->getValue($result['albumTitle'], $key)
        ) . '</li>';
      }
    
      $this->html .= sprintf($template,
        $class, $artistName, $this->getImage($result, 'Image of ' . $artistName), 
        $this->getTagList($result['tag']), $addToPlaylist
      );
      $i++;
    }
    $this->html .= '</ul>';
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
    foreach ($data as $key => $result) {
      $this->html .= '<h3>Albums tagged with <em>' . str_replace('http://dbtune.org/jamendo/tag/', '', $key) . '</em></h3>';
      $recordArray = array();
      // we rebuild every-TagResult array to look like an albumSearch-Array
      $records = $this->getValue($result['record']);
      // if there is only one entry, getValue will return a string,
      // but we want an array -- make it one
      if (!is_array($records)) $records = array(0 => $records);
      foreach($records as $i => $record) {
        $recordArray[$record] = array(
          'artist'     => array('value' => $this->getValue($result['artist'], $i)),
          'artistName' => array('value' => $this->getValue($result['artistName'], $i)),
          'record'     => array('value' => $this->getValue($result['record'], $i)),
          'albumTitle' => array('value' => $this->getValue($result['albumTitle'], $i)),
          'tag'        => array('value' => $this->getValue($result['tag'], $i)),
          'playlist'   => array('value' => $this->getValue($result['playlist'], $i)),
          'cover'      => array('value' => $this->getValue($result['cover'], $i))
        );
      }
      // and limit the results for each tag (the shown albums) too
      $recordArray = $this->limitData($recordArray);
      // and now we can use the createAlbumList for 
      $this->html .= $this->createAlbumListHTML($recordArray, $type);
    }
  }
  
  
  /**
   * Creates an HTML-UL-List with all albumresults, adding odd-classes to the
   * li elements and a tagList, if the type of search is songSearch or albumSearch
   *
   * @param array $data The whole data-set, ordered by records is best
   * @param string $type The type of search: songSearch etc.
   * @return string The HTML-Code for the ul
   * @author Steffen Becker
   */
  private function createAlbumListHTML($data, $type) {
    $html = '<ul class="clearfix">';
    $i = 0;
    foreach($data as $key => $result) {
       // counter variable for alternating li-elements with even/odd classes
      if (($i % 2) == 0) { $class = 'class="odd"'; } else { $class = ''; } $i++;
      
      $artistName = $this->getValue($result['artistName'], $key);      
      $template = $this->getTemplate($type);
      
      $title = $this->getValue($result['albumTitle'], $key);
      $image = $this->getImage($result, $artistName. ' &mdash; ' .  $title, $key, 'cover');
      $taglist = '';
      
      // special cases if songSearch: other titles and image
      if ($type == 'songSearch') $title = $this->getValue($result['songTitle'], $key);
      if ($type == 'songSearch') $image = $this->getImage($result, $artistName. ' &mdash; ' .  $title, $key);
      // the tagList is not empty for song or albumSearches
      if ($type == 'songSearch' || $type == 'albumSearch') $taglist = $this->getTagList($result['tag']);
      
      $html .= sprintf($template, $class, 
        $this->wrapAddToPlaylist(
          $this->getValue($result['record'], $key),
          $this->getValue($result['playlist'], $key), 
          $artistName . ' &mdash; ' . $title
        ),
        $this->wrapAddToPlaylist(
          $this->getValue($result['record'], $key),
          $this->getValue($result['playlist'], $key),
          $image
        ),
        $taglist
      );
    }
    $html .= '</ul>';
    return $html;
  }
  
  
  /**
   * Builds the HTML for recommendations
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
        // recommendation-results look like tagResults
        $this->html .= $this->createAlbumListHTML($resultSet, 'tagSearch');
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
   * @param string $type the type of search
   * @author Steffen Becker
   */
  private function infoHTML($data, $type) {
    $template = $this->getTemplate($type);
    $artist = $this->getValue($data['artist']);
    $artistName = $this->getValue($data['artistName']);
    // TODO
    $this->debugger->log($data);
    $this->debugger->log($this->getValue($data['record']);
    $albumList = $this->createAlbumListHTML(array($data), 'tagSearch');

    $homepage = ''; // could be empty
    if (isset($data['homepage'])) {
      $homepage = '<p><strong>Homepage</strong>:<br /><a href="' . $this->getValue($data['homepage']) . '">' 
                . $this->getValue($data['homepage']) . '</a></li>';
    }

    $additionalInfo = ''; // could also be empty
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
      $additionalInfo = '<h3>Information from external sources</h3>' 
                      . '<ul class="externalLinks">' . $additionalInfo . '</ul><iframe src=""></iframe>';
    }
        
    $this->html .= sprintf($template,
      $artistName, 
      $this->getImage($data, $artistName), 
      $this->getTagList($data['tag']), $homepage, $albumList, $additionalInfo
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
                       . $track['creator'] . ' &mdash; ' . $track['title']
                       . '</a></li>';  
        }
      } else { // only one track, no sub-array, no foreach, same data
        $track = $tracks;
        $this->html .= '<li><a rel="' . $albumID . '" href="' . $track['location'] . '" class="htrack">'
                     . $track['creator'] . ' &mdash; ' . $track['title']
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
    return '<p class="tagList"><strong>Tags:</strong><br />' . $tags . '</p>';
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
          <h4>%s</h4>
          <div class="image">%s</div>
          %s
          <strong>Avaiable albums:</strong>
          <ul>%s</ul>
        </li>
      ';
    }
    // the last %s is the optional tag-list
    if ($type == 'tagSearch' || $type == 'recommendations' ||
        $type == 'albumSearch' || $type == 'songSearch') {
      $template = ' 
        <li %s>
          <h4>%s</h4>
          <div class="image">%s</div>
          %s
        </li>
      ';
    }
    if ($type == 'info') {
      $template = '
        <h2>More Information for &raquo;%s&laquo;</h2>
        <div class="image">%s</div>
        %s %s
        <h3>Avaiable albums</h3>
        <div class="results">
        %s
        </div>
        %s
      ';
    }
    return $template;
  }
  
  
  /**
   * This helper-function returns an array or a string with the value.
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
          // the array contains only one value, return it's string
          return $value[0];
        } 
        if (count($value) > 1 && $index !== false) {
          if (isset($value[$index])) { 
            // return the special string
            return $value[$index];
          } else { 
            // if the $index does not exist, return value with index = 0
            return $value[0];
          }
        } else { 
          // an array is requested, tagList for example
          return $value;
        }
      } else { 
        // it's a string already, return it
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