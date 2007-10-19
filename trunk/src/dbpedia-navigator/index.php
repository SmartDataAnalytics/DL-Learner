<?php

@session_start();
include_once("Settings.php");
include_once("SparqlConnection.php");

 ini_set('error_reporting',E_ALL);
 ini_set('max_execution_time',200);

$content="";
$search="";
$examples="";
$positiveAdd="";
$negativeAdd="";

$settings=new Settings();

echo "<a href='index.php?clearsession=clear'>start from scratch</a>";
if(isset($_GET['clearsession'])){
	unset($_SESSION['positive']);
	unset($_SESSION['negative']);
}

$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);

//add positives or negatives to session
if (@$_GET['action']=='AddPositive'){
	$_GET=$_SESSION["lastGet"];
	if (!isset($_SESSION['positive'])){
		$array=array($_GET['subject']);
		$_SESSION['positive']=$array;
	}
	else{
		$array=$_SESSION['positive'];
		$array[]=$_GET['subject'];
		$_SESSION['positive']=$array;
	}
}
if (@$_GET['action']=='AddNegative'){
	$_GET=$_SESSION["lastGet"];
	if (!isset($_SESSION['negative'])){
		$array=array($_GET['subject']);
		$_SESSION['negative']=$array;
	}
	else{
		$array=$_SESSION['negative'];
		$array[]=$_GET['subject'];
		$_SESSION['negative']=$array;
	}
}
if (@$_GET['action']=='ClearPositive'){
	$_GET=$_SESSION["lastGet"];
	unset($_SESSION['positive']);
}
if (@$_GET['action']=='ClearNegative'){
	$_GET=$_SESSION["lastGet"];
	unset($_SESSION['negative']);
}

//learn a concept
if (@$_GET['action']=="GetConcept")
{
	$_GET=$_SESSION["lastGet"];
	$conc=$sc->getConceptFromExamples($_SESSION['positive'],$_SESSION['negative']);
	$_SESSION['learnedConcept']=$conc;
}
if (isset($_SESSION['learnedConcept'])) $learnedConcept="<br/>Last learned Concept: ".$_SESSION['learnedConcept'];
else $learnedConcept="";

//SearchBox
	$search="<form action=\"index.php\" method=\"GET\">\n".
			"<table border=\"0\">\n".
			"<tr><tb>Search:<br/></tb></tr>\n".
			"<tr><tb><input type=\"textfield\" name=\"search\"><select name=\"limit\" size=\"1\">\n".
      		"<option>1</option>\n".
      		"<option selected=\"selected\">5</option>\n".
      		"<option>10</option>\n".
      		"<option>15</option>\n".
      		"</select><br/></tb></tr>\n".
			"<tr><tb><input type=\"submit\" name=\"action\" value=\"Search\"><input type=\"submit\" name=\"action\" value=\"Fulltext\"></tb></tr>\n".
			"</table>\n".
			"</form>\n";

//Get Search result
	if (@$_GET['action']=='Search')
	{
		$label=$_GET['search'];
		$limit=$_GET['limit'];
		$subjects=$sc->getSubjects($label,$limit);
		foreach ($subjects as $subject){
			$content.="<a href=\"index.php?action=showPage&subject=".$subject."\">".$subject."</a><br/>\n";
		}	
	}

//Show Subject Page
	if (@$_GET['action']=="showPage")
	{
		$triples=$sc->getTriples($_GET['subject']);
		foreach ($triples as $triple){
			$content.="Subject: ".urldecode($triple[0])."<br/>Predicate: ".urldecode($triple[1])."<br/>Object: ".urldecode($triple[2])."<br/><br/>\n";
		}
		$positiveAdd="<input type=\"submit\" name=\"action\" value=\"AddPositive\">";
		$negativeAdd="<input type=\"submit\" name=\"action\" value=\"AddNegative\">";
	}
	
//add box for adding articles to positive or negative
	$examples.="<form action=\"index.php\" method=\"GET\">\n".
			   "Positive:<br/>\n";
	if (!isset($_SESSION['positive'])) $examples.="No positive example picked yet.<br/>\n";
	else{
		$pos=$_SESSION['positive'];
		foreach ($pos as $p)
		{
			$examples.=$p."<br/>\n";
		}
	}
	$examples.="<br/>".$positiveAdd."&nbsp;<input type=\"submit\" name=\"action\" value=\"ClearPositive\">\n".
			   "</form>\n";
		
	$examples.="<form action=\"index.php\" method=\"GET\">\n".
			   "<br/>Negative:<br/>\n";
	if (!isset($_SESSION['negative'])) $examples.="No negative example picked yet.<br/>\n";
	else{
		$neg=$_SESSION['negative'];
		foreach ($neg as $n)
		{
			$examples.=$n."<br/>\n";
		}
	}
	$examples.="<br/>".$negativeAdd."&nbsp;<input type=\"submit\" name=\"action\" value=\"ClearNegative\">\n".
			   "</form>\n";	

//fill concept box
	$concept="<form action=\"index.php\" method=\"GET\">\n".
			 "<input type=\"submit\" name=\"action\" value=\"GetConcept\">\n".
			 "</form>\n";
	$concept.=$learnedConcept;
	
//include master
	$_SESSION['lastGet']=$_GET;
	include("master.php");
	
	echo $masterContent;

	
	//make a box
	function makeBox($title,$content,$toggleBoxContent=true)
	{
		if($toggleBoxContent)
		{
			$click="<a class=\"title_switch\" onclick=\"toggleBoxContent(this);\">�</a>";
		}
		else{$click="";}
		$ret="
		<div class=\"box\" id=\"ontology\">
		 <div class=\"boxtitle\">".$title.$click."</div>
		  <div class=\"boxcontent\">
			".$content."
		  </div> <!-- boxcontent -->
		</div> <!-- box -->";
		return $ret;
	}
?>