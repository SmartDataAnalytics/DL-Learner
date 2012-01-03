<?php
/**
 * This class provides functions to build the sepcial SPARQL-Queries 
 * used in moosique.net
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class SparqlQueryBuilder extends Config {
  
  private $queryString; // The QueryString is stored here
  
  /**
   * Class-Constructor, automatically calls buildQuery with
   * the given variables when initalizing this class 
   *
   * @param mixed $search The clean search-Value, can be an array of values
   * @param string $searchType The type of search
   * @author Steffen Becker
   */
  function __construct($search, $searchType) {
    parent::__construct(); // init config
    $this->buildQuery($search, $searchType);
  }
  
  
  /**
   * Builds the complete Sparql-Query depending on the type of search
   * and saves it in the private $queryString. 
   *
   * @param mixed $search The clean search-Value, can be an array of values
   * @param string $searchType The type of search
   * @author Steffen Becker
   */
  private function buildQuery($search, $searchType) {
    /* Build up the Prefixes */
    $prefixes = '';
    foreach($this->getConfigPrefixes() as $prefix => $resource) {
      $prefixes .= 'PREFIX ' . $prefix . ': <' . $resource . '>' . "\n";
    }

    // if the globalLimit-config-value is set to true, we use maxResults as LIMIT
    if ($this->getConfig('globalLimit')) {
      $limit = "\n" . 'LIMIT ' . $this->getConfig('maxResults');  
    } else {
      $limit = '';
    }

    // we need all information we are asking for everytime, thus *
    $beginStatement = 'SELECT DISTINCT * WHERE { ' . "\n";

    // we always want the xspf-playlist only, the search filters is
    // flagged with "i" for case-insensitive search
    $endStatement = "\n" . ' FILTER (regex(str(?playlist), "xspf", "i")) . } ' . $limit;
    
    $baseQuery = '
      ?artist rdf:type mo:MusicArtist ;
              foaf:name ?artistName ;
              foaf:made ?record .
      ?record rdf:type mo:Record ;
              dc:title ?albumTitle .
    ';

    $query = '';
    switch($searchType) {
      case 'artistSearch'    : $query = $this->queryArtistSearch($search); break;
      case 'tagSearch'       : $query = $this->queryTagSearch($search); break;
      case 'albumSearch'     : $query = $this->queryAlbumSearch($search); break;
      case 'songSearch'      : $query = $this->querySongSearch($search); break;
      case 'recommendations' : $query = $this->queryRecommendations($search); break;
      case 'info'            : $query = $this->queryInfo($search); break;
    }
    // save the query
    $this->queryString = $prefixes . $beginStatement . $baseQuery . $query . $endStatement;
  }
  
  
  /**
   * Creates the SPARQL-Query-Part for an artist search
   *
   * @param string $search The search value, sth. like "Slayer" or "Jimi Hendrix" 
   * @return string The SPARQL-query part for an artist search
   * @author Steffen Becker
   */
  private function queryArtistSearch($search) {
    $queryString = ' {
      ?record mo:available_as ?playlist ;
              tags:taggedWithTag ?tag .
      
      OPTIONAL { ?artist foaf:img ?image . }
      OPTIONAL { ?artist foaf:homepage ?homepage . }
    }';
    
    $queryString .= 'FILTER (regex(str(?artistName), "' . $search . '", "i")) . ';
    return $queryString;
  }
  
  
  /**
   * For a Tag-Search we display the playable records, therefore we need record and 
   * artist information too, AlbumCover is optional
   * if $search is an array, this performs an exact-tagSerach instead
   * 
   * @param mixed $search The search value, sth. like "metal" or "breakcore", String or Array
   * @return string Sparql-Query part for tag-search
   */
  private function queryTagSearch($search) {
    if (is_array($search)) {
      // special case: if array, we do the exakt tagSearch, we have sth. like "stoner" and "rock"
      $queryString = $this->queryExactTagSearch($search);
    } else {
      $queryString = ' {
        ?record tags:taggedWithTag ?tag ; 
                mo:available_as ?playlist .
              
        OPTIONAL {
          ?record mo:image ?cover .
          FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
        }
      } ';
      $queryString .= ' FILTER (regex(str(?tag), "' . $search . '", "i")) . ';
    }
    return $queryString; 
  }
  

  /**
   * This function returns the SPARQL-Query part for an exact-Tag-Search from
   * a search array containing string like "stoner" "doom" "metal"
   * Performs an AND-Search, a result has to have all tags from the array
   *
   * @param mixed $search The search array, containing strings like "stoner", "rock" etc, single string also allowed
   * @return string Sparql-Query part for exact-tag-search
   * @author Steffen Becker
   */
  private function queryExactTagSearch($search) {
    $queryString = ' {
      ?record tags:taggedWithTag ?tag ; ';
    // extra check if no array, we use $search as string
    if (is_array($search)) {
      foreach($search as $tag) {
        $queryString .= ' tags:taggedWithTag <http://dbtune.org/jamendo/tag/' . $tag . '> ; ';
      }
    } else {
      $queryString .= ' tags:taggedWithTag <http://dbtune.org/jamendo/tag/' . $search . '> ; ';
    }
    $queryString .= ' mo:available_as ?playlist .
              
      OPTIONAL {
        ?record mo:image ?cover .
        FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
      }
    } ';
    return $queryString; 
  }


  /**
   * Creates the SPARQL-Query part for an album-Search
   *
   * @param string $search The search-string, a string like "dark side of the moon", "ladyland" etc.
   * @return string Sparql-Query part for an album-search
   * @author Steffen Becker
   */
  private function queryAlbumSearch($search) {
    $queryString = ' {
      ?record tags:taggedWithTag ?tag ; 
              mo:available_as ?playlist .
            
      OPTIONAL {
        ?record mo:image ?cover .
        FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
      }
    } ';
    
    $queryString .= 'FILTER (regex(str(?albumTitle), "' . $search . '", "i")) . ';    
    return $queryString;
  }
  
  
  /**
   * Creates the SPARQL-Query part for a song-Search
   *
   * @param string $search The search-string, a string like "souljacker", "purple haze" etc.
   * @return string Sparql-Query part for an album-search
   * @author Steffen Becker
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
   * Creates the SPARQL-Query part for a recommendations search, this
   * is much like album/tagSearch, but with additional info
   *
   * @param string $search The SPARQL-String retrieved from the recommendations-learn result
   * @return string Sparql-Query part for a recommendation search
   * @author Steffen Becker
   */
  private function queryRecommendations($search) {
    $queryString = ' {
      ?record mo:available_as ?playlist ;
      
      OPTIONAL {
        ?record mo:image ?cover .
        FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
      }
    } ';

    // OPTIONAL { ?artist foaf:img ?image . }
    // OPTIONAL { ?artist foaf:homepage ?artistHomepage . }
    // OPTIONAL { ?record tags:taggedWithTag ?tags . }
    
    // and finally we append the sparql-string from kb-Description-Conversion
    $queryString .= $search;
    return $queryString; 
  }
    

  /**
   * Creates the SPARQL-Query part for additional artist information
   *
   * @param string $search The relation, an album, sth like 
   * @return void
   * @author Steffen Becker
   */
  private function queryInfo($search) {
    $queryString = ' {
      ?artist foaf:made <' . $search . '> .
      ?record mo:available_as ?playlist ;
              tags:taggedWithTag ?tag .

      OPTIONAL { ?artist foaf:img ?image . }
      OPTIONAL { ?artist foaf:homepage ?homepage . }
      OPTIONAL { ?artist foaf:based_near ?location . }
      OPTIONAL { ?artist owl:sameAs ?sameAs . }
      
      OPTIONAL {
        ?record mo:image ?cover .
        FILTER (regex(str(?cover), "1.100.jpg", "i")) .  
      }
    } ';
    return $queryString;
  }
  
  
  /**
   * Returns the final Query-String
   * 
   * @return string The final Complete SPARQL-Query
   * @author Steffen Becker
   */
  public function getQuery() {
    return $this->queryString;
  }
  
}

?>