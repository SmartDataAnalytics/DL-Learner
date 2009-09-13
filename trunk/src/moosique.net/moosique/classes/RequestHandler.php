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
      // TODO
    }
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
   * Processes the request made through ajax, handles
   * the different searches and other related requests
   * 
   * @return 
   */
  private function processRequest() {
    
    // general search
    if (isset($_GET['searchType']) && isset($_GET['searchValue'])) {

      $search = $this->cleanString($_GET['searchValue']);
      
      // a search for "everything" causes 3 searches for artist, tags and songs
      if ($_GET['searchType'] == 'allSearch') {
        
        // TODO doing 3times the same thing is ugly, build function doing this
        
        // Artists
        $sparql = new SparqlQueryBuilder($search, 'artistSearch', 20);
        $query = $sparql->getQuery();
        $json = $this->connection->sparqlQuery($query);
        $result = json_decode($json);
        $artistObject = $result->results->bindings;
        $artistView = new View('artistSearch', $artistObject);
        $artistResponse = $artistView->getHTML();

        // Tags
        $sparql = new SparqlQueryBuilder($search, 'tagSearch', 20);
        $query = $sparql->getQuery();
        $json = $this->connection->sparqlQuery($query);
        $result = json_decode($json);
        $tagObject = $result->results->bindings;
        $tagView = new View('tagSearch', $tagObject);
        $tagResponse = $tagView->getHTML();

        
        // Songs
        $sparql = new SparqlQueryBuilder($search, 'songSearch', 20);
        $query = $sparql->getQuery();
        $json = $this->connection->sparqlQuery($query);
        $result = json_decode($json);
        $songObject = $result->results->bindings;
        $songView = new View('songSearch', $songObject);
        $songResponse = $songView->getHTML();

        // merge results, and return it
        return $artistResponse . $tagResponse . $songResponse;        
        
      } else { // normal tag/artist/song-search
        
        $sparql = new SparqlQueryBuilder($search, $_GET['searchType']);
        $query = $sparql->getQuery();
        
      
        // sparql-query to dellearner
        $json = $this->connection->sparqlQuery($query);
        // convert to useable object
        $result = json_decode($json);
        $resultObject = $result->results->bindings;     
         
        // create and return the response
        $view = new View($_GET['searchType'], $resultObject);
        $response = $view->getHTML();
      }
      return $response;
    }
    
    // TODO other requuests
    
    // a playlist is requested. due to a bug in ymp we don't
    // directly deliver the .xspf-file, we return a prepared 
    // list of <li> including the links to the mp3-files
    if (isset($_GET['get']) && isset($_GET['playlist'])) {
      $view = new View($_GET['get'], 
        array('playlist' => $_GET['playlist'], 'albumID' => $_GET['rel'])
      );
      return $view->getHTML();
    }
  }
  
  /**
   * Establishes a new Dl-Learner Connection and saves it in 
   * private connection for class-wide use. 
   *
   * @return 
   */
  private function establishConnection() {
    $this->connection = new DllearnerConnection();
  }

  /**
   * Removes unwanted chars from a string
   *
   */
  private function cleanString($string) {
    $remove = array(' ', '/', '?', '-', '_', '+', "=", '$', ',', '.', ':', ';', '\'', "\"", "\\", "\\\\");
    $string = str_replace($remove, '', $string);
    return $string;
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