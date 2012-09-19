<?php
/**
 * General Request handler - all requests made from the frontend
 * are handled here. Calls the different methods and classes for
 * seraching, learning and playlist/info requests.
 * only accepts ajax-requests
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class RequestHandler extends Config {
  
  private $response;
  private $dataHelper;
  
  /**
   * This inits requestHandling, a request is only processed, if it was 
   * called by an xmlHTTPrequest and the get-method - we don't use POST at all
   * 
   * @author Steffen Becker
   */
  function __construct() {
    parent::__construct(); // init config
    $this->dataHelper = new DataHelper(); 
        
    // we only accept ajax-get requests
    if ($this->isAjax() && $this->isGet()) {
      $this->response = $this->processRequest();
      // and return the response if it is not empty
      if (!empty($this->response)) {
        return $this->response;
      } else {
        if (isset($_GET['searchType'])) {
          return "<h2>Sorry, nothing found.</h2>";
        } 
        return "<h2>The repsonse from the server was empty.</h2>";
      }
    } else { // we do nothing. This application is based on AJAX - back to home
      header('Location: ' . $this->getConfigUrl('base'));
    }
  }


  /**
   * Processes the requests made to the server in the webapplication moosique.net
   * This is where the different requests are handled and data is fetched
   *
   * @return string The response/results as ready-to-use HTML
   * @author Steffen Becker
   */
  private function processRequest() {
    $response = '';
    
    // ========== SEARCH REQUEST =============
    if (isset($_GET['searchType']) && isset($_GET['searchValue'])) {
      $type = $_GET['searchType'];
      // clean up the search value, remove unwanted chars etc.
      $search = $this->cleanString($_GET['searchValue'], $type);

      // a search for "everything" causes 4 searches for artist, tags, album and songs
      // concatenating the response from all 4 searches
      if ($type == 'allSearch') {
        $response .= $this->createSearch($search, 'artistSearch');
        // do a special search-String-formatting for the tagSearch part
        $search = $this->cleanString($_GET['searchValue'], 'tagSearch');
        $response .= $this->createSearch($search, 'tagSearch');
        // and reset it afterwards
        $search = $this->cleanString($_GET['searchValue'], $type);
        $response .= $this->createSearch($search, 'albumSearch');
        $response .= $this->createSearch($search, 'songSearch');
      } 
      // normal search for artist, tag, album or song
      if ($type == 'artistSearch' || $type == 'tagSearch' ||
          $type == 'songSearch' || $type == 'albumSearch') {
        $response = $this->createSearch($search, $type);
      }
      // last.fm search, initiate the lastFM-Class and get the tags for
      // the user ($search) - then make tagSearches for every result
      if ($type == 'lastFM') {
        $lastFM = new LastFM();
        $tags = $lastFM->getTags($search);
        // no we have the topTags, do a search for related albums
        if (!empty($tags)) {
          foreach($tags as $tag) {
            // ALWAYS limit the results for last-FM search
            $response .= $this->createSearch($this->cleanString($tag, $type), 'tagSearch', $this->getConfig('maxResults'));
          }
        } else { // let the view handle it: <h2>-Error-Message
          $response .= $this->createSearch($tags, $type, $this->getConfig('maxResults'));
        }
      }
    }
    
    
    // ========== LEARNING REQUEST ===========
    if (isset($_GET['get']) && $_GET['get'] == 'recommendations') {
      // create a new recommendations-class and set the instances/posExamples
      $r = new Recommendations();
      $r->setPosExamples();
      $r->setInstances();
      // get all queries/kbSyntaxes
      $recommendations = $r->getQueries($this->dataHelper->connection);
      $this->debugger->log($recommendations, "RECS");
      $results = array();
      // if we have some recommendations and queries for them
      if (is_array($recommendations) && !empty($recommendations)) {
        if (!empty($recommendations['queries'])) {
          foreach($recommendations['queries'] as $query) {
            // get the results for each query created
            $data = $this->dataHelper->getData($query, 'recommendations');
            $results[] = $data;
          }
        }
        // add them to the multi-dimensional recommendations-Array, it now
        // contains results, scores, syntaxes and queries
        $recommendations['results'] = $results;
        // finally create the view for the recommendations
        $response .= $this->createView($recommendations, 'recommendations', '', $this->getConfig('maxResults'));
        
      } else { // an error occured during recommendation-retrieval
        // give the error message to the view
        $response .= $this->createView($recommendations, 'recommendations', '', $this->getConfig('maxResults'));
      }
    }
    

    // ========== ARTIST INFORMATION REQUEST =============
    if (isset($_GET['info']) && !(empty($_GET['info']))) {
      $response .= $this->createSearch($_GET['info'], 'info');
    }


    // ========== PLAYLIST REQUEST =================================
    // Due to a bug in YMP we don't directly deliver the .xspf-file, we return a prepared 
    // list of <li> including the links to the mp3-files - and this is build in the view of course.
    if (isset($_GET['get']) && isset($_GET['playlist'])) {
      $data = array('playlist' => $_GET['playlist'], 'albumID' => $_GET['rel']);
      $type = $_GET['get'];
      $search = ''; // nothing for playlist, in data already
      $preparedData = $this->dataHelper->prepareData($data, $type, $search);
      $response = $this->createView($preparedData, $type, $search);
    }
    
    // matched all possible cases, in other cases response is empty
    return $response;
  }
  
  
  /**
   * Creates a search request for the given SearchValue
   *
   * @param mixed $search The value to search for, can be a string or and array for tagSeach/lastFM
   * @param string $type The type of search: tagSearch, albumSearch, songSearch etc.
   * @param int $maxResults Limit for maximum shown results, optional
   * @return void
   * @author Steffen Becker
   */
  private function createSearch($search, $type, $maxResults = false) {
    $data = $this->dataHelper->getData($search, $type);
    $response = $this->createView($data, $type, $search, $maxResults);
    return $response;
  }
  
  
  /**
   * Creates a view for given data and searchvalue/types
   *
   * @param array $data The Data-Array to create the view from
   * @param string $type The type of search made
   * @param mixed $search The search string or array of strings (for tag/lastfm-serach)
   * @param int $maxResults A limit for showing results, optional
   * @return string The final HTML-Code, ready to use
   * @author Steffen Becker
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
   * resulting in an array with [0] => stoner [1] => doom [2] => metal for better results
   *
   * @param string $string The searchValue the user entered 
   * @param string $type The type of search: artistSearch, tagSearch etc.
   * @return mixed A cleaned string or an array of cleaned strings
   * @author Steffen Becker
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
    if ((strpos($string, " ") > 0) && ($type == 'lastFM' || $type == 'tagSearch')) {
      $string = explode(" ", $string);
    }
    return $string;
  }
  
  
  /**
   * Returns the private $response == the final HTML
   *
   * @return string The final HTML produced by this class
   * @author Steffen Becker
   */
  public function getResponse() {
    return $this->response;
  }
  

  /**
   * Checks for an ajax-request
   *
   * @return boolean True if the request was made via ajax, false if not
   * @author Steffen Becker
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
   * Checks for a GET-Request
   *
   * @return boolean True if the request is a GET-Request, false if not
   * @author Steffen Becker
   */
  private function isGet() {
    if (isset($_GET) && !empty($_GET)) {
      return true;
    } else {
      return false;
    }
  }

}

?>