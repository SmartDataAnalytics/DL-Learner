<?php
ini_set('error_reporting',E_ALL);
ini_set('max_execution_time',200);
session_start();

echo "<a href='clearsession.php'>start from scratch</a>";

require("ajax.php");
 
  
echo '<?xml version="1.0" encoding="UTF-8"?>'
?>
<html>
  <head>
    <title>DL Learner</title>
    <meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"/>
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
<h3>DBPedia-Navigator-Test</h3>
<div id="layer" style="display:none"><div id="layerContent" style="display:none"></div></div>
<div id="wrapper">
<div id="leftSidebar">

<div class="box" id="search">
  <div class="boxtitle">Search</div>
  <div class="boxcontent">
	<table border="0">
	<tr><tb>Search:<br/></tb></tr>
	<tr><tb><input type="textfield" name="label" id="label">&nbsp;&nbsp;&nbsp;<select name="limit" size="1" id="limit">
      		<option>1</option>
      		<option selected="selected">5</option>
      		<option>10</option>
      		<option>15</option>
      		</select><br/></tb></tr>
			<tr><tb><input type="button" value="Search" class="button" onclick="xajax_getsubjects(document.getElementById('label').value,document.getElementById('limit').value);return false;"/></tb></tr>
			</table>
  </div> <!-- boxcontent -->
</div> <!-- box -->

<div class="box" id="search">
  <div class="boxtitle">Searchresults</div>
  <div class="boxcontent">
  <div id="searchcontent" style="display:none"></div>
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

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
</div><!-- END leftSidebar -->

<div id="content">
<div class="box">
  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left">Article</td><td class="right"><span id="contentbuttons"></span></td></tr></table></div>
  <div class="boxcontent" id="article">
  <div id="articlecontent" style="display:none"></div>
  <div id="loadingArticle" style="display:none"><img src="ajax-loader.gif" alt="Loading..."/></div>
  </div> <!-- boxcontent -->
</div> <!-- box -->

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
</div><!-- content -->
<div id="rightSidebar">

<div class="box">
  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left">Positives</td><td class="right"><input type="button" value="Clear" class="button" onclick="xajax_clearPositives();return false;" /></td></tr></table></div>
  <div class="boxcontent" id="Positives">
  </div> <!-- boxcontent -->
</div> <!-- box -->

<div class="box">
  <div class="boxtitlewithbutton"><table border="0" class="titletable"><tr><td class="left">Negatives</td><td class="right"><input type="button" value="Clear" class="button" onclick="xajax_clearNegatives();return false;" /></td></tr></table></div>
  <div class="boxcontent" id="Negatives">
  </div> <!-- boxcontent -->
</div> <!-- box -->

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>      
</div><!-- rightSidebar -->
<div id="clear"></div>
</div>
  </body>
</html>
			