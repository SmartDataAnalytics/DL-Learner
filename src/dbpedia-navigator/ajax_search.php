<?php
	include('helper_functions.php');
	
	$label=urldecode($_POST['label']);
	$number=$_POST['number'];

	session_start();
	//write last action into session
	$_SESSION['lastAction']='search/'.urlencode($label);
	session_write_close();
	
	//initialise content
	$content="";
	
	mysql_connect($mysqlServer,$mysqlUser,$mysqlPass);
	mysql_select_db("navigator_db");
	$query="SELECT name, label FROM rank WHERE MATCH (label) AGAINST ('$label') ORDER BY number DESC LIMIT ".$number;
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
			$query="SELECT category FROM articlecategories WHERE name='".$result['name']."'";
			$res3=mysql_query($query);
			$arr=array();
			while ($result3=mysql_fetch_array($res3)){
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
					$res2=mysql_query($query);
					$result2=mysql_fetch_array($res2);
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
		$content.="Your Search brought no results.";
	
	print $content;
	print '$$';
	print "Searchresult for \"".$label."\"";
	print '$$';
	print $bestsearches;
?>