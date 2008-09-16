<?php
	include('helper_functions.php');
	
	$subject=$_POST['subject'];
	$label=$_POST['label'];

	session_start();
	unset($_SESSION['negative'][$subject]);
	if (!isset($_SESSION['positive'])){
		$array=array($subject => $label);
		$_SESSION['positive']=$array;
	}
	else{
		$array=$_SESSION['positive'];
		$array[$subject]=$label;
		$_SESSION['positive']=$array;
	}
	
	//add Positives and Negatives to Interests
	$interests=show_Interests($_SESSION);
		
	print $interests[0];
	print '$$$';
	print $interests[1]; 
?>