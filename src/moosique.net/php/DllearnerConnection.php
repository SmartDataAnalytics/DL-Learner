<?php

/**
 * 
 */
class dllearnerConnection {		
	
  private $conf;
  private $client;
  private $endpoint;

	function __construct($conf) {
    $this->conf = $conf;
    // we use jamendo as the default sparql-endpoint
    $this->setEndpoint($this->conf->getUrl('jamendo'));
    $this->connect();
	}
  
  private function connect() {
		// connect to DL-Learner-Web-Service
		$this->client = new SoapClient(
      $this->conf->getUrl('wsdlLocal')
    );
	}
  
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