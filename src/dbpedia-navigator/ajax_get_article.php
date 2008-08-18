<?php
	include('helper_functions.php');
	
	session_start();	

	$subject=$_POST['label'];
	$fromCache=$_POST['cache'];
	if (isset($_POST['path'])) $path=$_POST['path'];
	else $path="";
	
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
			if ($value['uri']==$uri){
				$fromCache=$key;
				break;
			}
		}
	}
	
	//initialize the content variables
	$content="";
	$lastArticles="";
	$artTitle="";
			
	//get the article
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
				$content.='<img src="'.$triples['http://xmlns.com/foaf/0.1/depiction'][0]['value'].'" alt="Picture of '.$triples['http://www.w3.org/2000/01/rdf-schema#label'][0]['value'].'" style="float:right; max-width:200px;" \>';
			
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
				$content .= '<p><img src="'.$path.'images/wikipedia_favicon.png" alt="Wikipedia" style="max-width:20px;" /> <a href="#" onclick="window.open(\''.getPrintableURL($triples['http://xmlns.com/foaf/0.1/page'][0]['value']).'\',\'Wikiwindow\',\'width=800,height=500,top=50,left=50,scrollbars=yes\');">view Wikipedia article</a>, '; 
			$content .= '<img src="'.$path.'images/dbpedia-favicon.ico" alt="DBpedia" style="max-width:20px;"/> <a href="#" onclick="window.open(\''.$uri.'\',\'Wikiwindow\',\'width=800,height=500,top=50,left=50,scrollbars=yes\');">view DBpedia resource description</a>';
			//display photo collection, if there is one
			if (isset($triples['http://dbpedia.org/property/hasPhotoCollection'])){
				$content.=', <img src="'.$path.'images/flickr.png" alt="Flickr" style="max-width:20px;" /> <a href="#" onclick="window.open(\''.$triples['http://dbpedia.org/property/hasPhotoCollection'][0]['value'].'\',\'Wikiwindow\',\'width=800,height=500,top=50,left=50,scrollbars=yes\');">view photo collection</a></p>';
			}
			
			$content.='<br/><hr><h4>Classes and Categories</h4><br/>';
			
			// display a list of classes
			if(isset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']))
				$content .= '<p>Yago classes: '.formatClassArray($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']).'</p>';

			//skos-subjects
			if (isset($triples['http://www.w3.org/2004/02/skos/core#subject'])){
				$content .= '<br/><p>Skos categories: <ul>';
				foreach($triples['http://www.w3.org/2004/02/skos/core#subject'] as $skos)
					$content .= '<li><a href="'.$skos['value'].'">'.$skos['value'].'</a></li>';
				$content .= '</ul></p>';			
			}
			
			//not used at the moment
			/*if(isset($triples['http://dbpedia.org/property/reference'])) {
				$content .= '<p>references: <ul>';
				foreach($triples['http://dbpedia.org/property/reference'] as $reference)
					$content .= '<li><a href="'.$reference['value'].'">'.$reference['value'].'</a></li>';
				$content .= '</ul></p>';
			}*/
			
			//display a Google Map if Geo-koordinates are available
			if (isset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#long'])&&isset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat'])){
				$content.='<br/><hr><h4>Map of the location</h4><br/>';
				$content.='<div id="map" style="width: 500px; height: 300px;margin-left:30px;"></div>';
				$lat=$triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat'][0]['value'];
				$long=$triples['http://www.w3.org/2003/01/geo/wgs84_pos#long'][0]['value'];
			}
			else{
				$lat="";
				$long="";
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
			unset($triples['http://www.w3.org/2000/01/rdf-schema#comment']);
			
			$content.='<br/><hr><h4>Remaining Triples</h4><br/>';
			
			// display the remaining properties as list which can be used for further navigation
			$content .= get_triple_table($triples);
			
			//Restart the Session
			session_start();
			
			//store article in session, to navigate between last 5 articles quickly
			$contentArray=array('content' => $content,'subject' => $artTitle,'uri' => $uri);
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
					$array=array($redirect => $artTitle);
				}
				else $array=array($uri => $artTitle);
				$_SESSION['positive']=$array;
			}
			else{
				$array=$_SESSION['positive'];
				if ($redirect!="") $array[$redirect] = $artTitle;
				else $array[$uri]=$artTitle;
				$_SESSION['positive']=$array;
			}
									
		} catch (Exception $e)
		{
			$content=$e->getMessage();
			$artTitle="Article not found";
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
			$lastArticles.="<a href=\"\" onclick=\"get_article('label=&cache=".$key."');return false;\">".$value['subject']."</a><br/>";
		}
	}
	
	//add Positives and Negatives to Interests
	$interests=show_Interests($_SESSION);
		
	print $content;
	print '$$';
	print $artTitle;
	print '$$';
	print $lastArticles;
	print '$$';
	print $interests[0];
	print '$$';
	print $interests[1];
	print '$$';
	print $lat;
	print '$$';
	print $long;
	
	//$objResponse->call('xajax_learnConcept');
?>