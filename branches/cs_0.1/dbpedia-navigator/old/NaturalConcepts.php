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
	
	function getSparqlQuery()
	{
		$temp=$this->concept;
		$andOrParts=$this->getAndOrParts($temp);
		print_r($andOrParts);
	}
	
	function getAndOrParts($temp)
	{
		$split=preg_split("/(OR)|(AND)/",$temp,-1,PREG_SPLIT_DELIM_CAPTURE|PREG_SPLIT_NO_EMPTY);
		$bracket=0;
		$arr=array();
		$temppart="";
		foreach ($split as $part){
			$part=trim($part);
			if (strpos($part,"(")===0){
				$bracket+=substr_count($part,"(");
			}
			if ($bracket>0){
				if (($part=="AND")||($part=="OR")) $temppart.=" ".$part." ";
				else $temppart.=$part;
			}
			else{
				if ((!strpos($part,"AND"))&&(!strpos($part,"OR"))) $arr[]=$part;
				else $arr[]=$this->getAndOrParts($part);
			}
			if ((strrpos($part,')')==(strlen($part)-1))&&($bracket>0)){
				$bracket-=substr_count($part,")");
				if ($bracket==0){
					if ((!strpos($temppart,"AND"))&&(!strpos($temppart,"OR"))) $arr[]=substr($temppart,1,strlen($temppart)-2);
					else $arr[]=$this->getAndOrParts(substr($temppart,1,strlen($temppart)-2));
					$temppart="";
				}
			}
		}
		return $arr;
	}
	
	function isExistsConstruct($construct)
	{
		if (!(strpos($construct,"EXISTS")===0)) return false;
		$split=preg_split("/(EXISTS \".*\")\./",$construct,-1,PREG_SPLIT_NO_EMPTY);
		$afterdot=$split[0];
		print $afterdot;
		$bracket=0;
		$offset=0;
		do{
			$nextBracketOn=strpos($afterdot,"(",$offset);
			$nextBracketOff=strpos($afterdot,")",$offset);
			print "On: ".$nextBracketOn+1;
			print "Off: ".$nextBracketOff+1;
			$min=min($nextBracketOn,$nextBracketOff);
			print $min+1;
			if ($nextBracketOn==$min) $bracket++;
			if ($nextBracketOff==$min) $bracket--;
			$offset=$min;
		} while(($bracket>0)||($offset>=strlen($afterdot)-1));
		print ($offset);
		return true;
	}
}

//$conc="(EXISTS http://dbpedia.org/property/website.(http://dbpedia.org/resource/Berlin AND http://dbpedia.org/resource/Berlin) OR (http://dbpedia.org/resource/Berlin AND http://dbpedia.org/resource/Berlin)) OR http://dbpedia.org/resource/Berlin";
$conc="EXISTS \"http://dbpedia.org/property/website\".(http://dbpedia.org/resource/Berlin AND http://dbpedia.org/resource/Berlin)";
$nc=new NaturalConcepts($conc);
$ic=$nc->isExistsConstruct($conc);
//print_r($ic);
?>