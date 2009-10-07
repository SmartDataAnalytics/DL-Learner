<?php

/**
 * 
 */
class RequestHandler extends Config {
  
  private $response;
  private $connection;
  
  /* This inits requestHandling, a request is
     only processed, if it was called by an 
     xmlHTTPrequest and the get-method */
  function __construct() {
    parent::__construct(); // init config
    
    $this->establishConnection();
        
    // we only accept ajax-get requests
    if ($this->isAjax() && $this->isGet()) {
      $this->response = $this->processRequest();
    } else {
      // we do nothing. This application is based on AJAX.
    }
  }
  
  /**
   * Processes the request made through ajax, handles
   * the different searches and other related requests
   * 
   * @return 
   */
  private function processRequest() {
    $response = '';
    
    // general search for all / artist / tags / song
    // ======================================================================
    if (isset($_GET['searchType']) && isset($_GET['searchValue'])) {
      
      // clean up the search value
      $search = $this->cleanString($_GET['searchValue'], $_GET['searchType']);
      
      // a search for "everything" causes 3 searches for artist, tags and songs
      // concatenating the response from all 3 searches
      if ($_GET['searchType'] == 'allSearch') {
        // artist
        $data = $this->getData($search, 'artistSearch');
        $view = new View($data, 'artistSearch');
        $response .= $view->getHTML();
        // tag
        $data = $this->getData($search, 'tagSearch');
        $view = new View($data, 'tagSearch');
        $response .= $view->getHTML();
        // song
        $data = $this->getData($search, 'songSearch');
        $view = new View($data, 'songSearch');
        $response .= $view->getHTML();
        
      } 
      
      if ($_GET['searchType'] == 'artistSearch' || 
          $_GET['searchType'] == 'tagSearch' ||
          $_GET['searchType'] == 'songSearch') {
        // normal search for artist, tag or song
        $data = $this->getData($search, $_GET['searchType']);
        $view = new View($data, $_GET['searchType']);
        $response = $view->getHTML();
        
      }
      
      if ($_GET['searchType'] == 'lastFM') {
        $lastFM = new LastFM($search);
        $tags = $lastFM->getTopTags();
        // no we have the topTags, do a search for related albums
        foreach($tags as $tag) {
          // TODO when using last.fm-Tags for tagSearch, we want exakt results, meaning
          // no "darkmetalbeermusic" when the lastfm tag is "metal"
          
          $tag = $this->cleanString($tag, $_GET['searchType']);
          // displaying 10 results per tag (=100 results) should be enough
          $data = $this->getData($tag, $_GET['searchType'], 20);
          $view = new View($data, $_GET['searchType']);
          $response .= $view->getHTML();
        }
      }
    }
    
    // TODO other requests --- artist Information
    
    
    
    
    // A Learning-Request
    // ======================================================================
    if (isset($_GET['learn']) && $_GET['learn'] == 'now') {
      $posExamples = $this->getPositiveExamples();
      $instances = $this->getInstances($posExamples);
    
      $this->debugger->log($instances, "INSTANZEN");
      $this->debugger->log($posExamples, "posExamples");
      
      $res = $this->connection->learn($instances, $posExamples);

      // TODO (Yes, BIG one) 
      // build sparql-query based on learning-results
      // build html-view build an sparql-results --- misuse tagSearch-Thingie
      foreach ($res as $solution) {
        $response .= round($solution->scoreValue*100, 2) . '% --- ' . $solution->descriptionKBSyntax . "\n";
        // $response .= $this->connection->kbToSqarql($solution->descriptionKBSyntax) . "\n";
      }
      
      $response = '<pre>' . $response . '</pre>';
      
    }
    
    
    // A Playlist-Request
    // ======================================================================
    if (isset($_GET['get']) && isset($_GET['playlist'])) {
      // Due to a bug in YMP we don't directly deliver the .xspf-file, we return a prepared 
      // list of <li> including the links to the mp3-files, and this is build in the view of course.
      $data = $this->prepareData(
        array('playlist' => $_GET['playlist'], 'albumID' => $_GET['rel']), 
        $_GET['get']
      );
      $view = new View($data, $_GET['get']);
      $response = $view->getHTML();
    }
    
    
    
    // Finally returning the response
    if (!empty($response)) {
      return $response;
    } else {
      if ($_GET['searchType'] == 'lastFM') {
        return '<h2>Nothing found for the last.fm-user &raquo;' . $_GET['searchValue'] . '&laquo;.';
      }
      return "<h2>The repsonse from the server was empty.</h2>";
    }
  }
  
  
  /**
   *
   *
   *
   */
  private function getPositiveExamples() {
    $posExamples = array();
    if (!empty($_COOKIE['moosique'])) {
      $recent = json_decode(stripslashes($_COOKIE['moosique']))->recentlyListened;
      foreach($recent as $link) {
        // extract relation from the cookie-link
        preg_match_all('#<a\s*(?:rel=[\'"]([^\'"]+)[\'"])?.*?>((?:(?!</a>).)*)</a>#i', $link, $record);
        array_push($posExamples, $record[1][0]);
      }
    }
    
    $posExamples = array_unique($posExamples);
    return $posExamples;
    
  }
  
