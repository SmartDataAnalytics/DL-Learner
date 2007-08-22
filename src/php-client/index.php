<?php

@session_start();
include_once("Settings.php");
include_once("LearnerClient.php");
include_once("Model.php");

 ini_set("soap.wsdl_cache_enabled","0");
 ini_set('error_reporting',E_ALL);
 ini_set('max_execution_time',200);

$lastAction="View";
$message="";
$possibleActions="";
$instances=" ";
$left="";
$right="";
$middle="";

$settings=new Settings();

echo "<a href='index.php?clearsession=clear'>start from scratch</a>";
if(isset($_GET['clearsession']))$_SESSION['Learner_ID'] =false;



if( (!isset($_SESSION['Learner_ID'] ) )	|| ($_SESSION['Learner_ID']==""))
	{
	//get new ID
	$lc=new LearnerClient($settings->wsdluri);
	$_SESSION['Learner_ID']=$lc->getID();
	//echo "New ID is: ".$_SESSION['Learner_ID']."<br>";
	}
	
else{
	//echo "Current ID is: ".$_SESSION['Learner_ID']."<br>";
	$lc=new LearnerClient($settings->wsdluri,$_SESSION['Learner_ID']);
	}
	
	
	if(isset($_GET['id']) && $_GET['id']>=0)
	{
		$lc=new LearnerClient($settings->wsdluri,$_GET['id']);
		$id=$_GET['id'];
	}
	else 
	{
		$id=$_SESSION['Learner_ID'];
	}
	

if( (!isset($_SESSION['retrieval'] ) )	|| ($_SESSION['retrieval']==""))$_SESSION['retrieval']="inactive";
if(isset($_GET['retrieval'])){	
	$_SESSION['retrieval'] =($_GET['retrieval']=="active")?"active":"inactive";
	
	}
	
	$system="Current id is ".$id;
	$model=new Model($id,$lc,$_SESSION['retrieval']);
// Connection to the SOAP SERVER is established	

// for Information about methods
//$lc->getInfo();


// ANALYSE THE GET VARIABLES

	if(isset($_GET['selectOntology']))
	{
		if($_GET['selectOntology']=="")
		{	//remove Ontology
			$lc->removeOntology($id);
			$lastAction="removed ontology <br>";
		}
		else 
		{	//selectOntology
			
			try{
			$lc->readOntology($id,$_GET['selectOntology']);
			$lastAction="read Ontology <br>".$_GET['selectOntology'];
			}catch(Exception $e)
			{$model->error.="-".$e->getMessage()."<br>";
				
			 $model->errorOccurred=true;}
		}
	}
	
// ADD REMOVE/CLASS
	if(isset($_GET['class']))
	{	$class=$_GET['class'];
		switch($_GET['action']){
			case "select":
					$tmp=$lc->selectAConcept($id,$class,100);
					//print_r( $tmp);
					$lastAction="selected ".$class;
					foreach ($tmp as $one){
						$model->message.="<br>".$one."";
					}
					break;
			case "ignore":
					
					$lc->addIgnoredConcept($id,$class);
					$lastAction="ignored ".$class;
					break; 
					
			case "aknowledge":
					$lc->removeIgnoredConcept($id,$class);
					$lastAction="aknowledged ".$class;
					break;
		}
	
	}


