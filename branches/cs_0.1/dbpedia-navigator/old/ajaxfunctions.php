<?php
ini_set('max_execution_time',200);

require("ajax.php");
$xajax->processRequest();

function getsubjects($label,$list)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	//get parts of the list
	$checkedInstances=preg_split("[,]",$list,-1,PREG_SPLIT_NO_EMPTY);
	
	//initialise content
	$content="";
	try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id,$ksID);
				
		$subjects=$sc->getSubjects($label,$checkedInstances);
		
		$content.=getTagCloud($subjects['tagcloud'],$subjects['tagcloudlabel']);
		$content.=getResultsTable($subjects['subjects']);
	} catch (Exception $e){
		$content=$e->getMessage();
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("ArticleTitle","innerHTML","Searchresult for ".$label);
	return $objResponse;
}

function getarticle($subject,$fromCache)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	if (isset($_SESSION['articles'])) $articles=$_SESSION['articles'];
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	setRunning($id,"true");
	
	//get first Letter of label big
	$uri=subjectToURI($subject);
	
	//if article is in session, get it out of the session
	if (isset($articles)){
		foreach ($articles as $key => $value)
		{
			if ($value['subject']==$subject){
				$fromCache=$key;
				break;
			}
		}
	}
	
	//initialize the content variables
	$content="";
	$searchResult="";
	$lastArticles="";
	$artTitle="";
	
	$objResponse = new xajaxResponse();
	
	//get the article
	//if $fromCache is -2, no new SearchResults should be processed
	//if $fromCache is -1, everything is normal
	//if $fromCache is >=0, the article is taken out of the cache
	if ($fromCache<0) {
		//if there are errors see catch block
		try{
			require_once("DLLearnerConnection.php");
			$sc=new DLLearnerConnection($id,$ksID);
			$triples=$sc->getTriples($uri);
			
			//BUILD ARTICLE			
			// goal: display the data in a nice (DBpedia specific way), maybe similar to
			// dbpedia.org/search
			
			// display a picture if there is one
			if(isset($triples['http://xmlns.com/foaf/0.1/depiction']))
				$content.='<img src="'.$triples['http://xmlns.com/foaf/0.1/depiction'][0]['value'].'" alt="Picture of '.$subject.'" style="float:right; max-width:200px;" \>';
			
			//display where it was redirected from, if it was redirected
			$redirect="";
			if (isset($triples['http://dbpedia.org/property/redirect'])){
				$content.="<span id=\"redirectedFrom\">redirected from '$subject'</span>";
				$redirect=$triples['http://dbpedia.org/property/redirect'][0]['value'];
			}
			
			// add short description in english
			$content.="<h4>Short Description</h4><p>".urldecode($triples['http://dbpedia.org/property/abstract'][0]['value'])."</p>";
				
			// give the link to the corresponding Wikipedia article
			if(isset($triples['http://xmlns.com/foaf/0.1/page']))
				$content .= '<p><img src="'.$_GET['path'].'images/wikipedia_favicon.png" alt="Wikipedia" /> <a href="'.$triples['http://xmlns.com/foaf/0.1/page'][0]['value'].'">view Wikipedia article</a>, '; 
			$content .= '<a href="'.$uri.'">view DBpedia resource description</a></p>';
	
			// display a list of classes
			if(isset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']))
				$content .= '<p>classes: '.formatClassArray($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']).'</p>';
				
			if(isset($triples['http://dbpedia.org/property/reference'])) {
				$content .= '<p>references: <ul>';
				foreach($triples['http://dbpedia.org/property/reference'] as $reference)
					$content .= '<li><a href="'.$reference['value'].'">'.$reference['value'].'</a></li>';
				$content .= '</ul></p>';
			}
			
			//display a Google Map if Geo-koordinates are available
			if (isset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#long'])&&isset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat'])){
				$content.="<br/><img src=\"".$_GET['path']."images/mobmaps_googlemapsicon.jpg\" alt=\"Google Maps\" style=\"max-width:25px;\" /> <a href=\"\" onClick=\"loadGoogleMap(".$triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat'][0]['value'].",".$triples['http://www.w3.org/2003/01/geo/wgs84_pos#long'][0]['value'].",'".$triples['http://www.w3.org/2000/01/rdf-schema#label'][0]['value']."');return false;\">Toggle a map of the location</a><br/><br/><div id=\"map\" style=\"width: 500px; height: 300px;display:none;\"></div>";
			}
			
			//display photo collection, if there is one
			if (isset($triples['http://dbpedia.org/property/hasPhotoCollection'])){
				$content.="<br/><img src=\"".$_GET['path']."images/flickr.jpg\" alt=\"Flickr\" style=\"max-width:25px;\" /> <a href=\"".$triples['http://dbpedia.org/property/hasPhotoCollection'][0]['value']."\">view a photo collection</a><br/>";
			}
			
			//skos-subjects
			if (isset($triples['http://www.w3.org/2004/02/skos/core#subject'])){
				$content .= '<br/><p>skos subjects: <ul>';
				foreach($triples['http://www.w3.org/2004/02/skos/core#subject'] as $skos)
					$content .= '<li><a href="'.$skos['value'].'">'.$skos['value'].'</a></li>';
				$content .= '</ul></p>';			
			}
			
			//BUILD ARTICLE TITLE
			$artTitle=$triples['http://www.w3.org/2000/01/rdf-schema#label'][0]['value'];
			
			// filter out uninteresting properties and properties which
			// have already been displayed
			unset($triples['http://xmlns.com/foaf/0.1/page']);
			unset($triples['http://xmlns.com/foaf/0.1/depiction']);
			unset($triples['http://dbpedia.org/property/abstract']);
			unset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']);
			unset($triples['http://dbpedia.org/property/redirect']);
			unset($triples['http://dbpedia.org/property/reference']);
			unset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#long']);
			unset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat']);
			unset($triples['http://dbpedia.org/property/hasPhotoCollection']);
			unset($triples['http://www.w3.org/2004/02/skos/core#subject']);
			unset($triples['http://www.w3.org/2000/01/rdf-schema#label']);
			
			// display the remaining properties as list which can be used for further navigation
			$content .= '<br/><br/><br/>'.get_triple_table($triples);
			
			//Restart the Session
			session_start();
			
			//store article in session, to navigate between last 5 articles quickly
			$contentArray=array('content' => $content,'subject' => $artTitle);
			if (!isset($_SESSION['nextArticle'])){
				$_SESSION['nextArticle']=0;
				$_SESSION['articles']=array();
			}
			if ($_SESSION['nextArticle']==5) $_SESSION['nextArticle']=0;
			$_SESSION['articles'][$_SESSION['nextArticle']]=$contentArray;
			$_SESSION['currentArticle']=$_SESSION['nextArticle'];
			$_SESSION['nextArticle']++;
				
			//Add Positives to Session
			if (!isset($_SESSION['positive'])){
				if ($redirect!=""){
					$array=array($redirect => $redirect);
				}
				else $array=array("http://dbpedia.org/resource/".str_replace(" ","_",$subject) => "http://dbpedia.org/resource/".str_replace(" ","_",$subject));
				$_SESSION['positive']=$array;
			}
			else{
				$array=$_SESSION['positive'];
				if ($redirect!="") $array[$redirect] = $redirect;
				else $array["http://dbpedia.org/resource/".str_replace(" ","_",$subject)]="http://dbpedia.org/resource/".str_replace(" ","_",$subject);
				$_SESSION['positive']=$array;
			}
									
		} catch (Exception $e)
		{
			$content=$e->getMessage();
			$artTitle="No Result";
			$objResponse->call('xajax_getsubjects',$subject);
		}
	}
	else {
		session_start();
		//Article is in session
		$content=$_SESSION['articles'][$fromCache]['content'];
		$artTitle=$_SESSION['articles'][$fromCache]['subject'];
	}
	
	//Build lastArticles
	if (isset($_SESSION['articles'])){
		foreach ($_SESSION['articles'] as $key => $value)
		{
			$lastArticles.="<a href=\"\" onclick=\"xajax_getarticle('',".$key.");return false;\">".$value['subject']."</a><br/>";
		}
	}
	
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("ArticleTitle","innerHTML",$artTitle);
	$objResponse->assign("lastarticles","innerHTML",$lastArticles);
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	$objResponse->call('xajax_learnConcept');
	return $objResponse;
}

function toPositive($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['negative'][$subject]);
	if (!isset($_SESSION['positive'])){
		$array=array($subject => $subject);
		$_SESSION['positive']=$array;
	}
	else{
		$array=$_SESSION['positive'];
		$array[$subject]=$subject;
		$_SESSION['positive']=$array;
	}
	
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function toNegative($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['positive'][$subject]);
	if (!isset($_SESSION['negative'])){
		$array=array($subject => $subject);
		$_SESSION['negative']=$array;
	}
	else{
		$array=$_SESSION['negative'];
		$array[$subject]=$subject;
		$_SESSION['negative']=$array;
	}
	
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function clearPositives()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['positive']);
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("Positives", "innerHTML", "");
	return $objResponse;
}

