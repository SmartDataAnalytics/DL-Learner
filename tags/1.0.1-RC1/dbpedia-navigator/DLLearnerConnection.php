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
	private $ttl;
	private $learnttl;
	private $lang;
	// 
	private $client;
	private $endpoint;
	
	// ID given to this client by the web service
	private $id;
	
	// ID of the DBpedia knowledge source
	private $ksID;

	private $ignoredConcepts;
	
	private $ignoredRoles;
	
	function DLLearnerConnection($id=0,$ksID=0)
	{
		ini_set('default_socket_timeout',200);
		require_once("Settings.php");
		$settings=new Settings();
		$this->ttl=$settings->sparqlttl;
		$this->learnttl=$settings->learnttl;
		$this->lang=$settings->language;
		$this->DBPediaUrl=$settings->dbpediauri;
		$this->endpoint=$settings->endpoint;
		$this->ignoredConcepts=$settings->ignoredConcepts;
		$this->ignoredRoles=$settings->ignoredRoles;
		$this->client=new SoapClient("main.wsdl",array('features' => SOAP_SINGLE_ELEMENT_ARRAYS));
		$this->id=$id;
		$this->ksID=$ksID;
	}
	
	function getIDs()
	{
		$id=$this->client->generateID();
		$ksID=$this->client->addKnowledgeSource($id,"sparql",$this->DBPediaUrl);
		return array(0 => $id, 1 => $ksID);
	}
	
	function getConceptFromExamples($posExamples,$negExamples,$number)
	{
		require_once("Settings.php");
		require_once("helper_functions.php");
		$settings=new Settings();
		
		$this->client->applyConfigEntryInt($this->id, $this->ksID, "recursionDepth",1);
		$this->client->applyConfigEntryString($this->id, $this->ksID, "predefinedFilter", "DBPEDIA-NAVIGATOR");
		$this->client->applyConfigEntryString($this->id, $this->ksID, "predefinedEndpoint", $this->endpoint);
		$this->client->applyConfigEntryString($this->id, $this->ksID, "predefinedManipulator", "DBPEDIA-NAVIGATOR");
		$this->client->applyConfigEntryBoolean($this->id, $this->ksID, "useCache", true);
		if(empty($negExamples)){
			if ($settings->classSystem=="YAGO") $filterClasses=array("http://xmlns.com/foaf/","http://dbpedia.org/ontology/");
			else if ($settings->classSystem=="DBpedia") $filterClasses=array("http://xmlns.com/foaf/","http://dbpedia.org/class/yago/","http://dbpedia.org/ontology/Resource");
			$negExamples=$this->client->getNegativeExamples($this->id,$this->ksID,$posExamples,count($posExamples),"http://dbpedia.org/resource/",$filterClasses);
			$negExamples=$negExamples->item;
			//$negExamples=getNegativeExamplesFromParallelClass($posExamples);
		}
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "instances", array_merge($posExamples,$negExamples));
		$this->client->setReasoner($this->id, "fastInstanceChecker");
		if(empty($negExamples))
			$this->client->setLearningProblem($this->id, "posOnlyLP");
		else
			$this->client->setLearningProblem($this->id, "posNegLPStandard");
		$this->client->setPositiveExamples($this->id, $posExamples);
		if(!empty($negExamples))
			$this->client->setNegativeExamples($this->id, $negExamples);
		$algorithmID=$this->client->setLearningAlgorithm($this->id, "refexamples");
		$this->client->applyConfigEntryBoolean($this->id, $algorithmID, "forceRefinementLengthIncrease", true);
		$this->client->applyConfigEntryBoolean($this->id, $algorithmID, "useHasValueConstructor", true);
		$this->client->applyConfigEntryBoolean($this->id, $algorithmID, "useCardinalityRestrictions", false);
		$this->client->applyConfigEntryInt($this->id, $algorithmID, "valueFrequencyThreshold", 2);
		$this->client->applyConfigEntryInt($this->id, $algorithmID, "guaranteeXgoodDescriptions", 3);
		$this->client->applyConfigEntryInt($this->id, $algorithmID, "maxExecutionTimeInSeconds", 3);
		$this->client->applyConfigEntryBoolean($this->id, $algorithmID, "useNegation", false);
		$this->client->applyConfigEntryBoolean($this->id, $algorithmID, "useAllConstructor", false);
		$this->client->applyConfigEntryStringArray($this->id, $algorithmID, "ignoredConcepts",$this->ignoredConcepts);
		$this->client->applyConfigEntryStringArray($this->id, $algorithmID, "ignoredRoles",$this->ignoredRoles);
		$start = microtime(true);
		
		$this->client->initAll($this->id);
		
		//look, if algorithm was stopped
		$file=fopen("./temp/".$this->id.".temp","r");
		$run=fgets($file);
		fclose($file);
		if ($run=="false"){
			return array();
		}
				
		$threaded=true;
		
		if($threaded == false) {
	
			$concept = $this->client->learn($this->id,'kb');
			
		} else {
		
			$this->client->learnThreaded($this->id);
			
			$i = 1;
			$sleeptime = 1;
			
			do {
				// sleep a while
				sleep($sleeptime);
				
				// see what we have learned so far
				//$concepts=$this->client->getCurrentlyBestConcepts($this->id,3,"kb");
				$running=$this->client->isAlgorithmRunning($this->id);
				
				$seconds = $i * $sleeptime;
				
				$i++;
				
				//look, if algorithm was stopped
				$file=fopen("./temp/".$this->id.".temp","r");
				$run=fgets($file);
				fclose($file);
				if ($run=="false"){
					$this->client->stop($this->id);
					return json_decode($this->client->getCurrentlyBestEvaluatedDescriptionsFiltered($this->id,$number,0.8,true),true);
				}
			} while($seconds<$this->learnttl&&$running);
			
			if ($running) $this->client->stop($this->id);
		}
		
		//return $concepts->item;
		return json_decode($this->client->getCurrentlyBestEvaluatedDescriptionsFiltered($this->id,$number,0.8,true),true);
	}
	
	function getNaturalDescription($concept)
	{
		return $this->client->getNaturalDescription($this->id, $concept);
	}
	
	function getConceptDepth()
	{
		return $this->client->getConceptDepth($this->id,3)->item;
	}
	
	function getConceptArity()
	{
		return $this->client->getConceptArity($this->id,3)->item;
	}
	
	function getConceptLength($concept)
	{
		return $this->client->getConceptLength($concept);
	}
			
	function getTriples($uri)
	{
		//i am filtering the references out at the moment, because they are causing errors with URL with ...&profile=bla, the XMLParser thinks &profile is a HTML-Entitie and misses the ;
		$query="SELECT ?pred ?obj ?sub ".
			   "WHERE {{<".$uri."> ?pred ?obj.Filter(!regex(str(?pred),'http://dbpedia.org/property/reference'))}UNION{<".$uri."> <http://dbpedia.org/property/redirect> ?Conc.?Conc ?pred ?obj.Filter(!regex(str(?pred),'http://dbpedia.org/property/reference'))}UNION{?sub  ?pred <".$uri.">}UNION{<".$uri."> <http://dbpedia.org/property/redirect> ?Conc.?sub ?pred ?Conc}}";
		$result=json_decode($this->getSparqlResultThreaded($query),true);
		if (count($result['results']['bindings'])==0) throw new Exception("An article with that name does not exist. The Search is started ..."); 
		$ret=array();
		$geonames="";
		foreach ($result['results']['bindings'] as $results){
			if (!(isset($results['xml:lang'])&&($results['xml:lang']!=$this->lang))){
				if (isset($results['obj'])){
					$ret[0][$results['pred']['value']][]=$results['obj'];
					if ($results['pred']['value']=="http://www.w3.org/2002/07/owl#sameAs"&&strlen($results['obj']['value'])>24&&substr($results['obj']['value'],0,24)=='http://sws.geonames.org/')
						$geonames=$results['obj']['value'];
				}
				else if (isset($results['sub'])) $ret[1][$results['pred']['value']][]=$results['sub'];
			}
		}
		//geonames
		/*if (strlen($geonames)>0){
			$query="SELECT * WHERE {<".$geonames."> <http://www.geonames.org/ontology#parentFeature> ?parent.?parent <http://www.w3.org/2002/07/owl#sameAs> ?parentName.<".$geonames."> <http://www.geonames.org/ontology#childrenFeatures> ?children.<".$geonames."> <http://www.geonames.org/ontology#nearbyFeatures> ?neighbours.}";
			$result=json_decode($this->client->sparqlQueryPredefinedEndpoint("LOCALGEONAMES", $query, true),true);
			var_dump($result);
		}*/		
		
		return $ret;
	}
	
	function getSparqlResultThreaded($query)
	{
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "defaultGraphURIs", array("http://dbpedia.org"));
		$queryID=$this->client->sparqlQueryThreaded($this->id,$this->ksID,$query);
		$running=true;
		$i = 1;
		$sleeptime = 500000;
		do {
			// sleep a while
			usleep($sleeptime);
					
				
			$running=$this->client->isSparqlQueryRunning($this->id,$queryID);
			if (!$running){
				$result=$this->client->getAsJSON($this->id,$queryID);
				return $result;
			}
				
			$microseconds = $i * $sleeptime;
			$i++;
			//look, if algorithm was stopped
			$file=fopen("./temp/".$this->id.".temp","r");
			$run=fgets($file);
			fclose($file);
			if ($run=="false"){
				$this->client->stopSparqlThread($this->id,$queryID);
				throw new Exception("Query stopped");
			}
		} while($microseconds<$this->ttl);
		$this->client->stopSparqlThread($this->id,$queryID);
	}
	
	function getSparqlResult($query)
	{
		$this->client->applyConfigEntryStringArray($this->id, $this->ksID, "defaultGraphURIs", array("http://dbpedia.org"));
		$result=$this->client->sparqlQuery($this->id,$this->ksID,$query);
		return $result;
	}

	//at the moment the subject search uses a database, so this function is not needed
	/*
	function getSubjects($label,$checkedInstances)
	{
		$offset=1;
		$steps=100;
		$ret=array();
		$labels=preg_split("[\040]",$label,-1,PREG_SPLIT_NO_EMPTY);
		//TODO if instances are checked the offset no longer works
		do{
			if (isset($checkedInstances[0])){
				$query="SELECT DISTINCT ?subject ?cat ?label\n".
					   "WHERE {?subject a <".$checkedInstances[0].">.{SELECT ?zw as ?subject\n".
					   "WHERE { ?zw <http://www.w3.org/2000/01/rdf-schema#label> ?object. ?object bif:contains '";
				$i=0;
				foreach ($labels as $l){
					if ($i==0) $query.="\"".$l."\"";
					else $query.=" and \"".$l."\"";
					$i=1;
				}
				$query.="'@en.?subject a ?cat.?cat <http://www.w3.org/2000/01/rdf-schema#label> ?label}\n".
					   "LIMIT ".$steps." OFFSET ".$offset."}}";
			}else {
				$query="SELECT DISTINCT ?subject ?cat ?label\n".
					   "WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?object. ?object bif:contains '";
				$i=0;
				foreach ($labels as $l){
					if ($i==0) $query.="\"".$l."\"";
					else $query.=" and \"".$l."\"";
					$i=1;
				}
				$query.="'@en.?subject a ?cat.?cat <http://www.w3.org/2000/01/rdf-schema#label> ?label}".
					   "LIMIT ".$steps." OFFSET ".$offset;			
			}
			$result=json_decode($this->getSparqlResultThreaded($query),true);
			$count=count($result['results']['bindings']);
			if (($count==0)&&($offset==1)) throw new Exception("Your query brought no result.");
			foreach ($result['results']['bindings'] as $results){
				$ret[$results['subject']['value']]=$results['subject']['value'];
				//tagcloud
				if (!isset($tagcloud[$results['cat']['value']])){
					$tagcloud[$results['cat']['value']]=1;
					$tagcloudLabel[$results['cat']['value']]=$results['label']['value'];
				}
				else $tagcloud[$results['cat']['value']]++;
				
			}
			$offset+=$steps;
		} while($count==$steps);
		//have to do this, because distinct doesn't work, and i use the key to eliminate doubles
		unset($tagcloud['http://www.w3.org/2004/02/skos/core#Concept']);
		foreach ($ret as $r)
			$return['subjects'][]=$r;
		$return['tagcloud']=$tagcloud;
		$return['tagcloudlabel']=$tagcloudLabel;
		return $return;
	}*/
	
	function getSubjectsFromConcept($concept,$number)
	{
		$query=$this->client->SparqlRetrieval($concept,$number);
		$result=json_decode($this->getSparqlResultThreaded($query),true);
		if (count($result['results']['bindings'])==0) throw new Exception("Your query brought no result.");
		$ret=array();
		foreach ($result['results']['bindings'] as $results){
			$ret[]=$results['subject']['value'];
		}
		return $ret;
	}
	/*not used at the moment
	function getYagoSubCategories($category)
	{
		$query="SELECT ?subject ?label count(?subclass) as ?numberOfSubclasses\n".
			   "WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf> <".$category.">.?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label.OPTIONAL {?subclass  <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?subject} }";
		$result=json_decode($this->getSparqlResult($query),true);
		if (count($result['results']['bindings'])==0) throw new Exception("Your query brought no result.");
		$ret=array();
		foreach ($result['results']['bindings'] as $results){
			$res=array();
			$res['value']=$results['subject']['value'];
			$res['label']=$results['label']['value'];
			$res['subclasses']=$results['numberOfSubclasses']['value'];
			if (strlen($res['label'])>0) $ret[]=$res;
		}
		return $ret;
	}*/
	
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
/*
ini_set('default_socket_timeout',200);
$sc=new DLLearnerConnection();
$ids=$sc->getIDs();
$sc=new DLLearnerConnection($ids[0],$ids[1]);
$triples=$sc->getConceptFromExamples(array('http://dbpedia.org/resource/Leipzig'),array());
var_dump($triples);*/
?>
