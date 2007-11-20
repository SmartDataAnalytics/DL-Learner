<?php

/*
	File: controls_xhtml_10_transitional.php
	
	Test script for html controls; objective is to use each control and to 
	build a heirarchy in compliance with the XHTML 1.0 TRANSITIONAL standard.
	
	See also:
		<controls_html_401_strict.php>
		<controls_html_401_transitional.php>
		<controls_xhtml_10_strict.php>
		<controls_xhtml_11.php>
*/

define('XAJAX_HTML_CONTROL_DOCTYPE_FORMAT', 'XHTML');
define('XAJAX_HMTL_CONTROL_DOCTYPE_VERSION', '1.0');
define('XAJAX_HTML_CONTROL_DOCTYPE_VALIDATION', 'TRANSITIONAL');

$sBaseFolder = dirname(dirname(dirname(__FILE__)));
$sCoreFolder = '/xajax_core';
$sCtrlFolder = '/xajax_controls';

include $sBaseFolder . $sCoreFolder . '/xajax.inc.php';

$xajax = new xajax();

$xajax->configure('javascript URI', '../../');

include $sBaseFolder . $sCtrlFolder . '/validate_XHTML10TRANSITIONAL.inc.php';
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
		new clsDoctype(),
		new clsHtml(array(
			'attributes' => array(
				'xmlns' => 'http://www.w3.org/1999/xhtml',
				'xml:lang' => 'en',
				'lang' => 'en'
				),
			'children' => array(
				new clsHead(array(
					'xajax' => $xajax,
					'children' => array(
						generateTitle(),
						generateStyle(),
						generateScript(),
						generateMeta(),
						generateLink(),
						generateBase()
						)
					)),
				new clsBody(array(
					'children' => array(
						generateOrderedList(),
						generateUnorderedList(),
						generateDefinitionList(),
						generateTable(),
						generateForm(),
						generateContent(),
						generateValidation(),
						generateIframe()
						)
					))
				)
			))
		)
	));
	
function generateTitle()
{
	return new clsTitle(array(
		'child' => new clsLiteral('Title')
		));
}

function generateStyle()
{
	return new clsStyle(array(
		'attributes' => array(
			'type' => 'text/css'
			),
		'child' => new clsLiteral('styleOne { background: #ffdddd; }')
		));
}

function generateScript()
{
	return new clsScript(array(
		'attributes' => array(
			'type' => 'text/javascript'
			),
		'child' => new clsLiteral('javascriptFunction = function(a, b) { alert(a*b); };')
		));
}

function generateMeta()
{
	return new clsMeta(array(
		'attributes' => array(
			'name' => 'keywords',
			'lang' => 'en-us', 
			'content' => 'xajax, javascript, php, ajax'
			)
		));
}

function generateLink()
{
	return new clsLink(array(
		'attributes' => array(
			'type' => 'text/css',
			'href' => './style.css'
			)
		));
}

function generateBase()
{
	global $xajax;
	return new clsBase(array(
		'attributes' => array(
			'href' => dirname($xajax->_detectURI()) . '/'
			)
		));
}

function generateOrderedList()
{
	return new clsOl(array(
		'children' => array(
			new clsLi(array('child' => new clsLiteral('List Item One'))),
			new clsLi(array('child' => new clsLiteral('List Item Two'))),
			new clsLi(array('child' => new clsLiteral('List Item Three'))),
			new clsLi(array('child' => new clsLiteral('List Item Four')))
			)
		));
}

function generateUnorderedList()
{
	return new clsUl(array(
		'children' => array(
			new clsLi(array('child' => new clsLiteral('List Item One'))),
			new clsLi(array('child' => new clsLiteral('List Item Two'))),
			new clsLi(array('child' => new clsLiteral('List Item Three'))),
			new clsLi(array('child' => new clsLiteral('List Item Four')))
			)
		));
}

function generateDefinitionList()
{
	return new clsDl(array(
		'children' => array(
			new clsDt(array('child' => new clsLiteral('Data term one'))),
			new clsDd(array('child' => new clsLiteral('Data term one definition'))),
			new clsDt(array('child' => new clsLiteral('Data term two'))),
			new clsDd(array('child' => new clsLiteral('Data term two definition'))),
			new clsDt(array('child' => new clsLiteral('Data term three'))),
			new clsDd(array('child' => new clsLiteral('Data term three definition')))
			)
		));
}
	
