<?php

/**
 * TODO, create a universal SparqlQueryBuilder for the different requests 
 * that can be made by a user or are made by the system for additional data
 * like geodates, images, stuff...
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
  private function buildQuery($search, $searchType, $limit) {
    
    /* Build up the Prefixes */
    $prefixes = '';
    foreach($this->getConfigPrefixes() as $prefix => $resource) {
      $prefixes .= 'PREFIX ' . $prefix . ': <' . $resource . '>' . "\n";
    }

    if ($this->getConfig('globalLimit') == 1) {
      $limit = "\n" . 'LIMIT ' . $this->getConfig('maxResults');  
    } else {
      if ($limit > 0) {
        $limit = "\n" . 'LIMIT ' . $limit;  
      } else {
        $limit = '';
      }
    }

    // we need all information we can get, everytime, thus *
    $beginStatement = 'SELECT * WHERE { ' . "\n";
    $endStatement = ' }' . $limit;
    
    $query = '';
    
    
    switch($searchType) {
      case 'artistSearch' : $query = $this->queryArtistSearch($search); break;
      case 'tagSearch'    : $query = $this->queryTagSearch($search); break;
      case 'songSearch'   : $query = $this->querySongSearch($search); break;
      case 'lastFM'       : $query = $this->queryTagSearch($search); break;
      
      /* TODO build functions for other queries
      case 'albumInfo'    : $query = $this->queryAlbumInfo($search); break;
      case 'songInfo'     : $query = $this->querySongInfo($search); break;
      */
    }

    // save the query
    $this->queryString = $prefixes . $beginStatement . $query . $endStatement;
  }
  
  
  /**
   * Returns the Sparql-Query part for an artist-search 
   *
   * @param Mixed
   * @return String Sparql-Query part for artist-search
   */
  private function queryArtistSearch($search) {
    $queryString = ' {
      ?artist rdf:type mo:MusicArtist ;
              foaf:name ?artistName ;
              foaf:made ?record .
      ?record mo:available_as ?playlist ;
              tags:taggedWithTag ?tag ;
              dc:title ?albumTitle .
      
      OPTIONAL { ?artist foaf:img ?artistImage . }
      OPTIONAL { ?artist foaf:homepage ?artistHomepage . }
    }';
    // we want the xspf-playlist only, the search filters is
    // flagged with  "i" for case-insensitive search
    $queryString .= 'FILTER (regex(str(?playlist), "xspf", "i")) . ';
    $queryString .= 'FILTER (regex(str(?artistName), "' . $search . '", "i")) . ';
    
    return $queryString;
  }
  
  
  /**
   * For a Tag-Search we display the playable records
   * Therefore we need record and artist information too.
   * AlbumCover is optional
   * Returns the Sparql-Query part for tag-search
   * 
   * @return String Sparql-Query part for tag-search
   */
  private function queryTagSearch($search) {
    /* TODO multi-tag search -- maybe building an extra function is better for this 
    $moreThanOneTag = is_array($search);
    $searchCount = 0;
    
    if ($moreThanOneTag === true) {
      $searchCount = count($search);
    }
    */
    $queryString = ' {
      ?artist rdf:type mo:MusicArtist ;
              foaf:name ?artistName ;
              foaf:made ?record .
      ?record rdf:type mo:Record ;
              dc:title ?albumTitle ; ';
    /*          
    if ($moreThanOneTag === true) {
      for ($i = 0; $i < $searchCount; $i++) {
        $queryString .= ' tags:taggedWithTag ?tag' . $i . ' ; ';
      }
    } else {
      */
      $queryString .= ' tags:taggedWithTag ?tag ; ';
      /*
    }
    */
    $queryString .= ' mo:available_as ?playlist .
      OPTIONAL {
        ?record mo:image ?cover .
        FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
      }
    } ';
    // we want the xspf-playlist only, the search filters is
    // flagged with  "i" for case-insensitive search
    $queryString .= ' FILTER (regex(str(?playlist), "xspf", "i")) . ';
    
    // searching for more than on value?
    /*
    if (is_array($search)) {
      $queryString .= ' FILTER (';
  
      // glueing the searches together using AND together
      for($i = 0; $i < $searchCount; $i++) {
        $queryString .= 'regex(str(?tag' . $i  . '), "' . $search[$i] . '", "i") ';
        if ($i !== ($searchCount - 1)) {
          $queryString .= ' && ';
        }
      }
      $queryString .= ' ) . ';
    } else {
      */
      $queryString .= ' FILTER (regex(str(?tag), "' . $search . '", "i")) . ';
      /*
    }
    */
    return $queryString; 
  }
  
  /**
   * Returns the Sparql-Query part for tag-search
   * 
   * @return String Sparql-Query part for song-search
   */
  private function querySongSearch($search) {
    $queryString = ' {
      ?artist rdf:type mo:MusicArtist ;
              foaf:name ?artistName ;
              foaf:made ?record .
      ?record rdf:type mo:Record ;
              mo:track ?track ;
              tags:taggedWithTag ?tag .
      ?track  dc:title ?songTitle ;
              mo:available_as ?playlist .
            
      OPTIONAL { ?artist foaf:img ?artistImage . }
    }';
    
    // we want the xspf-playlist only, the search filters is
    // flagged with  "i" for case-insensitive search
    $queryString .= 'FILTER (regex(str(?playlist), "xspf", "i")) . ';
    $queryString .= 'FILTER (regex(str(?songTitle), "' . $search . '", "i")) . ';    
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