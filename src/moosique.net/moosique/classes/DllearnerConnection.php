<?php

/**
 * 
 */
class DllearnerConnection extends Config {		
	
  private $client;
  private $endpoint;

  /**
   * 
   * @return 
   * @param object $config
   */
	function __construct() {
	  parent::__construct(); // init config
    // we use jamendo as the default sparql-endpoint
    $this->setEndpoint($this->getConfigUrl('jamendo'));
    // load WSDL files (has to be done due to a Java web service bug)
    include('Utilities.php');
    ini_set('soap.wsdl_cache_enabled', '0');
    Utilities::loadWSDLfiles($this->getConfigUrl('wsdl'));    
    $this->connect();
	}
  
  /**
   * 
   * @return 
   */
  private function connect() {
		// connect to DL-Learner-Web-Service
		$this->client = new SoapClient($this->getConfigUrl('wsdlLocal'));
	}
  
  /**
   * 
   * @return 
   * @param object $endpoint
   */
  public function setEndpoint($endpoint) {
    $this->endpoint = $endpoint;
  }

  
  public function getEndpoint() {
    return $this->endpoint;
  }

	public function sparqlQuery($query) {
		$id = $this->client->generateID();
		$knowledgeSourceId = $this->client->addKnowledgeSource($id, 'sparql', $this->endpoint);
		$result = $this->client->sparqlQuery($id, $knowledgeSourceId, $query);
		return $result;
	}

}
?>