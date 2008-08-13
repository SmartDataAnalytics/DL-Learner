<?php
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

function getResultsTable($names,$labels)
{
	$ret="<p>Your search brought ".count($names)." results.</p><br/>";
	$i=0;
	$display="block";
	while($i*30<count($names))
	{
		$ret.="<div id='results".$i."' style='display:".$display."'>Seite ".($i+1)."<br/><br/>";
		for ($j=0;($j<30)&&(($i*30+$j)<count($names));$j++)
		{
			$name=$names[$i*30+$j];
			$label=$labels[$i*30+$j];
			$ret.='&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="" onclick="get_article(\'label='.$name.'&cache=-1\');return false;">'.$label.'</a><br/>';
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