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
	if (isset($tags['NoCategory'])){
		$nc=true;
		unset($tags['NoCategory']);
	}
	else $nc=false;
	
	$max=max($tags);
	$min=min($tags);
	$diff=$max-$min;
	$distribution=$diff/3;
	
	$ret="<p>";
	$ret.='<a style="font-size:xx-large;" href="#" onclick="document.getElementById(\'hidden_class\').value=\'all\';show_results(\'all\',document.getElementById(\'hidden_number\').value);">All</a>&nbsp;';
	if ($nc) $ret.='<a style="font-size:xx-small;" href="#" onclick="document.getElementById(\'hidden_class\').value=\'NoCategory\';show_results(\'NoCategory\',document.getElementById(\'hidden_number\').value);">No Category</a>&nbsp;';
	foreach ($tags as $tag=>$count){
		if ($count==$min) $style="font-size:xx-small;";
		else if ($count==$max) $style="font-size:xx-large;";
		else if ($count>($min+2*$distribution)) $style="font-size:large;";
		else if ($count>($min+$distribution)) $style="font-size:medium;";
		else $style="font-size:small;";
		
		//$tag_with_entities=htmlentities("\"".$tag."\"");
		$ret.='<a style="'.$style.'" href="#" onclick="document.getElementById(\'hidden_class\').value=\''.$tag.'\';show_results(\''.$tag.'\',document.getElementById(\'hidden_number\').value);">'.$label[$tag].'</a>&nbsp;';
	}
	$ret.="</p><br/>";
	return $ret;
}

function getResultsTable($names,$labels,$classes,$number)
{
	$ret="<p>These are your Searchresults. Show best ";
	for ($k=10;$k<125;){
		$ret.="<a href=\"#\" onclick=\"var list=tree.getAllChecked();search_it('label='+document.getElementById('label').value+'&list='+list+'&number=".$k."');return false;\"";
		if ($k==$number) $ret.=" style=\"text-decoration:none;\"";
		else $ret.=" style=\"text-decoration:underline;\"";
		$ret.=">".($k)."</a>";
		if ($k!=100) $ret.=" | ";
		if($k==10) $k=25;
		else $k=$k+25;
	}
	$ret.="</p><br/>";
	$i=0;
	$display="block";
	$ret.="<div id=\"results\">";
	while($i*25<count($names))
	{
		for ($j=0;($j<25)&&(($i*25+$j)<count($names));$j++)
		{
			$name=$names[$i*25+$j];
			$label=$labels[$i*25+$j];
			$class=$classes[$i*25+$j];
			$ret.='<p style="display:'.$display.'">&nbsp;&nbsp;&nbsp;&nbsp;'.($i*25+$j+1).'.&nbsp;<a href="" class="'.$class.'" onclick="get_article(\'label='.$name.'&cache=-1\');return false;">'.$label.'</a></p>';
		}
		$i++;
		$display="none";
	}
	$ret.='<input type="hidden" id="hidden_class" value="all"/><input type="hidden" id="hidden_number" value="0"/></div><br/><p style="width:100%;text-align:center;" id="sitenumbers">';
	for ($k=0;$k<$i;$k++){
		$ret.="<span>";
		if ($k!=0) $ret.=" | ";
		$ret.="<a href=\"#\" onclick=\"document.getElementById('hidden_number').value='".(25*$k)."';show_results(document.getElementById('hidden_class').value,".(25*$k).");\"";
		if ($k==0) $ret.=" style=\"text-decoration:none;\"";
		else $ret.=" style=\"text-decoration:underline;\"";
		$ret.=">".($k+1)."</a>";
		$ret.="</span>";
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

function show_Interests($sess)
{
	if (isset($sess['positive'])) foreach($sess['positive'] as $name=>$lab){
		$ret[0]=$lab." <a href=\"\" onclick=\"toNegative('subject=".$name."&label=".$lab."');return false;\"><img src=\"".$_GET['path']."images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"removePosInterest('subject=".$name."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	else $ret[0]="";
	if (isset($sess['negative'])) foreach($sess['negative'] as $name=>$lab){
		$ret[1]=$lab." <a href=\"\" onclick=\"toPositive('subject=".$name."&label=".$lab."');return false;\"><img src=\"".$_GET['path']."images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"removeNegInterest('subject=".$name."');return false;\"><img src=\"".$_GET['path']."images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	else $ret[1]="";
	
	return $ret;
}
?>