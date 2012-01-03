<?php
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	include_once('helper_functions.php');
	
	$class=$_POST['class'];
	$fromCache=$_POST['cache'];
			
	session_start();
	if (isset($_SESSION['classes'])) $classes=$_SESSION['classes'];
	//write last action into session
	$actionuri=substr (strrchr ($class, "/"), 1);
	$_SESSION['lastAction']='showClass/'.$actionuri;
	session_write_close();
	
		
	//if article is in session, get it out of the session
	if (isset($classes)){
		foreach ($classes as $key => $value)
		{
			if ($value['uri']==$class){
				$fromCache=$key;
				break;
			}
		}
	}
	
	//initialize the content variables
	$content="";
	$lastClasses="";
	$title="";
			
	//get the article
	//if $fromCache is -1, everything is normal
	//if $fromCache is >=0, the article is taken out of the cache
	if ($fromCache<0) {
		//if there are errors see catch block
		try{
			//connect to the database
			$settings=new Settings();
			$databaseConnection=new DatabaseConnection($settings->database_type);
			$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
			$databaseConnection->select_database($settings->database_name);
			
			//build Select box with Child Classes
			$query="SELECT child FROM classhierarchy WHERE father='$class'";
			$res=$databaseConnection->query($query);
			$childClasses='';
			while ($result=$databaseConnection->nextEntry($res)){
				$query="SELECT label FROM categories WHERE category='".$result['child']."' LIMIT 1";
				$res2=$databaseConnection->query($query);
				$result2=$databaseConnection->nextEntry($res2);
				$identify=getLabel($result['child'],$result2['label']);
				if (strlen($identify)>100) $identify=substr($identify,0,100);
				$childClasses.='<option value="'.$result['child'].'">'.utf8_to_html($identify).'</option>';
			}
			if (strlen($childClasses)>0)
				$childClasses='<select size="1" style="width:500px" id="childSelect">'.$childClasses.'</select>';
			

			//build Select box with Father Classes
			$query="SELECT father FROM classhierarchy WHERE child='$class'";
			$res=$databaseConnection->query($query);
			$fatherClasses='';
			while ($result=$databaseConnection->nextEntry($res)){
				$query="SELECT label FROM categories WHERE category='".$result['father']."' LIMIT 1";
				$res2=$databaseConnection->query($query);
				$result2=$databaseConnection->nextEntry($res2);
				$identify=getLabel($result['father'],$result2['label']);
				if (strlen($identify)>100) $identify=substr($identify,0,100);
				$fatherClasses.='<option value="'.$result['father'].'">'.utf8_to_html($identify).'</option>';
			}
			if (strlen($fatherClasses)>0)
				$fatherClasses='<select size="1" style="width:500px" id="fatherSelect">'.$fatherClasses.'</select>';	
			
			//build Title
			$query="SELECT label FROM categories WHERE category='$class' LIMIT 1";
			$res=$databaseConnection->query($query);
			$result=$databaseConnection->nextEntry($res);
			$title=getLabel($class,$result['label']);
				
			$content.=getClassView($fatherClasses,$childClasses,$title,$class);
			
			//Restart the Session
			session_start();
			
			//store class in session, to navigate between last 5 classes quickly
			$contentArray=array('content' => $content,'title' => $title,'uri' => $class);
			if (!isset($_SESSION['nextClass'])){
				$_SESSION['nextClass']=0;
				$_SESSION['classes']=array();
			}
			if ($_SESSION['nextClass']==5) $_SESSION['nextClass']=0;
			$_SESSION['classes'][$_SESSION['nextClass']]=$contentArray;
			$_SESSION['currentClass']=$_SESSION['nextClass'];
			$_SESSION['nextClass']++;									
		} catch (Exception $e)
		{
			$content="An error occured while trying to get Class information. Please try again later.";
			$title="Class not found";
		}
	}
	else {
		session_start();
		//Article is in session
		$content=$_SESSION['classes'][$fromCache]['content'];
		$title=$_SESSION['classes'][$fromCache]['title'];
	}
	
	//Build lastClasses
	if (isset($_SESSION['classes'])){
		foreach ($_SESSION['classes'] as $key => $value)
		{
			$lastClasses.="<a href=\"\" onclick=\"get_class('class=".$value['uri']."&cache=".$key."');return false;\">".$value['title']."</a><br/>";
		}
	}
	
	print $content;
	print '$$$';
	print "Class: ".$title;
	print '$$$';
	print $lastClasses;
?>