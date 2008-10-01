<?php
	ini_set('max_execution_time',200);

	include('helper_functions.php');
	include('Settings.php');
	include('DatabaseConnection.php');
	
	session_start();
		
	if (isset($_SESSION['positive'])) $positives=$_SESSION['positive'];
	if (isset($_SESSION['negative'])) $negatives=$_SESSION['negative'];
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	setRunning($id,"true");
	$concept="";
	$conceptinformation="";
	if (isset($positives)&&count($positives)>0)
	{
		$posArray=array();
		foreach ($positives as $name=>$lab)
			$posArray[]=$name;
		$negArray=array();
		if (isset($negatives))
			foreach ($negatives as $name=>$lab)
				$negArray[]=$name;

		//connect to the database
		$settings=new Settings();
		$databaseConnection=new DatabaseConnection($settings->database_type);
		$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
		$databaseConnection->select_database($settings->database_name);
		
		$all=array_merge($posArray,$negArray);
		
		$classes=array();
		$noclass=array();
		foreach ($all as $pos){
			$newclasses=array();
			$query="SELECT category FROM articlecategories WHERE name='$pos'";
			$res=$databaseConnection->query($query);
			if (mysql_num_rows($res)<1) $noclass[]=$pos; 
			while ($result=$databaseConnection->nextEntry($res)){
				$classes[$pos][]=$result['category'];
				$newclasses[]=$result['category'];
			}
			for ($i=0;$i<1;$i++){
				$tempclasses=array();
				foreach ($newclasses as $clas){
					$query="SELECT father FROM classhierarchy WHERE child='$clas'";
					$res=$databaseConnection->query($query);
					while ($result=$databaseConnection->nextEntry($res)){
						$classes[$pos][]=$result['father'];
						$tempclasses[]=$result['father'];
					}
				}
				$newclasses=$tempclasses;	
			}
		}
		$groups=array();
		$groupclasses=array();
		$i=0;
		foreach ($classes as $key=>$value){
			$i++;
			$groups[$i][]=$key;
			$groupclasses[$i]=$value;
		}
				
		for ($j=1;$j<=$i;$j++){
			if (!isset($groups[$j])) continue;
			for ($k=$j+1;$k<=$i;$k++){
				if (!isset($groups[$k])) continue;
				if (count(array_intersect($groupclasses[$j],$groupclasses[$k]))>0){
					$groups[$j]=array_merge($groups[$j],$groups[$k]);
					$groupclasses[$j]=array_merge($groupclasses[$j],$groupclasses[$k]);
					unset($groups[$k]);
					unset($groupclasses[$k]);
				}
			}
		}
		if (count($noclass)>0) $groups[]=$noclass;
		$problems=array();
		foreach ($groups as $group){
			$pos=array();
			$neg=array();
			foreach ($group as $uri){
				if (in_array($uri,$posArray)) $pos[]=$uri;
				else $neg[]=$uri;
			}
			$problems[]=array('pos'=>$pos,'neg'=>$neg);
		}
		
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id, $ksID);
		if (count($problems)==1) $number=3;
		else if (count($problems)==2) $number=2;
		else $number=1;
		try{
			$concepts=array();
			foreach ($problems as $problem){
				$concepts[]=@$sc->getConceptFromExamples($problem['pos'],$problem['neg'],$number);
			}
						
			if (count($concepts)>0){
				$concept.="<table border=0>\n";
				$concept.="<tr><td>You could also be interested in articles matching these descriptions:</td></tr>";
				foreach ($concepts as $conc){
					foreach ($conc as $con){
						$label=$sc->getNaturalDescription($con['descriptionKBSyntax']);
						$concept.="<tr><td><a href=\"\" onclick=\"getSubjectsFromConcept('kb=".htmlentities($con['descriptionKBSyntax'])."&number=10');return false;\" />".$label."</a> (accuracy: ".(floatVal($con['accuracy'])*100)."%)</td></tr>";
					}
				}
				$concept.="</table>";
			}
			else $concept="-";
		} catch(Exception $e){
			$concept.="-";
		}
	}
	else $concept="-";
	
	print $concept;
?>