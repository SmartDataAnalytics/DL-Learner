<?php

/**
 * The basic Config class, stores all configuration in itself and
 * initializes a debugger if debugging is active
 *
 * @package moosique.net
 * @author Steffen Becker
 */
class Config {

  protected $config;
  protected $debugger;
    
  /**
   * On Class initialization, read the ini file to get 
   * the config values and hand them to $this->config
   * and create a new debugger-class if debugging is activated
   */
  function __construct() {
    $this->config = parse_ini_file(dirname(__FILE__) . '/../config.ini', true);

    // we activate the debugger output if debugging is active
    if ($this->getConfig('debug'))  {
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
   * @return string The wanted LastFM-Configvalue
   * @param string Value for the wanted Configvalue
   */
  public function getConfigLastFM($value) {
    return $this->config['lastFM'][$value];
  }
  
  
  /**
   * Returns the value of an URL defined in config.ini 
   *
   * @return string The wanted Url
   * @param string Value for the wanted Url
   */
  public function getConfigUrl($value) {
    // prepend base-url for wsdl and allRecords
    if ($value == 'wsdlLocal' || $value == 'allRecords') {
      return $this->config['url']['base'] . $this->config['url'][$value];
    }
    // prepend absPath (file:/) for ontology
    if ($value == 'tagOntology') {
      return 'file://' . $this->config['url']['absPath'] . $this->config['url'][$value];
    }
    return $this->config['url'][$value];
  }
  
  
  /**
   * This funtion returns one (if specified) or all learning-Config values from config.ini
   *
   * @param string Value for a single learning-Configuration, optional
   * @return mixed The wanted value as a string, or - if not specified - complete learingConfig as an array
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
   * @param string String-Value for a single prefix, optional
   * @return mixed The wanted prefix as a string, or - if not specified - all Prefixes as an array
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