function generateTable()
{
	return new clsTable(array(
		'children' => array(
			new clsCaption(array('child' => new clsLiteral('Table Caption'))),
			new clsColgroup(array(
				'attributes' => array(
					'width' => '20'
					),
				'children' => array(
					new clsCol(array(
						'attributes' => array(
							'span' => '2'
							)
						)),
					new clsCol(array(
						'attributes' => array(
							'class' => 'styleOne'
							)
						))
					)
				)),
			new clsThead(array(
				'child' => new clsTr(array(
					'children' => array(
						new clsTh(array(
							'child' => new clsLiteral('Column One')
							)),
						new clsTh(array(
							'child' => new clsLiteral('Column Two')
							)),
						new clsTh(array(
							'child' => new clsLiteral('Column Three')
							))
						)
					))
				)),
			new clsTfoot(array(
				'child' => new clsTr(array(
					'children' => array(
						new clsTd(array(
							'child' => new clsLiteral('Footer')
							)),
						new clsTd(array(
							'child' => new clsLiteral('is')
							)),
						new clsTd(array(
							'child' => new clsLiteral('here.')
							))
						)
					))
				)),
			new clsTbody(array(
				'children' => array(
					new clsTr(array(
						'children' => array(
							new clsTd(array(
								'child' => new clsLiteral('R1 C1')
								)),
							new clsTd(array(
								'child' => new clsLiteral('R1 C2')
								)),
							new clsTd(array(
								'child' => new clsLiteral('R1 C3')
								))
							)
						)),
					new clsTr(array(
						'children' => array(
							new clsTd(array(
								'child' => new clsLiteral('R2 C1')
								)),
							new clsTd(array(
								'child' => new clsLiteral('R2 C2')
								)),
							new clsTd(array(
								'child' => new clsLiteral('R2 C3')
								))
							)
						)),
					new clsTr(array(
						'children' => array(
							new clsTd(array(
								'child' => new clsLiteral('R3 C1')
								)),
							new clsTd(array(
								'child' => new clsLiteral('R3 C2')
								)),
							new clsTd(array(
								'child' => new clsLiteral('R3 C3')
								))
							)
						)),
					)
				))
			)
		));
}

function generateForm()
{
	return new clsDiv(array(
		'children' => array(
			new clsFieldset(array(
				'children' => array(
					new clsLegend(array('child' => new clsLiteral('Fieldset one'))),
					generateSelect(),
					generateInputText()
					)
				)),
			new clsFieldset(array(
				'children' => array(
					new clsLegend(array('child' => new clsLiteral('Fieldset two'))),
					generateInputCheckbox(),
					generateInputRadioButton(),
					generateInputTextArea(),
					generateInputButton(),
					generateInputHidden()
					)
				))
			)
		));
}

function generateSelect()
{
	return new clsSelect(array(
		'attributes' => array(
			'name' => 'InputSelect',
			'id' => 'InputSelect'
			),
		'children' => array(
			new clsOptionGroup(array(
				'attributes' => array(
					'label' => 'Option Group One'
					),
				'children' => array(
					new clsOption(array(
						'attributes' => array(
							'value' => 'OptionGroupOne_OptionOne'
							),
						'child' => new clsLiteral('Option Group One: Option One')
						)),
					new clsOption(array(
						'attributes' => array(
							'value' => 'OptionGroupOne_OptionTwo'
							),
						'child' => new clsLiteral('Option Group One: Option Two')
						))
					)
				)),
			new clsOptionGroup(array(
				'attributes' => array(
					'label' => 'Option Group Two'
					),
				'children' => array(
					new clsOption(array(
						'attributes' => array(
							'value' => 'OptionGroupTwo_OptionOne'
							),
						'child' => new clsLiteral('Option Group Two: Option One')
						)),
					new clsOption(array(
						'attributes' => array(
							'value' => 'OptionGroupTwo_OptionTwo'
							),
						'child' => new clsLiteral('Option Group Two: Option Two')
						))
					)
				))
			)
		));
}

function generateInputText()
{
	return new clsInputWithLabel('Text: ', 'left', array(
		'attributes' => array(
			'type' => 'text',
			'name' => 'InputText',
			'id' => 'InputText'
			)
		));
}

