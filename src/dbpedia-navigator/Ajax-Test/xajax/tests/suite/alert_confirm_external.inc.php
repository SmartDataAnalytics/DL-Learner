<?php
function showFormValues($aFormValues)
{
	$objResponse = new xajaxResponse();
	$objResponse->alert(print_r($aFormValues, true));
	return $objResponse;
}
?>