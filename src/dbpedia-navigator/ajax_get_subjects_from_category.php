<?php
	include('helper_functions.php');
	
	$category=$_POST['category'];
	$label=$_POST['label'];
	$number=$_POST['number'];
		
	//initialise content
	$content="";
	$bestsearches="";
	
	mysql_connect('localhost','navigator','dbpedia');
	mysql_select_db("navigator_db");
	$query="SELECT name FROM articlecategories WHERE category='$category' ORDER BY number DESC LIMIT ".$number;
	$res=mysql_query($query);
	$bestsearches="";
	if (mysql_num_rows($res)>0){
		$names=array();
		$labels=array();
		while ($result=mysql_fetch_array($res)){
			$names[]=$result['name'];
			$query="SELECT label FROM rank WHERE name='".$result['name']."' LIMIT 1";
			$res2=mysql_query($query);
			$result2=mysql_fetch_array($res2);
			$labels[]=$result2['label'];
		}
		$content.=getCategoryResultsTable($names,$labels,$category,$label,$number);
		$bestsearches=getBestSearches($names,$labels);
	}
	else
		$content.="Your Search brought no results.";
	
	print $content;
	print '$$';
	print "Searchresult for Category \"".$label."\"";
	print '$$';
	print $bestsearches;
?>