<?php

/**
 * TODO, create a universal SparqlQueryBuilder for the different requests 
 * that can be made by a user or are made by the system for additional data
 * like geodates, images, stuff...
 */
class SparqlQueryBuilder {
  
  private $config;
  private $queryString;
  
  function __construct($config, $search, $typeOfSearch, $results) {
    $this->config = $config;
    $this->buildQuery($search, $typeOfSearch, $results);
  }
  
  function getQuery() {
    return $this->queryString;
  }
  
  private function buildQuery($search, $typeOfSearch, $results) {
    $this->queryString = '';
    $this->queryString .= $this->sparqlPrefixes();
    $this->queryString .= $this->selectStatement($typeOfSearch);
    $this->queryString .= $this->whereStatement($search, $typeOfSearch, $results);
  }
  
  
  private function sparqlPrefixes() {
    $prefixes = '';
    foreach($this->config->getPrefixes() as $prefix => $resource) {
      $prefixes .= 'PREFIX ' . $prefix . ': ' . $resource . "\n";
    }
    return $prefixes;
  }
  
  private function selectStatement($typoOfSearch) {
    $select = 'SELECT ';
    switch($typoOfSearch) {
      
      case 'artist' :
        $select .= '?artist ?album';
      break;
      
      case 'song':
        $select .= '?artist ?album';
      break;
      
      case 'tag': 
        $select .= '?artist ?album';
      break;
    }
    return $select;
  }
  
  private function whereStatement($search, $typeOfSearch, $wantedResults) {
    $where = 'WHERE { ' . "\n";

    switch($typeOfSearch) {
      
      case 'artist' :
        $where .= '
      		?artist a mo:MusicArtist .
      		?artist foaf:name "' . $search . '" .
      		?artist foaf:homepage ?homepage .
      		?record foaf:maker ?artist .
      		?record dc:title ?title .
      		?record mo:image ?image .
        ';
      break;
      
      case 'song' :
        $where .= '
          ?track a mo:Track .
          ?track dc:title "' . $search . '" .
          ?track foaf:maker ?artist .
          ?artist foaf:name ?name .
          ?track dc:title ?tracksname .
          ?record mo:track ?track .
          ?record dc:title ?recordname .
        ';
      break;
      
      case 'tag' :

      break;
      
      // default case is artist
      default :
        $where .= '
      		?artist a mo:MusicArtist .
      		?artist foaf:name "' . $search . '" .
      		?artist foaf:homepage ?homepage .
      		?record foaf:maker ?artist .
      		?record dc:title ?title .
      		?record mo:image ?image .
        ';
      break;
      
    }
    $where .= ' }';
    return $where;
  }
  
}


?>