function generateInputCheckbox()
{
	return new clsInputWithLabel('Checkbox: ', 'left', array(
		'attributes' => array(
			'type' => 'checkbox',
			'name' => 'InputCheckbox',
			'id' => 'InputCheckbox'
			)
		));
}

function generateInputRadioButton()
{
	return new clsInputWithLabel('Radio: ', 'left', array(
		'attributes' => array(
			'type' => 'radio',
			'name' => 'InputRadio',
			'id' => 'InputRadio'
			)
		));
}

function generateInputTextArea()
{
	return new clsTextarea(array(
		'attributes' => array(
			'name' => 'InputTextarea',
			'id' => 'InputTextarea',
			'rows' => '20',
			'cols' => '20'
			)
		));
}

function generateInputButton()
{
	return new clsInput(array(
		'attributes' => array(
			'type' => 'button',
			'name' => 'InputButton',
			'id' => 'InputButton'
			)
		));
}

function generateInputHidden()
{
	return new clsInputWithLabel('Hidden: ', 'left', array(
		'attributes' => array(
			'type' => 'hidden',
			'name' => 'InputHidden',
			'id' => 'InputHidden'
			)
		));
}

function generateContent()
{
	$nbsp = new clsLiteral('&nbsp;');
	
	return new clsDiv(array(
		'children' => array(
			new clsLiteral('literal text'),
			new clsBr(),
			new clsHr(),
			new clsSub(array('child' => new clsLiteral('sub'))),
			$nbsp,
			new clsSup(array('child' => new clsLiteral('sup'))),
			$nbsp,
			new clsEm(array('child' => new clsLiteral('em'))),
			$nbsp,
			new clsStrong(array('child' => new clsLiteral('strong'))),
			$nbsp,
			new clsCite(array('child' => new clsLiteral('cite'))),
			$nbsp,
			new clsDfn(array('child' => new clsLiteral('dfn'))),
			$nbsp,
			new clsCode(array('child' => new clsLiteral('code'))),
			$nbsp,
			new clsSamp(array('child' => new clsLiteral('samp'))),
			$nbsp,
			new clsKbd(array('child' => new clsLiteral('kbd'))),
			$nbsp,
			new clsVar(array('child' => new clsLiteral('var'))),
			$nbsp,
			new clsAbbr(array('child' => new clsLiteral('abbr'))),
			$nbsp,
			new clsAcronym(array('child' => new clsLiteral('acronym'))),
			$nbsp,
			new clsTt(array('child' => new clsLiteral('tt'))),
			$nbsp,
			new clsItalic(array('child' => new clsLiteral('italic'))),
			$nbsp,
			new clsBold(array('child' => new clsLiteral('bold'))),
			$nbsp,
			new clsBig(array('child' => new clsLiteral('big'))),
			$nbsp,
			new clsSmall(array('child' => new clsLiteral('small'))),
			$nbsp,
			new clsIns(array('child' => new clsLiteral('ins'))),
			$nbsp,
			new clsDel(array('child' => new clsLiteral('del'))),
			new clsHeadline('1', array('child' => new clsLiteral('h1'))),
			new clsHeadline('2', array('child' => new clsLiteral('h2'))),
			new clsHeadline('3', array('child' => new clsLiteral('h3'))),
			new clsHeadline('4', array('child' => new clsLiteral('h4'))),
			new clsHeadline('5', array('child' => new clsLiteral('h5'))),
			new clsHeadline('6', array('child' => new clsLiteral('h6'))),
			new clsAddress(array('child' => new clsLiteral('address'))),
			new clsParagraph(array('child' => new clsLiteral('paragraph'))),
			new clsBlockquote(array('child' => new clsLiteral('blockquote'))),
			new clsPre(array('child' => new clsLiteral('pre')))
			)
		));
}

function generateValidation()
{
	return new clsDiv(array(
		'child' => new clsParagraph(array(
			'child' => new clsAnchor(array(
				'attributes' => array(
					'href' => 'http://validator.w3.org/check?uri=referer',
					'target' => '_new'
					),
				'child' => new clsImg(array(
					'attributes' => array(
						'src' => 'http://www.w3.org/Icons/valid-xhtml10-blue',
						'alt' => 'Valid XHTML 1.0 Transitional',
						'height' => 31,
						'width' => 88
						)
					))
				))
			))
		));
}

function generateIframe()
{
	return new clsIframe(array(
		'attributes' => array(
			'src' => 'frameset.php'
			)
		));
}

$objDocument->printHTML();
