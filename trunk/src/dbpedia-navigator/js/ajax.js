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
    		if (response[1]=='Article not found'){
    			setTimeout("var list=tree.getAllChecked();search_it('label='+document.getElementById('label').value+'&list='+list+'&number=10')",2000);
    		}
    		else
    			document.getElementById('LastArticlesBox').style.display='block';
    		}
    		if (response[5].length>0&&response[6].length>0)
    			loadGoogleMap(response[5],response[6],''+response[1]);
    }
    		
    XhrObj.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    XhrObj.send(param);
}
    	
function show_results(class, number)
{
    var links=document.getElementById('results').getElementsByTagName('p');
    var j=0;
    for (var i=0;i<links.length;i++){
    	if (links[i].getElementsByTagName('a')[0].className==class||class=='all'){
    		if ((j+1)>number&&j<(number+25)) links[i].style.display='block';
    		else links[i].style.display='none';
    		j++;
    	}
    	else links[i].style.display='none';
    }
    if (j<number){
    	show_results(class,0);
    	return;
    }
    		
    var sitenumbers=document.getElementById('sitenumbers').getElementsByTagName('span');
    for (var i=0;i<sitenumbers.length;i++){
    	if ((parseInt(sitenumbers[i].getElementsByTagName('a')[0].innerHTML)-1)*25==number) sitenumbers[i].getElementsByTagName('a')[0].style.textDecoration='none';
    	else sitenumbers[i].getElementsByTagName('a')[0].style.textDecoration='underline';
    	if ((parseInt(sitenumbers[i].getElementsByTagName('a')[0].innerHTML)-1)*25>=j)
    		sitenumbers[i].style.display='none';
    	else 
    		sitenumbers[i].style.display='inline';
    }
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
    				document.getElementById('ConceptInformation').innerHTML=response[1];
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