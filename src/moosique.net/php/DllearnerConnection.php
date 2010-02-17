<?php

/**
 * 
 */
class dllearnerConnection {		
	
  private $config;
  private $client;
  private $endpoint;

  /**
   * 
   * @return 
   * @param object $config
   */
	function __construct($config) {
    $this->config = $config;
    // we use jamendo as the default sparql-endpoint
    $this->setEndpoint($this->config->getUrl('jamendo'));
    $this->connect();
	}
  
  /**
   * 
   * @return 
   */
  private function connect() {
		// connect to DL-Learner-Web-Service
		$this->client = new SoapClient($this->config->getUrl('wsdlLocal'));
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