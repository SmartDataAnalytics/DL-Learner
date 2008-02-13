<?php

ini_set('error_reporting',E_ALL);
ini_set('max_execution_time',200);
ini_set("soap.wsdl_cache_enabled","1");

session_start();
require_once('DLLearnerConnection.php');
$sc=new DLLearnerConnection();
$ids=$sc->getIDs();
$_SESSION['id']=$ids[0];
$_SESSION['ksID']=$ids[1];

if (isset($_GET['path'])) $path=$_GET['path'];
else $path="";

// debugging code
// echo '<pre>';
// $sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri,$_SESSION['id'],$_SESSION['ksID']);
// print_r($sc->getTriples($settings->sparqlttl,'http://dbpedia.org/resource/Dog'));
// echo '</pre>';

require("ajax.php"); 
  
echo '<?xml version="1.0" encoding="UTF-8"?>';
?>
<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>DBpedia Navigator</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="<?php print $path;?>default.css"/>
    <?php $xajax->printJavascript($path.'xajax/'); ?>
	<script type="text/javascript">
        showLoading = function() {
            xajax.$('Loading').style.display='inline';
        };
        hideLoading = function() {
            xajax.$('Loading').style.display = 'none';
            xajax.$('SearchResultBox').style.display = 'block';
            xajax.$('LastArticlesBox').style.display = 'block';
        };
        
        function showdiv(id){
        	document.getElementById(id).style.display='block';
        }
        
        function hidediv(id) {
        	document.getElementById(id).style.display='none';
        } 
  </script>
  </head>
  <body <?php if (isset($_GET['resource'])) print "onLoad=\"xajax_getarticle('".$_GET['resource']."',-1);return false;\"";unset($_GET['resource']);?>>

<!--  <h1>DBpedia Navigator</h1> -->
<div><table border="0" width="100%"><tr><td width="35%"><img src="<?php print $path;?>images/dbpedia_navigator.png" alt="DBpedia Navigator" style="padding:5px" /></td><td width="50%"><span id="conceptlink"></span></td><td width="15%"><span id="Loading" style="display:none">Server Call... <img src="<?php print $path;?>images/remove.png" onclick="xajax_stopServerCall();return false;" /></span></td></tr></table></div>
<div id="layer" style="display:none">
	<div id="layerContent" style="display:none"></div>
</div>

<div id="wrapper">
	<div id="leftSidebar">

		<div class="box">
		  <div class="boxtitle">Search DBpedia</div>
		  <div class="boxcontent" id="search">
			<!-- Search:<br/> -->
			<form onSubmit="xajax_getarticle(document.getElementById('label').value,-1);return false;">
			<input type="text" name="label" id="label" /><br/>
			<input type="button" value="Search" class="button" onclick="xajax_getarticle(document.getElementById('label').value,-1);return false;" />
			<!--  &nbsp;&nbsp;&nbsp; <input type="button" value="Fulltext" class="button" onclick=""/> -->
			</form>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="SearchResultBox" style="display:none">
		  <div class="boxtitle">Search Results</div>
		  <div class="boxcontent">
		  <div id="searchcontent" style="display:block"></div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="credits">
			<p>DBpedia Navigator is powered by ... <br />
			&nbsp; <a href="http://dl-learner.org">DL-Learner</a><br />
			&nbsp; <a href="http//dbpedia.org">DBpedia</a><br/>
			&nbsp; <a href="http://virtuoso.openlinksw.com/wiki/main/">OpenLink Virtuoso</a><br />
			... and implemented by <a href="http://jens-lehmann.org">Jens Lehmann</a> and
			Sebastian Knappe at	the <a href="http:/aksw.org">AKSW</a> research group (University of Leipzig).</p>
			
			<a href="http://www.w3.org/2004/OWL/"><img src="<?php print $path;?>images/sw-owl-green.png" alt="OWL logo" /></a>
			<a href="http://www.w3.org/2001/sw/DataAccess/"><img src="<?php print $path;?>images/sw-sparql-green.png" alt="SPARQL logo"/></a>
		</div>
		
		<input type="button" value="Learn" class="button" onclick="xajax_learnConcept();return false;" />	
		
	</div><!-- END leftSidebar -->

	<div id="content">
		<div class="box">
		  <div class="boxtitle" id="ArticleTitle">Welcome</div>
		  <div class="boxcontent" id="article">
		  <div id="articlecontent" style="display:block">
		  <br /><br />
		  Welcome to the DBpedia Navigator interface! DBpedia Navigator allows you to search DBpedia
		  and uses the background knowledge in DBpedia to suggest possible interesting navigation
		  links. 
		  </div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->
	</div><!-- content -->
	
	<div id="rightSidebar">

		<div class="box">
		  <div class="boxtitlewithbutton" id="positivesboxtitle">search relevant &nbsp; <img src="<?php print $path;?>images/remove.png" onclick="xajax_clearPositives()" /> </div>
		  <div class="boxcontent" id="Positives">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box">
		  <div class="boxtitlewithbutton" id="negativesboxtitle">not relevant &nbsp; <img src="<?php print $path;?>images/remove.png" onclick="xajax_clearNegatives()" /> </div>
		  <div class="boxcontent" id="Negatives">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="LastArticlesBox" style="display:none">
		  <div class="boxtitle">Articles Last Viewed</div>
		  <div class="boxcontent" id="lastarticles">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

	</div><!-- rightSidebar -->
	
	<!--   <div id="clear"></div> -->
	
