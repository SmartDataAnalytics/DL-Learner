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
		$temp=$this->concept;
		$ret=array();
		$offset=0;
		while (true){
			$nextpos=strpos($temp,"http",$offset);
			if (!$nextpos) break;
			$nextend=preg_match("/\040|.TOP|.BOTTOM|.EXISTS|.ALL|.\(/",$temp,$treffer,PREG_OFFSET_CAPTURE,$nextpos);
			if (!$nextend){
				$uri=substr($temp,$nextpos,strlen($temp)-$nextpos);
				$ret[]=$uri;
				break;
			}
			$uri=substr($temp,$nextpos,$treffer[0][1]-$nextpos);
			$ret[]=$uri;
			$offset=$treffer[0][1];
		}
		
		print_r($ret);
		return $ret;
	}
}

$conc="EXISTS http://dbpedia.org/property/website.TOP AND http://dbpedia.org/resource/Berlin";
$nc=new NaturalConcepts($conc);
$ic=$nc->getNaturalConcept();
//print_r($ic);
?>