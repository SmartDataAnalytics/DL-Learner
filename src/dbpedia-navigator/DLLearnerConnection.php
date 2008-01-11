<?php

/**
 * Encapsulates all functions, which require communication with DL-Learner.
 * 
 * @author Jens Lehmann
 * @author Sebastian Knappe
 */
class DLLearnerConnection
{
	private $DBPediaUrl;
	private $DLLearnerUri;
	
	// 
	private $client;
	
	// ID given to this client by the web service
	private $id;
	
	// ID of the DBpedia knowledge source
	private $ksID;
		
	function DLLearnerConnection($DBPediaUrl,$DLLearnerUri,$id=0,$ksID=0)
	{
		ini_set('default_socket_timeout',200);
		$this->DBPediaUrl=$DBPediaUrl;
		$this->DLLearnerUri=$DLLearnerUri;
		$this->client=new SoapClient("main.wsdl");
		$this->id=$id;
		$this->ksID=$ksID;
	}
	
	function getIDs()
	{
		$id=$this->client->generateID();
		$ksID=$this->client->addKnowledgeSource($id,"sparql",$this->DBPediaUrl);
		return array(0 => $id, 1 => $ksID);
	}
	
	function test()
	{
		$object=$this->client->test($this->id,$this->ksID);
		return $object->item;
	}
	
	function getConceptFromExamples($ttl,$posExamples,$negExamples)
	{
		$this->client->applyConfigEntryInt($this->id, $this->ksID, "recursionDepth", 2);
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "instances", array_merge($posExamples,$negExamples));
		// $this->client->applyConfigEntryInt($this->id, $this->ksID, "filterMode", 0);
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "predList", array());
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "objList", array());
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "classList", array());
		$this->client->applyConfigEntryString($this->id, $this->ksID, "format", "KB");
		$this->client->applyConfigEntryBoolean($this->id, $this->ksID, "dumpToFile", true);
		
		$this->client->setReasoner($this->id, "dig");
		if(empty($negExamples))
			$this->client->setLearningProblem($this->id, "posOnlyDefinition");
		else
			$this->client->setLearningProblem($this->id, "posNegDefinition"); 
		$this->client->setPositiveExamples($this->id, $posExamples);
		if(!empty($negExamples))
			$this->client->setNegativeExamples($this->id, $negExamples);
		$this->client->setLearningAlgorithm($this->id, "refinement");
		
		$start = microtime(true);

		$this->client->initAll($this->id);

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
					
					// each property can occur multiple times (!)
					// bug: $ret[$items[0]]=$items[1];
						
					$ret[$items[0]][] = $items[1];
				}
				return $ret;
			}
			
			$seconds = $i * $sleeptime;
			$i++;
		} while($seconds<$ttl);
		
		$this->client->stopSparqlThread($this->id,$this->ksID,"triples");
		return array();	
	}
	
	function getSubjects($ttl,$label)
	{
		$options=array("subjects",$label,15);
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
	
	public function loadWSDLfiles($wsdluri){
		$main=DLLearnerConnection::getwsdl($wsdluri);
		$other=DLLearnerConnection::getOtherWSDL($main);
		$newMain=DLLearnerConnection::changeWSDL($main);
		DLLearnerConnection::writeToFile("main.wsdl",$newMain);
		$x=0;
		foreach ($other as $o){
			DLLearnerConnection::writeToFile("def".($x++).".xsd",DLLearnerConnection::getwsdl($o));
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