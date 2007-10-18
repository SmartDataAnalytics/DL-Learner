<?php

$masterContent="

 
<html>
  <head>
    <title>DL Learner</title>
    <meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"/>
    <link rel=\"stylesheet\" href=\"default.css\"/>
   </head>
  <body>
<script type=\"text/javascript\" src=\"jscript/wz_tooltip.js\"></script>
<script type=\"text/javascript\" src=\"jscript/scripts.js\"></script>
<h3>DBPedia-Navigator-Test</h3>
<div id=\"layer\" style=\"display:none\"><div id=\"layerContent\" style=\"display:none\"></div></div>
<div id=\"wrapper\">

<div id=\"leftSidebar\">

<div class=\"box\" id=\"search\">
  <div class=\"boxtitle\">Search<a class=\"title_switch\" onclick=\"toggleBoxContent(this);\">–</a></div>
  <div class=\"boxcontent\">
	".$search."
  </div> <!-- boxcontent -->
  
</div> <!-- box -->

".$left."

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
</div><!-- END leftSidebar -->

<div id=\"content\">
<div class=\"box\" id=\"search\">
  <div class=\"boxtitle\">Content</div>
  <div class=\"boxcontent\">
	".$content."
  </div> <!-- boxcontent -->
</div> <!-- box -->
".$middle."
<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
</div><!-- content -->

<div id=\"rightSidebar\">

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>      
</div><!-- rightSidebar -->
  
  <div id=\"clear\"></div>
</div><!-- wrapper -->

  </body>
</html>
";

?>