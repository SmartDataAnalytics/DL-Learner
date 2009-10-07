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
    $this->config = parse_ini_file(dirname(__FILE__) . '/../config.ini', true);

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
   * Returns the value of a general config-entry from config.ini
   *
   * @return String The wanted Configvalue
   * @param String Value for the wanted Configvalue
   */
  public function getConfig($value) {
    return $this->config['general'][$value];
  }
  
  
  /**
   * Returns the value of a last-fm config-entry from config.ini
   *
   * @return String The wanted Configvalue
   * @param String Value for the wanted Configvalue
   */
  public function getConfigLastFM($value) {
    return $this->config['lastFM'][$value];
  }
  
  
  /**
   * Returns the value of an URL defined in config.ini 
   *
   * @return String The wanted Url
   * @param String Value for the wanted Url
   */
  public function getConfigUrl($value) {
    return $this->config['url'][$value];
  }
  
  
  /**
   * This funtion returns one (if specified) or all learning-Config entries from config.ini
   *
   * @param String Value for a single learning-Configuration
   * @return Mixed The wanted value as a string, or - if not specified - complete learingConfig as an array
   */
  
  public function getConfigLearning($prefix = false) {
    if ($prefix !== false) {
      if (isset($this->config['learning'][$prefix])) {
        return $this->config['learning'][$prefix];
      } else {
        return false;
      }
    } else {
      return $this->config['learning'];
    }
  }
  
  
  /**
   * This funtion returns one (if specified) or all prefixes from the config.ini
   *
   * @param String String-Value for a single prefix
   * @return Mixed The wanted prefix as a string, or - if not specified - all Prefixes as an array
   */
  public function getConfigPrefixes($prefix = false) {
    if ($prefix !== false) {
      if (isset($this->config['prefix'][$prefix])) {
        return $this->config['prefix'][$prefix];
      } else {
        return false;
      }
    } else {
      return $this->config['prefix'];
    }
  }
  
}


?>