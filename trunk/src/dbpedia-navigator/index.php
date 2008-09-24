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

require_once('Settings.php');
$settings=new Settings();

//what happens onLoad
$onLoad="onLoad=\"document.getElementById('label').focus();";
if (isset($_GET['positives'])||isset($_GET['negatives'])) $onLoad.="setPositivesAndNegatives('positives=".$_GET['positives']."&negatives=".$_GET['negatives']."');";
if (isset($_GET['showArticle'])) $onLoad.="get_article('label=".$_GET['showArticle']."&cache=-1');";
else if (isset($_GET['search'])) $onLoad.="search_it('label=".$_GET['search']."&number=10');";
else if (isset($_GET['showClass'])) $onLoad.="get_class('class=http://dbpedia.org/class/yago/".$_GET['showClass']."&cache=-1');";
else if (isset($_GET['searchInstances'])) $onLoad.="getSubjectsFromCategory('category=http://dbpedia.org/class/yago/".$_GET['searchInstances']."&number=10');";
else if (isset($_GET['searchConceptInstances'])) $onLoad.="getSubjectsFromConcept('kb=".htmlentities(urldecode($_GET['concept']))."&number=10');";
else if (isset($_SESSION['currentArticle'])){
	$onLoad.="get_article('label=&cache=".$_SESSION['currentArticle']."');";
}

$onLoad.="\"";

  
echo '<?xml version="1.0" encoding="UTF-8"?>';
?>
<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>DBpedia Navigator</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="css/default.css"/>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<?php print $settings->googleMapsKey;?>"
      type="text/javascript"></script>
    <script  src="js/ajax.js"></script>
    <script  src="js/navigator.js"></script>
  </head>
  <body <?php print $onLoad;?>>

<!--  <h1>DBpedia Navigator</h1> -->
<div><table border="0" width="100%"><tr><td width="30%"><img src="images/dbpedia_navigator.png" alt="DBpedia Navigator" style="padding:5px" /></td>
<td width="49%">
	<div class="box" id="ConceptBox" style="display:none">
	  <div class="boxtitle" style="cursor:help;" title="Browsing suggestions considering search relevant and not relevant articles.">Navigation Suggestions</div>
	  <div class="boxcontent">
	  <div id="conceptlink" style="display:block"></div>
	  </div> <!-- boxcontent -->
	</div> <!-- box -->
</td><td width="19%" style="text-align:center"><span id="Loading" style="display:none;font-weight:bold;color:#ff0000;">Server Call... <a href=""><img src="images/remove.png" onclick="stopServerCall();return false;" /></a></span><span id="DatabaseLoading" style="display:none;font-weight:bold;color:#ff0000;">Database Call... <a href=""><img src="images/spinner.gif"/></a></span></td></tr></table></div>
<div id="layer" style="display:none">
	<div id="layerContent" style="display:none"></div>
</div>

<div id="wrapper">
	<div id="leftSidebar">

		<div class="box">
		  <div class="boxtitle" style="cursor:help;" title="Show an article or search for several articles in the DBpedia database.">Search DBpedia</div>
		  <div class="boxcontent" id="search">
			<!-- Search:<br/> -->
			<form onsubmit="get_article('label='+document.getElementById('label').value+'&cache=-1');return false;">
			<input type="text" name="label" id="label"/><br/>
			<input type="button" value="Article" class="button" onclick="get_article('label='+document.getElementById('label').value+'&cache=-1');return false;" title="Search an article with that name."/>&nbsp;&nbsp;<input type="button" value="Search" class="button" onclick="search_it('label='+document.getElementById('label').value+'&number=10');return false;" title="Search a number of articles related to that name."/>
			<!--  &nbsp;&nbsp;&nbsp; <input type="button" value="Fulltext" class="button" onclick=""/> -->
			</form>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="SearchResultBox" style="display:none">
		  <div class="boxtitle" style="cursor:help;" title="The best 10 Search Results are shown here.">Best Search Results</div>
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
			
			<a href="http://www.w3.org/2004/OWL/"><img src="images/sw-owl-green.png" alt="OWL logo" /></a>
			<a href="http://www.w3.org/2001/sw/DataAccess/"><img src="images/sw-sparql-green.png" alt="SPARQL logo"/></a>
		</div>
		
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
		  <div class="boxtitlewithbutton" id="positivesboxtitle" style="cursor:help;" title="The shown articles are considered when generating navigation tips.">search relevant &nbsp; <a href="#"><img src="images/remove.png" onclick="clearPositives()" title="Delete all articles of interest."/></a> </div>
		  <div class="boxcontent" id="Positives">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box">
		  <div class="boxtitlewithbutton" id="negativesboxtitle" style="cursor:help;" title="The shown articles are avoided when generating navigation tips.">not relevant &nbsp; <a href="#"><img src="images/remove.png" onclick="clearNegatives()" title="Delete all avoided articles."/></a> </div>
		  <div class="boxcontent" id="Negatives">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="LastArticlesBox" style="display:none">
		  <div class="boxtitle" style="cursor:help;" title="Up to 5 articles, that were last displayed, are shown here.">Articles Last Viewed</div>
		  <div class="boxcontent" id="lastarticles">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->
		
		<div class="box" id="LastClassesBox" style="display:none">
		  <div class="boxtitle" style="cursor:help;" title="Up to 5 classes, that were last displayed, are shown here.">Classes Last Viewed</div>
		  <div class="boxcontent" id="lastclasses">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

	</div><!-- rightSidebar -->
	
	<!--   <div id="clear"></div> -->
	
