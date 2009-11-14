<?php
/**
 * This class provides functions to get Data from the SPARQL-Endpoints
 * and to prepare and use this data with different array/object methods
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class DataHelper extends Config {
  
  public $connection; // here we save the dl-learnerconnection for public access
    
  /**
   * Get config (parent) and establish a connection to the dl-learner webservice
   *
   * @author Steffen Becker
   */
  function __construct() {
    parent::__construct(); // init config
    $this->establishConnection();
  }
  
  
  /**
   * Get Data from a search using a SPARQL-Requst over the DL-Learner
   *
   * @param mixed $search String/Array with the search-Value, array for more than one value
   * @param string $type the type of search performed (tagSearch, albumSearch etc.)
   * @return array The resulting Data, prepared and ready for use
   * @author Steffen Becker
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
   * @param mixed $data The Data-Object (from a Sparql-Query or a playlist-array)
   * @param string $type the type of search performed (tagSearch, albumSearch etc.)
   * @param mixed $search String/Array with the search-Value, array for more than one value
   * @return array A multidimensional Array ready for processing for HTML-output
   * @author Steffen Becker
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
    if ($type == 'info') { // same as artist, but only first array entry needed
      $mergedArray = $this->mergeArray($data, 'artist');
      $mergedArray = current($mergedArray);
    }
    // multidimensional array_unique for everything but playlist
    if ($type != 'playlist' ) {
      $mergedArray = $this->arrayUnique($mergedArray, $type);
    }    
    return $mergedArray;
  }
  
  
  /**
   * This function merges the result-Object to a nice array
   * we can process easily. The array is created by type,
   * returning the data sorted for artist, tag or song etc.
   *
   * @param object $data
   * @param string $type This can be 'artist', 'tag' or 'song' etc. without 'Search'
   * @return array a Multidimensional array sorted by type for output-use (or false)
   * @author Steffen Becker   
   */   
  public function mergeArray($data, $type) {
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
   * Like the php-function array_unique, but for multidimensional arrays, 
   * calls itself recursively does not unique anything for tagSearch
   * 
   * @param array $array The Array to clean up
   * @return array (Multidimensional) array without double entries 
   * @author Steffen Becker   
   */
  private function arrayUnique($array, $type) {
    $newArray = array();
    if (is_array($array)) {
      foreach($array as $key => $val) {
        // remove type && datatype, we don't need these
        if ($key != 'type' && $key != 'datatype') {
          if (is_array($val)) {
            $val2 = $this->arrayUnique($val, $type);
          } else {
            $val2 = $val;
            if ($type == 'tagSearch') {
              $newArray = $array;
            } else {
              $newArray = array_unique($array);
            }
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
   * Converts an object to an array recursively
   * 
   * @param object $object The object to convert
   * @return the corresponding array created from the object
   * @author Steffen Becker
   */
  private function object2array($object) { 
    $array = array();
    $_array = is_object($object) ? get_object_vars($object) : $object; 
    foreach ($_array as $key => $value) { 
      $value = (is_array($value) || is_object($value)) ? $this->object2array($value) : $value; 
      $array[$key] = $value; 
    } 
    return $array;
  }
  
  
  /**
   * Establishes a new Dl-Learner Connection 
   *
   * @author Steffen Becker
   */
  private function establishConnection() {
    $this->connection = new DllearnerConnection();
  }
  
}

?>