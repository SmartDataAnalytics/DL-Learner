<?php

class NaturalConcepts
{
	private $concept;
	
	function NaturalConcepts($conc){
		$this->concept=$conc;
	}
	
	function getNaturalConcept(){
		$identifiedConcepts=$this->identifyConcepts();
		return $identifiedConcepts;
	}
	
	function identifyConcepts()
	{
		$ret=array();
		while (true){
			$nextpos=strpos("http",$this->concept);
			$nextend=strpos()
		}
		return $ret;
	}
}

$conc="EXISTS http://dbpedia.org/property/website AND http://dbpedia.org/resource/Berlin";
$nc=new NaturalConcepts($conc);
$ic=$nc->getNaturalConcept();
print_r($ic);
?>