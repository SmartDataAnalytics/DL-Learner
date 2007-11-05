<?php

class SparqlConnection
{
	private $DBPediaUrl;
	private $DLLearnerUri;
	private $client;
	private $id;
	private $ksID;
		
	function SparqlConnection($DBPediaUrl,$DLLearnerUri)
	{
		ini_set('default_socket_timeout',200);
		$this->DBPediaUrl=$DBPediaUrl;
		$this->DLLearnerUri=$DLLearnerUri;
		$this->client=new SoapClient("main.wsdl");
		$this->id=$this->client->generateID();
		$this->ksID = $this->client->addKnowledgeSource($this->id, "sparql", $this->DBPediaUrl);
	}
	
	function getConceptFromExamples($ttl,$posExamples,$negExamples)
	{
		$this->client->applyConfigEntryInt($this->id, $this->ksID, "numberOfRecursions", 2);
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "instances", array_merge($posExamples,$negExamples));
		$this->client->applyConfigEntryInt($this->id, $this->ksID, "filterMode", 0);
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "predList", array());
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "objList", array());
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "classList", array());
		$this->client->applyConfigEntryString($this->id, $this->ksID, "format", "KB");
		$this->client->applyConfigEntryBoolean($this->id, $this->ksID, "dumpToFile", true);
		
		$this->client->setReasoner($this->id, "dig");
		$this->client->setLearningProblem($this->id, "posNegDefinition");
		$this->client->setPositiveExamples($this->id, $posExamples);
		$this->client->setNegativeExamples($this->id, $negExamples);
		$this->client->setLearningAlgorithm($this->id, "refinement");
		
		$start = microtime(true);

		$this->client->init($this->id);

		$threaded=true;
		
		if($threaded == false) {
	
			$concept = $this->client->learn($this->id);
			
		} else {
		
			$this->client->learnThreaded($this->id);
			
			$i = 1;
			$sleeptime = 1;
			
			do {
				// sleep a while
				sleep($sleeptime);
				
				// see what we have learned so far
				$concept=$this->client->getCurrentlyBestConcept($this->id);
				$running=$this->client->isAlgorithmRunning($this->id);
				
				$seconds = $i * $sleeptime;
				
				$i++;
			} while($seconds<$ttl&&$running);
			
			$this->client->stop($this->id);
		}
		return $concept;
	}
	
	function getTriples($ttl,$individual)
	{
		$options=array("triples",$individual);
		$this->client->startThread($this->id,$this->ksID,$options);
		$i = 1;
		$sleeptime = 1;
			
		do {
			// sleep a while
			sleep($sleeptime);
				
			// see if algorithm is running
			if (!$this->client->isThreadRunning($this->id,$this->ksID,"triples"))
			{
				$object=$this->client->getFromSparql($this->id,$this->ksID,"triples");
				$array=$object->item;
				if (count($array)==1) return $array;
				$ret=array();
				foreach ($array as $element)
				{
					$items=preg_split("[<]",$element,-1, PREG_SPLIT_NO_EMPTY);
					$ret[$items[0]]=$items[1];	
				}
				return $ret;
			}
			
			$seconds = $i * $sleeptime;
			$i++;
		} while($seconds<$ttl);
		
		$this->client->stopSparqlThread($this->id,$this->ksID,"triples");
		return array();	
	}
	
	function getSubjects($ttl,$label='Leipzig',$limit=5)
	{
		$options=array("subjects",$label,$limit);
		$this->client->startThread($this->id,$this->ksID,$options);
		$i = 1;
		$sleeptime = 1;
			
		do {
			// sleep a while
			sleep($sleeptime);
				
			// see if algorithm is running
			if (!$this->client->isThreadRunning($this->id,$this->ksID,"subjects"))
			{
				$object=$this->client->getFromSparql($this->id,$this->ksID,"subjects");
				return $object->item;
			}
			
			$seconds = $i * $sleeptime;
			$i++;
		} while($seconds<$ttl);
		
		$this->client->stopSparqlThread($this->id,$this->ksID,"subjects");
		return array();
	}
	
	function getSubjectsFromConcept($ttl,$concept)
	{
		$options=array("conceptSubjects",$concept);
		$this->client->startThread($this->id,$this->ksID,$options);
		$i = 1;
		$sleeptime = 1;
		do {
			// sleep a while
			sleep($sleeptime);
				
			// see if algorithm is running
			if (!$this->client->isThreadRunning($this->id,$this->ksID,"conceptSubjects"))
			{
				$object=$this->client->getFromSparql($this->id,$this->ksID,"conceptSubjects");
				return $object->item;
			}
			
			$seconds = $i * $sleeptime;
			$i++;
		} while($seconds<$ttl);
		
		$this->client->stopSparqlThread($this->id,$this->ksID,"conceptSubjects");
		return array();
	}
	
	/*public function testSoapTime()
	{
		$start = microtime(true);
		$this->id=$this->client->generateID();
		$test=$this->client->debug("Test");
		$time=microtime(true)-$start;
		return "Word: ".$test." got from SOAP in: ".$time;
	}*/
	
	public function startSearchAndShowArticle($keyword)
	{
		//TODO work on $keyword to get white space out
		//TODO change article get function
		$options=array("triples","http://dbpedia.org/resource/".$keyword);
		$this->client->startThread($this->id,$this->ksID,$options);
		
		$options=array("subjects",$keyword,15);
		$this->client->startThread($this->id,$this->ksID,$options);
	}
	
	public function checkSearch($stop)
	{
		$this->client=new SoapClient("main.wsdl");
		if ($stop){
			$this->client->stopSparqlThread($this->id,$this->ksID,"subjects");
			return;
		}
		
		// see if algorithm is running
		if (!$this->client->isThreadRunning($this->id,$this->ksID,"subjects"))
		{
			$object=$this->client->getFromSparql($this->id,$this->ksID,"subjects");
			return $object->item;
		}
		return NULL;
	}
	
	public function checkShowArticle($stop)
	{
		$this->client=new SoapClient("main.wsdl");
		if ($stop){
			$this->client->stopSparqlThread($this->id,$this->ksID,"triples");
			return;
		}
		
		if (!$this->client->isThreadRunning($this->id,$this->ksID,"triples"))
		{
			$object=$this->client->getFromSparql($this->id,$this->ksID,"triples");
			$array=$object->item;
			if (count($array)==1) return $array;
			$ret=array();
			foreach ($array as $element)
			{
				$items=preg_split("[<]",$element,-1, PREG_SPLIT_NO_EMPTY);
				$ret[$items[0]]=$items[1];	
			}
			return $ret;
		}
		return NULL;
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