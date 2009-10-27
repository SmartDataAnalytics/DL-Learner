<?php

class Recommendations extends Config {
  
  private $posExamples;
  private $instances;

  function __construct() {
    parent::__construct(); // init config
  }

  /**
   * Returns an array of prepared serach-statements to feed the sparql=
   * query-bulider with, converted from the natural description of the results
   *
   *
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
      
      // do we have some solutions?
      if ($solutions->solution1) {
        foreach ($solutions as $solution) {
          // precentage-value, can also be used for display, nicely formatted
          $score = round($solution->scoreValue*100, 2); 
          // scores below threshold are not used for recommendations
          if ($score > $this->getConfig('threshold')) {
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
              $sparql = $connection->kbToSqarql($solution->descriptionKBSyntax);
              // extract the subtring we use for the final sparql-query
              $sparql = str_replace("SELECT ?subject \nWHERE", '', $sparql);
              $sparql = str_replace('LIMIT ' . $this->getConfig('maxResults'), '', $sparql);
              $sparql = str_replace('subject a', 'record tags:taggedWithTag', $sparql);
              // push it to the queries-array and
              $queries[] = $sparql;
              $scores[] = $score;
              $kbSyntaxes[] = $solution->descriptionKBSyntax;
              
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
   *
   *
   *
   */
  public function setPosExamples($posExamples = false) {
    if ($posExamples === false) {
      $posExamples = array();
      if (!empty($_COOKIE['moosique'])) {
        $recent = json_decode(stripslashes($_COOKIE['moosique']))->recentlyListened;
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
   *
   *
   *
   */
  public function setInstances($instances = false) {
    
    // TODO more testing, what is the optimum posExamples/neutral ratio, 50/50? 
    // for now we assume 50/50
    // $totalInstances = $this->getConfigLearning('instances');
    
    if ($instances === false) {
      $instances = array();
      // and then add some random Records _not_ in this list
      $allRecords = file($this->getConfigUrl('allRecords'));
      $countPos = count($this->posExamples);

      for ($i = 0; $i < $countPos; $i++) {
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
   * 
   * 
   * 
   */
  public function getPosExamples() {
    return $this->posExamples;
  }


  /**
   * 
   * 
   * 
   */
  public function getInstances() {
    return $this->instances;
  }


}

?>