function clearNegatives()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['negative']);
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("Negatives", "innerHTML", "");
	return $objResponse;
}

function removePosInterest($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['positive'][$subject]);
		
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function removeNegInterest($subject)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	unset($_SESSION['negative'][$subject]);
		
	//add Positives and Negatives to Interests
	$posInterests="";
	if (isset($_SESSION['positive'])) foreach($_SESSION['positive'] as $pos){
		$posInterests.=urldecode(substr (strrchr ($pos, "/"), 1))." <a href=\"\" onclick=\"xajax_toNegative('".$pos."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"xajax_removePosInterest('".$pos."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	$negInterests="";
	if (isset($_SESSION['negative'])) foreach($_SESSION['negative'] as $neg){
		$negInterests.=urldecode(substr (strrchr ($neg, "/"), 1))." <a href=\"\" onclick=\"xajax_toPositive('".$neg."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"xajax_removeNegInterest('".$neg."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign('Positives','innerHTML',$posInterests);
	$objResponse->assign('Negatives','innerHTML',$negInterests);
	return $objResponse;
}

function learnConcept()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
		
	if (isset($_SESSION['positive'])) $positives=$_SESSION['positive'];
	if (isset($_SESSION['negative'])) $negatives=$_SESSION['negative'];
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	setRunning($id,"true");
	$concept="";
	$conceptinformation="";
	if (isset($positives))
	{
		$posArray=array();
		foreach ($positives as $pos)
			$posArray[]=$pos;
		$negArray=array();
		if (isset($negatives))
			foreach ($negatives as $neg)
				$negArray[]=$neg;
			
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id, $ksID);
		try{
			$concepts=$sc->getConceptFromExamples($posArray,$negArray);
			$conceptDepth=$sc->getConceptDepth();
			$conceptArity=$sc->getConceptArity();
			
			$concept.="<table border=0>\n";
			$i=1;
			foreach ($concepts as $con){
				$concept.="<tr><td><a href=\"\" onclick=\"xajax_getSubjectsFromConcept('".urlencode($con)."');return false;\" onMouseOver=\"showdiv('div".$i."');showdiv('ConceptBox');\" onMouseOut=\"hidediv('div".$i."');hidediv('ConceptBox');\" />".$con."</a></td></tr>";
				//put information about concepts in divs
				$conceptinformation.="<div id=\"div".$i."\" style=\"display:none\">Concept Depth: ".$conceptDepth[$i-1]."<br/>Concept Arity: ".$conceptArity[$i-1]."<br/>Concept Length: ".$sc->getConceptLength($con)."</div>";
				$i++;
			}
			$concept.="</table>";
		} catch(Exception $e){
			$concept.=$e->getMessage();
		}
	}
	else $concept="You must choose at least one positive example.";
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("conceptlink", "innerHTML", $concept);
	$objResponse->assign("ConceptInformation", "innerHTML", $conceptinformation);
	return $objResponse;
}

function getSubjectsFromConcept($concept)
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	$id=$_SESSION['id'];
	$ksID=$_SESSION['ksID'];
	session_write_close();
	
	setRunning($id,"true");
	
	$concept=html_entity_decode($concept);
	$content="";
	try{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection($id,$ksID);
		$subjects=$sc->getSubjectsFromConcept($concept);
		$content.=getResultsTable($subjects);
	} catch (Exception $e){
		$content=$e->getMessage();
	}
	
	$objResponse = new xajaxResponse();
	$objResponse->assign("articlecontent", "innerHTML", $content);
	$objResponse->assign("ArticleTitle", "innerHTML", "Search Results");
	return $objResponse;
}

