<?php
/**
 * This class provides methods for recommendations-creation,
 * setting and getting positive examples and instances and 
 * creating and retrieving recommendations using a connection
 * to the DL-Learner
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class Recommendations extends Config {
  
  private $posExamples;
  private $instances;

  /**
   * Only used for getting the global config in this class
   *
   * @author Steffen Becker
   */
  function __construct() {
    parent::__construct(); // init config
  }


  /**
   * Returns an array of prepared serach-statements to feed the sparql=
   * query-bulider with, converted from the natural description of the results
   *
   * @param object $connection a reference to a DllearnerConnection Class
   * @return mixed A multidimensional array with a SPARQL-Query, the KBsyntax and the score for all results or an error string
   * @author Steffen Becker
   */
  public function getQueries($connection) {
    $queries = array();
    $scores = array();
    $kbSyntaxes = array();
    
    // some posExamples and instances set? ready to go...
    if (!empty($this->posExamples) && !empty($this->instances)) {
      // learn sth!
      $res = $connection->learn($this->instances, $this->posExamples);
      $solutions = json_decode($res);
      $this->debugger->log($solutions, "solutions");
      // do we have some solutions?
      if ($solutions->solution1) {
        foreach ($solutions as $solution) {
          // precentage-value, can also be used for display, nicely formatted
          $score = round($solution->scoreValue*100, 2); 
          // scores below threshold are not used for recommendations
          if ($score >= $this->getConfig('threshold')) {
            // check for everything that is quoted
            $match = true;
            $kbSyntax = $solution->descriptionKBSyntax;
            
            // everything in quotes is a potential tag
            preg_match_all('/\"(\\.|[^\"])*\"/', $kbSyntax, $quoted);
            foreach($quoted[0] as $url) {
              if (preg_match('/^\"http:\/\//', $url)) { // if a URL, check if URL to Tag
                // if only one of the URLS used is not a tag, we don't use it
                if (!preg_match('/^\"http:\/\/dbtune\.org\/jamendo\/tag\//', $url)) {
                  $match = false;
                }
              }
            }
            if ($match) {
              $sparql = $connection->kbToSqarql($kbSyntax);
              // extract the subtring we use for the final sparql-query
              $sparql = str_replace("SELECT ?subject \nWHERE", '', $sparql);
              $sparql = str_replace('LIMIT ' . $this->getConfig('maxResults'), '', $sparql);
              $sparql = str_replace('subject a', 'record tags:taggedWithTag', $sparql);
              // push it to the queries-array and
              $queries[] = $sparql;
              $scores[] = $score;
              $kbSyntaxes[] = $kbSyntax;
            }
          }
        }
      } else {
        $error = 'There was an error creating recommendations. Please try resetting 
                  your recently-listened-to list, and try again.';
      }
    } else {
      $error = 'You have to listen to some songs first to fill up your recently-listened-to-list.';
    }
    
    if (isset($error)) {
      return $error;
    } else {
      // return scores, description and queries
      $recommendations = array('scores' => $scores, 'kbSyntaxes' => $kbSyntaxes, 'queries' => $queries);
      return $recommendations;
    }
  }


  /**
   * This function sets the positive examples in private $posExamples
   * if no array is given, it tries to get the positiveExamples from
   * the cookie moosique (set by moosique.js)
   *
   * @param array $posExamples an Array of url-strings of positive examples
   * @return boolean returns false if sth. goes wrong
   * @author Steffen Becker
   */
  public function setPosExamples($posExamples = false) {
    if ($posExamples === false) {
      $posExamples = array();
      if (!empty($_COOKIE['moosique'])) {
        $recent = json_decode($_COOKIE['moosique'])->recentlyListened;
        foreach($recent as $link) {
          // extract relation from the cookie-link
          preg_match_all('#<a\s*(?:rel=[\'"]([^\'"]+)[\'"])?.*?>((?:(?!</a>).)*)</a>#i', $link, $record);
          $posExamples[] = $record[1][0];
          $posExamples = array_unique($posExamples);
          $this->posExamples = $posExamples;
        }
      } 
    } else {
      if (is_array($posExamples)) {
        $this->posExamples = $posExamples;        
      } else {
        return false;
      }
    }
  }
  


  /**
   * This function sets the instances in private $instances
   * if no array is given, it tries to create instances from 
   * posExamples and a random list of records from allRecords.txt
   *
   * @param array $instances An array of url-strings for records of instances (should contain posExamples)
   * @author Steffen Becker
   */
  public function setInstances($instances = false) {
    if ($instances === false) {
      $instances = array();
      // and then add some random Records _not_ already in this list
      $allRecords = file($this->getConfigUrl('allRecords'));
      // $count = count($this->posExamples); // 50/50
      $count = $this->getConfigLearning('instances');

      for ($i = 0; $i < $count; $i++) {
        $randomRecord = trim($allRecords[array_rand($allRecords)]);
        // no double entries for the $instances-array
        if (!in_array($randomRecord, $this->posExamples)) {
          $instances[] = $randomRecord;
        }
      }
      // merge with posExamples
      $instances = array_merge($this->posExamples, $instances);
      $this->instances = $instances;
    } else {
      if (is_array($instances)) {
        $this->instances = $instances;
      }
    }
  }


  /**
   * Returns the positive Examples
   *
   * @return array Array of positive examples
   * @author Steffen Becker
   */
  public function getPosExamples() {
    return $this->posExamples;
  }


  /**
   * Returns the instances
   *
   * @return array Array of instances (containing positive Examples)
   * @author Steffen Becker
   */
  public function getInstances() {
    return $this->instances;
  }

}

?>