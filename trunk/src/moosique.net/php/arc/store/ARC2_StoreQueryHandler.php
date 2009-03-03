<?php
/*
homepage: http://arc.semsol.org/
license:  http://arc.semsol.org/license

class:    ARC2 RDF Store Query Handler
author:   Benjamin Nowack
version:  2008-02-01 (Addition: check for allow_extension_functions option)
*/

ARC2::inc('Class');

class ARC2_StoreQueryHandler extends ARC2_Class {

  function __construct($a = '', &$caller) {
    parent::__construct($a, $caller);
  }
  
  function ARC2_StoreQueryHandler($a = '', &$caller) {
    $this->__construct($a, $caller);
  }

  function __init() {/* db_con */
    parent::__init();
    $this->xsd = 'http://www.w3.org/2001/XMLSchema#';
    $this->allow_extension_functions = $this->v('store_allow_extension_functions', 1, $this->a);    
    $this->handler_type = '';
  }

  /*  */

  function getTermID($val, $term = '', $id_col = 'cid') {
    //$id_col = ($this->handler_type == 'select') ? 'cid' : 'id';
    return $this->store->getTermID($val, $term, $id_col);
  }
  
  /*  */

  function getTripleTable() {
    $r = $this->store->getTablePrefix() . 'triple';
    return $r;
  }
  
  /*  */
  
}
