<?php

$sBaseFolder = dirname(dirname(dirname(__FILE__)));
$sCoreFolder = '/xajax_core';
$sCtrlFolder = '/xajax_controls';

include $sBaseFolder . $sCoreFolder . '/xajaxControl.inc.php';

foreach (array(
	'/document.inc.php',
	'/content.inc.php',
	'/misc.inc.php') as $sFile)
	include $sBaseFolder . $sCtrlFolder . $sFile;

$objDocument = new clsDocument(array(
	'children' => array(
		new clsDoctype('HTML', '4.01', 'FRAMESET'),
		new clsHtml(array(
			'children' => array(
				new clsHead(array(
					'child' => new clsTitle(array('child' => new clsLiteral('Title')))
					)),
				new clsFrameset(array(
					'attributes' => array(
						'cols' => '50%, 50%'
						),
					'children' => array(
						new clsFrame(array(
							'attributes' => array(
								'name' => 'frame_left',
								'src' => 'frame_left.htm'
								)
							)),
						new clsFrame(array(
							'attributes' => array(
								'name' => 'frame_right',
								'src' => 'frame_right.php'
								)
							))
						)
					))
				)
			))
		)
	));

$objDocument->printHTML();
