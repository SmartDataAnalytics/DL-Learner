<?php

require_once 'pear/HTTP_Request.php';

class SparqlConnection
{
	private $DBPediaUrl;
	private $DLLearnerUri;
	private $client;
	private $id;
	
	public function getID(){
		return $this->id;
	}
	
	function SparqlConnection($DBPediaUrl,$DLLearnerUri,$getID=0)
	{
		ini_set("soap.wsdl_cache_enabled","0");
		$this->DBPediaUrl=$DBPediaUrl;
		$this->DLLearnerUri=$DLLearnerUri;
		$this->loadWSDLfiles($DLLearnerUri);
		$this->client=new SoapClient("main.wsdl");
		if($getID==0)
		{  		
     		$this->id=$this->client->generateID();
     	}
     	else
     	{
     		$this->id=$getID;
     	}
	}
	
	function getConceptFromExamples($posExamples,$negExamples)
	{
		$ksID = $this->client->addKnowledgeSource($this->id, "sparql", $this->DBPediaUrl);
		$this->client->applyConfigEntryInt($this->id, $ksID, "numberOfRecursions", 2);
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "instances", array_merge($posExamples,$negExamples));
		$this->client->applyConfigEntryInt($this->id, $ksID, "filterMode", 0);
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "predList", array());
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "objList", array());
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "classList", array());
		$this->client->applyConfigEntryString($this->id, $ksID, "format", "KB");
		$this->client->applyConfigEntryBoolean($this->id, $ksID, "dumpToFile", false);
		
		$this->client->setReasoner($this->id, "dig");
		$this->client->setLearningProblem($this->id, "posNegDefinition");
		$this->client->setPositiveExamples($this->id, $posExamples);
		$this->client->setNegativeExamples($this->id, $negExamples);
		$this->client->setLearningAlgorithm($this->id, "refinement");
		
		$start = microtime(true);

		$this->client->init($this->id);

		$learn_start = microtime(true);
		$init = $learn_start - $start;
		echo 'components initialised in '.$init.' seconds<br />';
		
		$threaded=true;
		
		if($threaded == false) {
	
			$concept = $this->client->learn($id);
			
			$learn = microtime(true) - $learn_start;
			echo 'concept learned in '.$learn.' seconds<br />';
			
			echo 'result: '.$concept;
		
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
				
				echo 'result after '.$seconds.' seconds of sleep: '.$concept.'<br />';
				
				$i++;
			} while($running);
			
			echo 'algorithm finished';
		}
		return $concept;
	}
	
	function getTriples($individual)
	{
		$ksID = $this->client->addKnowledgeSource($this->id, "sparql", $this->DBPediaUrl);
		$this->client->applyConfigEntryInt($this->id, $ksID, "numberOfRecursions", 1);
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "instances", array($individual));
		$this->client->applyConfigEntryInt($this->id, $ksID, "filterMode", 3);
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "predList", array());
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "objList", array());
		$this->client->applyConfigEntryStringArray($this->id, $ksID, "classList", array());
		$this->client->applyConfigEntryString($this->id, $ksID, "format", "Array");
		$this->client->applyConfigEntryBoolean($this->id, $ksID, "dumpToFile", false);
		$this->client->applyConfigEntryBoolean($this->id,$ksID,"useLits",true);
		
		$object=$this->client->getTriples($this->id,$ksID);
		$array=$object->item;
		$ret=array();
		foreach ($array as $element)
		{
			$items=preg_split("[<]",$element,-1, PREG_SPLIT_NO_EMPTY);
			$ret[]=$items;	
		}
		
		return $ret;
	}
	
	function getSubjects($label,$limit)
	{
		$ksID = $this->client->addKnowledgeSource($this->id, "sparql", $this->DBPediaUrl);
		$object=$this->client->getSubjects($this->id,$ksID,$label,$limit);
		return $object->item;
	}
	
	private function loadWSDLfiles($wsdluri){
		$main=$this->getwsdl($wsdluri);
		$other=$this->getOtherWSDL($main);
		$newMain=$this->changeWSDL($main);
		$this->writeToFile("main.wsdl",$newMain);
		$x=0;
		foreach ($other as $o){
			$this->writeToFile("def".($x++).".xsd",$this->getwsdl($o));
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

$positive=array("http://dbpedia.org/resource/Pythagoras",
				"http://dbpedia.org/resource/Philolaus",
				"http://dbpedia.org/resource/Archytas");
$negative=array("http://dbpedia.org/resource/Socrates",
				"http://dbpedia.org/resource/Zeno_of_Elea",
				"http://dbpedia.org/resource/Plato");

$sparqlConnection=new SparqlConnection("http://dbpedia.openlinksw.com:8890/sparql","http://localhost:8181/services?wsdl");
//$sparqlConnection->getConceptFromExamples($positive,$negative);
//$triples=$sparqlConnection->getTriples("http://dbpedia.org/resource/Leipzig");
//print_r($triples);
//$subjects=$sparqlConnection->getSubjects("Leipzig",5);
//print_r($subjects);
?>