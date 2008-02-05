<?php
ini_set('error_reporting',E_ALL);

require("xajaxtest.php"); 
  
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
  </head>
  <body>

<!--  <h1>DBpedia Navigator</h1> -->
<img src="images/dbpedia_navigator.png" alt="DBpedia Navigator" style="padding:5px" />
<div id="layer" style="display:none">
	<div id="layerContent" style="display:none"></div>
</div>

<div id="wrapper">
	<div id="leftSidebar">

		<div class="box" id="concept">
		  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left">Learned Concept</td><td class="right"><input type="button" value="Learn" class="button" onclick="xajax_learnConcept();return false;" /></td></tr></table></div>
		  <div class="boxcontent">
		  <div id="conceptcontent" style="display:none"></div>
		  <div id="loadingConcept" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->
		
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
		  <div id="loadingArticle" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
		  </div> <!-- boxcontent -->
		</div> <!-- box -->
	</div><!-- content -->
	
	<!--   <div id="clear"></div> -->
	
</div><!--  wrapper -->
  </body>
</html>
			