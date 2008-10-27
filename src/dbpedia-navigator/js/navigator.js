function setRunning(running)
{
    if (running) document.getElementById('Loading').style.display='inline';
    else document.getElementById('Loading').style.display='none';
}

function setDatabaseRunning(running)
{
	if (running) document.getElementById('DatabaseLoading').style.display='inline';
    else document.getElementById('DatabaseLoading').style.display='none';
}
    
function loadGoogleMap(Lat,Lng,Label) 
{
    if (GBrowserIsCompatible()) {
		// Create and Center a Map
	   	var map = new GMap2(document.getElementById("map"));
	   	map.setCenter(new GLatLng(Lat, Lng), 12);
	   	map.addControl(new GLargeMapControl());
	   	map.addControl(new GMapTypeControl());
	   	var marker=new GMarker(new GLatLng(Lat, Lng));
	   	map.addOverlay(marker);
	}
}
    	
function show_results(class, number, label) 
{
	var links=document.getElementById('results').getElementsByTagName('p');
	var j=0;
	var names;
	var hasClass;
	
	if (class=='all'&&document.getElementById('FilterTags')) document.getElementById('FilterTags').innerHTML='You currently don\'t filter your search results.';
	else if (document.getElementById('FilterTags')) document.getElementById('FilterTags').innerHTML='Filtered by class: '+label+'.';
		
	for (var i=0;i<links.length;i++){
		if (class=='all'){
			if ((j+1)>number&&j<(number+25)) links[i].style.display='block';
			else links[i].style.display='none';
			j++;
		}
		else{
			names=links[i].getElementsByTagName('a')[0].className.split(' ');
			hasClass=false;
			for (var k=0;k<names.length;k++){
				if (names[k]==class){
					hasClass=true;
					break;
				}
			}
			if (hasClass){
				if ((j+1)>number&&j<(number+25)) links[i].style.display='block';
				else links[i].style.display='none';
				j++;
			}
			else links[i].style.display='none';
		}
		links[i].innerHTML=links[i].innerHTML.substr(0,24)+j+links[i].innerHTML.substr(links[i].innerHTML.indexOf('<a')-7);
	}
	if (j<number){
		show_results(class,0);
	  	return;
	}
		    		
	var sitenumbers=document.getElementById('sitenumbers').getElementsByTagName('span');
	for (var i=0;i<sitenumbers.length;i++){
		if ((parseInt(sitenumbers[i].getElementsByTagName('a')[0].innerHTML)-1)*25==number) sitenumbers[i].getElementsByTagName('a')[0].style.textDecoration='none';
		else sitenumbers[i].getElementsByTagName('a')[0].style.textDecoration='underline';
		if ((parseInt(sitenumbers[i].getElementsByTagName('a')[0].innerHTML)-1)*25>=j||sitenumbers.length<2)
			sitenumbers[i].style.display='none';
		else 
			sitenumbers[i].style.display='inline';
	}
}

function toggleAttributes(element)
{
	var list=element.parentNode.getElementsByTagName('li');
	if (element.innerHTML.match('hide'+'$')=='hide'){
		element.innerHTML='<img src="images/arrow_down.gif">&nbsp;show';
		for (var i=3;i<list.length;i++)
			list[i].style.display='none';
	}
	else {
		element.innerHTML='<img src="images/arrow_up.gif">&nbsp;hide';
		for (var i=3;i<list.length;i++)
			list[i].style.display='list-item';
	}
}

function none()
{}