</div><!--  wrapper -->
<div id="footer">
			<p>Licensed under the GNU General Public License (GPL) 3 as part of the DL-Learner open source
			project.<br />Copyright &copy; Jens Lehmann 2007-2008 </p>
		<div id="validation">
			<?php
			$uri = 'http://'.$_SERVER['SERVER_NAME'].$_SERVER['REQUEST_URI'];
			
			echo '<div><a href="http://validator.w3.org/check?uri='.$uri.'"';
			echo '><img src="'.$path.'images/valid-xhtml10.png" alt="valid XHTML 1.0" /></a>'."\n";
			echo '<a href="http://jigsaw.w3.org/css-validator/validator?uri='.$uri.'"';
			echo '><img src="'.$path.'images/valid-css.png" alt="valid CSS" /></a></div>'."\n";
			?>	
		</div>	
		<p><a href='<?php print $path;?>rebuild.php'>rebuild [restart session and redownload WSDL file (for debugging)]</a></p>
</div>

<div id="todo">
<b>ToDo:</b>
<ul style="float:left">
	<li>Get learning component fast.</li>
	<li>Learning as a Thread.</li>
	<li>Get local DBpedia SPARQL endpoint working (next DBpedia release expected at the endof January and then every
	two months, so it would be nice to have a script based partly automated or at least documented solution for
	creating a DBpedia mirror).</li>
	<li>Improve stability: Fix sometimes occurring PHP errors and warnings (check PHP error log).</li>
	<li>Automatically learn concepts whenever an example has been added (and there is at least one
	positive example present).</li>
	<li>For each result, display a "+" which shows more information about the concept in an overlay box, e.g. its 
	Description Logic or OWL syntax, its classification accuracy on the examples, and which
	examples it classifies (in-)correctly.</li>
	<li>Move the "Learned Concepts" box above the main box in the center.</li>
	<li>Remove the "Subjects from Concept" box and instead change the learned concepts to links (clicking
	on a link shows instances of the concept).</li>
	<li>Create a small number of test cases (e.g. 3), which can be used to verify that DBpedia Navigator is 
	working in typical scenarios (in particular cases where concepts with length greater one are learned).</li>
	<li>Display "server call" in progress (or "n server calls in progress") in the top right corner 
	of the screen whenever AJAX queries are executed.</li>
	<li>Allow to disable caching functionality (in Settings.php).</li>
	<li>Fix the rebuild.php script such that PHP replaces the cached WSDL file by the new one.</li>
	<li>Make DBpedia Navigator RESTful, e.g. URLs $base/showArticle/$URL for displaying an article;
	$base/search/$phrase for searching; $base/listInstances/$complexClass for listing the instances of
	a learned. Maybe session variables (in particuar the selected positive and negative examples) can 
	also be given, e.g. $base/search/$phrase?positives=[$URL1,$URL2,$URL3]&negatives=[$URL4]. The supported
	URI design should be briefly documented (e.g. on a dbpedia.org wiki page). A good URI design allows
	easier external access (just give someone a link instead of saying exactly which actions have to be done to
	get to a state), simplifies debugging the application, and may be of use for creating further
	features.</li> 
	<li>Improve search functionality [we will probably get feedback from Georgi in February].</li>
	<li>[maybe] Display a tag cloud similar to <a href="http://dbpedia.org/search/">DBpedia search</a>.</li>
	<li>Get a nice DBpedia Navigator logo (preferrably in SVG format) [currently in contact with Matt, but not
	sure he has time to help].</li>
	<li>[maybe] Instead of only allowing a search as entry point to the application, also display
	a navigatable class tree.</li>
	<li>[if possible] When expensive SPARQL queries or learning problems have been posed, there should be
	some way to abandon these if the user has already switched to doing something else. Example: The user
	has added 3 positive and 1 negative examples. This is executed as a learning problem, but has no solution (so
	DL-Learner would run forever unless we pose some internal time limit). The user adds another negative example a 
	second later, so instead of letting the previous learning problem run for a long time (and needing much resources),
	it should be stopped by DBpedia Navigator.</li>
	<li>[if possible] Find an easy way to validate HTML/JS in AJAX applications.</li>
	<li>[maybe] Would be interesting to somehow view the Wikipedia article (without the left navigation part,
	tabs etc.) as an overlay, because the Wikipedia article will almost always be a human-friendlier
	description of an object compared to the extracted one.</li>
	<li>Handle redirect for example if you look for 'Deutschland'</li>
	<li>Ich habe mir gestern deswegen mal angeschaut welche fertigen SPARQL-APIs es gibt. Eine der verbreitesten und deshalb auch aktiven Projekt ist Jena. F�r uns relevant ist das ARQ-Teilprojekt: http://jena.sourceforge.net/ARQ/.
	</li> 
</ul>
</div>

  <div class="box" id="ConceptBox" style="position:absolute;top:0px;right:20px;opacity:0.90;display:none;z-index:5;">
  	<div class="boxtitle">Detailed Concept Information</div>
  	<div class="boxcontent" id="ConceptInformation"></div>
  </div>

  </body>
</html>
			