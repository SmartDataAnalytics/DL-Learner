<?php header("Content-type:text/xml"); print("<?xml version=\"1.0\"?>");
if (isset($_GET["id"]))
	$url_var=$_GET["id"];
else
	$url_var=0;



print("<tree id='".$url_var."'>");
	if (!$url_var) print("<item child=\"1\" id=\"http://dbpedia.org/class/yago/Entity100001740\" text=\"Entity\"><userdata name='ud_block'>ud_data</userdata></item>");
	else{
		require_once("DLLearnerConnection.php");
		$sc=new DLLearnerConnection();
		$ids=$sc->getIDs();
		$sc=new DLLearnerConnection($ids[0],$ids[1]);
		$categories=$sc->getYagoSubCategories($url_var);
		foreach ($categories as $category){
			if ($category['subclasses']=="0") $child=0;
			else $child=1;
			print("<item child=\"".$child."\" id=\"".$category['value']."\" text=\"".$category['label']."\"><userdata name=\"myurl\">".$category."</userdata></item>");
		}
	}
print("</tree>");
?> 