// ADD REMOVE/INSTANCE

	if(isset($_GET['action']))
	{
		switch ($_GET['action'])
		{

		case "add":
			
			$inst=$_GET['subject'];
			
			if ($_GET['where']=='pos')
			{
				//$_SESSION['pos'][]=$inst;
				$lc->addPositiveExample($id,$inst);
				$lastAction="added: <br>".shorten($inst)." <br>to positive examples<br>";
			}
			else if($_GET['where']=="neg")
			{
				//$_SESSION['neg'][]=$inst;
				$lc->addNegativeExample($id,$inst);
				$lastAction="added: <br>".shorten($inst)."<br>to negative examples<br>";
			}
			
			break;

		case "remove":
			$inst=$_GET['subject'];
			
			if($_GET['where']=='examples')
			{   
				if($inst=='all')
				{$lc->removeAllExamples($id);
				}else if($inst=='neg')
				{$lc->removeAllNegativeExamples($id);
				}else if($inst=='pos')
				{$lc->removeAllPositiveExamples($id);
				}
			}
			else if ($_GET['where']=="pos")
				{	
					
					$lc->removePositiveExample($id,$inst);
					$lastAction="removed positive example:<br> ".shorten($inst)."<br>";
					
				}
				else if($_GET['where']=="neg")
				{
					$lc->removeNegativeExample($id,$inst);
					$lastAction="removed negative example:<br> ".shorten($inst)."<br>";
				}


			break;

		}

	}

	// START LEARNING

	
	
	
	
	if(isset($_GET['start']) && $_GET['start']=="start")
	{
				
				$_SESSION['retrieval'] = "inactive";
				$lc->learnMonitored($id);
				
				$model->message.="<br> - retrieval has been automatically deactivated, switch on manually after learning";
				
	}	
	
	if(isset($_GET['stop']) && $_GET['stop']=="stop")
		{
					echo "stopped <br>";
					echo $lc->stop($id);
					
		}	
	
	
	
	if(isset($_GET['plain']) && $_GET['plain']=="plain")
		{
						die;
						
		}	
	
	 
	 // only non saveable processes beyond this point
	
		
	//$model=new Model($id,$lc);
	$model->make();
	$system.="<br>Algorithm status: ".$model->algorithmStatus;
	$system.="<br>Last action:<br>-".$lastAction;
	
	
	if($model->algorithmStatus=="running")
	{	
		$ret=	"algorithm is running<br>
				Currently best solution is:<br>"
				.$lc->getLastResult($id)."<br><br>".
				
				"hit refresh or stop<br>
				<form action=index.php  method=get>
				<input type='submit' name='refresh' value='refresh'>
				<input type='submit' name='stop' value='stop'>
				</form>";
		$middle.=makeBox("Result",$ret,false);
	}
	else if ($model->algorithmStatus=="finished")
	{
		$ret="Last result:<br>".$lc->getLastResult($id)."<br>";
		$middle.= makeBox("Result",$ret,false);
	}
	
	
	$left.=makeBox("Instances",$model->getInstanceView());
	$right.=makeBox("Examples",$model->getExamples());
	
	if(($concepts=$model->getConcepts())!="")
	{	
		$left.=makeBox("Classes",$concepts);
	}
	
	
	if(($subHier=$model->subsumptionHierarchy)!="")
		{	
			$left.=makeBox("Class Tree","<h3><xmp>".$subHier."</xmp></h3>");
	}
	if(($roles=$model->getRoles())!="")
			{	
				$left.=makeBox("Properties",$roles);
	}
	
	$possibleActions=$model->getPossibleActions();
	$ontology=$model->getOntologySelect();
	
	ini_set('error_reporting',E_ALL & ~E_NOTICE);
	$saved=$model->getSavedSets($_GET['savedset'],$_GET['filename'],$_GET['description']);
	ini_set('error_reporting',E_ALL );
	
	
	//echo $saved;
	if ($saved!=""){
		$right.=makebox("Saved Sets",$saved);
	}
	
	if($model->errorOccurred)
	{
		$message="The following error(s) occured: <br>".$model->getError()."<br>";
		$message.="
		<form action=index.php  method=get>
		<input type='submit' name='refresh' value='refresh'>
		</form>";
	} 
	
		$message.=$model->getMessage();
		if($message=="")$message="None";
	
	
	
	
	include("master.php");
	
	echo $masterContent;
	
	
	
	
	
	function makeBox($title,$content,$toggleBoxContent=true)
	{
		if($toggleBoxContent)
		{
			$click="<a class=\"title_switch\" onclick=\"toggleBoxContent(this);\">–</a>";
		}
		else{$click="";}
		$ret="
		<div class=\"box\" id=\"ontology\">
		 <div class=\"boxtitle\">".$title.$click."</div>
		  <div class=\"boxcontent\">
			".$content."
		  </div> <!-- boxcontent -->
		</div> <!-- box -->";
		return $ret;
	}


	function shorten($a)
	{
	    if(($strpos=strpos($a,'#'))>=4){
			return substr($a,$strpos);
		}
		else {return $a;}
	}

?>