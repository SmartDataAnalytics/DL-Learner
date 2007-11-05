<?php
ini_set('max_execution_time',200);
$sid = $_GET['sid'];
session_id($sid);
session_start();

require("ajax.php");
$xajax->processRequest();

function getsubjects($label, $limit)
{
	include_once("Settings.php");
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
	
	$content="";
	$subjects=$sc->getSubjects($settings->sparqlttl,$label,$limit);
	if (count($subjects)==1)
	{
		if (strpos($subjects,"[Error]")===0) $content.=substr($subjects,7);
		else $content.="<a href=\"\" onclick=\"xajax_getarticle('".$subjects."');return false;\">".urldecode(substr (strrchr ($subjects, "/"), 1))."</a><br/>";
	}
	else if (count($subjects)==0) $content.="No search result found in time.";
	else{
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."');return false;\">".urldecode(substr (strrchr ($subject, "/"), 1))."</a><br/>";
		}
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("searchcontent", "innerHTML", $content);
	return $objResponse;
}

function getarticle($subject)
{
	include_once("Settings.php");
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
	$triples=$sc->getTriples($settings->sparqlttl,$subject);
	$content="";
	if (count($triples)==1)
	{
		$content.=substr($triples,7);
	}
	else if (count($triples)==0) $content.="Article not found.";
	else {
		$content="";
		$content.="<img src=\"".$triples['http://xmlns.com/foaf/0.1/depiction']."\" alt=\"Picture of ".urldecode(substr (strrchr ($subject, "/"), 1))."\" width=\"50\"/ style=\"float:left\">";
		$content.="<div>".urldecode($triples['http://dbpedia.org/property/abstract'])."</div>";
		
		$contentbuttons="<input type=\"button\" value=\"Positive\" class=\"button\" onclick=\"xajax_addPositive('".$subject."');return false;\" />&nbsp;<input type=\"button\" value=\"Negative\" class=\"button\" onclick=\"xajax_addNegative('".$subject."');return false;\" />";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("contentbuttons", "innerHTML", $contentbuttons);
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
	
	$content=urldecode(substr (strrchr ($subject, "/"), 1))."<br/>";
	
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
	
	$content=urldecode(substr (strrchr ($subject, "/"), 1))."<br/>";
	
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
	if (isset($_SESSION['positive'])&&isset($_SESSION['negative']))
	{
		require_once("Settings.php");
		require_once("SparqlConnection.php");
		$settings=new Settings();
		$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
		
		$concept=$sc->getConceptFromExamples($settings->sparqlttl,$_SESSION['positive'],$_SESSION['negative']);
		$_SESSION['lastLearnedConcept']=$concept;
		if (strlen(substr (strrchr ($concept, "/"), 1))>0) $concept=urldecode(substr (strrchr ($concept, "/"), 1));
	}
	else $concept="You must choose at least one<br/> positive and one negative example.";
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptcontent", "innerHTML", $concept);
	return $objResponse;
}

function getSubjectsFromConcept()
{
	require_once("Settings.php");
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
	
	$content="";
	if (isset($_SESSION['lastLearnedConcept']))
	{
		$subjects=$sc->getSubjectsFromConcept($settings->sparqlttl,$_SESSION['lastLearnedConcept']);
		if (count($subjects)==1)
		{
			if (strpos($subjects,"[Error]")===0) $content.=substr($subjects,7);
			else $content.="<a href=\"\" onclick=\"xajax_getarticle('".$subjects."');return false;\">".urldecode(substr (strrchr ($subjects, "/"), 1))."</a><br/>";
		}
		else if (count($subjects)==0) $content.="No examples for concept found in time.";
		else {
			foreach ($subjects as $subject)
			{
				$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."');return false;\">".urldecode(substr (strrchr ($subject, "/"), 1))."</a><br/>";
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
	require_once("SparqlConnection.php");
	$settings=new Settings();
	
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
	
	$content="";
	
	$sc->startSearchAndShowArticle($keyword);
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_showThisArticle', urlencode(serialize($sc)),"http://dbpedia.org/resource/".$keyword);
	$objResponse->call('xajax_showThisSearchResult', urlencode(serialize($sc)));
	return $objResponse;
}

function showThisSearchResult($sc)
{
	require_once("Settings.php");
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=unserialize(urldecode($sc));
	$objResponse = new xajaxResponse();
	$i = 1;
	$sleeptime = 1;
	$searchComplete=false;
	do {
		if (!$searchComplete) $searchResult=$sc->checkSearch(false);
		if (!is_null($searchResult)){
			$searchComplete=true;
			break;
		}
				
		$seconds = $i * $sleeptime;
		$i++;
		
		// sleep a while
		sleep($sleeptime);
	} while ($seconds<$settings->sparqlttl);
	
	if (!$searchComplete){
		$sc->checkSearch(true);
		$objResponse->assign("searchcontent","innerHtml","No search result found in time.");
	}
	
	$content="";
	if (count($searchResult)==1)
	{
		if (strpos($searchResult,"[Error]")===0) $content.=substr($searchResult,7);
		else $content.="<a href=\"\" onclick=\"xajax_getarticle('".$searchResult."');return false;\">".urldecode(substr (strrchr ($searchResult, "/"), 1))."</a><br/>";
	}
	else if (count($searchResult)==0) $content.="No search result found in time.";
	else{
		foreach ($searchResult as $result)
		{
			$content.="<a href=\"\" onclick=\"xajax_getarticle('".$result."');return false;\">".urldecode(substr (strrchr ($result, "/"), 1))."</a><br/>";
		}
	}
	
	
	$objResponse->assign("searchcontent", "innerHTML", $content);
	return $objResponse;
}

function showThisArticle($sc,$subject)
{
	require_once("Settings.php");
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=unserialize(urldecode($sc));
	$i = 1;
	$sleeptime = 1;
	$showArticleComplete=false;
	do {
		if (!$showArticleComplete) $article=$sc->checkShowArticle(false);
		if (!is_null($article)){
			$showArticleComplete=true;
			break;
		}
			
		$seconds = $i * $sleeptime;
		$i++;
		
		// sleep a while
		sleep($sleeptime);
	} while ($seconds<$settings->sparqlttl);
	
	$content="";
	if (!$showArticleComplete){
		$sc->checkShowArticle(true);
	}
	
	
	if (count($article)==1)
	{
		$content.=substr($article,7);
	}
	else if (count($article)==0) $content.="Article not found.";
	else {
		$content="";
		$content.="<img src=\"".$article['http://xmlns.com/foaf/0.1/depiction']."\" alt=\"Picture of ".urldecode(substr (strrchr ($subject, "/"), 1))."\" width=\"50\"/ style=\"float:left\">";
		$content.="<div>".urldecode($article['http://dbpedia.org/property/abstract'])."</div>";
		
		$contentbuttons="<input type=\"button\" value=\"Positive\" class=\"button\" onclick=\"xajax_addPositive('".$subject."');return false;\" />&nbsp;<input type=\"button\" value=\"Negative\" class=\"button\" onclick=\"xajax_addNegative('".$subject."');return false;\" />";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("contentbuttons", "innerHTML", $contentbuttons);
	return $objResponse;
}

?>