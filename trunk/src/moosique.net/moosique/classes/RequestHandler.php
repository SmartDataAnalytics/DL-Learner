<?php


class RequestHandler {
  
  private $response;
  
  /* This inits requestHandling, a request is
     only processed, if it was called by an 
     xmlHTTPrequest and the get-method */
  function __construct() {
    if ($this->isAjax() && $this->isGet()) {
      // nice, must be some kind of search
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
    if ($_GET['searchSubmit'] && $_GET['typeOfSearch'] && $_GET['searchValue']) {
      // prepare the search-term and build the sparql-query
      $search = preg_quote($_GET['searchValue']);
      $sparql = new SparqlQueryBuilder($search, $_GET['typeOfSearch']);
      $query = $sparql->getQuery();
      
      // establish the connection to the DL-learner and search
      $connection = new DllearnerConnection();
      $json = $connection->sparqlQuery($query);
      
      $result = json_decode($json);
      $resultArray = $result->results->bindings;      
      
      // create and return the response
      $view = new View($_GET['typeOfSearch'], $resultArray);
      $response = $view->getHTML();
      return $response;
    }
    
    // TODO other requuests
    if (1==1) {
      
    }    
    
  }

  /**
   * True if the Request Method was AJAX
   * 
   * @return 
   */  
  private function isAjax() {
    if ( isset($_SERVER['HTTP_X_REQUESTED_WITH']) && 
         ($_SERVER['HTTP_X_REQUESTED_WITH'] == 'XMLHttpRequest') ) { 
      return true;
    } else {
      return false;
    } 
  }


  private function isGet() {
    if (isset($_GET) && !empty($_GET)) {
      return true;
    } else {
      return false;
    }
  }
  
  private function isPost() {
    if (isset($_POST) && !empty($_POST)) {
      return true;
    } else {
      return false;
    }
  }
  
}




?>