</div><!--  wrapper -->
<div id="footer">
		<div id="generatedURL"></div>
		<input type="button" class="button" value="generate URL" onclick="generateURL();"/>
		<p>Licensed under the GNU General Public License (GPL) 3 as part of the DL-Learner open source
			project.<br />Copyright &copy; Jens Lehmann 2007-2008 </p>
		<div id="validation">
			<?php
			$uri = 'http://'.$_SERVER['SERVER_NAME'].$_SERVER['REQUEST_URI'];
			
			echo '<div><a href="http://validator.w3.org/check?uri='.$uri.'"';
			echo '><img src="images/valid-xhtml10.png" alt="valid XHTML 1.0" /></a>'."\n";
			echo '<a href="http://jigsaw.w3.org/css-validator/validator?uri='.$uri.'"';
			echo '><img src="images/valid-css.png" alt="valid CSS" /></a></div>'."\n";
			?>	
		</div>	
		<p><a href='rebuild.php'>rebuild [restart session and redownload WSDL file (for debugging)]</a></p>
</div>

<div id="todo">
<b>ToDo:</b>
<ul style="float:left">
	<li>Get learning component fast.</li>
	<li>Get local DBpedia SPARQL endpoint working (next DBpedia release expected at the endof January and then every
	two months, so it would be nice to have a script based partly automated or at least documented solution for
	creating a DBpedia mirror).</li>
	<li>Improve stability: Fix sometimes occurring PHP errors and warnings (check PHP error log).</li>
	<li>For each result, display a "+" which shows more information about the concept in an overlay box, e.g. its 
	Description Logic or OWL syntax, its classification accuracy on the examples, and which
	examples it classifies (in-)correctly.</li>
	<li>Create a small number of test cases (e.g. 3), which can be used to verify that DBpedia Navigator is 
	working in typical scenarios (in particular cases where concepts with length greater one are learned).</li>
	<li>Make DBpedia Navigator RESTful, e.g. URLs $base/showArticle/$URL for displaying an article;
	$base/search/$phrase for searching; $base/listInstances/$complexClass for listing the instances of
	a learned. Maybe session variables (in particuar the selected positive and negative examples) can 
	also be given, e.g. $base/search/$phrase?positives=[$URL1,$URL2,$URL3]&negatives=[$URL4]. The supported
	URI design should be briefly documented (e.g. on a dbpedia.org wiki page). A good URI design allows
	easier external access (just give someone a link instead of saying exactly which actions have to be done to
	get to a state), simplifies debugging the application, and may be of use for creating further
	features.</li> 
	<li>[if possible] When expensive SPARQL queries or learning problems have been posed, there should be
	some way to abandon these if the user has already switched to doing something else. Example: The user
	has added 3 positive and 1 negative examples. This is executed as a learning problem, but has no solution (so
	DL-Learner would run forever unless we pose some internal time limit). The user adds another negative example a 
	second later, so instead of letting the previous learning problem run for a long time (and needing much resources),
	it should be stopped by DBpedia Navigator.</li>
	<li>[if possible] Find an easy way to validate HTML/JS in AJAX applications.</li> 
</ul>
</div>

  </body>
</html>
			