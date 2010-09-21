<?php 
	include('helper_functions.php');
	
	$subject=$_POST['subject'];
	
	session_start();
	unset($_SESSION['negative'][$subject]);
	
	//add Positives and Negatives to Interests
	$interests=show_Interests($_SESSION);
		
	print $interests[0];
	print '$$$';
	print $interests[1]; 
?>