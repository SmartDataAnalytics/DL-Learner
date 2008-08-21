function search_it(param)
{
	if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_search.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		if (response[2].length>0){
    			document.getElementById('searchcontent').innerHTML=response[2];
    			document.getElementById('SearchResultBox').style.display='block';
    		}
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
    		
    XhrObj.open("POST",'ajax_get_article.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		document.getElementById('lastarticles').innerHTML=response[2];
    		document.getElementById('Positives').innerHTML=response[3];
    		document.getElementById('Negatives').innerHTML=response[4];
    		setRunning(false);
    		if (response[5].length>0&&response[6].length>0)
    			loadGoogleMap(response[5],response[6],''+response[1]);
    		if (response[1]=='Article not found'){
    			setTimeout("search_it('label='+document.getElementById('label').value+'&number=10')",2000);
    		}
    		else
    			document.getElementById('LastArticlesBox').style.display='block';
    			setTimeout('setRunning(true);learnConcept();',1000);
    		}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}

function get_class(param)
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_get_class.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		document.getElementById('lastclasses').innerHTML=response[2];
    		document.getElementById('LastClassesBox').style.display='block';
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
    
    XhrObj.open("POST",'ajax_to_positive.php');
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
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
    		
    XhrObj.open("POST",'ajax_to_negative.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
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
    		
    XhrObj.open("POST",'ajax_clear_positives.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		document.getElementById('Positives').innerHTML = XhrObj.responseText;
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send();
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
    	
    XhrObj.open("POST",'ajax_clear_negatives.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		document.getElementById('Negatives').innerHTML = XhrObj.responseText;
    	}
    }
    	
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send();
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
    		
    XhrObj.open("POST",'ajax_remove_pos_interest.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
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
    		
    XhrObj.open("POST",'ajax_remove_neg_interest.php');
    		
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('Positives').innerHTML=response[0];
    		document.getElementById('Negatives').innerHTML=response[1];
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function learnConcept()
{
    if (document.all){
    	//IE
    	var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    
    XhrObj.open("POST",'ajax_learn_concepts.php');
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('conceptlink').innerHTML=response[0];
    		document.getElementById('ConceptBox').style.display='block';
    		setRunning(false);
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send();
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
    		
    XhrObj.open("POST",'ajax_stop_server_call.php');
    	
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send();
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
    		
    XhrObj.open("POST",'ajax_get_subjects_from_concept.php');
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}

function getSubjectsFromCategory(param)
{
	if (document.all){
		//IE
  		var XhrObj = new ActiveXObject("Microsoft.XMLHTTP");
    }
    else{
    	//Mozilla
    	var XhrObj = new XMLHttpRequest();
    }
    		
    XhrObj.open("POST",'ajax_get_subjects_from_category.php');
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
    		document.getElementById('articlecontent').innerHTML=response[0];
    		document.getElementById('ArticleTitle').innerHTML=response[1];
    		if (response[2].length>0){
    			document.getElementById('searchcontent').innerHTML=response[2];
    			document.getElementById('SearchResultBox').style.display='block';
    		}
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
    		
    XhrObj.open("POST",'ajax_set_positives_and_negatives.php');
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		var response = XhrObj.responseText.split('$$');
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
    		
    XhrObj.open("POST",'ajax_generate_URL.php');
    
    XhrObj.onreadystatechange = function()
    {
    	if (XhrObj.readyState == 4 && XhrObj.status == 200){
    		document.getElementById('generatedURL').innerHTML=XhrObj.responseText;
    	}
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send();
}