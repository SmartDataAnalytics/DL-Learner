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
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
	
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
		require_once("SparqlConnection.php");
		$settings=new Settings();
		$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
		$triples=$sc->getTriples($settings->sparqlttl,$subject);
		$content="";
		if (count($triples)==1)
		{
			$content.=substr($triples,7);
		}
		else if (count($triples)==0) $content.="Article not found.";
		else {
			$content="";
			$content.="<img src=\"".$triples['http://xmlns.com/foaf/0.1/depiction']."\" alt=\"Picture of ".str_replace("_"," ",urldecode(substr (strrchr ($subject, "/"), 1)))."\" width=\"50\"/ style=\"float:left\">";
			$content.="<div>".urldecode($triples['http://dbpedia.org/property/abstract'])."</div>";
			
			$contentbuttons="<input type=\"button\" value=\"Positive\" class=\"button\" onclick=\"xajax_addPositive('".$subject."');return false;\" />&nbsp;<input type=\"button\" value=\"Negative\" class=\"button\" onclick=\"xajax_addNegative('".$subject."');return false;\" />";		
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
	if (isset($_SESSION['positive'])&&isset($_SESSION['negative']))
	{
		require_once("Settings.php");
		require_once("SparqlConnection.php");
		$settings=new Settings();
		$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
		
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
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
	
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
	require_once("SparqlConnection.php");
	$settings=new Settings();
	
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
	
	$content="";
	
	$objResponse = new xajaxResponse();
	$objResponse->call('xajax_getarticle', "http://dbpedia.org/resource/".str_replace(" ","_",$keyword),-1);
	$objResponse->call('xajax_getsubjects',$keyword);
	return $objResponse;
}

?>