function stopServerCall()
{
	$sid = $_GET['sid'];
	session_id($sid);
	session_start();
	$id=$_SESSION['id'];
	session_write_close();
	setRunning($id,"false");
	$objResponse=new xajaxResponse();
	return $objResponse;
}

///////////////////////
// Helper Functions. //
///////////////////////

function subjectToURI($subject)
{
	//if the subject is already a URI return it
	if (strpos($subject,"http://dbpedia.org/resource/")===0)
		return $subject;
	//delete whitespaces at beginning and end
	$subject=trim($subject);
	//get first letters big
	$subject=ucfirst($subject);
	//replace spaces with _
	$subject=str_replace(' ','_',$subject);
	//add the uri
	$subject="http://dbpedia.org/resource/".$subject;
	
	return $subject;
}

function getTagCloud($tags,$label)
{
	$max=max($tags);
	$min=min($tags);
	$diff=$max-$min;
	$distribution=$diff/3;
	
	$ret="<p>";
	foreach ($tags as $tag=>$count){
		if ($count==$min) $style="font-size:xx-small;";
		else if ($count==$max) $style="font-size:xx-large;";
		else if ($count>($min+2*$distribution)) $style="font-size:large;";
		else if ($count>($min+$distribution)) $style="font-size:medium;";
		else $style="font-size:small;";
		
		$tag_with_entities=htmlentities("\"".$tag."\"");
		$ret.='<a style="'.$style.'" href="#" onclick="xajax_getSubjectsFromConcept(\''.$tag_with_entities.'\');">'.$label[$tag].'</a>';
	}
	$ret.="</p>";
	return $ret;
}

