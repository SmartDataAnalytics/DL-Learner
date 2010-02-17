/*
 * The only ow object in use
 */
var ow = {};

var ajaxOptions = {
	onFailure: function(resp) {
		alert('Ajax Error!');
	}, 
	onException: function(resp, exception) {
		alert('Ajax Exception: ' + exception);
	}
};

ow.openLayer = function(url) {
	$('layer').toggle();
	new Ajax.Updater('layerContent', ow.uribase + 'rest/' + url, {asynchronous: false});
	Effect.Appear('layerContent', {duration: 0.3});
}

ow.closeLayer = function() {
	$('layer').toggle();
}

ow.deleteStatement = function(id) {
	new Ajax.Request(ow.uribase + 'rest/deleteStatement/?stId=' + id);
	// ow.AjaxRequest(false, 'deleteStatement', id);
	powl.remove($('stmId' + id).parentNode.parentNode);
}

ow.tblEdit = function(img, prop, col) {
	// imgsrc = img.src;
	// img.src = ow.themebase + 'images/Throbber.gif';
	rows = powl.getAncestor(img, 'table').tBodies[0].rows;
	for (var i = 1; i < rows.length; ++i) {
		new Ajax.Updater(rows[i].cells[col+1], ow.uribase + 'rest/editInstance/' + rows[i].cells[0].getAttribute('name') + '/?property=' + prop);
		// ow.AjaxRequest(rows[i].cells[col+1], 'editInstance', rows[i].cells[0].getAttribute('name'), prop);
	}
	//	img.insertAdjacentHTML('afterEnd','<input type="submit" value="save" style="font-size:x-small;" />');
	powl.remove(img);
}

ow.uriEscape = function(string) {
/*	string=string.replace(/Ä/g,'Ae');
	string=string.replace(/Ö/g,'Oe');
	string=string.replace(/Ü/g,'Ue');
	string=string.replace(/ä/g,'ae');
	string=string.replace(/\ö/g,'oe');
	string=string.replace(/ü/g,'ue');
	string=string.replace(/ß/g,'sz');*/
	return string.replace(/[^A-Za-z0-9]/g, '');
}

ow.optionalToggle = function(name, nr) {
	powl.setState('ow.optionalToggle', name, nr);
	e = document.getElementsByName(name);
	var a = 0, d = 0;

	for (var i = 0; i < e.length; i++) {
		if (e[i].nodeName == 'A') {
			e[i].setAttribute('class', a == nr ? 'selected' : '');
			a++;
		} else if (e[i].nodeName == 'DIV') {
			powl.setVisibility(e[i], d == nr ? '' : 'none');
			d++;
		}
	}
}

ow.expand = function(img, instance) {
	if (img.src.search(/plus/) != -1) {
		if (img.nextSibling.nextSibling.innerHTML != '') {
			powl.setVisibility(img.nextSibling.nextSibling, 'block');
		} else {
			var uri = ow.uribase + 'rest/getInstance/?r=' + instance + '&allowEdit=false&maxLength=100';
			new Ajax.Updater(img.nextSibling.nextSibling, uri);
			// ow.AjaxRequest(img.nextSibling.nextSibling, 'ontowiki::renderInstance', instance, 0, 100);
		}
	} else {
		powl.setVisibility(img.nextSibling.nextSibling, 'none');
	}
	powl.togglePlusMinus(img);
}

ow.updateTable = function(el, id) {
	if ($('autosuggest_choices' + id).getStyle('display') != 'block') {
		new Ajax.Updater(el.parentNode.parentNode.getElementsByTagName('td')[1], ow.uribase + 'rest/editValues/?property=' + el.value, {evalScripts: true});
	}
}

///////////////////////////////////////////////////////////////////////////////

/*
 * Editing and tag visibility
 */
function toggleTagVisibility(name) {
	tie = document.getElementById('toggleTagVisibility' + name);
	e = document.getElementsByName(name);
	
	for (var i = 0; i < e.length; i++) {
		powl.toggleVisibility(e[i]);
	}

	if (tie.getAttribute('class') == 'selected') {
		/* powl.setStyleClass(tie, '');*/
		tie.setAttribute('class', '');
	} else {
		/* powl.setStyleClass(tie, 'selected');*/
		tie.setAttribute('class', 'selected');
	}
		
	powl.setCookie('toggleTagVisibility' + name, tie.getAttribute('class'));
}

/*
 * deactivate tag visibility
 */
function deactivateTagVisibility(name) {
	tie = document.getElementById('toggleTagVisibility' + name);
	e = document.getElementsByName(name);
	
	if (tie.getAttribute('class') == 'selected') {
		for (var i = 0; i < e.length; i++) {
			powl.toggleVisibility(e[i]);
		}
		/* powl.setStyleClass(tie, '');*/
		tie.setAttribute('class', '');
		powl.setCookie('toggleTagVisibility' + name, tie.getAttribute('class'));
	}
		

}


