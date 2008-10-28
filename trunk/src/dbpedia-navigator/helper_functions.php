<?php
function getLabel($uri,$label)
{
	$res=urldecode(str_replace("_"," ",substr (strrchr ($uri, "/"), 1)));
	if (strlen($label)>strlen($res)-5||preg_match('/[0-9]$/',$res)===1){
		$res=$label;
	}
	$res=utf8_to_html($res);
	
	$final='';
	$offset=0;
	preg_match_all("/[^-\040]([A-Z])[^A-Z]/",$res,$treffer,PREG_OFFSET_CAPTURE);
	foreach ($treffer[1] as $treff){
		if ($res[$treff[1]-1]!=' '&&$res[$treff[1]-1]!='-'&&$treff[1]!=0){
			$final.=substr($res,$offset,$treff[1]-$offset).' ';
			$offset=$treff[1];
		}
	}
	$final.=substr($res,$offset);
	
	$res=$final;
	
	//replacements
	$res=str_replace('Cities','City',$res);
	$res=str_replace('Players','Player',$res);
	
	return $res;
}

function subjectToURI($subject)
{
	//if the subject is already a URI return it
	if (strpos($subject,"http://dbpedia.org/resource/")===0){
		$part=substr (strrchr ($subject, "/"), 1);
		return substr($subject,0,strlen($subject)-strlen($part)).urlencode($part);
	}
	//delete whitespaces at beginning and end
	$subject=trim($subject);
	//get first letters big
	$subject=ucfirst($subject);
	//replace spaces with _
	$subject=str_replace(' ','_',$subject);
	$subject=urlencode($subject);
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
	foreach ($tags as $tag=>$count){
		if ($count==$min) $style="font-size:9px;";
		else if ($count==$max) $style="font-size:17px;";
		else if ($count>($min+2*$distribution)) $style="font-size:15px;";
		else if ($count>($min+$distribution)) $style="font-size:13px;";
		else $style="font-size:11px;";
		
		$lab=getLabel($tag,$label[$tag]);
		//$tag_with_entities=htmlentities("\"".$tag."\"");
		$ret.='<a style="'.$style.'" href="#" onclick="document.getElementById(\'hidden_class\').value=\''.$tag.'\';show_results(\''.$tag.'\',document.getElementById(\'hidden_number\').value,\''.utf8_to_html($lab).'\');">'.utf8_to_html($lab).'</a>&nbsp;';
	}
	$ret.="</p><br/>";
	$ret.='<span id="FilterTags">You currently don\'t filter your search results.</span>';
	$ret.=' You can <a style="font-size:11px;" href="#" onclick="document.getElementById(\'hidden_class\').value=\'all\';show_results(\'all\',document.getElementById(\'hidden_number\').value,\'all\');">show all results</a>&nbsp;';
	if ($nc) $ret.=' or <a style="font-size:11px;" href="#" onclick="document.getElementById(\'hidden_class\').value=\'NoCategory\';show_results(\'NoCategory\',document.getElementById(\'hidden_number\').value,\'No Category\');">show results with no category</a>&nbsp;';
	$ret.='<br/><br/>';
	return $ret;
}

