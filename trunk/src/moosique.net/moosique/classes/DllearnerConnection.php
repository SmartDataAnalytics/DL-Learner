<?php

/**
 * 
 */
class DllearnerConnection extends Config {		
	
  private $knowledgeSourceID;
  private $client;    // here we store the soap-client
  private $endpoint;  // the currently used endpoint
                      // use getEndpoint and setEndpoint to change

  /**
   * Object initializing: retrieve ini-values, set the endpoint
   * and include the neccessary wsdl-utilities and try to establish
   * a connection to the dl-learner webservice
   *  
   */
	function __construct() {
	  parent::__construct(); // init config
    // we use jamendo as the default sparql-endpoint
    $this->setEndpoint($this->getConfigUrl('jamendo'));
    
    // load WSDL files (has to be done due to a Java web service bug)
    include('Utilities.php');
    ini_set('soap.wsdl_cache_enabled', '0');
    // Utilities::loadWSDLfiles($this->getConfigUrl('wsdl'));    
    $this->connect();
	}
  
  /**
   * Tries to connect to the DL-Learner Webservice using the Tools 
   * from Utilities.php. Sets private var client.
   * 
   */
  private function connect() {
		// connect to DL-Learner-Web-Service
		try {
		  $this->client = new SoapClient($this->getConfigUrl('wsdlLocal'));
    } catch (Exception $e) {
      $this->debugger->log($e, "Error connecting to the DL-Learner Webservice.");
      echo '<h2>Could not connect to the DL-Learner Webservice.</h2>';
      exit;
    }
    
    // After establishing the SoapClient we create a Session ID
    // and add the default knowledgeSource (see config.ini)
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
   * @param String $endpoint The URI of the SPARQL-Endpoint to set
   */
  public function setEndpoint($endpoint) {
    $this->endpoint = $endpoint;
  }

  /**
   * Returns a String with the URI for the SPARQL-Endpoint currently used
   * 
   * @return String The Endpoint URI currently used 
   */
  public function getEndpoint() {
    return $this->endpoint;
  }

  /**
   * This method tries to create a connectionID from the DL-Learner
   * Webservice and returns a JSON-object with the results from the
   * Sparql-Query sent to the current endpoint
   * 
   * @return String A JSON-Object with the containing result-set
   * @param String $query The SPARQL-Querystring to send
   */
	public function sparqlQuery($query) {
		$result = $this->client->sparqlQuery($_SESSION['sessionID'], $this->knowledgeSourceID, $query);
		return $result;
	}
  
  
  
  /**
   *
   *
   *
   */
  public function learn($instances, $positiveExamples, $owlfile) {
    $id = $_SESSION['sessionID'];
    $conf = $this->getLearningConfig();
    
    // TODO? use this as knowledgesource ID?
    $this->client->addKnowledgeSource($id, 'owlfile', $owlfile);
    $this->client->setReasoner($id, $conf['reasoner']);

    // set the instances and pos examples
    $this->client->applyConfigEntryStringArray($id, $this->knowledgeSourceID, 'instances', $instances);
    $this->client->setLearningProblem($id, $conf['problem']);

    $this->client->setPositiveExamples($id, $positiveExamples);
    
    // recursion-depth and fragment saving
    $this->client->applyConfigEntryInt($id, $this->knowledgeSourceID, 'recursionDepth', $conf['recursionDepth']);
    $this->client->applyConfigEntryBoolean($id, $this->knowledgeSourceID, 'saveExtractedFragment', $conf['saveExtractedFragment']);

    // algorithm config
    $learnID = $this->client->setLearningAlgorithm($id, $conf['algorithm']);
    $this->client->applyConfigEntryInt($id, $learnID, 'maxExecutionTimeInSeconds', $conf['maxExecutionTimeInSeconds']);
    $this->client->applyConfigEntryInt($id, $learnID, 'valueFrequencyThreshold', $conf['valueFrequencyThreshold']);
    $this->client->applyConfigEntryBoolean($id, $learnID, 'useHasValueConstructor', $conf['useHasValueConstructor']);

    $this->client->applyConfigEntryStringTupleList($id, $this->knowledgeSourceID, 'replacePredicate', array(
      "http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag"), array("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    );

    $this->client->initAll($id);
    
    
    $concepts = false;

    $concepts = $this->client->learnDescriptionsEvaluated($id);
    $concepts = json_decode($concepts);
    
    return $concepts;
    
  }

}

?>
