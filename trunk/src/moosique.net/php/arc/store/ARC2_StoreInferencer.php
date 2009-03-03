<?php
/*
homepage: http://arc.semsol.org/
license:  http://arc.semsol.org/license

class:    ARC2 Store Consolidator
author:   Benjamin Nowack
version:  2008-04-03 (Tweak: Changed locking approach from "LOCK TABLE" to "GET LOCK")
*/

ARC2::inc('Class');

class ARC2_StoreInferencer extends ARC2_Class {

  function __construct($a = '', &$caller) {/* caller has to be a store */
    parent::__construct($a, $caller);
  }
  
  function ARC2_StoreInferencer($a = '', &$caller) {
    $this->__construct($a, $caller);
  }

  function __init() {
    parent::__init();
    $this->store =& $this->caller;
  }

  /*  */
  
  function run($res = '') {
    return array_merge($this->consolidate($res), $this->inferLabels($res));
  }
  
  /*  */

  function getTripleTable($p) {
    return $this->store->getTablePrefix() . 'triple';
  }
  
  function getTripleTables() {
    $prefix = $this->store->getTablePrefix();
    return array(
      $prefix . 'triple',
    );
  }

  /*  */

  function consolidate($res = '') {
    $r = array('r_count' => 0, 't_count' => 0);
    $r = $this->consolidateIFPs($res);
    //$r[] =$this->consolidateFPs($res);
    $r['dupes_removed'] = $this->removeTripleDuplicates();
    return $r;
  }
  
  /*  */

  function consolidateIFPs($res = '') {
    $r = array('r_count' => 0, 't_count' => 0, 'new_s2val_count' => 0, 'new_o2val_count' => 0);
    $ps = $this->store->getSetting('ifps', array());
    foreach($ps as $p) {
      if ($p = trim($p)) {
        $sub_r = $this->consolidateIFP($p, $res, 0);
        $r['r_count'] += $sub_r['r_count'];
        $r['t_count'] += $sub_r['t_count'];
        $r['new_o2val_count'] += $sub_r['new_o2val_count'];
      }
    }
    return $r;
  }
  
  function consolidateIFP($p, $res = '', $remove_dupes = 1) {
    $r = array('r_count' => 0, 't_count' => 0, 'new_o2val_count' => 0);
    $con = $this->store->getDBCon();
    $tbls = $this->getTripleTables();
    $prefix = $this->store->getTablePrefix();
    if (!$this->store->getLock(2)) return $this->addError('Could not get lock in "consolidateIFP" (' . $p . ')');
    do {
      $proceed = 0;
      $tbl = $this->getTripleTable($p);
      $p_id = $this->store->getTermID($p, 'p');
      $empty_id = $this->store->getTermID('', 'o');
      $s = $res ? $this->store->getTermID($res, 's') : '';
      $filter = $res ? "(T1.s = " . $s . " OR T2.s = " . $s. ") AND " : '';
      $sql = "
        SELECT DISTINCT T1.s, T1.s_type, T1.o 
        FROM (" . $tbl . " T1) 
        JOIN " . $tbl . " T2 ON (
          " . $filter. "(T1.p = " . $p_id. ") 
          AND (T1.s != T2.s)
          AND (T2.p = T1.p)
          AND (T2.o = T1.o)
        )
        WHERE 
          T1.o != " . $empty_id . "
        ORDER BY T1.o, T1.s_type /* prefer URIs */
      ";
      $rs = mysql_query($sql, $con);
      if ($er = mysql_error($con)) $this->addError($er);
      $o = '';
      $s = '';
      $s_type = '';
      while ($row = mysql_fetch_array($rs)) {
        $cur_s = $row['s'];
        $cur_o = $row['o'];
        if ($cur_o != $o) {/* new set */
          $s = $cur_s;
          $s_type = $row['s_type'];
          $o = $cur_o;
        }
        else {
          $proceed = 1;
          $r['r_count'] += 1;
          /* set V.cid */
          foreach (array('s', 'o') as $col) {
            $sql = "UPDATE " . $prefix . $col . "2val SET cid = " . $s . " WHERE cid = " . $cur_s;
            $sub_r = mysql_query($sql, $con);
            if ($er = mysql_error($con)) $this->addError($er);
          }
          /* set T.o/T.s */
          foreach ($tbls as $tbl) {
            $sql = "UPDATE " . $tbl . " SET o = " . $s . ", o_type = " . $s_type . " WHERE o = " . $cur_s;
            $sub_r = mysql_query($sql, $con);
            if ($er = mysql_error($con)) $this->addError($er);
            $r['t_count'] += mysql_affected_rows($con);
            $sql = "UPDATE " . $tbl . " SET s = " . $s . ", s_type = " . $s_type . " WHERE s = " . $cur_s;
            $sub_r = mysql_query($sql, $con);
            if ($er = mysql_error($con)) $this->addError($er);
            $r['t_count'] += mysql_affected_rows($con);
          }
          /* add id mapping to o2val */
          $sql = "
            INSERT IGNORE INTO " . $prefix . "o2val (id, cid, misc, val)
            SELECT cid, cid, misc, val FROM " . $prefix . "s2val WHERE cid = " . $s . "
          ";
          $sub_r = mysql_query($sql, $con);
          if ($er = mysql_error($con)) $this->addError($er);
          $r['new_o2val_count'] += mysql_affected_rows($con);
        }
      }
    } while ($proceed);
    $this->store->releaseLock();
    if ($remove_dupes) {/* called directly */
      $r['dupes_removed'] = $this->removeTripleDuplicates();
    }
    return $r;
  }

