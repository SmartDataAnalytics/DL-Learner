<?php
	session_start();
	$positives=$_SESSION['positive'];
	$negatives=$_SESSION['negative'];
	
	$attr=array();
	$pos="";
	$i=1;
	if (isset($_SESSION['positive'])) foreach ($positives as $key=>$value){
		if ($i<count($positives)) $pos.=$key.'][';
		else $pos.=$key;
		$i++;
	}
	if (strlen($pos)>0){
		$pos='positives=['.$pos.']';
		$attr[]=$pos;
	}
	$neg="";
	$i=1;
	if (isset($_SESSION['negative'])) foreach ($negatives as $key=>$value){
		if ($i<count($negatives)) $neg.=$key.'][';
		else $neg.=$key;
		$i++;
	}
	if (strlen($neg)>0){
		$neg.='negatives=['.$neg.']';
		$attr[]=$neg;
	}
	
	$last_action=$_SESSION['lastAction'];
	if (strpos($last_action,'searchConceptInstances')===0){
		$attr[]='concept='.substr($last_action,strpos($last_action, "/")+1);
		$last_action='searchConceptInstances/Concept'; 
	}
	$attributes='?'.implode('&',$attr);
	
	$url='http://'.$_SERVER['HTTP_HOST'].'/dbpedia-navigator/'.$last_action.$attributes;
	
	print '<a href="'.$url.'">'.$url.'</a><br/><br/>';
?>