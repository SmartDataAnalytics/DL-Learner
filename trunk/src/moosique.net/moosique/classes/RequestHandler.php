<?php

/**
 * 
 */
class RequestHandler extends Config {
  
  private $response;
  private $dataHelper;
  
  /* This inits requestHandling, a request is
     only processed, if it was called by an 
     xmlHTTPrequest and the get-method */
  function __construct() {
    parent::__construct(); // init config
    $this->dataHelper = new DataHelper();
        
    // we only accept ajax-get requests
    if ($this->isAjax() && $this->isGet()) {
      $this->response = $this->processRequest();
      
      if (!empty($this->response)) {
        return $this->response;
      } else {
        if (isset($_GET['searchType'])) {
          return "<h2>Sorry, nothing found.</h2>";
          if ($_GET['searchType'] == 'lastFM') {
            return '<h2>Nothing found for the last.fm-user &raquo;' . $_GET['searchValue'] . '&laquo;.';
          }
        } 
        return "<h2>The repsonse from the server was empty.</h2>";
      }
    } else { // we do nothing. This application is based on AJAX - back to home
      header('Location: ' . $this->getConfigUrl('base'));
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
    
    // ========== SEARCH REQUEST =================================
    if (isset($_GET['searchType']) && isset($_GET['searchValue'])) {
      $type = $_GET['searchType'];
      // clean up the search value
      $search = $this->cleanString($_GET['searchValue'], $type);

      
      // a search for "everything" causes 4 searches for artist, tags, album and songs
      // concatenating the response from all 4 searches
      if ($type == 'allSearch') {
        $response .= $this->createSearch($search, 'artistSearch');
        $response .= $this->createSearch($search, 'tagSearch');
        $response .= $this->createSearch($search, 'albumSearch');
        $response .= $this->createSearch($search, 'songSearch');
      } 
      // normal search for artist, tag or song
      if ($type == 'artistSearch' || $type == 'tagSearch' ||
          $type == 'songSearch' || $type == 'albumSearch') {
        $response = $this->createSearch($search, $type);
      }
      // last.fm search, initiate the lastFM-Class and get the tags for the user
      // then make an exakt-tag search with the result
      if ($type == 'lastFM') {
        $lastFM = new LastFM($search);
        $tags = $lastFM->getTopTags();
        // no we have the topTags, do a search for related albums
        if (!empty($tags)) {
          foreach($tags as $tag) {
            // FIXME when using last.fm-Tags for tagSearch, we want exakt results, meaning
            // no "darkmetalbeermusic" as result when the lastfm tag is "metal"
            // what we do here is nothing but a tag search for each tag
            $response .= $this->createSearch($this->cleanString($tag, $type), 'tagSearch', $this->getConfig('maxResults'));
          }
        } else { // let the view handle it, displays error
          $response .= $this->createSearch($tags, $type, $this->getConfig('maxResults'));
        }
      }
    }
    
    // ========== LEARNING REQUEST =================================
    if (isset($_GET['get']) && $_GET['get'] == 'recommendations') {
      $r = new Recommendations(); // init/reset recommendations
      $r->setPosExamples();
      $r->setInstances();
      $recommendations = $r->getQueries($this->dataHelper->connection);
      
      $results = array();
      if (is_array($recommendations) && !empty($recommendations)) {
        if (!empty($recommendations['queries'])) {
          foreach($recommendations['queries'] as $query) {
            // $data = $this->getData($query, 'recommendations', $this->getConfig('maxResults'));
            // TODO no limiting, else it is not really random...
            $data = $this->dataHelper->getData($query, 'recommendations');
            $results[] = $data;
          }
        }
        $recommendations['results'] = $results;
        $response .= $this->createView($recommendations, 'recommendations', '', $this->getConfig('maxResults'));
        
      } else { // an error occured during recommendation-retrieval
        // give the error message to the view
        $response .= $this->createView($recommendations, 'recommendations', '', $this->getConfig('maxResults'));
      }
    }
    

    // ========== ARTIST INFORMATION REQUEST =================================
    if (isset($_GET['info']) && !(empty($_GET['info']))) {
      $currentAlbum = $_GET['info'];
      $response .= '<p>Artist Information coming soon...</p>';
    }


    // ========== PLAYLIST REQUEST =================================
    // Due to a bug in YMP we don't directly deliver the .xspf-file, we return a prepared 
    // list of <li> including the links to the mp3-files, and this is build in the view of course.
    if (isset($_GET['get']) && isset($_GET['playlist'])) {
      $data = array('playlist' => $_GET['playlist'], 'albumID' => $_GET['rel']);
      $type = $_GET['get'];
      $search = ''; // nothing for playlist, in data already
      $preparedData = $this->dataHelper->prepareData($data, $type, $search);
      $response = $this->createView($preparedData, $type, $search);
    }
    
    // Finally returning the response
    return $response;
  }
  
  
  /**
   * 
   * 
   * 
   */
  private function createSearch($search, $type, $maxResults = false) {
    $data = $this->dataHelper->getData($search, $type);
    $response = $this->createView($data, $type, $search, $maxResults);
    return $response;
  }
  
  
  /**
   * 
   * 
   * 
   */
  private function createView($data, $type, $search, $maxResults = false) {
    $v = new View($data, $type, $search, $maxResults);
    $viewHTML = $v->getHTML();
    return $viewHTML;
  }
  
  
  /**
   * Removes unwanted chars from a search-string
   * 
   * If the search string contains a %20-Space-Char somewhere in the middle
   * of the string, it returns an array of search-values, divided by " "
   * Doing this we later can perform more searches for values like "stoner doom metal"
   * resulting in an array with [0] => stoner [1] => doom [2] => metal
   */
  private function cleanString($string, $type) {
    $remove = array('/', '?', '+', "=", '$', ',', '.', ':', ';', '\'', "\\", "\\\\");
    $string = trim($string);
    $string = str_replace($remove, '', $string);
    // and remove double whitespaces
    $string = str_replace(array('  ', '   '), ' ', $string);
    $string = strtolower($string);
        
    // if lastFM-Search or exakt search, we split "death metal" into an array 
    // containing "death" and "metal", for better search results
    // TODO implement for tagSearch too
    if ((strpos($string, " ") > 0) && ($type == 'lastFM' || $type == 'tagSearch')) {
      $string = explode(" ", $string);
    }
    
    return $string;
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