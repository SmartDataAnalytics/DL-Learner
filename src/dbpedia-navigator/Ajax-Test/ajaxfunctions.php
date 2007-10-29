<?php
session_start();
function getsubjects($label, $limit)
{
	include_once("Settings.php");
	require_once("SparqlConnection.php");
	$settings=new Settings();
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
	
	$content="";
	$subjects=$sc->getSubjects($label,$limit);
	foreach ($subjects as $subject)
	{
		$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."');return false;\">".urldecode(substr (strrchr ($subject, "/"), 1))."</a><br/>";
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
	$triples=$sc->getTriples($subject);
	$content="";
	foreach ($triples as $triple){
		$content.="Subject: ".urldecode($triple[0])."<br/>Predicate: ".urldecode($triple[1])."<br/>Object: ".urldecode($triple[2])."<br/><br/>\n";
	}
	
	$contentbuttons="<input type=\"button\" value=\"Positive\" class=\"button\" onclick=\"xajax_addPositive('".$subject."');return false;\" />&nbsp;<input type=\"button\" value=\"Negative\" class=\"button\" onclick=\"xajax_addNegative('".$subject."');return false;\" />";
	
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
		
		$concept=$sc->getConceptFromExamples($_SESSION['positive'],$_SESSION['negative']);
		$_SESSION['lastLearnedConcept']=$concept;
		$concept=urldecode(substr (strrchr ($concept, "/"), 1));
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
		$subjects=$sc->getSubjectsFromConcept($_SESSION['lastLearnedConcept']);
		foreach ($subjects as $subject)
		{
			$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."');return false;\">".urldecode(substr (strrchr ($subject, "/"), 1))."</a><br/>";
		}
	}
	else $content.="No concept to get Subjects from.";
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptsubjectcontent", "innerHTML", $content);
	return $objResponse;
}

require("ajax.php");
$xajax->processRequest();
?>