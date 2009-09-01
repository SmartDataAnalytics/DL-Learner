<?php

/**
 * A General Config-Class for retrieving values from config.ini
 * and initializing the Debugger
 */
class Config {

  protected $config;
  protected $debugger;
    
  /**
   * On Class initialization, read the ini file to get 
   * the config values and hand them to $this->config
   */
  function __construct() {
    $this->config = parse_ini_file('config.ini', true);

    // we activate the debugger output if debugging is active
    if ($this->getConfig('debug') == 1)  {
      require_once('Debugger.php');
      $this->debugger = new Debugger();
      ob_start();
    } else {
      $this->debugger = false;
    }
  }
  
  /**
   * 
   * @return String The wanted Configvalue
   * @param String Value for the wanted Configvalue
   */
  public function getConfig($value) {
    return $this->config['general'][$value];
  }
  
  /**
   * 
   * @return String The wanted Url
   * @param String Value for the wanted Url
   */
  public function getConfigUrl($value) {
    return $this->config['url'][$value];
  }
  
  public function getLearningConfig() {
    return $this->config['learning'];
  }
  
  /**
   * 
   * @return Array An associative array of all prefixes 
   */
  public function getAllPrefixes() {
    return $this->config['prefix'];
  }  
}


?>