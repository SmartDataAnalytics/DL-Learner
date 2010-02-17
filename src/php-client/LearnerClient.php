<?php
require_once 'pear/HTTP_Request.php';


 class LearnerClient{
	private $soapclient;
	private $id;
	
	
	public function __construct($wsdluri,$getID=0){
		
		$this->loadWSDLfiles($wsdluri);
     	$this->soapclient = new SoapClient("main.wsdl");
		if($getID==0)
		{  		
     		$this->id=$this->soapclient->getID();
     	}
     	else
     	{
     	$this->id=$getID;
     	}
     	
     	
	}
	
	/*public function __construct($wsdluri,$id){
			
	    	$this->loadWSDLfiles($wsdluri);
	     	$this->soapclient = new SoapClient("main.wsdl");
	     	$this->id=$id;
	     	
	}*/
	
	public function getID(){
		return $this->id;
	}
	
	
	// this could maybe be a 
	//nice function which encapsulates everything
	public function getClientState($id){
	
	
	}
	
	public function getInstances($id){
		$obj= $this->soapclient->getInstances($id);
		return $this->parseStringlist($obj);
					
	}
	
	public function getAtomicConcepts($id)
	{	$obj= $this->soapclient->getAtomicConcepts($id);
		return $this->parseStringlist($obj);
	}
	
	public function retrieval($id,$concept){
		$obj= $this->soapclient->retrieval($id,$concept);
		return $this->parseStringlist($obj);
	}
	
	public function getAtomicRoles($id){
		$obj= $this->soapclient->getAtomicRoles($id);
		return $this->parseStringlist($obj);
	}
	public function getIndividualsForARole($id,$Role){
		$obj= $this->soapclient->getIndividualsForARole($id,$Role);
		return $this->parseStringlist($obj);
	}
	
	public function getPositiveExamples($id){
		$obj= $this->soapclient->getPositiveExamples($id);
		return $this->parseStringlist($obj);
	
	}
	
	public function getNegativeExamples($id){
			$obj= $this->soapclient->getNegativeExamples($id);
			return $this->parseStringlist($obj);
			
			
	}
	public function getIgnoredConcepts($id){
				$obj= $this->soapclient->getIgnoredConcepts($id);
				return $this->parseStringlist($obj);
	}
	public function getSubsumptionHierarchy($id){
			return $this->soapclient->getSubsumptionHierarchy($id);
	}
	
	public function addPositiveExample($id,$name){
					return $this->soapclient->addPositiveExample($id,$name);
				
	}
	
	
	
	public function addNegativeExample($id,$name){
				return $this->soapclient->addNegativeExample($id,$name);
			
	}
	public function addIgnoredConcept($id,$name){
					return $this->soapclient->addIgnoredConcept($id,$name);
				
	}
	
	public function selectInstancesForAConcept($id,$Concept){
		$obj=$this->soapclient->selectInstancesForAConcept($id,$Concept);
		return $this->parseStringlist($obj);
	}
	public function selectAConcept($id,$Concept,$Percentage=100){
			$obj=$this->soapclient->selectAConcept($id,$Concept,$Percentage);
			return $this->parseStringlist($obj);
	}
	
	public function removePositiveExample($id,$name){
			return $this->soapclient->removePositiveExample($id,$name);
	}
	
	public function removeNegativeExample($id,$name){
			return $this->soapclient->removeNegativeExample($id,$name);
	}
	public function removeAllPositiveExamples($id){
			return $this->soapclient->removeAllPositiveExamples($id);
	}
	public function removeAllNegativeExamples($id){
			return $this->soapclient->removeAllNegativeExamples($id);
	}
	public function removeAllExamples($id){
				return $this->soapclient->removeAllExamples($id);
	}
	public function removeIgnoredConcept($id,$name){
				return $this->soapclient->removeIgnoredConcept($id,$name);
	}

	public function getCurrentOntologyURL($id){
			return $this->soapclient->getCurrentOntologyURL($id);
	}
	
	public function removeOntology($id){
		   $this->soapclient->removeOntology($id);
	}
	
	public function readOntology($id,$ontologyURI,$format='RDF/XML'){
		 $this->soapclient->readOntology($id,$ontologyURI,$format);
	}

	public function getAlgorithmStatus($id){
		
		return $this->soapclient->getAlgorithmStatus($id);
		
	}
	
	/*public function hello($arr){
	
	return $this->soapclient->hello($arr);
	
	}*/
	
	public function learnConcept($id){
	

		$concept = $this->soapclient->learnConcept($id);
		return $concept;
	}
	
	public function learnMonitored($id){
		$this->soapclient->learnMonitored($id);
	}
	
	/*public function relearn($id,$concept){
		$this->soapclient->relearn($id,$concept);
	}*/


	public function getLastResult($id){
	

		return  $this->soapclient->getLastResult($id);
		
	}
	
	public function stop($id){
		  try{
		  $this->soapclient->stop($id);
		  }catch (Exception $e){echo "<xmp>"; print_r($e);}
	}
	
	
	
	/**WSDL MANAGEMENT******/
	
	
	public function getInfo(){
	
		$functions = $this->soapclient->__getFunctions();
		echo '<b>Verfügbare Methoden:</b>';
		echo '<pre>';
		print_r($functions);
		echo '</pre>';
		
		
		}
	
	
	public function loadWSDLfiles($wsdluri){
		$main=$this->getwsdl($wsdluri);
		$other=$this->getOtherWSDL($main);
		$newMain=$this->changeWSDL($main);
		$this->writeToFile("main.wsdl",$newMain);
		$x=0;
		foreach ($other as $o){
			$this->writeToFile("def".($x++).".xsd",$this->getwsdl($o));
		}

	}
	
	public function changeWSDL($wsdl){
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
	
	public function getOtherWSDL($wsdl){
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
	

	
	
	public function getwsdl($wsdluri){
		// this is copied from the Pear example
		// please don't ask me how it works
		$req = &new HTTP_Request($wsdluri);
		$message="";
		$req->setMethod(HTTP_REQUEST_METHOD_GET);
		$req->sendRequest();
		$ret=$req->getResponseBody();
		return $ret;
	}
	
	
	
	public function writeToFile($filename,$content){

		$fp=fopen($filename,"w");
		fwrite($fp,$content);
		fclose($fp);
	
	}
	
	public function parseStringlist($a)
	{		
			ini_set('error_reporting',E_ALL & ~E_NOTICE);
			$list=$a->item;
			ini_set('error_reporting',E_ALL & ~E_NOTICE);
			if(sizeof($list)==0)
				{
					
					return array();
				}
			else if(sizeof($list)==1)
				{
				$tmp=array();
				$tmp[]=$list;
				return $tmp;
				}
					
		return $list;
	}
}

?>