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
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("article", "innerHTML", $content);
	return $objResponse;
}

require("ajax.php");
$xajax->processRequest();
?>