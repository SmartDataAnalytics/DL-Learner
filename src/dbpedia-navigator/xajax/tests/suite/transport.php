<?php
	require_once("./options.inc.php");
		
	$objResponse = new xajaxResponse();
	
	function testForm($strText, $formData, $arrArray) {
		global $objResponse;
		$data = "Text:\n" . $strText;
		$data .= "\n\nFormData:\n" . print_r($formData, true);
		$data .= "\n\nArray:\n" .print_r($arrArray, true); 
		$objResponse->alert($data);
		$objResponse->assign("submittedDiv", "innerHTML", "<pre>".$data."</pre>");
		return $objResponse;
	}
	
	$testForm = $xajax->register(XAJAX_FUNCTION, "testForm");
	$testForm->useSingleQuote();
	$testForm->addParameter(XAJAX_INPUT_VALUE, "textField1");
	$testForm->addParameter(XAJAX_FORM_VALUES, "testForm1");
	$testForm->addParameter(XAJAX_JS_VALUE, "getTestArray()");
	
	$xajax->processRequest();
	
	echo '<' . '?xml version=1.0' . ' encoding=' . $xajax->getConfiguration('characterEncoding') . ' ?' . '>' . "\n";
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/2000/REC-xhtml1-20000126/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Character Encoding Test | xajax Tests</title>
		<?php $xajax->printJavascript() ?>
		<script type="text/javascript">
			/* <![CDATA[ */
			function getTestArray()
			{
				var text = xajax.$('textField1').value;
				var testArray = new Array();
				testArray[0] = text;
				testArray[1] = text;
				testArray[2] = new Array();
				testArray[2][0] = text;
				testArray[2][1] = text; 
				testArray[3] = new Array();
				testArray[3][0] = text;
				testArray[3][1] = text;
				testArray[3][2] = new Array();
				testArray[3][2][0] = text;
				testArray[3][2][1] = text;
				
				return testArray;
			}
			/* ]]> */
		</script>
	</head>
	<body>
		<h1>Character Encoding Test</h1>
		<h2>Text Test Form</h2>

		<p><a href="http://www.i18nguy.com/unicode-example.html" target="_blank">Here are some Unicode examples</a> you can paste into the text box below.</p>
		<p>You can see <a href="http://www.unicode.org/iuc/iuc10/languages.html" target="_blank">more examples and a list of standard encoding schemes here</a>.</p>

		<form id="testForm1" onsubmit="return false;">
			<p><input type="text" value="Enter test text here" id="textField1" name="textField1" size="60" /></p>
			<p><input type="submit" value="Submit Text" onclick="<?php $testForm->printScript() ?>;" /></p>
		</form>

		<div id="submittedDiv"></div>
		<div id="debugDiv"></div>

	</body>
</html>