<?php
	include('helper_functions.php');
	
	session_start();
	$id=$_SESSION['id'];
	session_write_close();
	setRunning($id,"false");
?>