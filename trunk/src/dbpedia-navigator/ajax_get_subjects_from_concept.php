<?php
	include_once('helper_functions.php');
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	
	$manchester=html_entity_decode($_POST['manchester']);
	$kb=html_entity_decode($_POST['kb']);
	$number=$_POST['number'];
	$label=$_POST['label'];
	
	session_start();
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	$test=preg_match("/^([\(]*http:\/\/dbpedia\.org\/class\/yago\/[^\040]+[\)]*(\040(AND|OR)\040)?)+$/",$manchester);
	
	$content="";
	if ($test){
		//connect to the database
		$settings=new Settings();
		$databaseConnection=new DatabaseConnection($settings->database_type);
		$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
		$databaseConnection->select_database($settings->database_name);
	
		preg_match_all("/http:\/\/dbpedia\.org\/class\/yago\/[^\040()]+/",$manchester,$treffer,PREG_OFFSET_CAPTURE);

		$final='';
		$i=1;
		$pos=0;
		foreach ($treffer[0] as $tref){
			$final.=substr($manchester,$pos,$tref[1]-$pos).'(';
			$category=substr($manchester,$tref[1],strlen($tref[0]));
			$query='SELECT child FROM classhierarchy WHERE father=\''.$category.'\'';
			$res=$databaseConnection->query($query);
			while ($result=$databaseConnection->nextEntry($res)){
				$final.='cat'.$i.'.category=\''.$result['child'].'\' OR ';
			}
			$final.='cat'.$i.'.category=\''.$category.'\')';
			$i++;
			$pos=$tref[1]+strlen($tref[0]);
		}
		$final.=substr($manchester,$pos);
		$temp='SELECT cat1.name FROM ';
		for ($j=0;$j<$i-1;$j++)
			if ($j!=$i-2) $temp.='articlecategories as cat'.($j+1).',';
			else $temp.='articlecategories as cat'.($j+1);
		$temp.=' WHERE ';
		for ($j=1;$j<$i-1;$j++)
			$temp.='cat'.$j.'.name=cat'.($j+1).'.name AND ';
		
		$query=$temp.'('.$final.') LIMIT '.$number;
		
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
			$content.=getConceptResultsTable($names,$labels,htmlentities($manchester),htmlentities($kb),$label,$number);
			$bestsearches=getBestSearches($names,$labels);
		}
		else
			$content.="Your Search brought no results.";
	}
	else{
		/*try{
			require_once("DLLearnerConnection.php");
			$sc=new DLLearnerConnection($id,$ksID);
			$subjects=$sc->getSubjectsFromConcept($concept);
			$content.=getResultsTable($subjects);
		} catch (Exception $e){
			$content=$e->getMessage();
		}*/
	}
	
	print $content;
	print '$$$';
	print "Instances for Concept \"".$label."\"";
	print '$$$';
	print $bestsearches;
?>