<?php
	$start=microtime(true);
	$client = new SoapClient("http://www.foreclosuredatabank.com/soapserver.php?wsdl");
	print($client->latestProperties('33149'));
	$stop=microtime(true)-$start;
	print "<br/>".$stop;
?>