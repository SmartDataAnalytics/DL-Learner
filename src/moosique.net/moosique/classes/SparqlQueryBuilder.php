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

    /**
     * since there is no ORDER BY RAND() in Sparql and we are limiting
     * the number of results for performance, we have to do sth. to
     * at least randomize the results a bit..., the idea is to ORDER BY
     * the different variables ASC/DESC randomly always used variables are
     *?artist, ?artistName, ?record and ?playlist (record or track)
     */
    $orderBy = '';
    /* BUG: Nice idea, but the jamendo-sparql-endpoint just ignores limit 
       when using order-by, thus useless approach */
    /*
    $randomVariables = array('?artist', '?artistName', '?record', '?playlist');
    $randomOrder = array('ASC', 'DESC');
    if ($searchType != 'currentInfo') {
      $orderBy = ' ORDER BY ' . $randomOrder[array_rand($randomOrder)] 
               . '(' . $randomVariables[array_rand($randomVariables)] . ')' . "\n";
    }
    */

    // we need all information we can get, everytime, thus *
    $beginStatement = 'SELECT DISTINCT * WHERE { ' . "\n";
    $endStatement = ' }' . $orderBy . $limit;

    $query = '';
    switch($searchType) {
      case 'artistSearch'    : $query = $this->queryArtistSearch($search); break;
      case 'tagSearch'       : $query = $this->queryTagSearch($search); break;
      case 'songSearch'      : $query = $this->querySongSearch($search); break;
      case 'lastFM'          : $query = $this->queryTagSearch($search); break;
      case 'recommendations' : $query = $this->queryRecommendations($search); break;
      
      /* TODO build functions for other queries
      case 'currentInfo'    : $query = $this->queryCurrentInfo($search); break;
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
   * For a Tag-Search we display the playable records, therefore we need record and 
   * artist information too, AlbumCover is optional
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
   *
   *
   *
   *
   */  
  private function queryRecommendations($search) {
    $queryString = ' {
      ?artist rdf:type mo:MusicArtist ;
              foaf:name ?artistName ;
              foaf:made ?record .
      ?record mo:available_as ?playlist ;
              dc:title ?albumTitle .
      
      OPTIONAL { ?artist foaf:img ?artistImage . }
      OPTIONAL { ?artist foaf:homepage ?artistHomepage . }
      OPTIONAL {
        ?record mo:image ?cover .
        FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
      }
    } ';

    /* ?record tags:taggedWithTag ?tag
       makes the queries blow up high */

    $queryString .= 'FILTER (regex(str(?playlist), "xspf", "i")) . ';
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