function getResultsTable($names,$labels,$classes,$number)
{
	$ret="<p>You got ".count($names)." results. Show best ";
	for ($k=10;$k<125;){
		$ret.="<a href=\"#\" onclick=\"search_it('label='+document.getElementById('label').value+'&number=".$k."');return false;\"";
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
			if (strlen($label)==0) $label=urldecode(str_replace("_"," ",substr (strrchr ($name, "/"), 1)));
			$class="";
			$k=0;
			foreach ($classes[$i*25+$j] as $cl){
				if ($k!=count($classes[$i*25+$j])-1) $class.=$cl.' ';
				else $class.=$cl;
				$k++;
			}
			$ret.='<p style="display:'.$display.'">&nbsp;&nbsp;&nbsp;&nbsp;'.($i*25+$j+1).'.&nbsp;<a href="" class="'.$class.'" onclick="get_article(\'label='.$name.'&cache=-1\');return false;">'.utf8_to_html($label).'</a></p>';
		}
		$i++;
		$display="none";
	}
	$ret.='<input type="hidden" id="hidden_class" value="all"/><input type="hidden" id="hidden_number" value="0"/></div><br/><p style="width:100%;text-align:center;" id="sitenumbers">';
	for ($k=0;$k<$i;$k++){
		if ($i<2) $ret.="<span style=\"display:none\">";
		else $ret.="<span>";
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

function utf8_to_html($string)
{
	$string=str_replace("u00C4","&Auml;",$string);
	$string=str_replace("u00D6","&Ouml;",$string);
	$string=str_replace("u00DC","&Uuml;",$string);
	$string=str_replace("u00E4","&auml;",$string);
	$string=str_replace("u00F6","&ouml;",$string);
	$string=str_replace("u00FC","&uuml;",$string);
	$string=str_replace("u0161","&scaron;",$string);
	$string=str_replace("u00FA","&uacute;",$string);
	$string=str_replace("u00F0","&eth;",$string);
	$string=str_replace("u00E6","&aelig;",$string);
	
	return $string;
}

function getCategoryResultsTable($names,$labels,$category,$number)
{
	$ret="<p>You got ".count($names)." results. Show best ";
	for ($k=10;$k<125;){
		$ret.="<a href=\"#\" onclick=\"getSubjectsFromCategory('category=".$category."&number=".$k."');return false;\"";
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
			if (strlen($label)==0) $label=urldecode(str_replace("_"," ",substr (strrchr ($name, "/"), 1)));
			$ret.='<p style="display:'.$display.'">&nbsp;&nbsp;&nbsp;&nbsp;'.($i*25+$j+1).'.&nbsp;<a class="all" href="" onclick="get_article(\'label='.$name.'&cache=-1\');return false;">'.utf8_to_html($label).'</a></p>';
		}
		$i++;
		$display="none";
	}
	$ret.='<input type="hidden" id="hidden_class" value="all"/><input type="hidden" id="hidden_number" value="0"/></div><br/><p style="width:100%;text-align:center;" id="sitenumbers">';
	for ($k=0;$k<$i;$k++){
		if ($i<2) $ret.="<span style=\"display:none\">";
		else $ret.="<span>";
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

function getConceptResultsTable($names,$labels,$kb,$number)
{
	$ret="<p>You got ".count($names)." results. Show best ";
	for ($k=10;$k<125;){
		$ret.="<a href=\"#\" onclick=\"getSubjectsFromConcept('kb=".$kb."&number=".$k."');return false;\"";
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
			if (strlen($label)==0) $label=urldecode(str_replace("_"," ",substr (strrchr ($name, "/"), 1)));
			$ret.='<p style="display:'.$display.'">&nbsp;&nbsp;&nbsp;&nbsp;'.($i*25+$j+1).'.&nbsp;<a class="all" href="" onclick="get_article(\'label='.$name.'&cache=-1\');return false;">'.utf8_to_html($label).'</a></p>';
		}
		$i++;
		$display="none";
	}
	$ret.='<input type="hidden" id="hidden_class" value="all"/><input type="hidden" id="hidden_number" value="0"/></div><br/><p style="width:100%;text-align:center;" id="sitenumbers">';
	for ($k=0;$k<$i;$k++){
		if ($i<2) $ret.="<span style=\"display:none\">";
		else $ret.="<span>";
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

function getBestSearches($names,$labels)
{
	$ret="<div id=\"best-results\">";
	for ($j=0;($j<10)&&$j<count($names);$j++)
	{
		$name=$names[$j];
		$label=$labels[$j];
		if (strlen($label)==0) $label=urldecode(str_replace("_"," ",substr (strrchr ($name, "/"), 1)));
		$ret.='&nbsp;'.($j+1).'.&nbsp;<a href="" onclick="get_article(\'label='.$name.'&cache=-1\');return false;">'.utf8_to_html($label).'</a><br/>';
	}
	$ret.="</div>";
	return $ret;
}

function getPrintableURL($url)
{
	$parts=explode('/',$url);
	return $parts[0].'//'.$parts[2].'/w/index.php?title='.$parts[4].'&printable=yes';
}

function setRunning($id,$running)
{
	if(!is_dir("temp")) mkdir("temp");
	$file=fopen("./temp/".$id.".temp","w");
	fwrite($file, $running);
	fclose($file);
}

function get_triple_table($triples,$subjecttriples) {

	if ((is_array($triples)&&count($triples)>0)||(is_array($subjecttriples)&&count($subjecttriples)>0)){
		$table = '<table border="0" style="width:100%;overflow:hidden"><tr><td><b>Predicate</b></td><td><b>Object/Subject</b></td></tr>';
		$i=1;
		if (is_array($triples)&&count($triples)>0) foreach($triples as $predicate=>$object) {
			$number=count($object);
			if ($i>0) $backgroundcolor="eee";
			else $backgroundcolor="ffffff";
			$table .= '<tr style="background-color:#'.$backgroundcolor.';"><td><a href="'.$predicate.'" target="_blank">'.nicePredicate($predicate).'</a></td>';
			$table .= '<td>';
			if ($number>1) $table.='<ul>';
			$k=1;
			foreach($object as $element) {
				if ($k>3) $display=" style=\"display:none\"";
				else $display="";
				if ($element['type']=="uri"){
					if (strpos($element['value'],"http://dbpedia.org/resource/")===0&&substr_count($element['value'],"/")==4&&strpos($element['value'],"Template:")!=28){
						$label=str_replace('_',' ',substr($element['value'],28));
						if (strlen($label)>60) $label=substr($label,0,60).'...';
						if ($number>1) $table.='<li'.$display.'>';
						$table .= '<a href="#" onclick="get_article(\'label='.$element['value'].'&cache=-1\');">'.urldecode($label).'</a>';
						if ($number>1) $table.='</li>';
					}
					else{
						if ($number>1) $table.='<li'.$display.'>';
						$label=urldecode($element['value']);
						if (strlen($label)>60) $label=substr($label,0,60).'...';
						$table .= '<a href="'.$element['value'].'" target="_blank">'.$label.'</a>';
						if ($number>1) $table.='</li>';
					}
				}
				else{
					if ($number>1) $table.='<li'.$display.'>';
					$table .= $element['value'];
					if ($number>1) $table.='</li>';
				}
				$k++;
			}
			if ($number>3) $table.='<a href="javascript:none()" onclick="toggleAttributes(this)"><img src="images/arrow_down.gif"/>&nbsp;show</a>';
			if ($number>1) $table.='</ul>';
			$table .= '</td>';
			$i*=-1;
		}
		if (is_array($subjecttriples)&&count($subjecttriples)>0) foreach($subjecttriples as $predicate=>$object) {
			$number=count($object);
			if ($i>0) $backgroundcolor="eee";
			else $backgroundcolor="ffffff";
			$table .= '<tr style="background-color:#'.$backgroundcolor.';"><td><a href="'.$predicate.'" target="_blank">is '.nicePredicate($predicate).' of </a></td>';
			$table .= '<td>';
			if ($number>1) $table.='<ul>';
			$k=1;
			foreach($object as $element) {
				if ($k>3) $display=" style=\"display:none\"";
				else $display="";
				if ($element['type']=="uri"){
					if (strpos($element['value'],"http://dbpedia.org/resource/")===0&&substr_count($element['value'],"/")==4&&strpos($element['value'],"Template:")!=28){
						$label=str_replace('_',' ',substr($element['value'],28));
						if (strlen($label)>60) $label=substr($label,0,60).'...';
						if ($number>1) $table.='<li'.$display.'>';
						$table .= '<a href="#" onclick="get_article(\'label='.$element['value'].'&cache=-1\');">'.urldecode($label).'</a>';
						if ($number>1) $table.='</li>';
					}
					else{
						if ($number>1) $table.='<li'.$display.'>';
						$label=urldecode($element['value']);
						if (strlen($label)>60) $label=substr($label,0,60).'...';
						$table .= '<a href="'.$element['value'].'" target="_blank">'.$label.'</a>';
						if ($number>1) $table.='</li>';
					}
				}
				else{
					if ($number>1) $table.='<li'.$display.'>';
					$table .= $element['value'];
					if ($number>1) $table.='</li>';
				}
				$k++;
			}
			if ($number>3) $table.='<a href="javascript:none()" onclick="toggleAttributes(this)"><img src="images/arrow_down.gif"/>&nbsp;show</a>';
			if ($number>1) $table.='</ul>';
			$table .= '</td>';
			$i*=-1;
		}
		$table .= '</table>';
	}
	else $table="No Tripel left.";
	return $table;
}

function nicePredicate($predicate)
{
	if (strripos ($predicate, "#")>strripos ($predicate, "/")){
		//$namespace=substr ($predicate,0,strripos ($predicate, "#"));
		$name=substr ($predicate,strripos ($predicate, "#")+1);
	}
	else{
		//$namespace=substr ($predicate,0,strripos ($predicate, "/"));
		$name=substr ($predicate,strripos ($predicate, "/")+1);
	}
	
	/*switch ($namespace){
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
		case "http://www.georss.org/georss/point":		$namespace="georss";
													 	break;	
	}*/
	
	//change urls with ä,ö,ü
	$name=str_replace('_percent_C3_percent_A4','%C3%A4',$name);
	$name=str_replace('_percent_C3_percent_B6','%C3%B6',$name);
	$name=str_replace('_percent_C3_percent_BC','%C3%BC',$name);
	$name=str_replace('_',' ',$name);
	
	return urldecode($name);
}

function formatClassArray($ar,$classSystem) {
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	//connect to the database
	$settings=new Settings();
	$databaseConnection=new DatabaseConnection($settings->database_type);
	$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
	$databaseConnection->select_database($settings->database_name);
	
	$string="<ul>";
	for($i=0; $i<count($ar); $i++) {
		if ($classSystem=="YAGO") $prefix = 'http://dbpedia.org/class/yago/';
		else if ($classSystem=="DBpedia") $prefix='http://dbpedia.org/ontology/';
		if (substr($ar[$i]['value'],0,strlen($prefix))!=$prefix||$ar[$i]['value']=='http://dbpedia.org/ontology/Resource') continue;
		$query="SELECT label FROM categories WHERE category='".$ar[$i]['value']."' LIMIT 1";
		$res=$databaseConnection->query($query);
		$result=$databaseConnection->nextEntry($res);
		$label=getLabel($ar[$i]['value'],$result['label']);
		$label=utf8_to_html($label);
		$string .= '<li>' . formatClass($ar[$i]['value'],$label).'</li>';
	}
	return $string."</ul>";
}

// format a class nicely, i.e. link to it and possibly display
// it in a better way
function formatClass($className,$label) {
	return $label.'&nbsp;&nbsp;&nbsp;<a href="#" onclick="getSubjectsFromCategory(\'category='.$className.'&number=10\');">&rarr; search Instances</a>&nbsp;&nbsp;<a href="#" onclick="get_class(\'class='.$className.'&cache=-1\');">&rarr; show Class in Hierarchy</a>';	
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
	$ret=array();
	$ret[0]="";
	$ret[1]="";
	if (isset($sess['positive'])) foreach($sess['positive'] as $name=>$lab){
		$ret[0].=$lab." <a href=\"\" onclick=\"toNegative('subject=".$name."&label=".$lab."');return false;\"><img src=\"images/minus.jpg\" alt=\"Minus\"/></a> <a href=\"\" onclick=\"removePosInterest('subject=".$name."');return false;\"><img src=\"images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	if (isset($sess['negative'])) foreach($sess['negative'] as $name=>$lab){
		$ret[1].=$lab." <a href=\"\" onclick=\"toPositive('subject=".$name."&label=".$lab."');return false;\"><img src=\"images/plus.jpg\" alt=\"Plus\"/></a> <a href=\"\" onclick=\"removeNegInterest('subject=".$name."');return false;\"><img src=\"images/remove.png\" alt=\"Delete\"/></a><br/>";
	}
	
	return $ret;
}

function getClassView($fathers,$childs,$title,$class)
{
	$ret='This is the class view. You can browse through the hierarchy and search for instances of classes.<br/><br/>';
	$childButtons=true;
	if (strlen($childs)==0){
		$childs='There are no Child classes';
		$childButtons=false;
	}
	$fatherButtons=true;
	if (strlen($fathers)==0){
		$fathers='There are no Father classes';
		$fatherButtons=false;
	}
			
	$ret.='<table border="0" style="text-align:left;width:100%">';
	$ret.='<tr><td style="width:90%;font-size:14px;"><b>Father classes</b></td></tr>';
	$ret.='<tr style="height:10px"><td></td></tr>';
	$ret.='<tr><td>'.$fathers.'</td></tr>';
	$ret.='<tr style="height:10px"><td></td></tr>';
	$ret.='<tr><td>';
	if ($fatherButtons) $ret.='<input style="width:70px" type="button" value="Instances" class="button" onclick="getSubjectsFromCategory(\'category=\'+document.getElementById(\'fatherSelect\').options[document.getElementById(\'fatherSelect\').selectedIndex].value+\'&number=10\');" title="Search Instances of Father class."/>&nbsp;&nbsp;<input style="width:70px" type="button" value="Class" class="button" onclick="get_class(\'class=\'+document.getElementById(\'fatherSelect\').options[document.getElementById(\'fatherSelect\').selectedIndex].value+\'&cache=-1\');" title="Show Father class in class view."/>';
	$ret.='</td></tr>';
	$ret.='<tr style="height:20px"><td><hr/></td></tr>';
	$ret.='<tr><td style="font-size:14px;"><b>Current class</b></td></tr>';
	$ret.='<tr style="height:10px"><td></td></tr>';
	$ret.='<tr><td><b>'.$title.'</b></td></tr>';
	$ret.='<tr style="height:10px"><td></td></tr>';
	$ret.='<tr><td>';
	$ret.='<input style="width:70px" type="button" value="Instances" class="button" onclick="getSubjectsFromCategory(\'category='.$class.'&number=10\');" title="Search Instances of Shown class."/>';
	$ret.='</td></tr>';
	$ret.='<tr style="height:20px"><td><hr/></td></tr>';
	$ret.='<tr><td style="width:30%;font-size:14px;"><b>Child classes</b></td></tr>';
	$ret.='<tr style="height:10px"><td></td></tr>';
	$ret.='<tr><td>'.$childs.'</td></tr>';
	$ret.='<tr style="height:10px"><td></td></tr>';
	$ret.='<tr><td>';
	if ($childButtons) $ret.='<input style="width:70px" type="button" value="Instances" class="button" onclick="getSubjectsFromCategory(\'category=\'+document.getElementById(\'childSelect\').options[document.getElementById(\'childSelect\').selectedIndex].value+\'&number=10\');" title="Search Instances of Child class."/>&nbsp;&nbsp;<input style="width:70px" type="button" value="Class" class="button" onclick="get_class(\'class=\'+document.getElementById(\'childSelect\').options[document.getElementById(\'childSelect\').selectedIndex].value+\'&cache=-1\');" title="Show Child class in class view."/>';
	$ret.='</td></tr>';
	$ret.='</table>';
				
	return $ret;
}

function createCharacteristics($char)
{
	$ret='<table border="0" style="padding:5px">';
	$i=0;
	foreach ($char as $label=>$value){
		if ($i%2==0)
			$ret.='<tr>';
		$ret.='<td style="width:20%;text-align:right"><b>'.$label.'</b>:</td><td style="width:30%;text-align:left">';
		$number=count($value);	
		if ($number>1) $ret.='<ul>';
		foreach ($value as $val){
			if ($val['type']=="uri"){
				if (strpos($val['value'],"http://dbpedia.org/resource/")===0&&substr_count($val['value'],"/")==4&&strpos($val['value'],"Template:")!=28){
					$label=str_replace('_',' ',substr($val['value'],28));
					if ($number>1) $ret.='<li>';
					$ret .= '<a href="#" onclick="get_article(\'label='.$val['value'].'&cache=-1\');return false;">'.urldecode($label).'</a>';
					if ($number>1) $ret.='</li>';
				}
				else{
					if ($number>1) $ret.='<li>';
					$ret .= '<a href="'.$val['value'].'" target="_blank">'.urldecode($val['value']).'</a>';
					if ($number>1) $ret.='</li>';
				}
			}
			else{
				if ($number>1) $ret.='<li>';
				$ret .= $val['value'];
				if ($number>1) $ret.='</li>';
			}
		}
		if ($number>1) $ret.='</ul>';
		$ret.='</td>';
		if ($i%2==1||$i==count($char)-1){
			$ret.='</tr>';
			$ret.='<tr><td colspan="4" style="height:10px"></td></tr>';
		}
		$i++;
	}
	$ret.='</table>';
	return $ret;
}

function filterTriples($triples,$subjecttriples){
	if (isset($triples['http://www.w3.org/2002/07/owl#sameAs'])) unset($triples['http://www.w3.org/2002/07/owl#sameAs']);
			if (isset($subjecttriples['http://www.w3.org/2002/07/owl#sameAs'])) unset($subjecttriples['http://www.w3.org/2002/07/owl#sameAs']);
			if (isset($triples['http://xmlns.com/foaf/0.1/page'])) unset($triples['http://xmlns.com/foaf/0.1/page']);
			if (isset($triples['http://xmlns.com/foaf/0.1/depiction'])) unset($triples['http://xmlns.com/foaf/0.1/depiction']);
			if (isset($triples['http://dbpedia.org/property/abstract'])) unset($triples['http://dbpedia.org/property/abstract']);
			if (isset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'])) unset($triples['http://www.w3.org/1999/02/22-rdf-syntax-ns#type']);
			if (isset($triples['http://dbpedia.org/property/redirect'])) unset($triples['http://dbpedia.org/property/redirect']);
			if (isset($triples['http://dbpedia.org/property/reference'])) unset($triples['http://dbpedia.org/property/reference']);
			if (isset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#long'])) unset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#long']);
			if (isset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat'])) unset($triples['http://www.w3.org/2003/01/geo/wgs84_pos#lat']);
			if (isset($triples['http://dbpedia.org/property/hasPhotoCollection'])) unset($triples['http://dbpedia.org/property/hasPhotoCollection']);
			if (isset($triples['http://www.w3.org/2004/02/skos/core#subject'])) unset($triples['http://www.w3.org/2004/02/skos/core#subject']);
			if (isset($triples['http://www.w3.org/2000/01/rdf-schema#label'])) unset($triples['http://www.w3.org/2000/01/rdf-schema#label']);
			if (isset($triples['http://www.w3.org/2000/01/rdf-schema#comment'])) unset($triples['http://www.w3.org/2000/01/rdf-schema#comment']);
			if (isset($triples['http://dbpedia.org/property/latSec'])) unset($triples['http://dbpedia.org/property/latSec']);
			if (isset($triples['http://dbpedia.org/property/lonSec'])) unset($triples['http://dbpedia.org/property/lonSec']);
			if (isset($triples['http://dbpedia.org/property/lonDeg'])) unset($triples['http://dbpedia.org/property/lonDeg']);
			if (isset($triples['http://dbpedia.org/property/latMin'])) unset($triples['http://dbpedia.org/property/latMin']);
			if (isset($triples['http://dbpedia.org/property/lonMin'])) unset($triples['http://dbpedia.org/property/lonMin']);
			if (isset($triples['http://dbpedia.org/property/latDeg'])) unset($triples['http://dbpedia.org/property/latDeg']);
			if (isset($triples['http://dbpedia.org/property/lonMin'])) unset($triples['http://dbpedia.org/property/lonMin']);
			if (isset($triples['http://www.georss.org/georss/point'])) unset($triples['http://www.georss.org/georss/point']);
			if (isset($triples['http://dbpedia.org/property/audioProperty'])) unset($triples['http://dbpedia.org/property/audioProperty']);
			if (isset($triples['http://dbpedia.org/property/wikiPageUsesTemplate'])) unset($triples['http://dbpedia.org/property/wikiPageUsesTemplate']);
			if (isset($triples['http://dbpedia.org/property/relatedInstance'])) unset($triples['http://dbpedia.org/property/relatedInstance']);
			if (isset($triples['http://dbpedia.org/property/boxWidth'])) unset($triples['http://dbpedia.org/property/boxWidth']);
			if (isset($triples['http://dbpedia.org/property/pp'])) unset($triples['http://dbpedia.org/property/pp']);
			if (isset($triples['http://dbpedia.org/property/caption'])) unset($triples['http://dbpedia.org/property/caption']);
			if (isset($subjecttriples['http://dbpedia.org/property/caption'])) unset($subjecttriples['http://dbpedia.org/property/caption']);
			if (isset($triples['http://dbpedia.org/property/s'])) unset($triples['http://dbpedia.org/property/s']);
			if (isset($triples['http://dbpedia.org/property/lifetimeProperty'])) unset($triples['http://dbpedia.org/property/lifetimeProperty']);
			if (isset($triples['http://dbpedia.org/property/imagesize'])) unset($triples['http://dbpedia.org/property/imagesize']);
			if (isset($triples['http://dbpedia.org/property/id'])) unset($triples['http://dbpedia.org/property/id']);
			if (isset($triples['http://dbpedia.org/property/issue'])) unset($triples['http://dbpedia.org/property/issue']);
			if (isset($triples['http://dbpedia.org/property/hips'])&&$triples['http://dbpedia.org/property/hips'][0]['type']=='uri') unset($triples['http://dbpedia.org/property/hips']);
			if (isset($triples['http://dbpedia.org/property/weight'])&&$triples['http://dbpedia.org/property/weight'][0]['type']=='uri') unset($triples['http://dbpedia.org/property/weight']);
			if (isset($triples['http://dbpedia.org/property/waist'])&&$triples['http://dbpedia.org/property/waist'][0]['type']=='uri') unset($triples['http://dbpedia.org/property/waist']);
			if (isset($triples['http://dbpedia.org/property/height'])&&$triples['http://dbpedia.org/property/height'][0]['type']=='uri') unset($triples['http://dbpedia.org/property/height']);
			if (isset($triples['http://www.geonames.org/ontology#featureCode'])) unset($triples['http://www.geonames.org/ontology#featureCode']);
			if (isset($triples['http://www.geonames.org/ontology#featureClass'])) unset($triples['http://www.geonames.org/ontology#featureClass']);
			if (isset($triples['http://dbpedia.org/property/dmozProperty'])) unset($triples['http://dbpedia.org/property/dmozProperty']);
			if (isset($triples['http://dbpedia.org/property/color'])) unset($triples['http://dbpedia.org/property/color']);
			if (isset($triples['http://dbpedia.org/property/imageCaption'])) unset($triples['http://dbpedia.org/property/imageCaption']);
			if (isset($triples['http://dbpedia.org/property/name'])) unset($triples['http://dbpedia.org/property/name']);
			if (isset($triples['http://dbpedia.org/property/audioIpaProperty'])) unset($triples['http://dbpedia.org/property/audioIpaProperty']);
			if (isset($subjecttriples['http://dbpedia.org/property/redirect'])) unset($subjecttriples['http://dbpedia.org/property/redirect']);
			if (isset($triples['http://dbpedia.org/property/audioDeProperty'])) unset($triples['http://dbpedia.org/property/audioDeProperty']);
			if (isset($triples['http://dbpedia.org/property/art'])) unset($triples['http://dbpedia.org/property/art']);
			if (isset($subjecttriples['http://dbpedia.org/property/babAsProperty'])) unset($subjecttriples['http://dbpedia.org/property/babAsProperty']);
			if (isset($triples['http://dbpedia.org/property/babAsProperty'])) unset($triples['http://dbpedia.org/property/babAsProperty']);
			if (isset($triples['http://dbpedia.org/property/imageWidth'])) unset($triples['http://dbpedia.org/property/imageWidth']);
			if (isset($triples['http://dbpedia.org/property/rangeMapWidth'])) unset($triples['http://dbpedia.org/property/rangeMapWidth']);
			if (isset($triples['http://dbpedia.org/property/rangeMapCaption'])) unset($triples['http://dbpedia.org/property/rangeMapCaption']);
			if (isset($subjecttriples['http://dbpedia.org/property/nihongoProperty'])) unset($subjecttriples['http://dbpedia.org/property/nihongoProperty']);
			if (isset($triples['http://dbpedia.org/property/shortDescription'])) unset($triples['http://dbpedia.org/property/shortDescription']);
			if (isset($triples['http://dbpedia.org/property/refLabelProperty'])) unset($triples['http://dbpedia.org/property/refLabelProperty']);
			if (isset($triples['http://dbpedia.org/property/noteLabelProperty'])) unset($triples['http://dbpedia.org/property/noteLabelProperty']);
			if (isset($triples['http://dbpedia.org/property/wikisourcelangProperty'])) unset($triples['http://dbpedia.org/property/wikisourcelangProperty']);
			if (isset($triples['http://dbpedia.org/property/lk'])) unset($triples['http://dbpedia.org/property/lk']);
			if (isset($triples['http://dbpedia.org/property/abbr'])) unset($triples['http://dbpedia.org/property/abbr']);
			if (isset($triples['http://dbpedia.org/property/x'])) unset($triples['http://dbpedia.org/property/x']);
			if (isset($triples['http://dbpedia.org/property/y'])) unset($triples['http://dbpedia.org/property/y']);
			if (isset($triples['http://dbpedia.org/property/imageFlagSize'])) unset($triples['http://dbpedia.org/property/imageFlagSize']);
			if (isset($triples['http://dbpedia.org/property/imageCoatOfArmsSize'])) unset($triples['http://dbpedia.org/property/imageCoatOfArmsSize']);
			if (isset($triples['http://dbpedia.org/property/wikiaProperty'])) unset($triples['http://dbpedia.org/property/wikiaProperty']);
			if (isset($triples['http://dbpedia.org/property/coorDmProperty'])) unset($triples['http://dbpedia.org/property/coorDmProperty']);
			if (isset($triples['http://dbpedia.org/property/nuts'])) unset($triples['http://dbpedia.org/property/nuts']);
			if (isset($triples['http://dbpedia.org/property/years'])) unset($triples['http://dbpedia.org/property/years']);
			if (isset($triples['http://dbpedia.org/property/dateofbirth'])) unset($triples['http://dbpedia.org/property/dateofbirth']);
			if (isset($triples['http://dbpedia.org/property/caps_percent_28goals_percent_29'])) unset($triples['http://dbpedia.org/property/caps_percent_28goals_percent_29']);
			if (isset($triples['http://dbpedia.org/property/nationalcaps_percent_28goals_percent_29'])) unset($triples['http://dbpedia.org/property/nationalcaps_percent_28goals_percent_29']);
			if (isset($triples['http://dbpedia.org/property/ntupdate'])) unset($triples['http://dbpedia.org/property/ntupdate']);
			if (isset($triples['http://dbpedia.org/property/footballPlayerStatistics2Property'])) unset($triples['http://dbpedia.org/property/footballPlayerStatistics2Property']);
			if (isset($triples['http://dbpedia.org/property/footballPlayerStatistics3Property'])) unset($triples['http://dbpedia.org/property/footballPlayerStatistics3Property']);
			if (isset($triples['http://dbpedia.org/property/_percent_7D_percent_7D_percent_7B_percent_7BsuccessionBox_percent_23_percent_23_percent_23_percent_23Before'])) unset($triples['http://dbpedia.org/property/_percent_7D_percent_7D_percent_7B_percent_7BsuccessionBox_percent_23_percent_23_percent_23_percent_23Before']);
			if (isset($triples['http://dbpedia.org/property/_percent_7D_percent_7D_percent_7B_percent_7BsuccessionBox_percent_23_percent_23_percent_23_percent_23title'])) unset($triples['http://dbpedia.org/property/_percent_7D_percent_7D_percent_7B_percent_7BsuccessionBox_percent_23_percent_23_percent_23_percent_23title']);
			if (isset($triples['http://dbpedia.org/property/accessdate'])) unset($triples['http://dbpedia.org/property/accessdate']);
			if (isset($triples['http://dbpedia.org/property/state'])) unset($triples['http://dbpedia.org/property/state']);
			if (isset($triples['http://dbpedia.org/property/coordinates'])) unset($triples['http://dbpedia.org/property/coordinates']);
			if (isset($triples['http://dbpedia.org/property/lga'])) unset($triples['http://dbpedia.org/property/lga']);
			if (isset($triples['http://dbpedia.org/property/fedgov'])) unset($triples['http://dbpedia.org/property/fedgov']);
			if (isset($triples['http://dbpedia.org/property/dist'])) unset($triples['http://dbpedia.org/property/dist']);
			if (isset($triples['http://dbpedia.org/property/dir'])) unset($triples['http://dbpedia.org/property/dir']);
			if (isset($triples['http://dbpedia.org/property/float'])) unset($triples['http://dbpedia.org/property/float']);
			if (isset($triples['http://dbpedia.org/property/left'])) unset($triples['http://dbpedia.org/property/left']);
			if (isset($triples['http://dbpedia.org/property/quick'])) unset($triples['http://dbpedia.org/property/quick']);
			if (isset($triples['http://dbpedia.org/property/clear'])) unset($triples['http://dbpedia.org/property/clear']);
			if (isset($triples['http://dbpedia.org/property/utc'])) unset($triples['http://dbpedia.org/property/utc']);
			if (isset($triples['http://dbpedia.org/property/utcDst'])) unset($triples['http://dbpedia.org/property/utcDst']);
			if (isset($triples['http://dbpedia.org/property/spokenWikipedia2Property'])) unset($triples['http://dbpedia.org/property/spokenWikipedia2Property']);
}

function getNegativeExamplesFromParallelClass($posExamples){
	include_once('Settings.php');
	include_once('DatabaseConnection.php');
	//connect to the database
	$settings=new Settings();
	$databaseConnection=new DatabaseConnection($settings->database_type);
	$databaseConnection->connect($settings->database_server,$settings->database_user,$settings->database_pass);
	$databaseConnection->select_database($settings->database_name);
	
	$examples=array();
	foreach ($posExamples as $pos){
		$query="SELECT category FROM articlecategories WHERE name='".$pos."' AND category!='http://dbpedia.org/ontology/Resource'";
		$res=$databaseConnection->query($query);
		if ($databaseConnection->numberOfEntries($res)>0) $zufall = rand(1,$databaseConnection->numberOfEntries($res));
		$i=1;
		while ($result=$databaseConnection->nextEntry($res)){
			if ($i==$zufall) $class=$result['category'];
			$i++;
		}
		$query="SELECT father FROM classhierarchy WHERE child='".$class."'";
		$res=$databaseConnection->query($query);
		if ($databaseConnection->numberOfEntries($res)>0) $zufall = rand(1,$databaseConnection->numberOfEntries($res));
		$i=1;
		while ($result=$databaseConnection->nextEntry($res)){
			if ($i==$zufall) $father=$result['father'];
			$i++;
		}	
		
		$query="SELECT child FROM classhierarchy WHERE father='".$father."' AND child!='".$class."'";
		$res=$databaseConnection->query($query);
		if ($databaseConnection->numberOfEntries($res)>0) $zufall = rand(1,$databaseConnection->numberOfEntries($res));
		$i=1;
		while ($result=$databaseConnection->nextEntry($res)){
			if ($i==$zufall) $child=$result['child'];
			$i++;
		}
		$query="SELECT name FROM articlecategories WHERE category='".$child."' AND name!='".$pos."' ORDER BY RAND() LIMIT 1";
		$res=$databaseConnection->query($query);
		if ($databaseConnection->numberOfEntries($res)>0){
			$result=$databaseConnection->nextEntry($res);
			$examples[]=$result['name'];
		}	
	}
	
	return $examples;
}
?>