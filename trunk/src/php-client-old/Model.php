<?php


include_once("Settings.php");
include_once("LearnerClient.php");
require_once 'pear/HTTP_Request.php';


class Model{
	
	public  $lc;
	public  $id;
	
	
	
	
	public  $lastResult;
	public  $currentOntology;
	public  $ontologySelected;
	public  $positives;
	public  $negatives;
	public  $instances;
	public  $error="";
	public  $errorOccurred;
	public  $algorithmStatus;
	public  $message="";
	public  $instancesOfConcepts;
	public  $conceptsOfinstances;
	public  $concepts;
	public  $rolesOfInstances;
	public  $roles;
	public  $retrievalActive;
	public  $ignoredClasses;
	public  $subsumptionHierarchy;
	

	public function __construct($id,$learnerclient,$retrievalActive="active"){
	
		$this->lc=$learnerclient;
		$this->ontologySelected=false;
		$this->id=$id;
		$this->errorOccurred=false;
		$this->algorithmStatus="";
		$this->conceptsOfinstances=array();
		$this->rolesOfInstances=array();
		$this->roles=array();
		$this->instancesOfConcepts=array();
		$this->ignoredConcepts=array();
		$this->retrievalActive=($retrievalActive=='active')?true:false;
		
		

	}
	
	
	public function make(){
	
			$this->lastResult=$this->lc->getLastResult($this->id);
			
			
			try{ 
				
				$this->currentOntology=$this->lc->getCurrentOntologyURL($this->id);
				//exception is thrown above, if no ontology is selected
				$this->ontologySelected=true;
				$this->concepts=$this->lc->getAtomicConcepts($this->id);
				$this->roles=$this->lc->getAtomicRoles($this->id);
				$this->instances=$this->lc->getInstances($this->id);
				//print_r($this->instances);
				$this->positives=$this->lc->getPositiveExamples($this->id);
				$this->negatives=$this->lc->getNegativeExamples($this->id);
				$this->instances=array_diff($this->instances,$this->positives,$this->negatives);
				$this->ignoredConcepts=$this->lc->getIgnoredConcepts($this->id);
				$this->algorithmStatus=$this->lc->getAlgorithmStatus($this->id);
				$this->subsumptionHierarchy=$this->lc->getSubsumptionHierarchy($this->id);
				
				if($this->retrievalActive)
				{					
					foreach ($this->concepts as $one) 
					{	
						$instanceList=$this->lc->retrieval($this->id,$one);
						$this->instancesOfConcepts["$one"]=$instanceList;
						foreach ($instanceList as $inner) 
						{
							$this->conceptsOfinstances["$inner"] []=$one;
						}
					}
					foreach ($this->roles as $one) 
					{	
						$instanceList=$this->lc->getIndividualsForARole($this->id,$one);
						foreach ($instanceList as $inner) 
						{
							$this->rolesOfInstances["$inner"] []=$one;
						}
					}
					
					
				}
				
				//echo "<xmp>";
				//print_r($this->instancesOfConcepts);
				//print_r($this->conceptsOfinstances);
				
				
				
			
			
			
			}catch(Exception $e){
				ini_set('error_reporting',E_ALL );
				$this->errorOccurred=true;
				$this->error.="-".$e->getMessage()."<br>";
				if($this->error=="Select Ontology First"){
					$this->ontologySelected=false;
					}
				
				
				}
		}
			
	
	
	public function getError(){
		return $this->error;
	}
	public function getMessage(){
		return $this->message;
	}
	
	
	public function getOntologySelect(){
		if($this->ontologySelected){
		
		$ret= "Currently selected ontology: <br>".$this->currentOntology."
	<a href='index.php?selectOntology='>choose other ontology</a>";
			return $ret;
		}
		
		else {
		
			$ret= "Choose or type in Ontology:<br><br>";
			$settings=new Settings();
			$ontos=$this->listAvailableOntologies($settings->baseuri, $settings->ontodir);
			//print_r( $ontos);
			foreach (array_keys($ontos) as $ont){
				$ret.= "<a href=\"index.php?selectOntology=".$ontos["$ont"]." \">".$ont."</a><br>";
				}
			$ret.= "	<form action='index.php' method='get'>
				   	<input type='text' name='selectOntology'>
					<input type='submit' value='submit'>
				 	</form>";
			return  $ret;
		}
	}
	
