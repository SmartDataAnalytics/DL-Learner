<?php
	include('helper_functions.php');
	
	$subject=$_POST['subject'];
	$label=$_POST['label'];

	session_start();
	unset($_SESSION['positive'][$subject]);
	if (!isset($_SESSION['negative'])){
		$array=array($subject => $label);
		$_SESSION['negative']=$array;
	}
	else{
		$array=$_SESSION['negative'];
		$array[$subject]=$label;
		$_SESSION['negative']=$array;
	}
	
	//add Positives and Negatives to Interests
	$interests=show_Interests($_SESSION);
		
	print $interests[0];
	print '$$$';
	print $interests[1];
?>