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
			$concept.="<table border=0>\n";
			foreach ($concepts as $con){
				$concept.="<tr><td><a href=\"\" onclick=\"getSubjectsFromConcept('concept=".$con['descriptionManchesterSyntax']."');return false;\" />".$con['descriptionManchesterSyntax']."</a> (Accuracy: ".(floatVal($con['accuracy'])*100)."%)</td></tr>";
			}
			$concept.="</table>";
		} catch(Exception $e){
			$concept.=$e->getMessage();
		}
	}
	else $concept="You must choose at least one positive example.";
	
	print $concept;
?>