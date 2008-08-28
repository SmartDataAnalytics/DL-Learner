<?php
	include('helper_functions.php');
	
	$concept=$_POST['concept'];
	
	session_start();
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	$concept=html_entity_decode($concept);
	
	$test=preg_match("/^http://dbpedia.org/class/yago/[^/]+$/",$concept);
	
	$content="";
	/*try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id,$ksID);
		$subjects=$sc->getSubjectsFromConcept($concept);
		$content.=getResultsTable($subjects);
	} catch (Exception $e){
		$content=$e->getMessage();
	}*/
	
	print $test."ho";
	print '$$';
	print "Instances for Concept ".$concept;
?>