<?php
	include_once('helper_functions.php');
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	
	$positives=$_POST['positives'];
	$negatives=$_POST['negatives'];
	
	if (strlen($positives)>0) $positives=explode('][',substr($positives,1,strlen($positives)-2));
	else $positives=array();
	if (strlen($negatives)>0) $negatives=explode('][',substr($negatives,1,strlen($negatives)-2));
	else $negatives=array();
			
	//connect to the database
	$settings=new Settings();
	$databaseConnection=new DatabaseConnection($settings->database_type);
	$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
	$databaseConnection->select_database($settings->database_name);
	
	$ptemp=array();
	foreach ($positives as $pos){
		$query="SELECT label FROM rank WHERE name='$pos' LIMIT 1";
		$res=$databaseConnection->query($query);
		$result=$databaseConnection->nextEntry($res);
		$ptemp[$pos]=$result['label'];
	}
	
	$ntemp=array();
	foreach ($negatives as $neg){
		$query="SELECT label FROM rank WHERE name='$neg' LIMIT 1";
		$res=$databaseConnection->query($query);
		$result=$databaseConnection->nextEntry($res);
		$ntemp[$neg]=$result['label'];
	}
			
	session_start();
	
	$_SESSION['positive']=$ptemp;
	$_SESSION['negative']=$ntemp;
		
	//add Positives and Negatives to Interests
	$interests=show_Interests($_SESSION);
		
	print $interests[0];
	print '$$';
	print $interests[1]; 
?>