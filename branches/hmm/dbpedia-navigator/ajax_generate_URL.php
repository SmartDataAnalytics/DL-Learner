<?php
	session_start();
	if (isset($_SESSION['positive'])) $positives=$_SESSION['positive'];
	if (isset($_SESSION['negative'])) $negatives=$_SESSION['negative'];
	
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
		$neg='negatives=['.$neg.']';
		$attr[]=$neg;
	}
	
	if (isset($_SESSION['lastAction'])){
		$last_action=$_SESSION['lastAction'];
		if (strpos($last_action,'searchConceptInstances')===0){
			$attr[]='concept='.substr($last_action,strpos($last_action, "/")+1);
			$last_action='searchConceptInstances/Concept'; 
		}
		if (count($attr)>0) $attributes='?'.implode('&',$attr);
		else $attributes='';
		
		$url='http://'.$_SERVER['HTTP_HOST'].'/dbpedia-navigator/'.$last_action.$attributes;
		
		print '<font style="font-size:9px"><a href="'.$url.'">'.$url.'</a></font><br/><br/>';
	}
?>