function search_it(param)
{
	setDatabaseRunning(true);
	if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_search.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		if (response[2].length>0){
    			document.getElementById('searchcontent').innerHTML=response[2];
    			document.getElementById('SearchResultBox').style.display='block';
    		}
    	}
    	if (XhrObj.readyState == 4){
    		setDatabaseRunning(false);
    		generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function get_article(param)
{
	setRunning(true);
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_get_article.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4)
    		setRunning(false);
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		if (response[0]!='-'){
	    		document.getElementById('articlecontent').innerHTML=response[0];
	    		document.getElementById('ArticleTitle').innerHTML=response[1];
	    		document.getElementById('lastarticles').innerHTML=response[2];
	    		document.getElementById('Positives').innerHTML=response[3];
	    		document.getElementById('Negatives').innerHTML=response[4];
	    		if (response[6].length>0&&response[7].length>0)
	    			loadGoogleMap(response[6],response[7],''+response[1]);
	    		document.getElementById('LastArticlesBox').style.display='block';
	    		if (response[5]=="false") learnConcept();
	    	}else setTimeout("search_it('label='+document.getElementById('label').value+'&number=10')",2000);
	    	generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}

function get_class(param)
{
	setDatabaseRunning(true);
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_get_class.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		document.getElementById('lastclasses').innerHTML=response[2];
    		document.getElementById('LastClassesBox').style.display='block';
    	}
    	if (XhrObj.readyState == 4){
    		setDatabaseRunning(false);
    		generateURL();
    	}	
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function toPositive(param)
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    
    XhrObj.open("POST",'ajax_to_positive.php',true);
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
    		learnConcept();
    		generateURL();
    	}
    }
    	
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function toNegative(param)
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_to_negative.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
    		learnConcept();
    		generateURL();
    	}
    }
    	
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function clearPositives()
{
	if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_clear_positives.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		document.getElementById('Positives').innerHTML = XhrObj.responseText;
    		generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send('');
}
    	
function clearNegatives()
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    	
    XhrObj.open("POST",'ajax_clear_negatives.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		document.getElementById('Negatives').innerHTML = XhrObj.responseText;
    		learnConcept();
    		generateURL();
    	}
    }
    	
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send('');
}
    	
function removePosInterest(param)
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_remove_pos_interest.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
    		learnConcept();
    		generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function removeNegInterest(param)
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_remove_neg_interest.php',true);
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
    		learnConcept();
    		generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function learnConcept()
{
	setRunning(true);
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    
    XhrObj.open("POST",'ajax_learn_concepts.php',true);
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4){
    		setRunning(false);
    	}
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		if (XhrObj.responseText!='-'){
	    		document.getElementById('conceptlink').innerHTML=XhrObj.responseText;
	    		document.getElementById('ConceptBox').style.display='block';
	    	}
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send('');
}
    	
function stopServerCall()
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_stop_server_call.php',true);
    	
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		setRunning(false);
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send('');
}
    	
function getSubjectsFromConcept(param)
{
	if (document.all){
		//IE
  		var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_get_subjects_from_concept.php',true);
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		if (response[2].length>0){
    			document.getElementById('searchcontent').innerHTML=response[2];
    			document.getElementById('SearchResultBox').style.display='block';
    		}
    		generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}

function getSubjectsFromCategory(param)
{
	setDatabaseRunning(true);
	if (document.all){
		//IE
  		var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_get_subjects_from_category.php',true);
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		if (response[2].length>0){
    			document.getElementById('searchcontent').innerHTML=response[2];
    			document.getElementById('SearchResultBox').style.display='block';
    		}
    	}
    	if (XhrObj.readyState == 4){
    		setDatabaseRunning(false);
    		generateURL();
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}

function setPositivesAndNegatives(param)
{
	if (document.all){
		//IE
  		var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_set_positives_and_negatives.php',true);
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}

function generateURL()
{
	if (document.all){
		//IE
  		var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_generate_URL.php',true);
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		document.getElementById('generatedURL').innerHTML=XhrObj.responseText;
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send('');
}