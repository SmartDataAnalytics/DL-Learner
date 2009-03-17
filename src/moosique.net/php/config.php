<?php

/**
 * 
 */
class config {

  private $config;
    
  /**
   * On Class initialization, read the ini file to get 
   * the config values and hand them to $this->config
   */
  function __construct() {
    $this->config = parse_ini_file('config.ini', true);

    // load WSDL files (has to be done due to a Java web service bug)
    include('Utilities.php');
    ini_set('soap.wsdl_cache_enabled', '0');
    Utilities::loadWSDLfiles($this->getUrl('wsdl'));
  }
  
  /**
   * 
   * @return 
   * @param String $value
   */
  function getGeneral($value) {
    return $this->config['general'][$value];
  }

  /**
   * 
   * @return 
   * @param object $value
   */
  function getUrl($value) {
    return $this->config['url'][$value];
  }
  
  /**
   * 
   * @return 
   */
  function getPrefixes() {
    return $this->config['prefix'];
  }  
}

// instantiate the config class
$conf = new config;

?>