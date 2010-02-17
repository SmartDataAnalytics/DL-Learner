<?php
	include_once('helper_functions.php');
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	
	$label=urldecode($_POST['label']);
	$number=$_POST['number'];

	session_start();
	//write last action into session
	$_SESSION['lastAction']='search/'.urlencode($label);
	session_write_close();
	
	//initialise content
	$content="";
	
	//connect to the database
	$settings=new Settings();
	$databaseConnection=new DatabaseConnection($settings->database_type);
	$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
	$databaseConnection->select_database($settings->database_name);
	
	$query="SELECT name, label FROM rank WHERE MATCH (label) AGAINST ('$label') ORDER BY number DESC LIMIT ".$number;
	$res=$databaseConnection->query($query);
	$bestsearches="";
	if ($databaseConnection->numberOfEntries($res)>0){
		$names=array();
		$labels=array();
		$classes=array();
		$tags=array();
		$catlabels=array();
		while ($result=$databaseConnection->nextEntry($res)){
			$labels[]=$result['label'];
			$names[]=$result['name'];
			$query="SELECT category FROM articlecategories WHERE name='".$result['name']."'";
			$res3=$databaseConnection->query($query);
			$arr=array();
			while ($result3=$databaseConnection->nextEntry($res3)){
				$arr[]=$result3['category'];
			}
			if (count($arr)==0){
				$arr[]="NoCategory";
				if (!isset($tags['NoCategory'])) $tags['NoCategory']=1;
				else $tags['NoCategory']++;
				if (!isset($catlabels['NoCategory'])) $catlabels['NoCategory']='No Category';
			}
			else
			{
				for ($i=0;$i<count($arr);$i++){
					if (!isset($tags[$arr[$i]])) $tags[$arr[$i]]=1;
					else $tags[$arr[$i]]++;
					$query="SELECT label FROM categories WHERE category='".$arr[$i]."' LIMIT 1";
					$res2=$databaseConnection->query($query);
					$result2=$databaseConnection->nextEntry($res2);
					if (!isset($catlabels[$arr[$i]])) $catlabels[$arr[$i]]=$result2['label'];
				}
			}
			$classes[]=$arr;  
		}
		$content.=getTagCloud($tags,$catlabels);
		$content.=getResultsTable($names,$labels,$classes,$number);
		$bestsearches=getBestSearches($names,$labels);
	}
	else
		$content.="Your search brought no results.";
	
	print $content;
	print '$$$';
	print "search result for \"".$label."\"";
	print '$$$';
	print $bestsearches;
?>