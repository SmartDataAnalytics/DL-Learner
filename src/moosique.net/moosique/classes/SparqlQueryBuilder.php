<?php

/**
 * TODO, create a universal SparqlQueryBuilder for the different requests 
 * that can be made by a user or are made by the system for additional data
 * like geodates, images, stuff...
 */
class SparqlQueryBuilder extends Config {
  
  private $queryString;
  
  function __construct($search, $typeOfSearch) {
    parent::__construct(); // init config
    
    /* Build up the Prefixes */
    $prefixes = '';
    foreach($this->getAllPrefixes() as $prefix => $resource) {
      $prefixes .= 'PREFIX ' . $prefix . ': <' . $resource . '>' . "\n";
    }

    $beginWhere = 'WHERE { ' . "\n";
    $endWhere = ' }';
    if ($this->getConfig('globalLimit') == 1) {
      $limit = "\n" . 'LIMIT ' . $this->getConfig('maxResults');  
    } else {
      $limit = '';
    }
    

    switch($typeOfSearch) {
      
      /* in case artistSearch tags, an image of the artist and the 
       * homepage of the artist are optional, playable albums aren't
       */
      case 'artistSearch' :
        $select = 'SELECT * ' . "\n";
        
        $where = ' {
          ?artist rdf:type mo:MusicArtist ;
                  foaf:name ?artistName ;
                  foaf:made ?record .
                  
           
          ?record mo:available_as ?playlist ;
                  dc:title ?albumTitle ;
          
          
          OPTIONAL {
            ?record tags:taggedWithTag ?tags .
          }
          
          OPTIONAL {
            ?artist foaf:img ?artistImage .
          }
          
          OPTIONAL {
            ?artist foaf:homepage ?artistHomepage .
          }
        } ';
        
        // we want the xspf-playlist only
        $filter = '
          FILTER (regex(str(?playlist), "xspf", "i")) .            
          FILTER (regex(str(?artistName), "' . $search . '", "i")) . 
        ';

      break;
      
      /* For a Tag-Search we display the playable records
       * Therefore we need record and artist information too.
       * AlbumCover is optional
       */
      case 'tagSearch' :
        $select = 'SELECT * ' . "\n"; // TODO specify this later on
        $where = ' {
          ?artist rdf:type mo:MusicArtist ;
                  foaf:name ?artistName ;
                  foaf:made ?record .

          ?record rdf:type mo:Record ;
                  dc:title ?albumTitle ;
                  tags:taggedWithTag ?tag ;
                  mo:available_as ?playlist .
                  
          OPTIONAL {
            ?record mo:image ?cover .
            FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
          }
        }';
        
        $filter = '      
          FILTER (regex(str(?playlist), "xspf", "i")) .      
          FILTER (regex(str(?tag), "' . $search . '", "i")) . 
        ';
      break;
      
      case 'songSearch' :
        $select = 'SELECT * ' . "\n"; // TODO specify this later on
        $where = ' {
          ?artist rdf:type mo:MusicArtist ;
                  foaf:name ?artistName ;
                  foaf:made ?record .

          ?record rdf:type mo:Record ;
                  dc:title ?albumTitle ;
                  mo:track ?song .
         
          ?song dc:title ?songTitle ;
                mo:available_as ?playlist ;
                mo:track_number ?numberOnAlbum .

          } ';
        
        /* we only want xspf-playlists for easier parsing */
        $filter = '            
          FILTER (regex(str(?songTitle), "' . $search . '", "i")) . 
          FILTER (regex(str(?playlist), "xspf", "i")) . 
        ';

      break;      
      
      /* Additional Information searches */
      case 'artistInformation' :
      break;
      
      case 'albumInformation' :
      break;
      
      case 'songInformation' :
      break;
      
      default :
        $where = '';
      break;
      
    }

    // the search filters - flag "i" for case-insensitive
    
      
    
    
    $finalQuery = $prefixes . $select . 
                  $beginWhere . 
                    $where . $filter . 
                  $endWhere .
                  $limit;
                  
    // save the query
    $this->queryString = $finalQuery;
    
  }
  
  public function getQuery() {
    return $this->queryString;
  }
  
  
}


?>