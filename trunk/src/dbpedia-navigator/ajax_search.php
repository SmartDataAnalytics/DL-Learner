<?php
	include('helper_functions.php');
	
	$label=$_POST['label'];
	$list=$_POST['list'];
	session_start();	
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	//get parts of the list
	$checkedInstances=preg_split("[,]",$list,-1,PREG_SPLIT_NO_EMPTY);
	
	//initialise content
	$content="";
	/*try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id,$ksID);
				
		$subjects=$sc->getSubjects($label,$checkedInstances);
		
		$content.=getTagCloud($subjects['tagcloud'],$subjects['tagcloudlabel']);
		$content.=getResultsTable($subjects['subjects']);
	} catch (Exception $e){
		$content=$e->getMessage();
	}*/
	mysql_connect('localhost','navigator','dbpedia');
	mysql_select_db("navigator_db");
	$query="SELECT name, label FROM rank WHERE MATCH (label) AGAINST ('$label') ORDER BY number LIMIT 3";
	$res=mysql_query($query);
	while ($result=mysql_fetch_array($res)){
		$content.='<a href="" onclick="get_article(\'label='.$result['name'].'&cache=-1\');return false;">'.$result['label'].'</a><br/>';
	}
	
	print $content;
	print '$$';
	print "Searchresult for ".$label;
?>