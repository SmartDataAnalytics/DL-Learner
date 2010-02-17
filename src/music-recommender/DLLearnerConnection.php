<?php

/**
 * Encapsulates all functions, which require communication with DL-Learner.
 * TODO: use SESSION to store client id
 * 
 * @author Jens Lehmann
 * @author Anita Janassary
 */
class DLLearnerConnection
{		
	private $endpointURL;
	private $client;

	function DLLearnerConnection() {
		ini_set('default_socket_timeout',200);

		// read in ini values
		$ini = parse_ini_file("settings.ini");
		$this->endpointURL = $ini['endpointURL'];

		// connect to DL-Learner-Web-Service
		$this->client=new SoapClient($ini["wsdlURLLocal"],array('features' => SOAP_SINGLE_ELEMENT_ARRAYS));
	}

	function sparqlQuery($query) {
		$id=$this->client->generateID();
		$ksID=$this->client->addKnowledgeSource($id,"sparql",$this->endpointURL);
		$result=$this->client->sparqlQuery($id,$ksID,$query);
		return $result;
	}

}
?>