	public function getInstanceView(){
		
		if(sizeof($this->instances )==0){return "None";}
		
		$ret="Press 'pos' to add instance to positive examples, 'neg' for negative<br><br>";
		//$arr=array_diff($instances,$positives,$negatives);
		
		foreach ($this->instances as $a)
		{	
			$b=$this->shorten($a);
			//unable to use \n in tooltip, html only
			$tooltip=$this->getTooltipForInstance($a);
			$ret.= "<dummy ".$tooltip.">";
			$ret.=$b ."</dummy>
			<a href='index.php?action=add&where=pos&subject=".urlencode($a)."'>pos</a>|
			<a href='index.php?action=add&where=neg&subject=".urlencode($a)."'>neg</a>
			<br>";
		}
		
		return $ret;
	
	}
	
	public function getExamples()
	{	
		$ret= "<font color='#00AAAA'>
		<b>Positive Examples</b>
		(<a href='index.php?action=remove&where=examples&subject=pos'>clear</a>):<br>";
		if(sizeof($this->positives)==0)
		{
			$this->positives=array();
			$ret.= "None selected<br>";
		}

		foreach ($this->positives as $p)
		{
			$b=$this->shorten($p);
			//unable to use \n in tooltip, html only
			$tooltip=$this->getTooltipForInstance($p);
			$ret.= "<dummy ".$tooltip.">";
			$ret.=$b ."</dummy>
			<a href='index.php?action=remove&where=pos&subject=".urlencode($p)."'>remove</a><br>";
		}

		$ret.= "</font><br><font color='#FF0000'>
		<b>Negative Examples</b>
		(<a href='index.php?action=remove&where=examples&subject=neg'>clear</a>):<br>";
		if(sizeof($this->negatives)==0)
		{	
			$this->negatives=array();
			$ret.= "None selected<br>";
		}
		foreach ($this->negatives as $n)
		{		
			$b=$this->shorten($n);
			//unable to use \n in tooltip, html only
			$tooltip=$this->getTooltipForInstance($n);
			$ret.= "<dummy  ".$tooltip."  >";
			$ret.=$b ."</dummy>
			 <a href='index.php?action=remove&where=neg&subject=".urlencode($n)."'>remove</a><br>";
		}
			$ret.="</font><br>";
			
			
			
			$test=array_intersect($this->positives,$this->negatives);
			
			$tmp="";
			foreach ($test as $one)
			{		
				$b=$this->shorten($one);
				//unable to use \n in tooltip, html only
				$tooltip=$this->getTooltipForInstance($one);
				$tmp.= "<dummy  ".$tooltip."  >";
				$tmp.=$b ."</dummy>
				 <a href='index.php?action=remove&where=neg&subject=".urlencode($one)."'><font color='#00AAAA'>pos</font></a>|
				 <a href='index.php?action=remove&where=pos&subject=".urlencode($one)."'><font color='#FF0000'>neg</font></a> <br>";
			}
			if(sizeof($test)>=1){
				$t=$this->getTooltip("Inconsistent examples means, that there instances<br> beloning at the same time to the positive and<br> negative example set<br>Choose in which set they should belong");
				$ret.="<font color='#FFAA00' ".$t."><b><blink>Inconsistencies:</blink></b><br>".$tmp."</font><br>";
				}
			
			if(sizeof($this->positives)>=1||sizeof($this->negatives)>=1)
			{
				$ret.="
				<br>save your example set here:
				<br>
				<form action='index.php' method='get'>
				<input type='text' name='filename' value='filename'>
				<input type='submit' name='savedset' value='save'>
				</form><br>
				<a href='index.php?action=remove&where=examples&subject=all' >clear all examples</a>";
			}
			
			
			
		return $ret;
	}
	
	public function getConcepts()
	{
		if(sizeof( $this->concepts)==0){return "";}
		else 
		{	$ret="";
			$uri="index.php?";
			
			$tt1=$this->getTooltip("Choose to automatically add instances of this class to positives examples<br>and choose randomly 50% of instances left over to negative examples");
			$tt2=$this->getTooltip("Choose to ignore a class when learning, can be used to relearn a concept definition");
			
			$ret.="Ignored Classes:<br>".((sizeof($this->ignoredConcepts)==0)?"None<br>":"");
			
			foreach ($this->ignoredConcepts as $one)
			{	

				$tooltip=$this->getTooltipForConcepts($one);
				$ret.="<i  ".$tooltip." >".
					shorten($one)." (".sizeof($this->instancesOfConcepts["$one"]).")
					</i>".
					" <a href='".$uri."action=select&class=".urlencode($one)."' ".($tt1).">select</a> |
					 <a href='".$uri."action=aknowledge&class=".urlencode($one)."' ".($tt2).">aknowledge</a><br>";
			}

			$ret.="<br>Classes:<br>";
			foreach ($this->concepts as $one)
			{	
				if(in_array($one,$this->ignoredConcepts))continue;
				$tooltip=$this->getTooltipForConcepts($one);
				$ret.="<dummy  ".$tooltip." >".
					shorten($one)." (".sizeof($this->instancesOfConcepts["$one"]).")
					</dummy>".
					" <a href='".$uri."action=select&class=".urlencode($one)."' ".($tt1).">select</a> |
					 <a href='".$uri."action=ignore&class=".urlencode($one)."' ".($tt2).">ignore</a><br>";
			}
		 return $ret;
		}
	
	}
	
