<?php

ini_set('error_reporting',E_ALL);
ini_set('max_execution_time',200);
ini_set("soap.wsdl_cache_enabled","1");

session_start();

//what happens onLoad
$onLoad="onLoad=\"document.getElementById('label').focus();";
if (isset($_GET['positives'])||isset($_GET['negatives'])) $onLoad.="setPositivesAndNegatives('positives=".@$_GET['positives']."&negatives=".@$_GET['negatives']."');";
else if (isset($_SESSION['positives'])||isset($_SESSION['negatives'])) $onLoad.="setPositivesAndNegatives('positives=".$_SESSION['positives']."&negatives=".$_SESSION['negatives']."');";
if (isset($_GET['showArticle'])){
	session_unset();
	$onLoad.="get_article('label=".$_GET['showArticle']."&cache=-2');";
}
else if (isset($_GET['search'])){
	session_unset();
	$onLoad.="search_it('label=".$_GET['search']."&number=10');";
}
else if (isset($_GET['showClass'])){
	session_unset();
	$onLoad.="get_class('class=http://dbpedia.org/class/yago/".$_GET['showClass']."&cache=-1');";
}
else if (isset($_GET['searchInstances'])){
	session_unset();
	$onLoad.="getSubjectsFromCategory('category=http://dbpedia.org/class/yago/".$_GET['searchInstances']."&number=10');";
}
else if (isset($_GET['searchConceptInstances'])){
	session_unset();
	$onLoad.="getSubjectsFromConcept('kb=".htmlentities(urldecode($_GET['concept']))."&number=10');";
}
else if (isset($_SESSION['currentArticle'])){
	$onLoad.="get_article('label=&cache=".$_SESSION['currentArticle']."');";
}

$onLoad.="\"";

require_once('DLLearnerConnection.php');
$sc=new DLLearnerConnection();
$ids=$sc->getIDs();
$_SESSION['id']=$ids[0];
$_SESSION['ksID']=$ids[1];

require_once('Settings.php');
$settings=new Settings();
  
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
    <link rel="Shortcut Icon" href="images/favicon.ico">
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
			&nbsp; <a href="http://dl-learner.org" target="_blank">DL-Learner</a><br />
			&nbsp; <a href="http//dbpedia.org" target="_blank">DBpedia</a><br/>
			&nbsp; <a href="http://virtuoso.openlinksw.com/wiki/main/" target="_blank">OpenLink Virtuoso</a><br />
			... and implemented by <a href="http://jens-lehmann.org" target="_blank">Jens Lehmann</a> and
			Sebastian Knappe at	the <a href="http:/aksw.org" target="_blank">AKSW</a> research group (University of Leipzig).</p>
			
			<a href="http://www.w3.org/2004/OWL/" target="_blank"><img src="images/sw-owl-green.png" alt="OWL logo" /></a>
			<a href="http://www.w3.org/2001/sw/DataAccess/" target="_blank"><img src="images/sw-sparql-green.png" alt="SPARQL logo"/></a>
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
		<p>Licensed under the GNU General Public License (GPL) 3 as part of the DL-Learner open source
			project.<br />Copyright &copy; Jens Lehmann 2007-2008 </p>
		<div id="validation">
			<?php
			$uri = 'http://'.$_SERVER['SERVER_NAME'].$_SERVER['REQUEST_URI'];
			
			echo '<div><a href="http://validator.w3.org/check?uri='.$uri.'"';
			echo ' target="_blank"><img src="images/valid-xhtml10.png" alt="valid XHTML 1.0" /></a>'."\n";
			echo '<a href="http://jigsaw.w3.org/css-validator/validator?uri='.$uri.'"';
			echo ' target="_blank"><img src="images/valid-css.png" alt="valid CSS" /></a></div>'."\n";
			?>	
		</div>
</div>
  </body>
</html>
			