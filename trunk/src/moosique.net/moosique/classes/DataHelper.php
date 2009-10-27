<?php

class DataHelper extends Config {
  
  public $connection;
    
  /**
   * 
   * 
   * 
   */
  function __construct() {
    parent::__construct(); // init config
    $this->establishConnection();
  }
  
  
  /**
   *
   * 
   */
  public function getData($search, $type) {
    $sparql = new SparqlQueryBuilder($search, $type);
    $query = $sparql->getQuery();
    // sparql-query to dellearner
    $json = $this->connection->sparqlQuery($query);
    // convert to useable object
    $result = json_decode($json);
    $resultObject = $result->results->bindings;     
       
    // prepare the data for HTML processing
    $data = $this->prepareData($resultObject, $type, $search);
    return $data;
  }


  /**
   * Cleans up objects or retrieves them from a XML-File (for playlists)
   * and converts them into arrays for use in the view class
   *
   * @param Mixed $data The Data-Object (from a Sparql-Query or a playlist-array)
   * @param String $type To define what kind of data to prepare
   * @return Array A multidimensional Array ready for processing for HTML-output
   */ 
  public function prepareData($data, $type, $search) {
    $mergedArray = array();
    
    if ($type == 'artistSearch') {
      $mergedArray = $this->mergeArray($data, 'artist');
    }
    if ($type == 'tagSearch' && !is_array($search)) {
      $mergedArray = $this->mergeArray($data, 'tag');
    }
    if (($type == 'tagSearch' && is_array($search)) ||
         $type == 'albumSearch' ||
         $type == 'recommendations') {
      $mergedArray = $this->mergeArray($data, 'record');
    }
    if ($type == 'songSearch') {
      $mergedArray = $this->mergeArray($data, 'track');
    }
    if ($type == 'playlist') {
      $playlistObject = simplexml_load_file($data['playlist']);
      $mergedArray = $this->object2array($playlistObject);
      // prepend the album stream-information
      $mergedArray['albumID'] = $data['albumID'];
    }
    
    // multidimensional array_unique for everything but single-tagSearch and playlist
    if ($type != 'playlist' && $type != 'tagSearch' && !is_array($search)) {
      $mergedArray = $this->arrayUnique($mergedArray);
    }    
    
    return $mergedArray;
  }
  
  
  /**
   * This function merges the result-Object to a nice array
   * we can process easily. The array is created by type,
   * returning the data sorted for artist, tag or song
   *
   * @param Object $data
   * @param String $type This can be 'artist', 'tag' or 'song'
   * @return Array A Multidimensional array sorted by type for output-use
   */   
  private function mergeArray($data, $type) {
    // convert the $data-response object to an array
    $array = $this->object2array($data);
    $combinedArray = array();
        
    foreach($array as $subArray) {
      if (!array_key_exists($subArray[$type]['value'], $combinedArray)) {
        $combinedArray[$subArray[$type]['value']] = $subArray;
      } else {
        // we already have an object with this tag? -> merge!
        $combinedArray[$subArray[$type]['value']] = array_merge_recursive(
          $combinedArray[$subArray[$type]['value']], $subArray
        );
      }
    }
    
    if (!empty($combinedArray)) {
      return $combinedArray;
    } else return false;
  }
  
  /**
   * Like the php-function array_unique, but for multidimensional arrays, calls itself recursively
   * 
   * @return Array (Multidimensional) array without double entries 
   * @param Array $array The Array to clean up
   */
  private function arrayUnique($array) {
    $newArray = array();
    if (is_array($array)) {
      foreach($array as $key => $val) {
        if ($key != 'type' && $key != 'datatype') {
          if (is_array($val)) {
            $val2 = $this->arrayUnique($val);
          } else {
            $val2 = $val;
            $newArray = array_unique($array);
            break;
          }
          if (!empty($val2)) {
            $newArray[$key] = $val2;
          }
        }
      }
    }
    return $newArray;
  }
  
  
  /**
   * Converts a simple Object to an array
   * 
   * @return Array the Array created from the Object
   * @param object $obj The Object to convert
   */
  private function object2array($obj) { 
    $arr = array();
    $_arr = is_object($obj) ? get_object_vars($obj) : $obj; 
    foreach ($_arr as $key => $val) { 
      $val = (is_array($val) || is_object($val)) ? $this->object2array($val) : $val; 
      $arr[$key] = $val; 
    } 
    return $arr;
  }
  
  
  /**
   * Establishes a new Dl-Learner Connection and saves it in 
   * private $connection for class-wide use. 
   *
   * @return 
   */
  private function establishConnection() {
    $this->connection = new DllearnerConnection();
  }
  
  
}


?>