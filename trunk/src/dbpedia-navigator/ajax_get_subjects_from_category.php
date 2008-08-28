<?php
	include('helper_functions.php');
	
	$category=$_POST['category'];
	$number=$_POST['number'];

	session_start();
	//write last action into session
	$actionuri=substr (strrchr ($category, "/"), 1);
	$_SESSION['lastAction']='searchInstances/'.$actionuri;
	session_write_close();
	
	//initialise content
	$content="";
	$bestsearches="";
	
	mysql_connect($mysqlServer,$mysqlUser,$mysqlPass);
	mysql_select_db("navigator_db");
	
	//get label of the category
	$query="SELECT label FROM categories WHERE category='$category' LIMIT 1";
	$res=mysql_query($query);
	$result=mysql_fetch_array($res);
	$label=$result['label'];
		
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
		$content.=getCategoryResultsTable($names,$labels,$category,$number);
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