	public function getRoles()
		{
			if(sizeof( $this->roles)==0){return "";}
			else 
			{	$ret="";
				//$ret.="Ignored Classes:<br>".((sizeof($this->ignoredConcepts)==0)?"None<br>":"");
				
				foreach ($this->roles as $one)
				{		
					$tooltip=$this->getTooltip($one);
					$ret.="<li  ".$tooltip." >".
						shorten($one)." </li>";
				}
	
				
				return $ret;
			}
		
		}
	
	
	
	public function getPossibleActions()
	{
		$ret="Start learning: <br>";
		
		if(!$this->ontologySelected)
		{
			$ret.="Select ontology first";
		}
		
		else if($this->algorithmStatus=="running")
		{
			$ret.="Algorithm is running, <br>try to refresh using the buttons";
		}
		else if(sizeof(array_intersect($this->positives,$this->negatives))>=1)
		{	$t=$this->getTooltip("Inconsistent examples means, that there instances<br> beloning at the same time to the positive and<br> negative example set<br>Choose in which set they should belong");
			$ret.="<font color='#FFAA00' ".$t."><b>There are inconsistencies</b></font>";
		}
		else if(sizeof($this->positives)*sizeof($this->negatives)>=1)
		{
			$ret.="
				<form action=index.php  method=get>
				<input type='submit' name='start' value='start'>
				</form>";
		}
		else 
		{	
			$ret.="<b>Select at least one positive and one negative instance first</b>";
	
		}
		
		$tooltip="Retrieval means the extra tooltip information,<br> if you hover over the instances<br> greatly improves performance";
		if($this->retrievalActive){
			$ret.="<br><a href='index.php?retrieval=inactive' ".$this->getTooltip($tooltip).">stop retrieval</a>";
			}
		else{
		$ret.="<br><a href='index.php?retrieval=active' ".$this->getTooltip($tooltip).">start retrieval</a>";
			}
		
		return $ret;
		
	
	}
	
	public function getView(){
		
	
	
	}
	
	public function shorten($a)
	{
	    if(($strpos=strpos($a,'#'))>=4){
			return substr($a,$strpos);
		}
		else {return $a;}
	}
			
			
		
	
	public function getSavedSets($savedset=false,$filename=false,$desc=false){
		$ret="";
		$settings=new Settings();
		
		switch ($savedset){
			case "delete":
					if(is_file($settings->savedir.$filename))
						unlink($settings->savedir.$filename);
					break;
			case "restore":
				$this->restoreSavedSet($filename);			
				break;
			case "save":
				$this->saveSets($filename,$desc);
				break;
		
		}
	
		
		$saves=$this->listdir($settings->savedir);

		foreach ($saves as $one){
			$xml=simplexml_load_file(($settings->savedir).$one);
			if($xml->ontologyURI==$this->currentOntology)
			{	
				$ret.="<li><dummy onmouseover=\"Tip('".$xml->description."')\"   >"
				.$one.
				"</dummy>
				<a href='index.php?savedset=restore&filename=".$one."'>restore</a>
				<a href='index.php?savedset=delete&filename=".$one."'>delete</a></li>
				<br>";
			}
		
		}//foreach
		
		if ($ret!=""){
			$ret="Hover over the names to get a description<br>".$ret;
		}
		return $ret;
	//	echo "<xmp>";
	//			print_r($xml);
	//	echo  "</xmp>";
	
	}
	public function saveSets($filename,$description){
		$filename=str_replace( " ","_",$filename);
		
		$xmlstr="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root></root>";
		$xml = new SimpleXMLElement($xmlstr);
		$xml->addChild('ontologyURI',$this->currentOntology);
		
		$xml->addChild('positives');
		$xml->addChild('negatives');
		$description="".$this->currentOntology;
		$description.="<br><br>positives:<br>";
		foreach ($this->positives as $one){
			$xml->positives->addChild('positive',$one);
			$description.=$one."<br>";
		}
		$description.="<br>negatives:<br>";
		foreach ($this->negatives as $one){
			$xml->negatives->addChild('negative',$one);
			$description.=$one."<br>";
		}
		
		//$description=sizeof($this->positives)"";
		$xml->addChild('description',$description);
		$settings=new Settings();
		if(file_exists(urlencode($settings->savedir.$filename)))
			$message.="Warning: file already existed and was overwritten<br>";
		$xml->asXML(urlencode($settings->savedir.$filename));
		
	}
	
