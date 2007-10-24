<?php
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
		$content.="<a href=\"\" onclick=\"xajax_getarticle('".$subject."');return false;\">".urldecode($subject)."</a><br/>";
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
	
	$content=$subject."<br/>";
	
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
	
	$content=$subject."<br/>";
	
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

require("ajax.php");
$xajax->processRequest();
?>