<?php

/**
 * 
 * 
 * 
 */
class SparqlQueryBuilder extends Config {
  
  private $queryString; // The QueryString is stored here
  
  /**
   * Class-Constructor, automatically calls buildQuery with
   * the given variables when initalizing this class 
   *
   * @return 
   * @param object $search
   * @param object $typeOfSearch
   * @param object $limit[optional]
   */
  function __construct($search, $searchType, $limit = 0) {
    parent::__construct(); // init config
    $this->buildQuery($search, $searchType, $limit);
  }
  
  
  /**
   * Builds the complete Sparql-Query depending on the type of search
   * and saves it in the private $queryString. 
   *
   * @param object $search
   * @param object $typeOfSearch
   * @param object $limit
   */
  private function buildQuery($search, $searchType) {
    
    /* Build up the Prefixes */
    $prefixes = '';
    foreach($this->getConfigPrefixes() as $prefix => $resource) {
      $prefixes .= 'PREFIX ' . $prefix . ': <' . $resource . '>' . "\n";
    }

    // if a global limit is set, we use it
    // else we use the optional limit given to the parent class when constructing it
    if ($this->getConfig('globalLimit') == 1) {
      $limit = "\n" . 'LIMIT ' . $this->getConfig('maxResults');  
    } else {
      $limit = '';
    }

    // we need all information we are asking for everytime, thus *
    $beginStatement = 'SELECT DISTINCT * WHERE { ' . "\n";
    // we always want the xspf-playlist only, the search filters is
    // flagged with "i" for case-insensitive search
    $endStatement = "\n" . ' FILTER (regex(str(?playlist), "xspf", "i")) . } ' . $limit;
    
    $baseQuery = ' {
      ?artist rdf:type mo:MusicArtist ;
              foaf:name ?artistName ;
              foaf:made ?record .
      ?record rdf:type mo:Record ;
              dc:title ?albumTitle .
    } ';

    $query = '';
    switch($searchType) {
      case 'artistSearch'    : $query = $this->queryArtistSearch($search); break;
      case 'tagSearch'       : $query = $this->queryTagSearch($search); break;
      case 'albumSearch'     : $query = $this->queryAlbumSearch($search); break;
      case 'songSearch'      : $query = $this->querySongSearch($search); break;
      case 'recommendations' : $query = $this->queryRecommendations($search); break;
      
      /* TODO build functions for other queries
      case 'currentInfo'    : $query = $this->queryCurrentInfo($search); break;
      */
    }
    // save the query
    $this->queryString = $prefixes . $beginStatement . $baseQuery . $query . $endStatement;
  }
  
  
  /**
   * Returns the Sparql-Query part for an artist-search 
   *
   * @param Mixed
   * @return String Sparql-Query part for artist-search
   */
  private function queryArtistSearch($search) {
    $queryString = ' {
      ?record mo:available_as ?playlist ;
              tags:taggedWithTag ?tag .
      
      OPTIONAL { ?artist foaf:img ?image . }
      OPTIONAL { ?artist foaf:homepage ?artistHomepage . }
    }';
    
    $queryString .= 'FILTER (regex(str(?artistName), "' . $search . '", "i")) . ';
    return $queryString;
  }
  
  
  /**
   * For a Tag-Search we display the playable records, therefore we need record and 
   * artist information too, AlbumCover is optional
   * Returns the Sparql-Query part for tag-search
   * 
   * @return String Sparql-Query part for tag-search
   */
  private function queryTagSearch($search) {
    if (is_array($search)) {
      // special case again: if array, we do the exakt tagSearch
      $queryString = $this->queryExactTagSearch($search);
    } else {
      $queryString = ' {
        ?record tags:taggedWithTag ?tag ; 
                mo:available_as ?playlist .
              
        OPTIONAL {
          ?record mo:image ?image .
          FILTER (regex(str(?image), "1.100.jpg", "i")) .  
        }
      } ';
      $queryString .= ' FILTER (regex(str(?tag), "' . $search . '", "i")) . ';
    }
    return $queryString; 
  }
  
  
  /**
   * 
   * 
   * 
   * 
   */
  private function queryExactTagSearch($search) {
    $queryString = ' {
      ?record tags:taggedWithTag ?tag ; ';
              
    if (is_array($search)) {
      foreach($search as $tag) {
        $queryString .= ' tags:taggedWithTag <http://dbtune.org/jamendo/tag/' . $tag . '> ; ';
      }
    } else {
      $queryString .= ' tags:taggedWithTag <http://dbtune.org/jamendo/tag/' . $search . '> ; ';
    }
    $queryString .= ' mo:available_as ?playlist .
              
      OPTIONAL {
        ?record mo:image ?image .
        FILTER (regex(str(?image), "1.100.jpg", "i")) .  
      }
    } ';
    
    return $queryString; 
  }


  /**
   * Returns the Sparql-Query part for album-search
   * 
   * @return String Sparql-Query part for album-search
   */
  private function queryAlbumSearch($search) {
    $queryString = ' {
      ?record tags:taggedWithTag ?tag ; 
              mo:available_as ?playlist .
            
      OPTIONAL {
        ?record mo:image ?image .
        FILTER (regex(str(?image), "1.100.jpg", "i")) .  
      }
    } ';
    
    $queryString .= 'FILTER (regex(str(?albumTitle), "' . $search . '", "i")) . ';    
    return $queryString;
  }
  
  
  /**
   * Returns the Sparql-Query part for song-search
   * 
   * @return String Sparql-Query part for song-search
   */
  private function querySongSearch($search) {
    $queryString = ' {
      ?record mo:track ?track ;
              tags:taggedWithTag ?tag .
      ?track  dc:title ?songTitle ;
              mo:available_as ?playlist .
            
      OPTIONAL { ?artist foaf:img ?image . }
    }';
    
    $queryString .= 'FILTER (regex(str(?songTitle), "' . $search . '", "i")) . ';    
    return $queryString;
  }
    
    
  /**
   *
   *
   *
   *
   */  
  private function queryRecommendations($search) {
    $queryString = ' {
      ?record mo:available_as ?playlist ;
      
      OPTIONAL { ?artist foaf:img ?artistImage . }
      OPTIONAL { ?artist foaf:homepage ?artistHomepage . }
      OPTIONAL {
        ?record mo:image ?image .
        FILTER (regex(str(?image), "1.100.jpg", "i")) .  
      }
    } ';

    // TODO ?record tags:taggedWithTag ?tag makes the queries blow up high

    // and finally we append the sparql-string from kb-Description
    $queryString .= $search;
    return $queryString; 
  }
    
    
  /**
   * Returns the build Query-String
   * 
   * @return String Complete SPARQL-Query stored in SparqlQueryBuilder
   */
  public function getQuery() {
    return $this->queryString;
  }
  
  
}


?>