	public function restoreSavedSet($filename){
		
		$settings=new Settings();
		$uri=($settings->uri)."index.php?plain=plain&id=".$this->id;
		$xml=simplexml_load_file(($settings->savedir).$filename);
		
		$orders=array();
		//$orders[]=$uri."&selectOntology=";
		//$orders[]=$uri."&selectOntology=".urlencode($xml->ontologyURI);
		
		foreach ($xml->positives[0] as $one)
		{
			$orders[]=$uri."&action=add&where=pos&subject=".urlencode($one);
			
		}
		foreach ($xml->negatives[0] as $one)
		{
			$orders[]=$uri."&action=add&where=neg&subject=".urlencode($one);
					
		}
		
		foreach ($orders as $one)
		{
			//echo $one."<br>";
			$this->sendandreceive($one)."<br>";
			
			
		}
		$this->message.=
			"Refresh needed, hit the button:
			<form action=index.php  method=get>
			<input type='submit' name='refresh' value='refresh'>
			</form><br>";
		
		
	}
	
	public function getTooltip($tooltip){
	
		return " onmouseover=\"Tip('".$tooltip."')\" ";
	}
	
	public function getTooltipForInstance($inst){
			if(!$this->retrievalActive) return  $this->getTooltip("deactivated for performance reasons,<br> see Possible Actions");
			$tooltip=$inst."<br>";
			$tooltip.="Instance of:<br>";
			//echo "<xmp>";
			//echo $inst;
			
			if(sizeof($this->conceptsOfinstances["$inst"])==0)
			{	$this->conceptsOfinstances["$inst"]=array();
				$tooltip.="<li>Thing</li><br>";
			}
			foreach($this->conceptsOfinstances["$inst"] as $concept)
			{
				$tooltip.="<li>".shorten($concept)."</li>";
			}
			$tooltip.="Properties:<br>";
			
			if(sizeof($this->rolesOfInstances["$inst"])==0)
			{	$this->rolesOfInstances["$inst"]=array();
				$tooltip.="<li>None</li><br>";
			}
			foreach($this->rolesOfInstances["$inst"] as $role)
			{
				$tooltip.="<li>".shorten($role)."</li>";
			}
			return $this->getTooltip($tooltip);
	
	}
	
	public function getTooltipForConcepts($concept){
		if(!$this->retrievalActive) return  $this->getTooltip("deactivated for performance reasons,<br> see Possible Actions");
		$tooltip="Class: ".$concept."<br>";
		$tooltip.="Instances:<br>";
		//print_r($this->instancesOfConcepts["$concept"]);
		$x=0;
		if(sizeof($this->instancesOfConcepts["$concept"] )==0)
		{	$this->instancesOfConcepts["$concept"] =array();
			$tooltip.="<li>None</li><br>";
		}
		foreach($this->instancesOfConcepts["$concept"] as $one)
		{   $x++;
			$tooltip.="".shorten($one)." | ".(($x%6==0)?"<br>":"");
			//echo "<li>".shorten($one)."</li>";
		}
		
		//print_r($this->instancesOfConcepts);
		$tooltip.="<br>";
		return $this->getTooltip($tooltip);

	}
	
	public function listAvailableOntologies($baseuri, $ontodir){
		$ontos=$this->listdir($ontodir);
		$ret=array();
		foreach($ontos as $ont)
		{
			$ret[substr($ont,0,-4)]=$baseuri.$ont;
		}
		return $ret;
	}

	public function listdir($dirname){
		$verz=opendir ($dirname);
		$first= readdir ($verz);
		$first= readdir ($verz);
		$ret=array();
		while ($file = readdir ($verz)) 
		{	if($file==".svn")continue;
			$ret[]=$file;
		}
		closedir($verz);
		return $ret;
	}
	
	public function sendandreceive($uri){
		$req = &new HTTP_Request($uri);
		$message="";
		$req->setMethod(HTTP_REQUEST_METHOD_GET);
		$req->sendRequest();
		$ret=$req->getResponseBody();
	}
}
?>