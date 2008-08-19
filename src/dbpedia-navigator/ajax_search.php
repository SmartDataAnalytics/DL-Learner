<?php
	include('helper_functions.php');
	
	$label=$_POST['label'];
	$list=$_POST['list'];
	$number=$_POST['number'];
		
	//get parts of the list
	$checkedInstances=preg_split("[,]",$list,-1,PREG_SPLIT_NO_EMPTY);
	
	//initialise content
	$content="";
	
	mysql_connect('localhost','navigator','dbpedia');
	mysql_select_db("navigator_db");
	$query="SELECT name, label, category FROM rank WHERE MATCH (label) AGAINST ('$label') ORDER BY number DESC LIMIT ".$number;
	$res=mysql_query($query);
	$bestsearches="";
	if (mysql_num_rows($res)>0){
		$names=array();
		$labels=array();
		$classes=array();
		$tags=array();
		$catlabels=array();
		while ($result=mysql_fetch_array($res)){
			$labels[]=$result['label'];
			$names[]=$result['name'];
			if (!isset($result['category'])){
				$result['category']="NoCategory";
				$result2['label']="No Category";
			}
			else
			{
				$query="SELECT label FROM categories WHERE category='".$result['category']."' LIMIT 1";
				$res2=mysql_query($query);
				$result2=mysql_fetch_array($res2);
			}
			$classes[]=$result['category'];
			if (!isset($tags[$result['category']])) $tags[$result['category']]=1;
			else $tags[$result['category']]++;
			if (!isset($catlabels[$result['category']])) $catlabels[$result['category']]=$result2['label'];  
		}
		$content.=getTagCloud($tags,$catlabels);
		$content.=getResultsTable($names,$labels,$classes,$number);
		$bestsearches=getBestSearches($names,$labels);
	}
	else
		$content.="Your Search brought no results.";
	
	print $content;
	print '$$';
	print "Searchresult for \"".$label."\"";
	print '$$';
	print $bestsearches;
?>