<?php
ini_set('max_execution_time',200);

require("ajax.php");
$xajax->processRequest();

function getsubjects($label)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	//initialise content
	$content="";
	try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id,$ksID);
				
		$subjects=$sc->getSubjects($label);
		
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getarticle('".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."',-2);return false;\">".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."</a><br/>";
		}
	} catch (Exception $e){
		$content=$e->getMessage();
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchcontent", "innerHTML", $content);
	return $objResponse;
}

function getarticle($subject,$fromCache)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	if (isset($_SESSION['articles'])) $articles=$_SESSION['articles'];
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	setRunning($id,"true");
	
	//get first Letter of label big
	$subject=ucfirst($subject);
	
	//if article is in session, get it out of the session
	if (isset($articles)){
		foreach ($articles as $key => $value)
		{
			if ($value['subject']==$subject){
				$fromCache=$key;
				break;
			}
		}
	}
	
	//initialize the content variables
	$content="";
	$searchResult="";
	$lastArticles="";
	$artTitle="";
	
	$objResponse = new xajaxResponse();
	
	//get the article
	//if $fromCache is -2, no new SearchResults should be processed
	//if $fromCache is -1, everything is normal
	//if $fromCache is >=0, the article is taken out of the cache
	if ($fromCache<0) {
		//if there are errors see catch block
		try{
			require_once("DLLearnerConnection.php");
			$sc=new DLLearnerConnection($id,$ksID);
			$triples=$sc->getTriples($subject);
			
			//BUILD ARTICLE			
			// goal: display the data in a nice (DBpedia specific way), maybe similar to
			// dbpedia.org/search
			
			// display a picture if there is one
			if(isset($triples['http://xmlns.com/foaf/0.1/depiction']))
				$content.='<img src="'.$triples['http://xmlns.com/foaf/0.1/depiction'][0].'" alt="Picture of '.$subject.'" style="float:right; max-width:200px;" \>';
					
			// add short description in english
			$content.="<h4>Short Description</h4><p>".urldecode($triples['http://dbpedia.org/property/abstract'][0])."</p>";
				
			// give the link to the corresponding Wikipedia article
			if(isset($triples['http://xmlns.com/foaf/0.1/page']))
				$content .= '<p><img src="'.$_GET['path'].'images/wikipedia_favicon.png" alt"Wikipedia" /> <a href="'.$triples['http://xmlns.com/foaf/0.1/page'][0].'">view Wikipedia article</a>, '; 
			$content .= '<a href="'.$subject.'">view DBpedia resource description</a></p>';
	
			// display a list of classes
			if(isset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']))
				$content .= '<p>classes: '.formatClassArray($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']).'</p>';
				
			if(isset($triples['http://dbpedia.org/property/reference'])) {
				$content .= '<p>references: <ul>';
				foreach($triples['http://dbpedia.org/property/reference'] as $reference)
					$content .= '<li><a href="'.$reference.'">'.$reference.'</a></li>';
				$content .= '</ul></p>';
			}
			
			
			// filter out uninteresting properties and properties which
			// have already been displayed
			unset($triples['http://xmlns.com/foaf/0.1/page']);
			unset($triples['http://xmlns.com/foaf/0.1/depiction']);
			unset($triples['http://dbpedia.org/property/abstract']);
			unset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']);
			
			// display the remaining properties as list which can be used for further navigation
				
			$content .= '<br/><br/><br/><br/><br/><br/>'.get_triple_table($triples);
			
			//BUILD SEARCHRESULT
			if ($fromCache==-1) 
				$searchResult.="<a href=\"\" onclick=\"xajax_getsubjects('".$subject."');return false;\">Show more Results</a>";
			
			//BUILD ARTICLE TITLE
			$artTitle=$triples['http://www.w3.org/2000/01/rdf-schema#label'][0];
			
			//Restart the Session
			session_start();
			
			//store article in session, to navigate between last 5 articles quickly
			$contentArray=array('content' => $content,'subject' => $artTitle);
			if (!isset($_SESSION['nextArticle'])){
				$_SESSION['nextArticle']=0;
				$_SESSION['articles']=array();
			}
			if ($_SESSION['nextArticle']==5) $_SESSION['nextArticle']=0;
			$_SESSION['articles'][$_SESSION['nextArticle']]=$contentArray;
			$_SESSION['currentArticle']=$_SESSION['nextArticle'];
			$_SESSION['nextArticle']++;
				
			//Add Positives to Session
			if (!isset($_SESSION['positive'])){
				if (isset($triples['http://dbpedia.org/property/redirect'])){
					$array=array($triples['http://dbpedia.org/property/redirect'][0] => $triples['http://dbpedia.org/property/redirect'][0]);
				}
				else $array=array("http://dbpedia.org/resource/".str_replace(" ","_",$subject) => "http://dbpedia.org/resource/".str_replace(" ","_",$subject));
				$_SESSION['positive']=$array;
			}
			else{
				$array=$_SESSION['positive'];
				if (isset($triples['http://dbpedia.org/property/redirect'])) $array[$triples['http://dbpedia.org/property/redirect'][0]] = $triples['http://dbpedia.org/property/redirect'][0];
				else $array["http://dbpedia.org/resource/".str_replace(" ","_",$subject)]="http://dbpedia.org/resource/".str_replace(" ","_",$subject);
				$_SESSION['positive']=$array;
			}
									
		} catch (Exception $e)
		{
			$content=$e->getMessage();
			$artTitle="No Result";
			$objResponse->call('xajax_getsubjects',$subject);
		}
	}
	else {
		session_start();
		//Article is in session
		$content=$_SESSION['articles'][$fromCache]['content'];
		$artTitle=$_SESSION['articles'][$fromCache]['subject'];
	}
	
	//Build lastArticles
	if (isset($_SESSION['articles'])){
		foreach ($_SESSION['articles'] as $key => $value)
		{
			$lastArticles.="<a href=\"\" onclick=\"xajax_getarticle('',".$key.");return false;\">".$value['subject']."</a><br/>";
		}
	}
	
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("ArticleTitle","innerHTML",$artTitle);
	$objResponse->assign("lastarticles","innerHTML",$lastArticles);
	$objResponse->assign("searchcontent", "innerHTML", $searchResult);
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);	
	return $objResponse;
}