function getResultsTable($results)
{
	$ret="<p>Your search brought ".count($results)." results.</p><br/>";
	$i=0;
	$display="block";
	while($i*30<count($results))
	{
		$ret.="<div id='results".$i."' style='display:".$display."'>Seite ".($i+1)."<br/><br/>";
		for ($j=0;($j<30)&&(($i*30+$j)<count($results));$j++)
		{
			$result=$results[$i*30+$j];
			$ret.="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"\" onclick=\"xajax_getarticle('".$result."',-1);return false;\">".urldecode(str_replace("_"," ",substr (strrchr ($result, "/"), 1)))."</a><br/>";
		}
		$ret.="</div>";
		$i++;
		$display="none";
	}
	$ret.="<br/><p style='width:100%;text-align:center;'>";
	for ($k=0;$k<$i;$k++){
		$ret.="<a href=\"\" onClick=\"showdiv('results".($k)."');";
		for ($l=0;$l<$i;$l++)
		{
			if ($l!=$k) $ret.="hidediv('results".$l."');";
		}
		$ret.="return false;\">".($k+1)."</a>";
		if ($k!=($i-1)) $ret.=" | ";
	}
	$ret.="</p>";
	return $ret;
}

function setRunning($id,$running)
{
	if(!is_dir("temp")) mkdir("temp");
	$file=fopen("./temp/".$id.".temp","w");
	fwrite($file, $running);
	fclose($file);
}

function get_triple_table($triples) {

	$table = '<table border="0"><tr><td>predicate</td><td>object</td></tr>';
	$i=1;
	foreach($triples as $predicate=>$object) {
		if ($i>0) $backgroundcolor="eee";
		else $backgroundcolor="ffffff";
		$table .= '<tr style="background-color:#'.$backgroundcolor.';"><td><a href="'.$predicate.'">'.nicePredicate($predicate).'</a></td>';
		$table .= '<td><ul>';
		foreach($object as $element) {
			if ($element['type']=="uri") $table .= '<li><a href="'.$element['value'].'">'.$element['value'].'</a></li>';
			else $table .= '<li>'.$element['value'].'</li>';
		}
		$table .= '</ul></td>';
		$i*=-1;
	}
	$table .= '</table>';
	return $table;
}

function nicePredicate($predicate)
{
	if (strripos ($predicate, "#")>strripos ($predicate, "/")){
		$namespace=substr ($predicate,0,strripos ($predicate, "#"));
		$name=substr ($predicate,strripos ($predicate, "#")+1);
	}
	else{
		$namespace=substr ($predicate,0,strripos ($predicate, "/"));
		$name=substr ($predicate,strripos ($predicate, "/")+1);
	}
	
	switch ($namespace){
		case "http://www.w3.org/2000/01/rdf-schema": 	$namespace="rdfs";
													 	break;
		case "http://www.w3.org/2002/07/owl": 		 	$namespace="owl";
													 	break;
		case "http://xmlns.com/foaf/0.1":			 	$namespace="foaf";
													 	break;
		case "http://dbpedia.org/property":			 	$namespace="p";
													 	break;
		case "http://www.w3.org/2003/01/geo/wgs84_pos":	$namespace="geo";
													 	break;
		case "http://www.w3.org/2004/02/skos/core":		$namespace="skos";
													 	break;	
	}
	
	return $namespace.':'.$name;
}

function formatClassArray($ar) {
	$string = formatClass($ar[0]['value']);
	for($i=1; $i<count($ar); $i++) {
		$string .= ', ' . formatClass($ar[$i]['value']);
	}
	return $string;
}

// format a class nicely, i.e. link to it and possibly display
// it in a better way
function formatClass($className) {
	$yagoPrefix = 'http://dbpedia.org/class/yago/';
	if(substr($className,0,30)==$yagoPrefix) {
		return '<a href="'.$className.'">'.substr($className,30).'</a>';	
	// DBpedia is Linked Data, so it makes always sense to link it
	// ToDo: instead of linking to other pages, the resource should better
	// be openened within DBpedia Navigator
	} else if(substr($className,0,14)=='http://dbpedia') {
		return '<a href="'.$className.'">'.$className.'</a>';
	} else {
		return $className;
	}
}

function arrayToCommaSseparatedList($ar) {
	$string = $ar[0];
	for($i=1; $i<count($ar); $i++) {
		$string .= ', ' . $ar[$i];
	}
	return $string;
}

?>