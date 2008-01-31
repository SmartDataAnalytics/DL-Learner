<?php
ini_set('max_execution_time',200);
$sid = $_GET['sid'];
session_id($sid);
session_start();

require("ajax.php");
$xajax->processRequest();

function getsubjects($label)
{
	require_once("DLLearnerConnection.php");
	
	//initialise content
	$content="";
	try{
		$sc=new DLLearnerConnection($_SESSION['id'],$_SESSION['ksID']);
		
		
		$subjects=$sc->getSubjects($label);
		
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getAndShowArticle('".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."',-2);return false;\">".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."</a><br/>";
		}
	} catch (Exception $e){
		$content=$e->getMessage();
	}
	
	$_SESSION['subjects']=$content;
		
	$objResponse = new xajaxResponse();
	return $objResponse;
}

function showSubjects()
{
	while (!isset($_SESSION['subjects'])){
		sleep(0.5);
	}
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchcontent", "innerHTML", $_SESSION['subjects']);
	unset($_SESSION['subjects']);
	return $objResponse;
}

function getarticle($subject,$fromCache)
{
	//if article is in session, get it out of the session
	if (isset($_SESSION['articles'])){
		foreach ($_SESSION['articles'] as $key => $value)
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
	
	//get the article
	//if $fromCache is -2, no new SearchResults should be processed
	//if $fromCache is -1, everything is normal
	//if $fromCache is >=0, the article is taken out of the cache
	if ($fromCache<0) {
		//if there are errors see catch block
		try{
			require_once("DLLearnerConnection.php");
			$sc=new DLLearnerConnection($_SESSION['id'],$_SESSION['ksID']);
			$triples=$sc->getTriples($subject);
			
			//BUILD ARTICLE			
			// goal: display the data in a nice (DBpedia specific way), maybe similar to
			// dbpedia.org/search
			
			// display a picture if there is one
			if(isset($triples['http://xmlns.com/foaf/0.1/depiction']))
				$content.='<img src="'.$triples['http://xmlns.com/foaf/0.1/depiction'][0].'" alt="Picture of '.$subject_nice.'" style="float:right; max-width:200px;" \>';
					
			// add short description in english
			$content.="<h4>Short Description</h4><p>".urldecode($triples['http://dbpedia.org/property/abstract'][0])."</p>";
				
			// give the link to the corresponding Wikipedia article
			if(isset($triples['http://xmlns.com/foaf/0.1/page']))
				$content .= '<p><img src="images/wikipedia_favicon.png" alt"Wikipedia" /> <a href="'.$triples['http://xmlns.com/foaf/0.1/page'][0].'">view Wikipedia article</a>, '; 
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
			
			
			
			//BUILD ARTICLE TITLE
			$artTitle=$triples['http://www.w3.org/2000/01/rdf-schema#label'][0];
			
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
				$array=array("http://dbpedia.org/resource/".str_replace(" ","_",$subject) => "http://dbpedia.org/resource/".str_replace(" ","_",$subject));
				$_SESSION['positive']=$array;
			}
			else{
				$array=$_SESSION['positive'];
				$array["http://dbpedia.org/resource/".str_replace(" ","_",$subject)]="http://dbpedia.org/resource/".str_replace(" ","_",$subject);
				$_SESSION['positive']=$array;
			}
				
			
			//BUILD SEARCHRESULT
			if ($fromCache==-1) 
				$searchResult.="<a href=\"\" onclick=\"xajax_getAndShowSubjects('".$subject."');return false;\">Show more Results</a>";			
		} catch (Exception $e)
		{
			$content=$e->getMessage();
			$artTitle="Fehler";
		}
	}
	else {
		//Article is in session
		$content=$_SESSION['articles'][$fromCache]['content'];
		$artTitle=$_SESSION['articles'][$fromCache]['subject'];
	}
	
	//Build lastArticles
	if (isset($_SESSION['articles'])){
		foreach ($_SESSION['articles'] as $key => $value)
		{
			$lastArticles.="<a href=\"\" onclick=\"xajax_getAndShowArticle('',".$key.");return false;\">".$value['subject']."</a><br/>";
		}
	}
	
	//put whole site content into session
	$_SESSION['artContent']=$content;
	$_SESSION['artTitle']=$artTitle;
	$_SESSION['artLast']=$lastArticles;
	$_SESSION['artSubjects']=$searchResult;
	
	$objResponse = new xajaxResponse();
	return $objResponse;
}

