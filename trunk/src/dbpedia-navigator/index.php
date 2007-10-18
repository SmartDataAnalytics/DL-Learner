<?php

@session_start();
include_once("Settings.php");
include_once("SparqlConnection.php");

 ini_set('error_reporting',E_ALL);
 ini_set('max_execution_time',200);

$lastAction="View";
$content="";
$possibleActions="";
$instances=" ";
$left="";
$right="";
$middle="";
$search="";
$system="";

$settings=new Settings();

echo "<a href='index.php?clearsession=clear'>start from scratch</a>";
if(isset($_GET['clearsession']))$_SESSION['State_ID'] =false;



if( (!isset($_SESSION['State_ID'] ) )	|| ($_SESSION['State_ID']==""))
{
	//get new ID
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri);
	$_SESSION['State_ID']=$sc->getID();
	
}
	
else{
	//echo "Current ID is: ".$_SESSION['Learner_ID']."<br>";
	$sc=new SparqlConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['State_ID']);
}
	
	
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
	}
	
	
	include("master.php");
	
	echo $masterContent;
	
	
	
	
	
	function makeBox($title,$content,$toggleBoxContent=true)
	{
		if($toggleBoxContent)
		{
			$click="<a class=\"title_switch\" onclick=\"toggleBoxContent(this);\">–</a>";
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


	function shorten($a)
	{
	    if(($strpos=strpos($a,'#'))>=4){
			return substr($a,$strpos);
		}
		else {return $a;}
	}

?>