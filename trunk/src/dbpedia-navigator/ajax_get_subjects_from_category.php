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
	$query="SELECT name, label FROM rank WHERE category='$category' ORDER BY number DESC LIMIT ".$number;
	$res=mysql_query($query);
	$bestsearches="";
	if (mysql_num_rows($res)>0){
		$names=array();
		$labels=array();
		while ($result=mysql_fetch_array($res)){
			$labels[]=$result['label'];
			$names[]=$result['name'];	  
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