  /**
   *
   *
   *
   */
  private function getInstances($posExamples) {
    // TODO more testing, what is the optimum posExamples/neutral ratio, 50/50? 
    // for now we assume 50/50
    // $totalInstances = $this->getConfigLearning('instances');
    $instances = array();
    // and then add some random Records _not_ in this list
    $allRecords = file($this->getConfigUrl('allRecords'));
    $countPos = count($posExamples);

    for ($i = 0; $i < $countPos; $i++) {
      $randomRecord = trim($allRecords[array_rand($allRecords)]);
      // no double entries for the $instances-array
      if (!in_array($randomRecord, $posExamples)) {
        array_push($instances, $randomRecord);
      }
    }
    // merge with posExamples
    $instances = array_merge($posExamples, $instances);
    shuffle($instances);
    
    return $instances;
  }
  
  
  /**
   *
   * @param Integer $limit optional Limit for Sparql-Query
   */
  private function getData($search, $type, $limit = 0) {
    $sparql = new SparqlQueryBuilder($search, $type, $limit);
    $query = $sparql->getQuery();
    // sparql-query to dellearner
    $json = $this->connection->sparqlQuery($query);
    // convert to useable object
    $result = json_decode($json);
    $resultObject = $result->results->bindings;     
       
    // prepare the data for HTML processing
    $data = $this->prepareData($resultObject, $type);
    return $data;
  }
  
  
  /**
   * Establishes a new Dl-Learner Connection and saves it in 
   * private $connection for class-wide use. 
   *
   * @return 
   */
  private function establishConnection() {
    $this->connection = new DllearnerConnection();
  }

  /**
   * Removes unwanted chars from a search-string
   * 
   * TODO - NOT IMPLEMENTED but prepared
   * If the search string contains a %20-Space-Char somewhere in the middle
   * of the string, it returns an array of search-values, divided by " "
   * Doing this we later can perform more searches for values like "stoner doom metal"
   * resulting in an array with [0] => stoner [1] => doom [2] => metal
   */
  private function cleanString($string, $type) {
    // $remove = array('/', '?', '+', "=", '$', ',', '.', ':', ';', '\'', "\"", "\\", "\\\\");
    $remove = array(' ', '/', '?', '+', "=", '$', ',', '.', ':', ';', '\'', "\"", "\\", "\\\\");
    $string = trim($string);
    $string = str_replace($remove, '', $string);
    // and remove double whitespaces
    $string = str_replace(array('  ', '   '), ' ', $string);
    
    // when searching for tags due to the jamendo-tag handling we split 
    // tags into an array for better filter-results
    /*
    if ((strpos($string, " ") > 0) && $type = 'tagSearch') {
      $string = explode(" ", $string);
      $this->debugger->log($string, "MEHR ALS ZWEI");
    }
    */
    return $string;
  }
  
  
  
  /**
   * Cleans up objects or retrieves them from a XML-File (for playlists)
   * and converts them into arrays for use in the view class
   *
   * @param Mixed $data The Data-Object (from a Sparql-Query or a playlist-array)
   * @param String $type To define what kind of data to prepare
   * @return Array A multidimensional Array ready for processing for HTML-output
   */ 
  private function prepareData($data, $type) {
    $mergedArray = array();
    
    switch ($type) {
      case 'artistSearch' :
        $mergedArray = $this->mergeArray($data, 'artist');
        $mergedArray = $this->arrayUnique($mergedArray);
      break;
      
      case 'tagSearch' :
        $mergedArray = $this->mergeArray($data, 'tag');
      break;
      
      case 'songSearch' :
        $mergedArray = $this->mergeArray($data, 'track');
        $mergedArray = $this->arrayUnique($mergedArray);
      break;

      case 'lastFM' :
        $mergedArray = $this->mergeArray($data, 'tag');
      break;
      
      case 'albumPlaylist' :
        $playlistObject = simplexml_load_file($data['playlist']);
        $mergedArray = $this->object2array($playlistObject);
        // prepend the album stream-information
        $mergedArray['albumID'] = $data['albumID'];
      break;

      case 'trackPlaylist' :
        $playlistObject = simplexml_load_file($data['playlist']);
        $mergedArray = $this->object2array($playlistObject);
        $mergedArray['albumID'] = $data['albumID'];
      break;
    }
    
    return $mergedArray;
  }
  
  
  /**
   * TOOD implement nice merging for multi-tag-search
   * This function merges the result-Object to a nice array
   * we can process easily. The array is created by type,
   * returning the data sorted for artist, tag or song
   *
   * @param Object $data
   * @param String $type This can be 'artist', 'tag' or 'song'
   * @return Array A Multidimensional array sorted by type for output-use
   */   
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
   * Like the php-function array_unique, but for multidimensional arrays, calls itself recursively
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
  
  
  /**
   * Returns the HTML stored in the private $response
   * 
   * @return String HTML-Code generated  
   */
  public function getResponse() {
    return $this->response;
  }
  
  
  /**
   * Checks if the request made is an AJAX-Request and returns true if so
   * 
   * @return Boolean 
   */  
  private function isAjax() {
    if ( isset($_SERVER['HTTP_X_REQUESTED_WITH']) && 
         ($_SERVER['HTTP_X_REQUESTED_WITH'] == 'XMLHttpRequest') ) { 
      return true;
    } else {
      return false;
    } 
  }


  /**
   * Checks if the request made is a GET-Request and returns true if so 
   *
   * @return Boolean 
   */
  private function isGet() {
    if (isset($_GET) && !empty($_GET)) {
      return true;
    } else {
      return false;
    }
  }
  
  
  /**
   * Checks if the request made is a POST-Request and returns true if so 
   * 
   * @return Boolean
   */
  private function isPost() {
    if (isset($_POST) && !empty($_POST)) {
      return true;
    } else {
      return false;
    }
  }

}

?>