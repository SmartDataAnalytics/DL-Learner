<?php
/*
	File: thewall.common.php

	Example which demonstrates a xajax implementation of a graffiti wall.
	
	Title: Graffiti Wall Example
	
	Please see <copyright.inc.php> for a detailed description, copyright
	and license information.
*/

/*
	Section: Files
	
	- <thewall.php>
	- <thewall.common.php>
	- <thewall.server.php>
*/

/*
	@package xajax
	@version $Id: thewall.common.php 362 2007-05-29 15:32:24Z calltoconstruct $
	@copyright Copyright (c) 2005-2006 by Jared White & J. Max Wilson
	@license http://www.xajaxproject.org/bsd_license.txt BSD License
*/

require_once ("../../xajax_core/xajax.inc.php");

$xajax = new xajax("thewall.server.php");
$xajax->registerFunction("scribble");
$xajax->registerFunction("updateWall");
?>