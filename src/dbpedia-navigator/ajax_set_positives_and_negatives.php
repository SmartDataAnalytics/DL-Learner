<?php
	include('helper_functions.php');
	
	$positives=$_POST['positives'];
	$negatives=$_POST['negatives'];
	
	if (strlen($positives)>0) $positives=explode('][',substr($positives,1,strlen($positives)-2));
	else $positives=array();
	if (strlen($negatives)>0) $negatives=explode('][',substr($negatives,1,strlen($negatives)-2));
	else $negatives=array();
			
	mysql_connect($mysqlServer,$mysqlUser,$mysqlPass);
	mysql_select_db("navigator_db");
	
	$ptemp=array();
	foreach ($positives as $pos){
		$query="SELECT label FROM rank WHERE name='$pos' LIMIT 1";
		$res=mysql_query($query);
		$result=mysql_fetch_array($res);
		$ptemp[$pos]=$result['label'];
	}
	
	$ntemp=array();
	foreach ($negatives as $neg){
		$query="SELECT label FROM rank WHERE name='$neg' LIMIT 1";
		$res=mysql_query($query);
		$result=mysql_fetch_array($res);
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