<?php
	include_once('helper_functions.php');
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	
	$kb=html_entity_decode($_POST['kb']);
	$number=$_POST['number'];
	$kb=str_replace('\"','"',$kb);
			
	session_start();
	if (isset($_SESSION['id'])){
		$id=$_SESSION['id'];
		$ksID=$_SESSION['ksID'];
	}
	else{
		print "Your Session expired. Please reload.";
		die();
	}
	//write last action into session
	$actionuri=urlencode($kb);
	$_SESSION['lastAction']='searchConceptInstances/'.$actionuri;
	session_write_close();
	
	setRunning($id,"true");
	
	require_once("DLLearnerConnection.php");
	$sc=new DLLearnerConnection($id,$ksID);
	$label=$sc->getNaturalDescription($kb);
	//connect to the database
	$settings=new Settings();
	$databaseConnection=new DatabaseConnection($settings->database_type);
	$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
	$databaseConnection->select_database($settings->database_name);
	
	if ($settings->classSystem=="YAGO") $test=preg_match("/^([\(]*\"http:\/\/dbpedia\.org\/class\/yago\/[^\040]+\"[\)]*(\040(AND|OR)\040)?)+$/",$kb);
	else if ($settings->classSystem=="DBpedia") $test=preg_match("/^([\(]*\"http:\/\/dbpedia\.org\/ontology\/[^\040]+\"[\)]*(\040(AND|OR)\040)?)+$/",$kb);
			
	$content="";
	if ($test){
		if ($settings->classSystem=="YAGO") preg_match_all("/\"http:\/\/dbpedia\.org\/class\/yago\/[^\040()]+\"/",$kb,$treffer,PREG_OFFSET_CAPTURE);
		else if ($settings->classSystem=="DBpedia") preg_match_all("/\"http:\/\/dbpedia\.org\/ontology\/[^\040()]+\"/",$kb,$treffer,PREG_OFFSET_CAPTURE);
		
		$final='';
		$i=1;
		$pos=0;
		foreach ($treffer[0] as $tref){
			$final.=substr($kb,$pos,$tref[1]-$pos).'(';
			$category=substr($kb,$tref[1],strlen($tref[0]));
			$query='SELECT child FROM classhierarchy WHERE father='.$category.'';
			$res=$databaseConnection->query($query);
			while ($result=$databaseConnection->nextEntry($res)){
				$final.='cat'.$i.'.category="'.$result['child'].'" OR ';
			}
			$final.='cat'.$i.'.category='.$category.')';
			$i++;
			$pos=$tref[1]+strlen($tref[0]);
		}
		$final.=substr($kb,$pos);
		$temp='SELECT cat1.name FROM ';
		for ($j=0;$j<$i-1;$j++)
			if ($j!=$i-2) $temp.='articlecategories as cat'.($j+1).',';
			else $temp.='articlecategories as cat'.($j+1);
		$temp.=' WHERE ';
		for ($j=1;$j<$i-1;$j++)
			$temp.='cat'.$j.'.name=cat'.($j+1).'.name AND ';
		
		$query=$temp.'('.$final.') ORDER BY number DESC LIMIT '.$number;
		
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
			$content.=getConceptResultsTable($names,$labels,htmlentities($kb),$number);
			$bestsearches=getBestSearches($names,$labels);
		}
		else
			$content.="Your Search brought no results.";
	}
	else{
		try{
			$subjects=$sc->getSubjectsFromConcept($kb,$number);
			$names=array();
			$labels=array();
			foreach ($subjects as $subject){
				$query='SELECT number, label FROM rank WHERE name="'.$subject.'"';
				$res=$databaseConnection->query($query);
				$result=$databaseConnection->nextEntry($res);
				$names[]=$result['number'].'<'.$subject;
				$labels[]=$result['number'].$subject.'<'.$result['label'];
			}
			rsort($labels);
			rsort($names);
			for ($i=0;$i<count($names);$i++){
				$labels[$i]=substr($labels[$i],strpos($labels[$i],'<')+1);
				$names[$i]=substr($names[$i],strpos($names[$i],'<')+1);
			}
			$content.=getConceptResultsTable($names,$labels,htmlentities($kb),$number);
			$bestsearches=getBestSearches($names,$labels);
		} catch (Exception $e){
			$content=$e->getMessage();
		}
	}
	
	print $content;
	print '$$$';
	print "Instances for Concept \"".$label."\"";
	print '$$$';
	print $bestsearches;
?>