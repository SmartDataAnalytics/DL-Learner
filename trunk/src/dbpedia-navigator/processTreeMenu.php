<?php header("Content-type:text/xml"); print("<?xml version=\"1.0\"?>");
if (isset($_GET["id"]))
	$url_var=$_GET["id"];
else
	$url_var=0;



print("<tree id='".$url_var."'>");
	if (!$url_var) print("<item child=\"1\" id=\"http://dbpedia.org/class/yago/Entity100001740\" text=\"Entity\"><userdata name='ud_block'>ud_data</userdata></item>");
	else{
		/*require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection();
		$ids=$sc->getIDs();
		$sc=new DLLearnerConnection($ids[0],$ids[1]);
		$categories=$sc->getYagoSubCategories($url_var);*/
		mysql_connect('localhost','navigator','dbpedia');
		mysql_select_db("navigator_db");
		$query="SELECT name FROM articlecategories WHERE category='$url_var' AND name LIKE 'http://dbpedia.org/class/yago/%' LIMIT 100";
		$res=mysql_query($query);
		while ($result=mysql_fetch_array($res)){
			$query="SELECT name FROM articlecategories WHERE category='".$result['name']."' AND name LIKE 'http://dbpedia.org/class/yago/%' LIMIT 1";
			$res2=mysql_query($query);
			if (mysql_num_rows($res2)>0) $child=1;
			else $child=0;
			$query="SELECT label FROM categories WHERE category='".$result['name']."' LIMIT 1";
			$res2=mysql_query($query);
			$result2=mysql_fetch_array($res2);
			print("<item child=\"".$child."\" id=\"".$result['name']."\" text=\"".$result2['label']."\"><userdata name=\"myurl\">".$result['name']."</userdata><userdata name=\"mylabel\">".$result2['label']."</userdata></item>");
		}
	}
print("</tree>");
?> 