  /*  */

  function consolidateFPs($res = '') {
    $r = array();
    $ps = $this->store->getSetting('fps', array());
    foreach($ps as $p) {
      if ($p = trim($p)) {
        $r[] = $this->consolidateFP($p, $res);
      }
    }
    return $r;
  }
  
  function consolidateFP($p, $res = '') {
  
  }
  
  /*  */
  
  function removeTripleDuplicates() {
    $con = $this->store->getDBCon();
    $tbls = $this->getTripleTables();
    $dbv = $this->store->getDBVersion();
    $r = 0;
    if (!$this->store->getLock()) return $this->addError('Could not get lock in "removeTripleDuplicates"');
    foreach ($tbls as $tbl) {
      /* can't reliably use single DELETE (http://bugs.mysql.com/bug.php?id=5733) */
      $sql = '
        SELECT T2.t
        FROM (' . $tbl . ' T1) 
        JOIN ' . $tbl . ' T2 ON (
          (T2.s = T1.s)
          AND (T2.t > T1.t)
          AND (T2.p = T1.p)
          AND (T2.o = T1.o)
          AND (T2.o_lang_dt = T1.o_lang_dt)
          AND (T2.s_type = T1.s_type)
          AND (T2.o_type = T1.o_type)
        )
        LIMIT 50
      ';
      $loops = 0;
      do {
        $proceed = 0;
        $t_code = '';
        $rs = mysql_query($sql, $con);
        if ($er = mysql_error($con)) $this->addError($er);
        while ($row = mysql_fetch_array($rs)) {
          $t_code .= $t_code ? ', ' . $row['t'] : $row['t'];
        }
        if ($t_code) {
          $proceed = 1;
          $loops++;
          $del_sql = 'DELETE FROM ' . $tbl . ' WHERE t IN (' . $t_code . ')';
          $del_rs = mysql_query($del_sql, $con);
          if ($er = mysql_error($con)) $this->addError($er);
          $r += mysql_affected_rows($con);
        }
      } while ($proceed && ($loops < 1000));
    }
    $this->store->releaseLock();
    return $r;
  }

  /*  */

  function inferLabels($res = '') {
    $this->label_props = array('foaf:name', 'dc:title', 'foaf:nick', 'rdfs:label', 'skos:prefLabel');
  }
  
  /*  */
  
}
