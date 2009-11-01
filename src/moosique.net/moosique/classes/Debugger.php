<?php

/**
 * General Debugger-Class, implementing a FirePHP-Debugger,
 * for usage see http://www.firephp.org/HQ/Use.htm
 * 
 * Usage for Classees implementing Config.php:
 * if ($this->debugger) $this->debugger->log($data, "Label");
 * 
 * @package moosique.net
 * @author Steffen Becker
 */
class Debugger {
  
  private $fb; // instance of the FirePHP-Class
  
  /**
   * Initializes the Debugger by inlcuding and instanciating FirePHP  
   *
   * @author Steffen Becker   
   */
  function __construct() {
    require_once('FirePHP.class.php');
    $this->fb = FirePHP::getInstance(true);
  }
  
  
  /**
   * Logs an Object using FirePHP
   * 
   * @param mixed $var Any object, array, string etc. to log
   * @param string $label The label for the object to log
   * @author Steffen Becker
   */
  public function log($var, $label = '') {
    $this->fb->log($var, $label);
  }
  
}

?>