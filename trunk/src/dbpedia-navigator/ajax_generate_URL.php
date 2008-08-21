<?php
	session_start();
	$positives=$_SESSION['positive'];
	$negatives=$_SESSION['negative'];
	
	$pos="";
	$i=1;
	if (isset($_SESSION['positive'])) foreach ($positives as $key=>$value){
		if ($i<count($positives)) $pos.=$key.'][';
		else $pos.=$key;
		$i++;
	}
	if (strlen($pos)>0) $pos='positives=['.$pos.']';
	$neg="";
	$i=1;
	if (isset($_SESSION['negative'])) foreach ($negatives as $key=>$value){
		if ($i<count($negatives)) $neg.=$key.'][';
		else $neg.=$key;
		$i++;
	}
	if (strlen($neg)>0) $neg='negatives=['.$neg.']';
		
	if (strlen($pos)>0||strlen($neg)>0) $interests='?'.$pos.$neg;
	else $interests="";
	
	$url='http://'.$_SERVER['HTTP_HOST'].'/dbpedia-navigator/'.$_SESSION['lastAction'].$interests;
	
	print $url.'<br/><br/>';
?>