<?php 

//handles connection to the database, at the moment only supports mysql-Database
class DatabaseConnection{
	
	var $type;

	function DatabaseConnection($type){
		$this->type=$type;
	}
	
	function connect($server,$user,$pass){
		if ($this->type=='mysql'){
			mysql_connect($server,$user,$pass);
		}
	}
	
	function select_database($database){
		if ($this->type=='mysql'){
			mysql_select_db($database);
		}
	}
	
	function query($query){
		if ($this->type=='mysql'){
			return mysql_query($query);
		}
	}
	
	function nextEntry($result){
		if ($this->type=='mysql'){
			return mysql_fetch_array($result);
		}
	}
	
	function numberOfEntries($result){
		if ($this->type=='mysql'){
			return mysql_num_rows($result);
		}
	}
}
?>