function showArticle()
{
	while (!isset($_SESSION['artSubjects'])){
		sleep(0.5);
	}
	$objResponse = new xajaxResponse();
	$objResponse->assign("articlecontent", "innerHTML", $_SESSION['artContent']);
	$objResponse->assign("ArticleTitle","innerHTML",$_SESSION['artTitle']);
	$objResponse->assign("lastarticles","innerHTML",$_SESSION['artLast']);
	if ($_SESSION['artSubjects']!="") $objResponse->assign("searchcontent", "innerHTML", $_SESSION['artSubjects']);
	if (strpos($_SESSION['artContent'],"Did not find an article with that name")===0)
		$objResponse->call('xajax_getAndShowSubjects',$_SESSION['artTitle']);
	unset($_SESSION['artContent']);
	unset($_SESSION['artTitle']);
	unset($_SESSION['artLast']);
	unset($_SESSION['artSubjects']);
	
	$objResponse->call('xajax_showInterests');	
	return $objResponse;
}

function getAndShowArticle($subject,$fromCache)
{
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_getarticle',$subject,$fromCache);
	$objResponse->call('xajax_showArticle');
	return $objResponse;
}

function toPositive($subject)
{
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
	
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showInterests');
	return $objResponse;
}

function toNegative($subject)
{
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
	
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showInterests');
	return $objResponse;
}

function clearPositives()
{
	unset($_SESSION['positive']);
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("Positives", "innerHTML", "");
	return $objResponse;
}

function clearNegatives()
{
	unset($_SESSION['negative']);
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("Negatives", "innerHTML", "");
	return $objResponse;
}

function showInterests()
{
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests=$posInterests.substr (strrchr ($pos, "/"), 1)." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"images/remove.png\" alt=\"Minus\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests=$negInterests.substr (strrchr ($neg, "/"), 1)." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"images/remove.png\" alt=\"Minus\"/></a><br/>";
	}
	
	$objResponse=new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function removePosInterest($subject)
{
	unset($_SESSION['positive'][$subject]);
		
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showInterests');
	return $objResponse;
}

function removeNegInterest($subject)
{
	unset($_SESSION['negative'][$subject]);
		
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showInterests');
	return $objResponse;
}

function learnConcept()
{
	$concept="";
	if (isset($_SESSION['positive']))
	{
		$posArray=array();
		foreach ($_SESSION['positive'] as $pos)
			$posArray[]=$pos;
		$negArray=array();
		if (isset($_SESSION['negative']))
			foreach ($_SESSION['negative'] as $neg)
				$negArray[]=$neg;
			
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($_SESSION['id'],$_SESSION['ksID']);
		
		
		$concepts=$sc->getConceptFromExamples($posArray,$negArray);
		
		$_SESSION['lastLearnedConcept']=$concepts;
		$concept.="<table border=0>\n";
		foreach ($concepts as $con){
			$concept.="<tr><td><a href=\"\" onclick=\"xajax_getAndShowSubjectsFromConcept('".$con."');return false;\" />".$con."</a></td></tr>";
		}
		$concept.="</table>";
	}
	else $concept="You must choose at least one positive example.";
	
	$_SESSION['conceptcontent']=$concept;
	
	$objResponse = new xajaxResponse();
	return $objResponse;
}

function getSubjectsFromConcept($concept)
{
	$content="";
	try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($_SESSION['id'],$_SESSION['ksID']);
		$subjects=$sc->getSubjectsFromConcept($concept);
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getAndShowArticle('".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."',-2);return false;\">".urldecode(str_replace("_"," ",substr (strrchr ($subject, "/"), 1)))."</a><br/>";
		}
	} catch (Exception $e){
		$content=$e->getMessage();
	}
		
	$_SESSION['conceptsubjectcontent']=$content;
	$objResponse = new xajaxResponse();
	return $objResponse;
}

function getAndShowSubjects($keyword)
{
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_getsubjects',$keyword);
	$objResponse->call('xajax_showSubjects');
	return $objResponse;
}

function learnAndShowConcept()
{
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_learnConcept');
	$objResponse->call('xajax_showConcept');
	return $objResponse;
}

function showConcept()
{
	while (!isset($_SESSION['conceptcontent'])){
		sleep(0.5);
	}
		
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptlink", "innerHTML", $_SESSION['conceptcontent']);
	unset($_SESSION['conceptcontent']);
	return $objResponse;
}

function getAndShowSubjectsFromConcept($concept)
{
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_getSubjectsFromConcept',$concept);
	$objResponse->call('xajax_showSubjectsFromConcept');
	return $objResponse;
}

function showSubjectsFromConcept()
{
	while (!isset($_SESSION['conceptsubjectcontent'])){
		sleep(0.5);
	}
		
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchcontent", "innerHTML", $_SESSION['conceptsubjectcontent']);
	unset($_SESSION['conceptsubjectcontent']);
	return $objResponse;
}

///////////////////////
// Helper Functions. //
///////////////////////

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