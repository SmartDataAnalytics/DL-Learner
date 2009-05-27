<?php

/**
 * General Debugger-Class, implementing a FirePHP-Debugger,
 * for usage see http://www.firephp.org/HQ/Use.htm
 * 
 * Usage for Classees implementing Config.php
 * if ($this->debugger) $this->debugger->log($data, "Label");
 * 
 */
class Debugger {
  
  // instance of the FirePHP-Class
  private $fb;
  
  /**
   * Initializes the Debugger by inlcuding and instanciating FirePHP  
   */
  function __construct() {
    require_once('FirePHP.php');
    $this->fb = FirePHP::getInstance(true);
  }
  
  /**
   * Logs an Object using FirePHP
   * 
   * @param Object $var Any Object, Array, String to log
   * @param String $label The Label for the Object to log
   */
  public function log($var, $label) {
    $this->fb->log($var, $label);
  }
  
  
}

?>