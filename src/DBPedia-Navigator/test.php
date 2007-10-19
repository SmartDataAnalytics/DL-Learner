<?php

	session_start();
echo "<a href='test.php?clearsession=clear'>start from scratch</a>";
if(isset($_GET['clearsession'])){
	$_SESSION['State_ID'] =false;
	unset($_SESSION['positive']);
	unset($_SESSION['negative']);
}
if (!isset($_SESSION['positive'])){
	$array=array("test");
	$_SESSION['positive']=$array;
}
else{
		$array=$_SESSION['positive'];
		$array[]="test";
		$_SESSION['positive']=$array;
	}
print_r($_SESSION);
?>