function toggleTagVisibilityEdit() {
	toggleTagVisibility('edit');
}

function toggleTagVisibilityComment() {
	toggleTagVisibility('comment');
}

if (powl.getCookie('toggleTagVisibilityedit') == 'selected') {
	powl.SafeAddOnload(toggleTagVisibilityEdit);
}

if (powl.getCookie('toggleTagVisibilitycomment') == 'selected') {
	powl.SafeAddOnload(toggleTagVisibilityComment);
}

/*
 * Image resizing
 */
function resizeContentIMG() {
	imgs = document.getElementsByName('contentIMG');
	for (i = 0; i < imgs.length; i++) {
		resizeIMG(imgs[i]);
	}
}

function resizeIMG(img) {
	maxWidth = 150;
	maxHeight = 0;
	if (maxWidth && img.width > maxWidth) {
		img.height = img.height / (img.width / maxWidth);
		img.width = maxWidth;
	}
	if (maxHeight && img.height > maxHeight) {
		img.width = img.width / (img.height / maxHeight);
		img.height = maxHeight;
	}
}

powl.SafeAddOnload(resizeContentIMG);

/*
 * Box helpers
 */
function toggleBoxContent(el) {
	// get the second div child of the surrounding box
	var boxContent = el.parentNode.parentNode.getElementsByTagName('div')[1];

	if (boxContent.style.display == 'none') {
		// open the box
		// using scriptaculous
		Effect.BlindDown(boxContent, {duration: 0.5});
		// old version
		// boxContent.style.display = '';
		el.innerHTML = '–';
		new Ajax.Request(ow.uribase + '/rest/sessionStore?name=' + el.parentNode.parentNode.id + '_box&value=open');
	}
	else {
		// close the box
		// using scriptaculous
		Effect.BlindUp(boxContent, {duration: 0.5});
		// old version
		// boxContent.style.display = 'none';
		el.innerHTML = '';
		new Ajax.Request(ow.uribase + '/rest/sessionStore?name=' + el.parentNode.parentNode.id + '_box&value=closed');
	}
}

/*
 * Javascript form submit
 */
function subm(form, elem) {
	if (!form.combine || !form.combine.checked) {
		for (i = 0; i < form.elements.length; ++i) {
			if (elem != form.elements[i]) {
				if (form.elements[i].type != 'hidden') {
					form.elements[i].value = '';
				} /*else {
					if (form.elements[i] != 'hidden')
						form.elements[i].selectedIndex = 0;
				}*/
			}
		}
	}
	form.submit();
}

/*
 * Rating support functions
 */
function ratingHighlight(img, num) {
	// get all images named img.name
	imgs = document.getElementsByName(img.name);
	// array of ratings
	var ratings = new Array('Awful', 'Poor', 'Average', 'Good', 'Excellent');
	// for any rating > 0 we need hover pictures
	var name;
	if (num == 0) {
		name = 'white';
	} else {
		name = 'hover';
	}
	var name = (num == 0 ? 'white' : 'hover');
	for (var i = 0; i < imgs.length; i++) {
		imgs[i].src = ow.themebase + 'images/stars/yri_star_' + name + '.png';
		if (((typeof num) == 'undefined' && img == imgs[i]) || i+1 == num) {
			document.getElementById('ratingSpan').innerHTML = ratings[i];
			name = 'white';
		}
	}
}

function ratingReset(img, userRating) {
	if (typeof ow.userRating != 'undefined') {
		ratingHighlight(img, ow.userRating);
	} else {
		ratingHighlight(img, userRating);
	}
	document.getElementById('ratingSpan').innerHTML = '';
}

function ratingSet(instance, rating) {
	// TODO: merge into one request
	new Ajax.Request(ow.uribase + 'rest/setRating/' + instance + '/?rating=' + rating, {asynchronous: false});
	new Ajax.Updater($('ratingContainer'), ow.uribase + 'rest/getRating/' + instance);
	ow.userRating = rating;
}

/*
 * Value editing helper
 */
function editValues(element, property, value) {
	value = value ? value : '';
	new Ajax.Updater(element, ow.uribase + 'rest/editValues/?property=' + property + '&value=' + value);
	// var ret = ow.AjaxRequest(false, 'editValues', cls, property, value);
}

function addProperty(id, property, value) {
	var table = document.getElementById('instanceEdit');
	row = table.insertRow(table.rows.length);
	c1 = row.insertCell(0);
	c2 = row.insertCell(1);
	c1.setAttribute('style', 'vertical-align:top;');
	new Ajax.Updater(c1, ow.uribase + 'rest/getPropertySelector/?property=' + property, {evalScripts: true});
	// c1.insertAdjacentHTML('beforeEnd', getPropertySelector(property));
	// c2.insertAdjacentHTML('beforeEnd', (typeof value == 'string' ? editValues(property, value) : ''));
}
