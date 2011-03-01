<?php
/**
 * This Class handles the connection to the DL-Learner WebService
 * and sends/receives all request (Sparql/Learning) to it
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class DllearnerConnection extends Config {    
  
  private $knowledgeSourceID; // the id for the knowledge-source
  private $client;    // here we store the soap-client
  private $endpoint;  // the currently used endpoint

  /**
   * Object initializing: retrieve ini-values (parent::__constuct), set the endpoint
   * and include the neccessary wsdl-utilities and try to establish
   * a connection to the dl-learner webservice
   *  
   * @author Steffen Becker   
   */
  function __construct() {
    parent::__construct(); // init config
    // we use jamendo as the default sparql-endpoint
    $this->setEndpoint($this->getConfigUrl('jamendo'));
    // load WSDL files (has to be done due to a Java web service bug)
    ini_set('soap.wsdl_cache_enabled', '0');
    // include('Utilities.php');
    // Utilities::loadWSDLfiles($this->getConfigUrl('wsdl'));    
    $this->connect();
  }
  
  
  /**
   * Tries to connect to the DL-Learner Webservice using the Tools 
   * from Utilities.php. Sets private var client if successful
   * 
   * @author Steffen Becker   
   */
  private function connect() {
    try {
      $this->client = new SoapClient($this->getConfigUrl('wsdlLocal'));
    } catch (Exception $e) {
      if ($this->debugger) $this->debugger->log($e, "Error connecting to the DL-Learner Webservice.");
      echo '<h2>Could not connect to the DL-Learner Webservice.</h2>';
      exit;
    }
    // After establishing the SoapClient we create a Session ID
    // and add the default knowledgeSource
    if (isset($_SESSION['sessionID'])) {
      // if there is a current session running, we don't need to
      // register a new client at the dl-learner webservice
      try {
        $this->knowledgeSourceID = $this->client->addKnowledgeSource($_SESSION['sessionID'], 'sparql', $this->endpoint);        
      } catch (Exception $e) {
        /* the current session-ID is not connected to the dllearner,
         * this can happen if the dl-learner ws is restarted, but the session
         * still exists in the browser -- kill the session, and 
         * create a new ID and try again
         */
        if ($this->debugger) $this->debugger->log($e->getMessage(), 'Error creating ID for connection to the DL-Learner Webservice, trying again...');
        session_destroy();
        $_SESSION['sessionID'] = $this->client->generateID();
        try { 
          $this->knowledgeSourceID = $this->client->addKnowledgeSource($_SESSION['sessionID'], 'sparql', $this->endpoint);  
        } catch (Exception $e) { // if this happens again, we are _really_ not able to connect 
          if ($this->debugger) $this->debugger->log($e->getMessage(), 'Could not register at the DL-Learner Webservice.');
          exit;
        }
      }
    } else { // no session started yet? do it now.
      $_SESSION['sessionID'] = $this->client->generateID();
      $this->knowledgeSourceID = $this->client->addKnowledgeSource($_SESSION['sessionID'], 'sparql', $this->endpoint);
    }
  }


  /**
   * Sets the SPARQL-endpoint, the DL-Learner Webservice is connecting to 
   *
   * @param string $endpoint The URI of the SPARQL-Endpoint to set
   * @author Steffen Becker
   */
  public function setEndpoint($endpoint) {
    $this->endpoint = $endpoint;
  }


  /**
   * Returns a String with the URI for the SPARQL-Endpoint currently used
   * 
   * @return string The Endpoint URI currently used 
   * @author Steffen Becker
   */
  public function getEndpoint() {
    return $this->endpoint;
  }


  /**
   * Uses the Webservice to get results from a SPARQL-Query from the current endpoint
   * 
   * @param string $query The SPARQL-Querystring to send
   * @return string A JSON-Object with the containing results
   * @author Steffen Becker   
   */
  public function sparqlQuery($query) {
    $result = $this->client->sparqlQuery($_SESSION['sessionID'], $this->knowledgeSourceID, $query);
    return $result;
  }
    
    
  /**
   * Bulid the exclusion-String-Array from config.ini for faster
   * recommendation-generation. The Exclusions are the fragments/nodes
   * that will not be extracted for learning
   * 
   * @param array $conf The Learning-Conf Array
   * @return array The Array of Strings with predicates to exclude
   */  
  private function getExclusions($conf) {
    $exclusionsArray = array();
    $exclude = explode(',', $conf['exclude']);
    foreach($exclude as $exclusion) {
      $splitPrefix = explode(':', $exclusion);
      $exclusionsArray[] = $this->getConfigPrefixes($splitPrefix[0]) . $splitPrefix[1];
    }
    if ($this->debugger) $this->debugger->log($exclusionsArray, "Exluding from extraction for faster learning");
    return $exclusionsArray;
  }
  
  
  /**
   * Creates a learning request with the given instances an positive Examples
   *
   * @param array $instances An Array of strings with the URLs to the instances
   * @param array $positiveExamples An Array of strings with positive Example URLS
   * @return string A JSON Object with the results
   * @author Steffen Becker
   */
  public function learn($instances, $positiveExamples) {
    $result = false;
    $conf = $this->getConfigLearning();    
    if ($this->debugger) $this->debugger->log(array($instances, $positiveExamples), "Instances and positive examples are");    
    
    // if there are any problems with java, generate a new ID, this
    // fixes broken openjdk/soap implementations
    if ($this->getConfig('javaProblems')) {
      $id = $this->client->generateID();
      $kID = $this->client->addKnowledgeSource($id, 'sparql', $this->endpoint);
      
    } else {
      $id = $_SESSION['sessionID'];
      $kID = $this->knowledgeSourceID;      
    }

    $this->debugger->log($this->getConfigUrl('tagOntology'), "OWL");

    $this->client->addKnowledgeSource($id, 'owlfile', $this->getConfigUrl('tagOntology'));
    $this->client->setReasoner($id, $conf['reasoner']);

    // set the instances, the learning-Problem and pos examples
    $this->client->applyConfigEntryStringArray($id, $kID, 'instances', $instances);
    $this->client->setLearningProblem($id, $conf['problem']);
    $this->client->setPositiveExamples($id, $positiveExamples);
    
    // recursion-depth and fragment saving
    $this->client->applyConfigEntryInt($id, $kID, 'recursionDepth', $conf['recursionDepth']);
    $this->client->applyConfigEntryBoolean($id, $kID, 'saveExtractedFragment', $conf['saveExtractedFragment']);
    
    // cache the sparql-queries?
    $this->client->applyConfigEntryBoolean($id, $kID, 'useCache', $this->getConfig('useCache'));
    
    // algorithm config
    $learnID = $this->client->setLearningAlgorithm($id, $conf['algorithm']);
    $this->client->applyConfigEntryInt($id, $learnID, 'maxExecutionTimeInSeconds', $conf['maxExecutionTimeInSeconds']);
    $this->client->applyConfigEntryInt($id, $learnID, 'valueFrequencyThreshold', $conf['valueFrequencyThreshold']);
    $this->client->applyConfigEntryBoolean($id, $learnID, 'useHasValueConstructor', $conf['useHasValueConstructor']);

    // replace prefixes
    $this->client->applyConfigEntryStringTupleList($id, $kID, 'replacePredicate', 
      array($this->getConfigPrefixes('tags') . 'taggedWithTag'), array($this->getConfigPrefixes('rdf') . 'type')
    );
    
    // and exclude some items from this learning process to fasten things up
    $exclude = $this->getExclusions($conf);
    if ($conf['recursionDepth'] > 1 && is_array($exclude) && !empty($exclude)) {
      $this->client->applyConfigEntryStringArray($id, $kID, 'predList', $exclude);    
    }
    
    // after we have set all conf-values, we initialize the learning process
    $this->client->initAll($id);
    $result = $this->client->learnDescriptionsEvaluated($id);
    return $result;
  }

  
  /**
   * Converts a natural Description in a SPARQL-Query for recommendation retrieval
   * and adds the global limit set in maxResults
   *
   * @param string $kb Result-String in KB-Syntax
   * @return string A SPARQL-Query String for the KB-Syntax
   * @author Steffen Becker
   */
  public function kbToSqarql($kb) {
    return $this->client->SparqlRetrieval($kb, $this->getConfig('maxResults'));
  }

}

?>
