<?php

//require_once 'pear/HTTP_Request.php';

class SparqlConnection
{
	private $DBPediaUrl;
	private $DLLearnerUri;
	private $client;
		
	function SparqlConnection($DBPediaUrl,$DLLearnerUri)
	{
		ini_set('default_socket_timeout',200);
		$this->DBPediaUrl=$DBPediaUrl;
		$this->DLLearnerUri=$DLLearnerUri;
		$this->client=new SoapClient("main.wsdl");
	}
	
	function getConceptFromExamples($posExamples,$negExamples)
	{
		$id=$this->client->generateID();
		
		$ksID = $this->client->addKnowledgeSource($id, "sparql", $this->DBPediaUrl);
		$this->client->applyConfigEntryInt($id, $ksID, "numberOfRecursions", 2);
		$this->client->applyConfigEntryStringArray($id, $ksID, "instances", array_merge($posExamples,$negExamples));
		$this->client->applyConfigEntryInt($id, $ksID, "filterMode", 0);
		$this->client->applyConfigEntryStringArray($id, $ksID, "predList", array());
		$this->client->applyConfigEntryStringArray($id, $ksID, "objList", array());
		$this->client->applyConfigEntryStringArray($id, $ksID, "classList", array());
		$this->client->applyConfigEntryString($id, $ksID, "format", "KB");
		$this->client->applyConfigEntryBoolean($id, $ksID, "dumpToFile", true);
		
		$this->client->setReasoner($id, "dig");
		$this->client->setLearningProblem($id, "posNegDefinition");
		$this->client->setPositiveExamples($id, $posExamples);
		$this->client->setNegativeExamples($id, $negExamples);
		$this->client->setLearningAlgorithm($id, "refinement");
		
		$start = microtime(true);

		$this->client->init($id);

		$threaded=true;
		
		if($threaded == false) {
	
			$concept = $this->client->learn($id);
			
		} else {
		
			$this->client->learnThreaded($id);
			
			$i = 1;
			$sleeptime = 1;
			
			do {
				// sleep a while
				sleep($sleeptime);
				
				// see what we have learned so far
				$concept=$this->client->getCurrentlyBestConcept($id);
				$running=$this->client->isAlgorithmRunning($id);
				
				$seconds = $i * $sleeptime;
				
				$i++;
			} while($running);
			
		}
		return $concept;
	}
	
	function getTriples($individual)
	{
		$id=$this->client->generateID();
		
		$ksID = $this->client->addKnowledgeSource($id, "sparql", $this->DBPediaUrl);
		$this->client->applyConfigEntryInt($id, $ksID, "numberOfRecursions", 1);
		$this->client->applyConfigEntryStringArray($id, $ksID, "instances", array($individual));
		$this->client->applyConfigEntryInt($id, $ksID, "filterMode", -1);
		$this->client->applyConfigEntryStringArray($id, $ksID, "predList", array());
		$this->client->applyConfigEntryStringArray($id, $ksID, "objList", array());
		$this->client->applyConfigEntryStringArray($id, $ksID, "classList", array());
		$this->client->applyConfigEntryString($id, $ksID, "format", "Array");
		$this->client->applyConfigEntryBoolean($id, $ksID, "dumpToFile", false);
		$this->client->applyConfigEntryBoolean($id,$ksID,"useLits",true);
		
		$object=$this->client->getTriples($id,$ksID);
		$array=$object->item;
		$ret=array();
		foreach ($array as $element)
		{
			$items=preg_split("[<]",$element,-1, PREG_SPLIT_NO_EMPTY);
			$ret[$items[1]]=$items[2];	
		}
		
		return $ret;
	}
	
	function getSubjects($label='Leipzig',$limit=5)
	{
		$id=$this->client->generateID();
		
		$ksID = $this->client->addKnowledgeSource($id, "sparql", $this->DBPediaUrl);
		$object=$this->client->getSubjects($id,$ksID,$label,$limit);
		return $object->item;
	}
	
	function getSubjectsFromConcept($concept)
	{
		$id=$this->client->generateID();
		
		$ksID = $this->client->addKnowledgeSource($id, "sparql", $this->DBPediaUrl);
		$object=$this->client->getSubjectsFromConcept($id,$ksID,$concept);
		return $object->item;
	}
	
	public function loadWSDLfiles($wsdluri){
		ini_set("soap.wsdl_cache_enabled","0");
		
		$main=SparqlConnection::getwsdl($wsdluri);
		$other=SparqlConnection::getOtherWSDL($main);
		$newMain=SparqlConnection::changeWSDL($main);
		SparqlConnection::writeToFile("main.wsdl",$newMain);
		$x=0;
		foreach ($other as $o){
			SparqlConnection::writeToFile("def".($x++).".xsd",SparqlConnection::getwsdl($o));
		}

	}
	
	private function changeWSDL($wsdl){
		$before="<xsd:import schemaLocation=\"";
		$after="\" namespace=\"";
		$newWSDL="";
		$desca="def";
		$descb=".xsd";
		$x=0;
		while($posstart= strpos ( $wsdl, $before  )){

			$posstart+=strlen($before);
			$newWSDL.=substr($wsdl,0,$posstart);
			$wsdl=substr($wsdl,$posstart);
			$newWSDL.=$desca.($x++).$descb;
			$posend= strpos ( $wsdl, $after  );
			$wsdl=substr($wsdl,$posend);

		}
		return $newWSDL.$wsdl;
			
	}
	
	private function getOtherWSDL($wsdl){
		$before="<xsd:import schemaLocation=\"";
		$after="\" namespace=\"";
		$ret=array();
		while($posstart= strpos ( $wsdl, $before  )){
			$posstart+=strlen($before);
			$wsdl=substr($wsdl,$posstart);
			$posend= strpos ( $wsdl, $after  );
			$tmp=substr($wsdl,0,$posend);
			$ret[]=$tmp;
			$wsdl=substr($wsdl,$posend+strlen($after));
		}
		return $ret;
	}
	

	
	
	private function getwsdl($wsdluri){
		// this is copied from the Pear example
		// please don't ask me how it works
		$req = &new HTTP_Request($wsdluri);
		$message="";
		$req->setMethod(HTTP_REQUEST_METHOD_GET);
		$req->sendRequest();
		$ret=$req->getResponseBody();
		return $ret;
	}
	
	
	
	private function writeToFile($filename,$content){

		$fp=fopen($filename,"w");
		fwrite($fp,$content);
		fclose($fp);
	
	}
}
?>