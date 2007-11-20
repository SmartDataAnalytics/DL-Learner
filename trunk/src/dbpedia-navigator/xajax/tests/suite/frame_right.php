<?php

$sBaseFolder = dirname(dirname(dirname(__FILE__)));
$sCoreFolder = '/xajax_core';
$sCtrlFolder = '/xajax_controls';

include $sBaseFolder . $sCoreFolder . '/xajaxControl.inc.php';

foreach (array(
	'/document.inc.php',
	'/structure.inc.php',
	'/content.inc.php',
	'/form.inc.php',
	'/group.inc.php',
	'/misc.inc.php') as $sFile)
	include $sBaseFolder . $sCtrlFolder . $sFile;

$objDocument = new clsDocument(array(
	'children' => array(
		new clsDoctype('HTML', '4.01', 'TRANSITIONAL'),
		new clsHtml(array(
			'children' => array(
				new clsHead(array(
					'children' => array(
						new clsTitle(array(
							'child' => new clsLiteral('Title')
							))
						)
					)),
				new clsBody(array(
					'child' => new clsDiv(array(
						'child' => new clsParagraph(array(
							'child' => new clsLiteral('This is the right side frame (PHP)')
							))
						))
					))
				)
			))
		)
	));

$objDocument->printHTML();
