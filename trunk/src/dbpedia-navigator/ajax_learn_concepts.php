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
			if (count($concepts)>0){
				$concept.="<table border=0>\n";
				$concept.="<tr><td>You could also be interested in articles matching these descriptions:</td></tr>";
				foreach ($concepts as $con){
					$label=$sc->getNaturalDescription($con['descriptionKBSyntax']);
					$concept.="<tr><td><a href=\"\" onclick=\"getSubjectsFromConcept('manchester=".htmlentities($con['descriptionManchesterSyntax'])."&kb=".htmlentities($con['descriptionKBSyntax'])."&label=".$label."number=10');return false;\" />".$label."</a> (Accuracy: ".(floatVal($con['accuracy'])*100)."%)</td></tr>";
				}
				$concept.="</table>";
			}
			else $concept="-";
		} catch(Exception $e){
			$concept.=$e->getMessage();
		}
	}
	else $concept="-";
	
	print $concept;
?>