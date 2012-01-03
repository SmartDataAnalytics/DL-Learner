<?php
	include_once('helper_functions.php');
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	
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
	
	//connect to the database
	$settings=new Settings();
	$databaseConnection=new DatabaseConnection($settings->database_type);
	$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
	$databaseConnection->select_database($settings->database_name);
	
	//get label of the category
	$query="SELECT label FROM categories WHERE category='$category' LIMIT 1";
	$res=$databaseConnection->query($query);
	$result=$databaseConnection->nextEntry($res);
	$label=$result['label'];
		
	$query="SELECT DISTINCT name FROM articlecategories WHERE category='$category' ORDER BY number DESC LIMIT ".$number;
	$res=$databaseConnection->query($query);
	$bestsearches="";
	if ($databaseConnection->numberOfEntries($res)>0){
		$names=array();
		$labels=array();
		while ($result=$databaseConnection->nextEntry($res)){
			$names[]=$result['name'];
			$query="SELECT label FROM rank WHERE name='".$result['name']."' LIMIT 1";
			$res2=$databaseConnection->query($query);
			$result2=$databaseConnection->nextEntry($res2);
			$labels[]=$result2['label'];
		}
		$content.=getCategoryResultsTable($names,$labels,$category,$number);
		$bestsearches=getBestSearches($names,$labels);
	}
	else
		$content.="Your Search brought no results.";
	
	print $content;
	print '$$$';
	print "Instances of Class \"".$label."\"";
	print '$$$';
	print $bestsearches;
?>