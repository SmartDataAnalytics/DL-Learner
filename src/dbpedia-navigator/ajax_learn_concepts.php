<?php
	include('helper_functions.php');
	
	session_start();
		
	if (isset($_SESSION['positive'])) $positives=$_SESSION['positive'];
	if (isset($_SESSION['negative'])) $negatives=$_SESSION['negative'];
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	setRunning($id,"true");
	$concept="";
	$conceptinformation="";
	if (isset($positives))
	{
		$posArray=array();
		foreach ($positives as $name=>$lab)
			$posArray[]=$name;
		$negArray=array();
		if (isset($negatives))
			foreach ($negatives as $name=>$lab)
				$negArray[]=$name;
			
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id, $ksID);
		try{
			$concepts=$sc->getConceptFromExamples($posArray,$negArray);
			$conceptDepth=$sc->getConceptDepth();
			$conceptArity=$sc->getConceptArity();
			
			$concept.="<table border=0>\n";
			$i=1;
			foreach ($concepts as $con){
				$concept.="<tr><td><a href=\"\" onclick=\"getSubjectsFromConcept('concept=".$con."');return false;\" onMouseOver=\"showdiv('div".$i."');showdiv('ConceptBox');\" onMouseOut=\"hidediv('div".$i."');hidediv('ConceptBox');\" />".$con."</a></td></tr>";
				//put information about concepts in divs
				$conceptinformation.="<div id=\"div".$i."\" style=\"display:none\">Concept Depth: ".$conceptDepth[$i-1]."<br/>Concept Arity: ".$conceptArity[$i-1]."<br/>Concept Length: ".$sc->getConceptLength($con)."</div>";
				$i++;
			}
			$concept.="</table>";
		} catch(Exception $e){
			$concept.=$e->getMessage();
		}
	}
	else $concept="You must choose at least one positive example.";
	
	print $concept;
	print '$$';
	print $conceptinformation;
?>