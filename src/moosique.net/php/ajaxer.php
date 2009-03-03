<?php

  if (isset($_POST)) {

    $out = '';
    include_once('arc/ARC2.php');
    $rdfParser = ARC2::getRDFParser();
    
    if (isset($_POST['search']) && !empty($_POST['search'])) {
      
      if ($_POST['typeOfSearch'] === 'lastfm') {
        $lastfmContent = '';
        $lastfmResource = 'http://dbtune.org/last-fm/' . urlencode($_POST['search']);
        
        $rdfParser->parse($lastfmResource);
        
        $index = $rdfParser->getSimpleIndex();
        
        $out = json_encode($index);
        
        } else {

        // some dev stuff
        // This is the record where the mp3-samples are from
        $jamendoURI = 'http://dbtune.org/jamendo/sparql/?query=';
        $jamendoResource = $jamendoURI
                 . 'describe'
                 . urlencode(' <')
                 . 'http://dbtune.org/jamendo/record/8654'
                 . urlencode('>');
        
        // arc-testing
        $rdfParser->parse($jamendoResource);
        $index = $rdfParser->getSimpleIndex();
        
        $out = json_encode($index);

        
        /* The song-stream hides in a playlist a la
          <mo:available_as rdf:resource="http://www.jamendo.com/get/track/id/track/audio/play/89531"/>
          <mo:available_as rdf:resource="http://www.jamendo.com/get/track/id/track/audio/xspf/89531"/>
          
          We use the xspf if avaiable and convert it to json and send it back...
        // testing 
        $playlistResource = 'http://www.jamendo.com/get/track/id/track/audio/xspf/89531';
        $playlist = file_get_contents($playlistResource);
        
        include_once('xml2json/xml2json.php');    
        $out = xml2json::transformXmlStringToJson($playlist);
        */
        
        
        
        
      }
      
      
      
    } else {
      $out = json_encode('Searching for nothing results in nothing...');
    } 
    
    echo $out;
  }

?>