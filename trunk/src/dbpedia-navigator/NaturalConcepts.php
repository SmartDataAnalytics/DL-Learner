<?php

class NaturalConcepts
{
	private $concept;
	
	function NaturalConcepts($conc){
		$this->concept=$conc;
	}
	
	function getNaturalConcept(){
		$identifiedConcepts=$this->identifyConcepts();
		$labels=$this->getLabels($identifiedConcepts);
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
		
		return $ret;
	}
	
	function getLabels($conc)
	{
		$query="SELECT DISTINCT ";
		for ($i=0;$i<count($conc)-1;$i++)
			$query.="?obj".$i.", ";
		$query.="?obj".$i."\n";
		$query.="WHERE {\n";	
		foreach ($conc as $key=>$con){
			$query.="<".$con."> <http://www.w3.org/2000/01/rdf-schema#label> ?obj".$key.".\n";
		}
		$query.="}";
		print $query;
		return $query;
	}
}

$conc="EXISTS http://dbpedia.org/property/website.TOP AND http://dbpedia.org/resource/Berlin";
$nc=new NaturalConcepts($conc);
$ic=$nc->getNaturalConcept();
//print_r($ic);
?>