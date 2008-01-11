<?php

ini_set('error_reporting',E_ALL);
ini_set('max_execution_time',200);
ini_set("soap.wsdl_cache_enabled","1");

session_start();
require_once('Settings.php');
require_once('DLLearnerConnection.php');
$settings=new Settings();
$sc=new DLLearnerConnection($settings->dbpediauri,$settings->wsdluri);
$ids=$sc->getIDs();
$_SESSION['id']=$ids[0];
$_SESSION['ksID']=$ids[1];

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
    <link rel="stylesheet" href="default.css"/>
    <?php $xajax->printJavascript('xajax/'); ?>
	<script type="text/javascript">
        showLoadingSubjects = function() {
            xajax.$('loadingSubject').style.display='block';
            xajax.$('searchcontent').style.display = 'none';
        };
        hideLoadingSubjects = function() {
            xajax.$('loadingSubject').style.display = 'none';
            xajax.$('searchcontent').style.display='block';
        };
        showLoadingArticle = function() {
            xajax.$('loadingArticle').style.display='block';
            xajax.$('articlecontent').style.display = 'none';
        };
        hideLoadingArticle = function() {
            xajax.$('loadingArticle').style.display = 'none';
            xajax.$('articlecontent').style.display = 'block';
        };
        showLoadingConcept = function() {
            xajax.$('loadingConcept').style.display='block';
            xajax.$('conceptcontent').style.display = 'none';
        };
        hideLoadingConcept = function() {
            xajax.$('loadingConcept').style.display = 'none';
            xajax.$('conceptcontent').style.display = 'block';
        };
        showLoadingConceptSubjects = function() {
            xajax.$('loadingConceptSubjects').style.display='block';
            xajax.$('conceptsubjectcontent').style.display = 'none';
        };
        hideLoadingConceptSubjects = function() {
            xajax.$('loadingConceptSubjects').style.display = 'none';
            xajax.$('conceptsubjectcontent').style.display = 'block';
        }
  </script>
  </head>
  <body>

<h1>DBpedia Navigator</h1>
<div id="layer" style="display:none">
	<div id="layerContent" style="display:none"></div>
</div>

<div id="wrapper">
	<div id="leftSidebar">

		<div class="box">
		  <div class="boxtitle">Search DBpedia</div>
		  <div class="boxcontent" id="search">
			<!-- Search:<br/> -->
			<form onSubmit="xajax_searchAndShowArticle(document.getElementById('label').value);return false;">
			<input type="text" name="label" id="label" /><br/>
			<input type="button" value="Search" class="button" onclick="xajax_searchAndShowArticle(document.getElementById('label').value);return false;" />
			<!--  &nbsp;&nbsp;&nbsp; <input type="button" value="Fulltext" class="button" onclick=""/> -->
			</form>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box">
		  <div class="boxtitle">Search Results</div>
		  <div class="boxcontent">
		  <div id="searchcontent" style="display:block"></div>
		  <div id="loadingSubject" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="concept">
		  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left">Learned Concept</td><td class="right"><input type="button" value="Learn" class="button" onclick="xajax_learnConcept();return false;" /></td></tr></table></div>
		  <div class="boxcontent">
		  <div id="conceptcontent" style="display:none"></div>
		  <div id="loadingConcept" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box" id="conceptSubjects">
		  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left">Subjects From Concept</td><td class="right"><input type="button" value="Show" class="button" onclick="xajax_getSubjectsFromConcept();return false;" /></td></tr></table></div>
		  <div class="boxcontent">
		  <div id="conceptsubjectcontent" style="display:none"></div>
		  <div id="loadingConceptSubjects" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
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
		  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left" id="ArticleTitle">Welcome</td><td class="right"><span id="contentbuttons"></span></td></tr></table></div>
		  <div class="boxcontent" id="article">
		  <div id="articlecontent" style="display:block">
		  <br /><br />
		  Welcome to the DBpedia Navigator interface! DBpedia Navigator allows you to search DBpedia
		  and uses the background knowledge in DBpedia to suggest possible interesting navigation
		  links. 
		  </div>
		  <div id="loadingArticle" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->
	</div><!-- content -->
	
	<div id="rightSidebar">

		<div class="box">
		  <div class="boxtitlewithbutton" id="positivesboxtitle">search relevant &nbsp; <img src="images/remove.png" onclick="xajax_clearPositives()" /> </div>
		  <div class="boxcontent" id="Positives">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box">
		  <div class="boxtitlewithbutton" id="negativesboxtitle">not relevant &nbsp; <img src="images/remove.png" onclick="xajax_clearNegatives()" /> </div>
		  <div class="boxcontent" id="Negatives">
		  </div> <!-- boxcontent -->
		</div> <!-- box -->

		<div class="box">
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
			echo '><img src="images/valid-xhtml10.png" alt="valid XHTML 1.0" /></a>'."\n";
			echo '<a href="http://jigsaw.w3.org/css-validator/validator?uri='.$uri.'"';
			echo '><img src="images/valid-css.png" alt="valid CSS" /></a></div>'."\n";
			?>	
		</div>	
		<p><a href='rebuild.php'>rebuild [restart session and redownload WSDL file (for debugging)]</a></p>	
		<p>
ToDo:
<ul>
	<li>get learning process working (with new SPARQL component)</li>
	<li>get local DBpedia SPARQL endpoint working</li>
	<li>many queries work correctly on the server, but yield to response in the interface (seems to be rather random)</li>
	<li>fix sometimes occurring PHP errors and warnings</li>
	<li>would be interesting to somehow view the Wikipedia article (without the left navigation part,
	tabs etc.) as an overlay, because the Wikipedia article will almost always be a human-friendlier
	description of an object compared to the extracted one</li> 
</ul>
</p>
</div>


  </body>
</html>
			