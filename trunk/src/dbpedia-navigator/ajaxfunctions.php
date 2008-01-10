<?php
ini_set('max_execution_time',200);
$sid = $_GET['sid'];
session_id($sid);
session_start();

require("ajax.php");
$xajax->processRequest();

function getsubjects($label)
{
	require_once("Settings.php");
	require_once("DLLearnerConnection.php");
	$settings=new Settings();
	$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
	
	$content="";
	$subjects=$sc->getSubjects($settings->sparqlttl,$label);
	if (count($subjects)==1)
	{
		if (strpos($subjects,"[Error]")===0) $content.=substr($subjects,7);
		else $content.="<a href=\"\" onclick=\"xajax_getarticle('".$subjects."',-1);return false;\">".str_replace("_"," ",urldecode(substr (strrchr ($subjects, "/"), 1)))."</a><br/>";
	}
	else if (count($subjects)==0) $content.="No search result found in time.";
	else{
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."',-1);return false;\">".str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1)))."</a><br/>";
		}
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchcontent", "innerHTML", $content);
	return $objResponse;
}

function getarticle($subject,$fromCache)
{
	if (isset($_SESSION['articles']))
		foreach ($_SESSION['articles'] as $key => $value)
		{
			if ($value['subject']==$subject){
				$fromCache=$key;
				break;
			}
		}
	if ($fromCache==-1) {
		require_once("Settings.php");
		require_once("DLLearnerConnection.php");
		$settings=new Settings();
		$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
		$triples=$sc->getTriples($settings->sparqlttl,$subject);
		$content="";
		if (count($triples)==1)
		{
			$content.=substr($triples,7);
		}
		else if (count($triples)==0) 
			$content.="Did not find an article with that name.";
		else {
		
			// goal: display the data in a nice (DBpedia specific way), maybe similar to
			// dbpedia.org/search
		
			$content="";
			// replace by label(?)
			$subject_nice = str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1)));
			
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
			
			// filter out uninteresting properties
			// unset
			
			// display the remaining properties as list which can be used for further navigation
			
			$content .= '<br/><br/><br/><br/><br/><br/>'.get_triple_table($triples);
			
			// $contentbuttons="<input type=\"button\" value=\"Positive\" class=\"button\" onclick=\"xajax_addPositive('".$subject."');return false;\" />&nbsp;<input type=\"button\" value=\"Negative\" class=\"button\" onclick=\"xajax_addNegative('".$subject."');return false;\" />";
			$contentbuttons='<img src="images/green-plus.png" alt="positive example" onclick="xajax_addPositive(\''.$subject.'\')" /> &nbsp;<img src="images/red-minus.png" alt="negative example" onclick="xajax_addNegative(\''.$subject.'\') />';		
		}
		
		//store article in session, to navigate between last 5 articles quickly
		$contentArray=array('content' => $content,'contentbuttons' => $contentbuttons, 'subject' => $subject);
		if (!isset($_SESSION['nextArticle'])){
			$_SESSION['nextArticle']=0;
			$_SESSION['articles']=array();
		}
		if ($_SESSION['nextArticle']==5) $_SESSION['nextArticle']=0;
		$_SESSION['articles'][$_SESSION['nextArticle']]=$contentArray;
		$_SESSION['currentArticle']=$_SESSION['nextArticle'];
		$_SESSION['nextArticle']++;
	}
	else {
		$content=$_SESSION['articles'][$fromCache]['content'];
		$contentbuttons=$_SESSION['articles'][$fromCache]['contentbuttons'];
		$subject=$_SESSION['articles'][$fromCache]['subject'];
	}
	
	$lastArticles="";
	foreach ($_SESSION['articles'] as $key => $value)
	{
		$lastArticles.="<a href=\"\" onclick=\"xajax_getarticle('',".$key.");return false;\">".str_replace("_"," ",urldecode(substr (strrchr ($value['subject'], "/"), 1)))."</a><br/>";
	}
	
	//build the response
	$objResponse = new xajaxResponse();
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("contentbuttons", "innerHTML", $contentbuttons);
	$objResponse->assign("ArticleTitle","innerHTML",str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1))));
	$objResponse->assign("lastarticles","innerHTML",$lastArticles);
	return $objResponse;
}

function addPositive($subject)
{
	if (!isset($_SESSION['positive'])){
		$array=array($subject);
		$_SESSION['positive']=$array;
	}
	else{
		$array=$_SESSION['positive'];
		$array[]=$subject;
		$_SESSION['positive']=$array;
	}
	
	$content=str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1)))."<br/>";
	
	$objResponse = new xajaxResponse();
	$objResponse->append("Positives", "innerHTML", $content);
	return $objResponse;
}

function addNegative($subject)
{
	if (!isset($_SESSION['negative'])){
		$array=array($subject);
		$_SESSION['negative']=$array;
	}
	else{
		$array=$_SESSION['negative'];
		$array[]=$subject;
		$_SESSION['negative']=$array;
	}
	
	$content=str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1)))."<br/>";
	
	$objResponse = new xajaxResponse();
	$objResponse->append("Negatives", "innerHTML", $content);
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

function learnConcept()
{
	if (isset($_SESSION['positive']))
	{
		require_once("Settings.php");
		require_once("DLLearnerConnection.php");
		$settings=new Settings();
		$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
		
		$concept=$sc->getConceptFromExamples($settings->sparqlttl,$_SESSION['positive'],$_SESSION['negative']);
		$_SESSION['lastLearnedConcept']=$concept;
		if (strlen(substr (strrchr ($concept, "/"), 1))>0) $concept=urldecode(substr (strrchr ($concept, "/"), 1));
	}
	else $concept="You must choose at least one<br/> positive example.";
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptcontent", "innerHTML", $concept);
	return $objResponse;
}

function getSubjectsFromConcept()
{
	require_once("Settings.php");
	require_once("DLLearnerConnection.php");
	$settings=new Settings();
	$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
	
	$content="";
	if (isset($_SESSION['lastLearnedConcept']))
	{
		$subjects=$sc->getSubjectsFromConcept($settings->sparqlttl,$_SESSION['lastLearnedConcept']);
		if (count($subjects)==1)
		{
			if (strpos($subjects,"[Error]")===0) $content.=substr($subjects,7);
			else $content.="<a href=\"\" onclick=\"xajax_getarticle('".$subjects."');return false;\">".str_replace("_"," ",urldecode(substr (strrchr ($subjects, "/"), 1)))."</a><br/>";
		}
		else if (count($subjects)==0) $content.="No examples for concept found in time.";
		else {
			foreach ($subjects as $subject)
			{
				$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."');return false;\">".str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1)))."</a><br/>";
			}
		}
	}
	else $content.="No concept to get Subjects from.";
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptsubjectcontent", "innerHTML", $content);
	return $objResponse;
}

function searchAndShowArticle($keyword)
{
	require_once("Settings.php");
	require_once("DLLearnerConnection.php");
	$settings=new Settings();
	
	$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
	
	$content="";
	
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_getarticle', "http://dbpedia.org/resource/".str_replace(" ","_",$keyword),-1);
	$objResponse->call('xajax_getsubjects',$keyword);
	return $objResponse;
}

// helper function
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

?>