function toPositive($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['negative'][$subject]);
	if (!isset($_SESSION['positive'])){
		$array=array($subject => $subject);
		$_SESSION['positive']=$array;
	}
	else{
		$array=$_SESSION['positive'];
		$array[$subject]=$subject;
		$_SESSION['positive']=$array;
	}
	
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function toNegative($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['positive'][$subject]);
	if (!isset($_SESSION['negative'])){
		$array=array($subject => $subject);
		$_SESSION['negative']=$array;
	}
	else{
		$array=$_SESSION['negative'];
		$array[$subject]=$subject;
		$_SESSION['negative']=$array;
	}
	
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function clearPositives()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['positive']);
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("Positives", "innerHTML", "");
	return $objResponse;
}

function clearNegatives()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['negative']);
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("Negatives", "innerHTML", "");
	return $objResponse;
}

function removePosInterest($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['positive'][$subject]);
		
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showInterests');
	return $objResponse;
}

function removeNegInterest($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['negative'][$subject]);
		
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showInterests');
	return $objResponse;
}

function learnConcept()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
		
	$positives=$_SESSION['positive'];
	$negatives=$_SESSION['negative'];
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	setRunning($id,"true");
	$concept="";
	if (isset($positives))
	{
		$posArray=array();
		foreach ($positives as $pos)
			$posArray[]=$pos;
		$negArray=array();
		if (isset($negatives))
			foreach ($negatives as $neg)
				$negArray[]=$neg;
			
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id, $ksID);
		try{
			$concepts=$sc->getConceptFromExamples($posArray,$negArray);
			
			$concept.="<table border=0>\n";
			foreach ($concepts as $con){
				$concept.="<tr><td><a href=\"\" onclick=\"xajax_getSubjectsFromConcept('".$con."');return false;\" />".$con."</a></td></tr>";
			}
			$concept.="</table>";
		} catch(Exception $e){
			$concept.=$e->getMessage();
		}
	}
	else $concept="You must choose at least one positive example.";
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptlink", "innerHTML", $concept);
	return $objResponse;
}

function getSubjectsFromConcept($concept)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	$content="";
	try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id,$ksID);
		$subjects=$sc->getSubjectsFromConcept($concept);
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getarticle('".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."',-2);return false;\">".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."</a><br/>";
		}
	} catch (Exception $e){
		$content=$e->getMessage();
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchcontent", "innerHTML", $content);
	return $objResponse;
}

function stopServerCall()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	$id=$_SESSION['id'];
	session_write_close();
	setRunning($id,"false");
	$objResponse=new xajaxResponse();
	return $objResponse;
}

///////////////////////
// Helper Functions. //
///////////////////////

function setRunning($id,$running)
{
	if(!is_dir("temp")) mkdir("temp");
	$file=fopen("./temp/".$id.".temp","w");
	fwrite($file, $running);
	fclose($file);
}

function get_triple_table($triples) {

	$table = '<table border="1"><tr><td>predicate</td><td>object</td></tr>';
	foreach($triples as $predicate=>$object) {
		$table .= '<tr><td>'.$predicate.'</td>';
		$table .= '<td><ul>';
		foreach($object as $element) {
			$table .= '<li>'.$element.'</li>';
		}
		$table .= '</ul></td>';
	}
	$table .= '</table>';
	return $table;
}

function formatClassArray($ar) {
	$string = formatClass($ar[0]);
	for($i=1; $i<count($ar); $i++) {
		$string .= ', ' . formatClass($ar[$i]);
	}
	return $string;
}

// format a class nicely, i.e. link to it and possibly display
// it in a better way
function formatClass($className) {
	$yagoPrefix = 'http://dbpedia.org/class/yago/';
	if(substr($className,0,30)==$yagoPrefix) {
		return '<a href="'.$className.'">'.substr($className,30).'</a>';	
	// DBpedia is Linked Data, so it makes always sense to link it
	// ToDo: instead of linking to other pages, the resource should better
	// be openened within DBpedia Navigator
	} else if(substr($className,0,14)=='http://dbpedia') {
		return '<a href="'.$className.'">'.$className.'</a>';
	} else {
		return $className;
	}
}

function arrayToCommaSseparatedList($ar) {
	$string = $ar[0];
	for($i=1; $i<count($ar); $i++) {
		$string .= ', ' . $ar[$i];
	}
	return $string;
}

?>