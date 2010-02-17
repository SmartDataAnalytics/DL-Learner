<?php
/*
homepage: http://arc.semsol.org/
license:  http://arc.semsol.org/license

class:    ARC2 RDF Store SELECT Query Handler
author:   Benjamin Nowack
version:  2008-12-15 (Undone: Tweak: improved ORDER BY by adding LIMIT to individual UNION branches
                      Fix: added NULL binding to getResultVarsSQL method for same no of columns in UNION queries)
*/

ARC2::inc('StoreQueryHandler');

class ARC2_StoreSelectQueryHandler extends ARC2_StoreQueryHandler {

  function __construct($a = '', &$caller) {/* caller has to be a store */
    parent::__construct($a, $caller);
  }
  
  function ARC2_StoreSelectQueryHandler($a = '', &$caller) {
    $this->__construct($a, $caller);
  }

  function __init() {/* db_con */
    parent::__init();
    $this->store =& $this->caller;
    $con = $this->store->getDBCon();
    $this->handler_type = 'select';
  }

  /*  */

  function runQuery($infos) {
    $con = $this->store->getDBCon();
    /* result vars */
    $vars = array();
    $aggregate_vars = array();
    foreach ($infos['query']['result_vars'] as $entry) {
      $vars[] = $entry['aggregate'] ? $entry['alias'] : $entry['var'];
      if ($entry['aggregate']) $aggregate_vars[] = $entry['alias'];
    }
    $this->infos = $infos;
    $this->infos['null_vars'] = array();
    $index_keys = array(
      'from' => array(),
      'join' => array(),
      'left_join' => array(), 
      'vars' => array(), 'graph_vars' => array(), 'graph_uris' => array(), 
      'bnodes' => array(), 
      'triple_patterns' => array(),  
      'sub_joins' => array(),
      'constraints' => array(),
      'union_branches'=> array(), 
      'patterns' => array(),
      'havings' => array()
    );
    $this->index = $index_keys;
    $this->buildIndex($infos['query']['pattern'], 0);
    $tmp = $this->index;
    $this->analyzeIndex($this->getPattern('0'));
    $this->initial_index = $this->index;
    $this->index = $tmp;
    
    $this->is_union_query = $this->index['union_branches'] ? 1 : 0;
    $indexes = $this->is_union_query ? $this->getUnionIndexes($this->index) : array($this->index);
    $q_sql = '';
    $nl = "\n";
    $this->indexes = array();
    $group_limit_order_sql = '';
    foreach ($indexes as $index) {
      $this->index = array_merge($index_keys, $index);
      $this->analyzeIndex($this->getPattern('0'));
      //$group_limit_order_sql .= $q_sql ? '' : $this->getGROUPSQL() . $this->getORDERSQL() . $this->getLIMITSQL();
      $q_sql .= $q_sql ? $nl . 'UNION' . $this->getDistinctSQL() . $nl : '';
      $q_sql .= $this->is_union_query ? '(' . $this->getQuerySQL() . ')' : $this->getQuerySQL();
      $this->indexes[] = $this->index;
    }
    $q_sql .= $this->is_union_query ? $this->getLIMITSQL() : '';
    $rf = $this->v('result_format', '', $infos);
    /* sql */
    if ($rf == 'sql') return $q_sql;
    /* debug formats */
    if ($rf == 'structure') {
      return $this->infos;
    }
    if ($rf == 'index') {
      return $this->indexes;
    }
    /* create intermediate table */
    $tmp_tbl = 'Q' . md5($q_sql . time() . uniqid(rand()));
    $tmp_sql = 'CREATE TEMPORARY TABLE ' . $tmp_tbl . ' ( ' . $this->getTempTableDef($tmp_tbl, $q_sql) . ') ';
    //$tmp_sql = 'CREATE TABLE ' . $tmp_tbl . ' ( ' . $this->getTempTableDef($tmp_tbl, $q_sql) . ') '; /* mysql sometimes chokes on temp */
    $v = $this->store->getDBVersion();
    $tmp_sql .= (($v < '04-01-00') && ($v >= '04-00-18')) ? 'ENGINE' : (($v >= '04-01-02') ? 'ENGINE' : 'TYPE');
    //$tmp_sql .= ($v < '04-01-00') ? '=MYISAM' : '=MEMORY';/* HEAP doesn't support AUTO_INCREMENT */
    $tmp_sql .= '=MYISAM';/* HEAP doesn't support AUTO_INCREMENT, and MySQL breaks on MEMORY sometimes */
    if (!mysql_query($tmp_sql, $con) && !mysql_query(str_replace('CREATE TEMPORARY', 'CREATE', $tmp_sql), $con)) {
      return $this->addError(mysql_error($con));
    }
    if ($this->v('order_infos', 0, $this->infos['query'])) {
      $q_sql = preg_replace('/SELECT(\s+DISTINCT)?\s*/', 'SELECT\\1 NULL AS `_pos_`, ', $q_sql);
    }
    mysql_unbuffered_query('INSERT INTO ' . $tmp_tbl . ' ' . "\n" . $q_sql, $con);
    if ($er = mysql_error($con)) return $this->addError($er);
    $r = $this->getFinalQueryResult($q_sql, $vars, $tmp_tbl, $aggregate_vars);
    mysql_query('DROP TABLE IF EXISTS ' . $tmp_tbl, $con);
    return $r;
  }
  
  function getTempTableDef($tmp_tbl, $q_sql) {
    $col_part = preg_replace('/^SELECT\s*(DISTINCT)?(.*)FROM.*$/s', '\\2', $q_sql);
    $parts = explode(',', $col_part);
    $r = '';
    $added = array();
    foreach ($parts as $part) {
      if (preg_match('/\.?(.+)\s+AS\s+`(.+)`/U', trim($part), $m) && !isset($added[$m[2]])) {
        $col = $m[1];
        $alias = $m[2];
        $r .= $r ? ',' : '';
        $r .= "\n `" . $alias . "` mediumint UNSIGNED";
        $added[$alias] = 1;
      }
    }
    if ($this->v('order_infos', 0, $this->infos['query'])) {
      $r = "\n" . '`_pos_` mediumint NOT NULL AUTO_INCREMENT PRIMARY KEY, ' . $r;
    }
    return  $r ? $r . "\n" : ''; 
  }
    
  function getFinalQueryResult($q_sql, $vars, $tmp_tbl, $aggregate_vars = '') {
    $aggregate_vars = $aggregate_vars ? $aggregate_vars : array();
    $r = array('variables' => $vars);
    $v_sql = $this->getValueSQL($tmp_tbl);
    $t1 = ARC2::mtime();
    $con = $this->store->getDBCon();
    $rs = mysql_unbuffered_query($v_sql, $con);
    if ($er = mysql_error($con)) {
      $this->addError($er);
    }
    $t2 = ARC2::mtime();
    $rows = array();
    $types = array(0 => 'uri', 1 => 'bnode', 2 => 'literal');
    if ($rs) {
  		while ($pre_row = mysql_fetch_array($rs)) {
        $row = array();
        foreach ($vars as $var) {
          if (isset($pre_row[$var])) {
            $row[$var] = $pre_row[$var];
            $row[$var . ' type'] = isset($pre_row[$var . ' type']) ? $types[$pre_row[$var . ' type']] : (in_array($var, $aggregate_vars) ? 'literal' : 'uri');
            if (isset($pre_row[$var . ' lang_dt']) && ($lang_dt = $pre_row[$var . ' lang_dt'])) {
              if (preg_match('/^([a-z]+(\-[a-z0-9]+)*)$/i', $lang_dt)) {
                $row[$var . ' lang'] = $lang_dt;
              }
              else {
                $row[$var . ' datatype'] = $lang_dt;
              }
            }
          }
        }
        if ($row) {
          $rows[] = $row;
        }
      }
    }
    $r['rows'] = $rows;
    return $r;
  }
  
  /*  */
  
  function buildIndex($pattern, $id) {
    $pattern['id'] = $id;
    $type = $this->v('type', '', $pattern);
    if (($type == 'filter') && $this->v('constraint', 0, $pattern)) {
      $sub_pattern = $pattern['constraint'];
      $sub_pattern['parent_id'] = $id;
      $sub_id = $id . '_0';
      $this->buildIndex($sub_pattern, $sub_id);
      $pattern['constraint'] = $sub_id;
    }
    else {
      $sub_patterns = $this->v('patterns', array(), $pattern);
      foreach ($sub_patterns as $i => $sub_pattern) {
        $sub_pattern['parent_id'] = $id;
        $sub_id = $id . '_' . $i;
        $this->buildIndex($sub_pattern, $sub_id);
        $pattern['patterns'][$i] = $sub_id;
        if ($type == 'union') {
          $this->index['union_branches'][] = $sub_id;
        }
      }
    }
    $this->index['patterns'][$id] = $pattern;
  }
  
  /*  */

  function analyzeIndex($pattern) {
    $type = $pattern['type'];
    $id = $pattern['id'];
    /* triple */
    if ($type == 'triple') {
      foreach (array('s', 'p', 'o') as $term) {
        if ($pattern[$term . '_type'] == 'var') {
          $val = $pattern[$term];
          $this->index['vars'][$val] = array_merge($this->v($val, array(), $this->index['vars']), array(array('table' => $pattern['id'], 'col' =>$term)));
        }
        if ($pattern[$term . '_type'] == 'bnode') {
          $val = $pattern[$term];
          $this->index['bnodes'][$val] = array_merge($this->v($val, array(), $this->index['bnodes']), array(array('table' => $pattern['id'], 'col' =>$term)));
        }
      }
      $this->index['triple_patterns'][] = $pattern['id'];
      /* joins */
      if ($this->isOptionalPattern($id)) {
        $this->index['left_join'][] = $id;
      }
      elseif (!$this->index['from']) {
        $this->index['from'][] = $id;
      }
      elseif (!$this->getJoinInfos($id)) {
        $this->index['from'][] = $id;
      }
      else {
        $this->index['join'][] = $id;
      }
      /* graph infos, graph vars */
      $this->index['patterns'][$id]['graph_infos'] = $this->getGraphInfos($id);
      foreach ($this->index['patterns'][$id]['graph_infos'] as $info) {
        if ($info['type'] == 'graph') {
          if ($info['var']) {
            $val = $info['var']['value'];
            $this->index['graph_vars'][$val] = array_merge($this->v($val, array(), $this->index['graph_vars']), array(array('table' => $id)));
          }
          elseif ($info['uri']) {
            $val = $info['uri'];
            $this->index['graph_uris'][$val] = array_merge($this->v($val, array(), $this->index['graph_uris']), array(array('table' => $id)));
          }
        }
      }
    }
    $sub_ids = $this->v('patterns', array(), $pattern);
    foreach ($sub_ids as $sub_id) {
      $this->analyzeIndex($this->getPattern($sub_id));
    }
  }
  
  /*  */

  function getGraphInfos($id) {
    $r = array();
    if ($id) {
      $pattern = $this->index['patterns'][$id];
      $type = $pattern['type'];
      /* graph */
      if ($type == 'graph') {
        $r[] = array('type' => 'graph', 'var' => $pattern['var'], 'uri' => $pattern['uri']);
      }
      $p_pattern = $this->index['patterns'][$pattern['parent_id']];
      if (isset($p_pattern['graph_infos'])) {
        return array_merge($p_pattern['graph_infos'], $r);
      }
      return array_merge($this->getGraphInfos($pattern['parent_id']), $r);
    }
    /* FROM / FROM NAMED */
    else {
      if (isset($this->infos['query']['dataset'])) {
        foreach ($this->infos['query']['dataset'] as $set) {
          $r[] = array_merge(array('type' => 'dataset'), $set);
        }
      }
    }
    return $r;
  }
  
  /*  */

  function getPattern($id) {
    if (is_array($id)) {
      return $id;
    }
    return $this->v($id, array(), $this->index['patterns']);
  }

  function getInitialPattern($id) {
    return $this->v($id, array(), $this->initial_index['patterns']);
  }

  /*  */
  
  function getUnionIndexes($pre_index) {
    $r = array();
    $branches = array();
    $min_length = 1000;
    foreach ($pre_index['union_branches'] as $id) {
      $branches[$id] = strlen($id);
      $min_length = min($min_length, strlen($id));
    }
    foreach ($branches as $branch_id => $length) {
      if ($length == $min_length) {
        $union_id = substr($branch_id, 0, -2);
        $index = array('keeping' => $branch_id, 'union_branches' => array(), 'patterns' => $pre_index['patterns']);
        $old_branches = $index['patterns'][$union_id]['patterns'];
        $skip_id = ($old_branches[0] == $branch_id) ? $old_branches[1] : $old_branches[0];
        $index['patterns'][$union_id]['type'] = 'group';
        $index['patterns'][$union_id]['patterns'] = array($branch_id);
        $has_sub_unions = 0;
        foreach ($index['patterns'] as $pattern_id => $pattern) {
          if (preg_match('/^' .$skip_id. '/', $pattern_id)) {
             unset($index['patterns'][$pattern_id]);
          }
          elseif ($pattern['type'] == 'union') {
            foreach ($pattern['patterns'] as $sub_union_branch_id) {
              $index['union_branches'][] = $sub_union_branch_id;
            }
          }
        }    
        if ($index['union_branches']) {
          $r = array_merge($r, $this->getUnionIndexes($index));
        }
        else {
          $r[] = $index;
        }
      }
    }
    return $r;
  }

  /*  */

  function isOptionalPattern($id) {
    $pattern = $this->getPattern($id);
    if ($this->v('type', '', $pattern) == 'optional') {
      return 1;
    }
    if ($this->v('parent_id', '0', $pattern) == '0') {
      return 0;
    }
    return $this->isOptionalPattern($pattern['parent_id']);
  }

  function getOptionalPattern($id) {
    $pn = $this->getPattern($id);
    do {
      $pn = $this->getPattern($pn['parent_id']);
    } while ($pn['parent_id'] && ($pn['type'] != 'optional'));
    return $pn['id'];
  }
  
  function sameOptional($id, $id2) {
    return $this->getOptionalPattern($id) == $this->getOptionalPattern($id2);
  }
  
  /*  */

  function isUnionPattern($id) {
    $pattern = $this->getPattern($id);
    if ($this->v('type', '', $pattern) == 'union') {
      return 1;
    }
    if ($this->v('parent_id', '0', $pattern) == '0') {
      return 0;
    }
    return $this->isUnionPattern($pattern['parent_id']);
  }
  
  /*  */

  function getValueTable($col) {
    return $this->store->getTablePrefix() . (preg_match('/^(s|o)$/', $col) ? $col . '2val' : 'id2val');
  }
  
  function getGraphTable() {
    return $this->store->getTablePrefix() . 'g2t';
  }
  
  /*  */
  
  function getQuerySQL() {
    $nl = "\n";
    $where_sql = $this->getWHERESQL();  /* pre-fills $index['sub_joins'] $index['constraints'] */
    $order_sql = $this->getORDERSQL();  /* pre-fills $index['sub_joins'] $index['constraints'] */
    return '' .
      ($this->is_union_query ? 'SELECT' : 'SELECT' . $this->getDistinctSQL()) . $nl .
      $this->getResultVarsSQL() . $nl . /* fills $index['sub_joins'] */
      $this->getFROMSQL() . 
      $this->getAllJoinsSQL() . 
      $this->getWHERESQL() . 
      $this->getGROUPSQL() . 
      $this->getORDERSQL() . 
      (!$this->is_union_query ? $this->getLIMITSQL() : '') .
      $nl .
    '';
  }

  /*  */
  
  function getDistinctSQL() {
    if ($this->is_union_query) {
      return ($this->v('distinct', 0, $this->infos['query']) || $this->v('reduced', 0, $this->infos['query'])) ? '' : ' ALL';  
    }
    return ($this->v('distinct', 0, $this->infos['query']) || $this->v('reduced', 0, $this->infos['query'])) ? ' DISTINCT' : '';  
  }

  /*  */
  
  function getResultVarsSQL() {
    $r = '';
    $vars = $this->infos['query']['result_vars'];
    $nl = "\n";
    $added = array();
    foreach ($vars as $var) {
      $var_name = $var['var'];
      $tbl_alias = '';
      if ($tbl_infos = $this->getVarTableInfos($var_name, 0)) {
        $tbl = $tbl_infos['table'];
        $col = $tbl_infos['col'];
        $tbl_alias = $tbl_infos['table_alias'];
      }
      elseif ($var_name == 1) {/* ASK query */
        $r .= '1 AS `success`';
      }
      else {
        $this->addError('Result variable "' .$var_name. '" not used in query.');
      }
      if ($tbl_alias) {
        if ($var['aggregate']) {
          $conv_code = '';
          if (strtolower($var['aggregate']) != 'count') {
            $tbl_alias = 'V_' . $tbl . '_' . $col . '.val';
            $conv_code = '0 + ';
          }
          if (!isset($added[$var['alias']])) {
            $r .= $r ? ',' . $nl . '  ' : '  ';
            $distinct_code = (strtolower($var['aggregate']) == 'count') && $this->v('distinct', 0, $this->infos['query']) ? 'DISTINCT ' : '';
            $r .= $var['aggregate'] . '(' . $conv_code . $distinct_code . $tbl_alias. ') AS `' . $var['alias'] . '`';
            $added[$var['alias']] = 1;
          }
        }
        else {
          if (!isset($added[$var_name])) {
            $r .= $r ? ',' . $nl . '  ' : '  ';
            $r .= $tbl_alias . ' AS `' . $var_name . '`';
            if ($tbl_alias == 'NULL') {
              $r .= (in_array($col, array('s', 'o'))) ? ', ' . $nl . '    ' .$tbl_alias . ' AS `' . $var_name . ' type`' : '';
              $r .= (in_array($col, array('o'))) ? ', ' . $nl . '    ' .$tbl_alias . ' AS `' . $var_name . ' lang_dt`' : '';
            }
            else {
              $r .= (in_array($col, array('s', 'o'))) ? ', ' . $nl . '    ' .$tbl_alias . '_type AS `' . $var_name . ' type`' : '';
              $r .= (in_array($col, array('o'))) ? ', ' . $nl . '    ' .$tbl_alias . '_lang_dt AS `' . $var_name . ' lang_dt`' : ', ' . $nl . '    NULL AS `' . $var_name . ' lang_dt`';
            }
            $added[$var_name] = 1;
          }
        }
        if (!in_array($tbl_alias, $this->index['sub_joins'])) {
          $this->index['sub_joins'][] = $tbl_alias;
        }
      }
    }
    return $r;
  }
  
  function getVarTableInfos($var, $ignore_initial_index = 1) {
    if ($var == '*') {
      return array('table' => '', 'col' => '', 'table_alias' => '*');
    }
    if ($infos = $this->v($var, 0, $this->index['vars'])) {
      $infos[0]['table_alias'] = 'T_' . $infos[0]['table'] . '.' . $infos[0]['col'];
      return $infos[0];
    }
    if ($infos = $this->v($var, 0, $this->index['graph_vars'])) {
      $infos[0]['col'] = 'g';
      $infos[0]['table_alias'] = 'G_' . $infos[0]['table'] . '.' . $infos[0]['col'];
      return $infos[0];
    }
    if ($this->is_union_query && !$ignore_initial_index) {
      if (($infos = $this->v($var, 0, $this->initial_index['vars'])) || ($infos = $this->v($var, 0, $this->initial_index['graph_vars']))) {
        if (!in_array($var, $this->infos['null_vars'])) {
          $this->infos['null_vars'][] = $var;
        }
        $infos[0]['table_alias'] = 'NULL';
        $infos[0]['col'] = !isset($infos[0]['col']) ? '' : $infos[0]['col'];
        return $infos[0];
      }
    }
    return 0;
  }
  
  /*  */
  
  function getFROMSQL() {
    $r = '';
    foreach ($this->index['from'] as $id) {
      $r .= $r ? ', ' : 'FROM (';
      $r .= $this->getTripleTable($id) . ' T_' . $id;
    }
    return $r ? $r . ')' : '';
  }

  /*  */
  
  function getOrderedJoinIDs() {
    return array_merge($this->index['from'], $this->index['join'], $this->index['left_join']);  
  }

  function getJoinInfos($id) {
    $r = array();
    $tbl_ids = $this->getOrderedJoinIDs();
    $pattern = $this->getPattern($id);
    foreach ($tbl_ids as $tbl_id) {
      $tbl_pattern = $this->getPattern($tbl_id);
      if ($tbl_id != $id) {
        foreach (array('s', 'p', 'o') as $tbl_term) {
          foreach (array('var', 'bnode', 'uri') as $term_type) {
            if ($tbl_pattern[$tbl_term . '_type'] == $term_type) {
              foreach (array('s', 'p', 'o') as $term) {
                if (($pattern[$term . '_type'] == $term_type) && ($tbl_pattern[$tbl_term] == $pattern[$term])) {
                  $r[] = array('term' => $term, 'join_tbl' => $tbl_id, 'join_term' => $tbl_term);
                }
              }
            }
          }
        }
      }
    }
    return $r;
  }
  
  function getAllJoinsSQL() {
    $entries = array_merge($this->getJoins(), $this->getLeftJoins());
    $id2code = array();
    foreach ($entries as $entry) {
      if (preg_match('/([^\s]+) ON (.*)/s', $entry, $m)) {
        $id2code[$m[1]] = $entry;
      }
    }
    $deps = array();
    foreach ($id2code as $id => $code) {
      $deps[$id]['rank'] = 0;
      foreach ($id2code as $other_id => $other_code) {
        $deps[$id]['rank'] += ($id != $other_id) && preg_match('/' . $other_id . '/', $code) ? 1 : 0;
        $deps[$id][$other_id] = ($id != $other_id) && preg_match('/' . $other_id . '/', $code) ? 1 : 0;
      }
    }
    $r = '';
    do {
      /* get next 0-rank */
      $next_id = 0;
      foreach ($deps as $id => $infos) {
        if ($infos['rank'] == 0) {
          $next_id = $id;
          break;
        }
      }
      if ($next_id) {
        $r .= "\n" . $id2code[$next_id];
        unset($deps[$next_id]);
        foreach ($deps as $id => $infos) {
          $deps[$id]['rank'] = 0;
          unset($deps[$id][$next_id]);
          foreach ($infos as $k => $v) {
            if (!in_array($k, array('rank', $next_id))) {
              $deps[$id]['rank'] += $v;
              $deps[$id][$k] = $v;
            }
          }
        }
      }
    }
    while ($next_id);
    if ($deps) {
      $this->addError('Not all patterns could be rewritten to SQL JOINs');
    }
    return $r;
  }
  
  function getJoins() {
    $r = array();
    $nl = "\n";
    foreach ($this->index['join'] as $id) {
      $sub_r = $this->getJoinConditionSQL($id);
      $r[] = 'JOIN ' . $this->getTripleTable($id) . ' T_' . $id . ' ON (' . $sub_r . $nl . ')';
    }
    foreach (array_merge($this->index['from'], $this->index['join']) as $id) {
      if ($sub_r = $this->getRequiredSubJoinSQL($id)) {
        $r[] = $sub_r;
      }
    }
    return $r;
  }
  
  function getLeftJoins() {
    $r = array();
    $nl = "\n";
    foreach ($this->index['left_join'] as $id) {
      $sub_r = $this->getJoinConditionSQL($id);
      $r[] = 'LEFT JOIN ' . $this->getTripleTable($id) . ' T_' . $id . ' ON (' . $sub_r . $nl . ')';
    }
    foreach ($this->index['left_join'] as $id) {
      if ($sub_r = $this->getRequiredSubJoinSQL($id, 'LEFT')) {
        $r[] = $sub_r;
      }
    }
    return $r;
  }
  
  function getJoinConditionSQL($id) {
    $r = '';
    $nl = "\n";
    $infos = $this->getJoinInfos($id);
    $pattern = $this->getPattern($id);
    
    $tbl = 'T_' . $id;
    /* core dependency */
    $d_tbls = $this->getDependentJoins($id);
    foreach ($d_tbls as $d_tbl) {
      if (preg_match('/^T_([0-9\_]+)\.[spo]+/', $d_tbl, $m) && ($m[1] != $id) && $this->isJoinedBefore($m[1], $id) && !in_array($m[1], array_merge($this->index['from'], $this->index['join']))) {
        $r .= $r ? $nl . '  AND ' : $nl . '  ';
        $r .= '(' . $d_tbl . ' IS NOT NULL)';
      }
    }
    /* triple-based join info */
    foreach ($infos as $info) {
      if ($this->isJoinedBefore($info['join_tbl'], $id) && $this->joinDependsOn($id, $info['join_tbl'])) {
        $r .= $r ? $nl . '  AND ' : $nl . '  ';
        $r .= '(' . $tbl . '.' . $info['term'] . ' = T_' . $info['join_tbl'] . '.' . $info['join_term'] . ')';
      }
    }
    /* filters etc */
    if ($sub_r = $this->getPatternSQL($pattern, 'join__T_' . $id)) {
      $r .= $r ? $nl . '  AND ' . $sub_r  : $nl . '  ' . '(' . $sub_r . ')';
    }
    return $r;
  }
  
  function isJoinedBefore($tbl_1, $tbl_2) {
    $tbl_ids = $this->getOrderedJoinIDs();
    foreach ($tbl_ids as $id) {
      if ($id == $tbl_1) {
        return 1;
      }
      if ($id == $tbl_2) {
        return 0;
      }
    }
  }
  
  function joinDependsOn($id, $id2) {
    if (in_array($id2, array_merge($this->index['from'], $this->index['join']))) {
      return 1;
    }
    $d_tbls = $this->getDependentJoins($id2);
    //echo $id . ' :: ' . $id2 . '=>' . print_r($d_tbls, 1);
    foreach ($d_tbls as $d_tbl) {
      if (preg_match('/^T_' .$id. '\./', $d_tbl)) {
        return 1;
      }
    }
    return 0;
  }
  
  function getDependentJoins($id) {
    $r = array();
    /* sub joins */
    foreach ($this->index['sub_joins'] as $alias) {
      if (preg_match('/^(T|V|G)_' . $id . '/', $alias)) {
        $r[] = $alias;
      }
    }
    /* siblings in shared optional */
    $o_id = $this->getOptionalPattern($id);
    foreach ($this->index['sub_joins'] as $alias) {
      if (preg_match('/^(T|V|G)_' . $o_id . '/', $alias) && !in_array($alias, $r)) {
        $r[] = $alias;
      }
    }
    foreach ($this->index['left_join'] as $alias) {
      if (preg_match('/^' . $o_id . '/', $alias) && !in_array($alias, $r)) {
        $r[] = 'T_' . $alias . '.s';
      }
    }
    return $r;
  }
  
  /*  */
  
  function getRequiredSubJoinSQL($id, $prefix = '') {/* id is a triple pattern id. Optional FILTERS and GRAPHs are getting added to the join directly */
    $nl = "\n";
    $r = '';
    foreach ($this->index['sub_joins'] as $alias) {
      if (preg_match('/^V_' . $id . '_([a-z\_]+)\.val$/', $alias, $m)) {
        $col = $m[1];
        $sub_r = '';
        if ($this->isOptionalPattern($id)) {
          $pattern = $this->getPattern($id);
          do {
            $pattern = $this->getPattern($pattern['parent_id']);
          } while ($pattern['parent_id'] && ($pattern['type'] != 'optional'));
          $sub_r = $this->getPatternSQL($pattern, 'sub_join__V_' . $id);
        }
        $sub_r = $sub_r ? $nl . '  AND (' . $sub_r . ')' : '';
        /* lang dt only on literals */
        if ($col == 'o_lang_dt') {
          $sub_sub_r = 'T_' . $id . '.o_type = 2'; 
          $sub_r .= $nl . '  AND (' . $sub_sub_r . ')';
        }
        //$cur_prefix = $prefix ? $prefix . ' ' : 'STRAIGHT_';
        $cur_prefix = $prefix ? $prefix . ' ' : '';
        if ($col == 'g') {
          $r .= trim($cur_prefix . 'JOIN '. $this->getValueTable($col) . ' V_' .$id . '_' . $col. ' ON (' .$nl. '  (G_' . $id . '.' . $col. ' = V_' . $id. '_' . $col. '.id)' . $sub_r . $nl . ')');
        }
        elseif (in_array($col, array('s', 'o'))) {
          $r .= trim($cur_prefix . 'JOIN '. $this->getValueTable($col) . ' V_' .$id . '_' . $col. ' ON (' .$nl. '  (T_' . $id . '.' . $col. ' = V_' . $id. '_' . $col. '.cid) ' . $sub_r . $nl . ')');
        }
        else {
          $r .= trim($cur_prefix . 'JOIN '. $this->getValueTable($col) . ' V_' .$id . '_' . $col. ' ON (' .$nl. '  (T_' . $id . '.' . $col. ' = V_' . $id. '_' . $col. '.id) ' . $sub_r . $nl . ')');
        }
      }
      elseif (preg_match('/^G_' . $id . '\.g$/', $alias, $m)) {
        $pattern = $this->getPattern($id);
        $sub_r = $this->getPatternSQL($pattern, 'graph_sub_join__G_' . $id);
        $sub_r = $sub_r ? $nl . '  AND ' . $sub_r : '';
        /* dataset restrictions */
        $gi = $this->getGraphInfos($id);
        $sub_sub_r = '';
        $added_gts = array();
        foreach ($gi as $set) {
          if (isset($set['graph']) && !in_array($set['graph'], $added_gts)) {
            $sub_sub_r .= $sub_sub_r !== '' ? ',' : '';
            $sub_sub_r .= $this->getTermID($set['graph'], 'g'); 
            $added_gts[] = $set['graph'];
          }
        }
        $sub_r .= ($sub_sub_r !== '') ? $nl . ' AND (G_' . $id . '.g IN (' . $sub_sub_r . '))' : ''; // /* ' . str_replace('#' , '::', $set['graph']) . ' */';
        /* other graph join conditions */
        foreach ($this->index['graph_vars'] as $var => $occurs) {
          $occur_tbls = array();
          foreach ($occurs as $occur) {
            $occur_tbls[] = $occur['table'];
            if ($occur['table'] == $id) break;
          }
          foreach($occur_tbls as $tbl) {
            if (($tbl != $id) && in_array($id, $occur_tbls) && $this->isJoinedBefore($tbl, $id)) {
              $sub_r .= $nl . '  AND (G_' .$id. '.g = G_' .$tbl. '.g)'; 
            }
          }
        }
        //$cur_prefix = $prefix ? $prefix . ' ' : 'STRAIGHT_';
        $cur_prefix = $prefix ? $prefix . ' ' : '';
        $r .= trim($cur_prefix . 'JOIN '. $this->getGraphTable() . ' G_' .$id . ' ON (' .$nl. '  (T_' . $id . '.t = G_' .$id. '.t)' . $sub_r . $nl . ')');
      }
    }
    return $r;
  }

  /*  */

  function getWHERESQL() {
    $r = '';
    $nl = "\n";
    /* standard constraints */
    $sub_r = $this->getPatternSQL($this->getPattern('0'), 'where');
    /* additional constraints */
    foreach ($this->index['from'] as $id) {
      if ($sub_sub_r = $this->getConstraintSQL($id)) {
        $sub_r .= $sub_r ? $nl . ' AND ' . $sub_sub_r : $sub_sub_r;
      }
    }
    $r .= $sub_r ? $sub_r : '';
    /* left join dependencies */
    foreach ($this->index['left_join'] as $id) {
      $d_joins = $this->getDependentJoins($id);
      $added = array();
      $d_aliases = array();
      //echo $id . ' =>' . print_r($d_joins, 1);
      $id_alias = 'T_' . $id . '.s';
      foreach ($d_joins as $alias) {
        if (preg_match('/^(T|V|G)_([0-9\_]+)(_[spo])?\.([a-z\_]+)/', $alias, $m)) {
          $tbl_type = $m[1];
          $tbl_pattern_id = $m[2];
          $suffix = $m[3];
          if (($tbl_pattern_id >= $id) && $this->sameOptional($tbl_pattern_id, $id)) {/* get rid of dependency permutations and nested optionals */
            if (!in_array($tbl_type . '_' . $tbl_pattern_id . $suffix, $added)) {
              $sub_r .= $sub_r ? ' AND ' : '';
              $sub_r .= $alias . ' IS NULL';
              $d_aliases[] = $alias;
              $added[] = $tbl_type . '_' . $tbl_pattern_id . $suffix;
              $id_alias = ($tbl_pattern_id == $id) ? $alias : $id_alias;
            }
          }
        }
      }
      if (count($d_aliases) > 2) {/* @@todo fix this! */
        $sub_r1 = '  /* '.$id_alias.' dependencies */';
        $sub_r2 = '((' . $id_alias . ' IS NULL) OR (CONCAT(' . join(', ', $d_aliases) . ') IS NOT NULL))';
        $r .= $r ? $nl . $sub_r1 . $nl . '  AND ' .$sub_r2 : $sub_r1 . $nl . $sub_r2;
      }
    }
    return $r ? $nl . 'WHERE ' . $r : '';
  }

  /*  */
  
  function addConstraintSQLEntry($id, $sql) {
    if (!isset($this->index['constraints'][$id])) {
      $this->index['constraints'][$id] = array();
    }
    if (!in_array($sql, $this->index['constraints'][$id])) {
      $this->index['constraints'][$id][] = $sql;
    }
  }
  
  function getConstraintSQL($id) {
    $r = '';
    $nl = "\n";
    $constraints = $this->v($id, array(), $this->index['constraints']);
    foreach ($constraints as $constraint) {
      $r .= $r ? $nl . '  AND ' . $constraint : $constraint;
    }
    return $r;
  }
  
  /*  */
  
  function getPatternSQL($pattern, $context) {
    $type = $pattern['type'];
    $m = 'get' . ucfirst($type) . 'PatternSQL';
    return method_exists($this, $m) ? $this->$m($pattern, $context) : $this->getDefaultPatternSQL($pattern, $context);
  }

  function getDefaultPatternSQL($pattern, $context) {
    $r = '';
    $nl = "\n";
    $sub_ids = $this->v('patterns', array(), $pattern);
    foreach ($sub_ids as $sub_id) {
      $sub_r = $this->getPatternSQL($this->getPattern($sub_id), $context);
      $r .= ($r && $sub_r) ? $nl . '  AND (' . $sub_r . ')' : ($sub_r ? $sub_r  : '');
    }
    return $r ? $r : '';
  }
  
  function getTriplePatternSQL($pattern, $context) {
    $r = '';
    $nl = "\n";
    $id = $pattern['id'];
    /* s p o */
    $vars = array();
    foreach (array('s', 'p', 'o') as $term) {
      $sub_r = '';
      $type = $pattern[$term . '_type'];
      if ($type == 'uri') {
        $term_id = $this->getTermID($pattern[$term], $term);
        $sub_r = '(T_' . $id . '.' . $term . ' = ' . $term_id . ') /* ' . str_replace('#' , '::', $pattern[$term]) . ' */';
      }
      elseif ($type == 'literal') {
        $term_id = $this->getTermID($pattern[$term], $term);
        $sub_r = '(T_' . $id . '.' . $term . ' = ' . $term_id . ') /* ' . preg_replace('/[\#\n]/' , ' ', $pattern[$term]) . ' */';
        if (($lang_dt = $this->v1($term . '_lang', '', $pattern)) || ($lang_dt = $this->v1($term . '_datatype', '', $pattern))) {
          $lang_dt_id = $this->getTermID($lang_dt);
          $sub_r .= $nl . '  AND (T_' . $id . '.' .$term. '_lang_dt = ' . $lang_dt_id . ') /* ' . str_replace('#' , '::', $lang_dt) . ' */';
        }
      }
      elseif ($type == 'var') {
        $val = $pattern[$term];
        if (isset($vars[$val])) {/* repeated var in pattern */
          $sub_r = '(T_' . $id . '.' . $term . '=' . 'T_' . $id . '.' . $vars[$val] . ')';
        }
        $vars[$val] = $term;
        if ($infos = $this->v($val, 0, $this->index['graph_vars'])) {/* graph var in triple pattern */
          $sub_r .= $sub_r ? $nl . '  AND ' : '';
          $tbl = $infos[0]['table'];
          $sub_r .= 'G_' . $tbl . '.g = T_' . $id . '.' . $term;
        }
      }
      if ($sub_r) {
        if (preg_match('/^(join)/', $context) || (preg_match('/^where/', $context) && in_array($id, $this->index['from']))) {
          $r .= $r ? $nl . '  AND ' . $sub_r  : $sub_r;
        }
      }
    }
    /* g */
    if ($infos = $pattern['graph_infos']) {
      $tbl_alias = 'G_' . $id . '.g';
      if (!in_array($tbl_alias, $this->index['sub_joins'])) {
        $this->index['sub_joins'][] = $tbl_alias;
      }
      $sub_r = array('graph_var' => '', 'graph_uri' => '', 'from' => '', 'from_named' => '');
      foreach ($infos as $info) {
        $type = $info['type'];
        if ($type == 'graph') {
          if ($info['uri']) {
            $term_id = $this->getTermID($info['uri'], 'g');
            $sub_r['graph_uri'] .= $sub_r['graph_uri'] ? $nl . ' AND ' : '';
            $sub_r['graph_uri'] .= '(' .$tbl_alias. ' = ' . $term_id . ') /* ' . str_replace('#' , '::', $info['uri']) . ' */';
          }
        }
      }
      if ($sub_r['from'] && $sub_r['from_named']) {
        $sub_r['from_named'] = '';
      }
      if (!$sub_r['from'] && !$sub_r['from_named']) {
        $sub_r['graph_var'] = '';
      }
      if (preg_match('/^(graph_sub_join)/', $context)) {
        foreach ($sub_r as $g_type => $g_sql) {
          if ($g_sql) {
            $r .= $r ? $nl . '  AND ' . $g_sql  : $g_sql;
          }
        }
      }
    }
    /* optional sibling filters? */
    if (preg_match('/^(join|sub_join)/', $context) && $this->isOptionalPattern($id)) {
      $o_pattern = $pattern;
      do {
        $o_pattern = $this->getPattern($o_pattern['parent_id']);
      } while ($o_pattern['parent_id'] && ($o_pattern['type'] != 'optional'));
      if ($sub_r = $this->getPatternSQL($o_pattern, 'optional_filter' . preg_replace('/^(.*)(__.*)$/', '\\2', $context))) {
        $r .= $r ? $nl . '  AND ' . $sub_r  : $sub_r;
      }
      /* created constraints */
      if ($sub_r = $this->getConstraintSQL($id)) {
        $r .= $r ? $nl . '  AND ' . $sub_r  : $sub_r;
      }
    }
    /* result */
    if (preg_match('/^(where)/', $context) && $this->isOptionalPattern($id)) {
      return '';
    }
    return $r;
  }
  
  /*  */
  
  function getFilterPatternSQL($pattern, $context) {
    $r = '';
    $id = $pattern['id'];
    $constraint_id = $this->v1('constraint', '', $pattern);
    $constraint = $this->getPattern($constraint_id);
    $constraint_type = $constraint['type'];
    if ($constraint_type == 'built_in_call') {
      $r = $this->getBuiltInCallSQL($constraint, $context);
    }
    elseif ($constraint_type == 'expression') {
      $r = $this->getExpressionSQL($constraint, $context, '', 'filter');
    }
    else {
      $m = 'get' . ucfirst($constraint_type) . 'ExpressionSQL';
      if (method_exists($this, $m)) {
        $r = $this->$m($constraint, $context, '', 'filter');
      }
    }
    if ($this->isOptionalPattern($id) && !preg_match('/^(join|optional_filter)/', $context)) {
      return '';
    }
    /* unconnected vars in FILTERs eval to false */
    if ($sub_r = $this->hasUnconnectedFilterVars($id)) {
      if ($sub_r == 'alias') {
        if (!in_array($r, $this->index['havings'])) $this->index['havings'][] = $r;
        return '';
      }
      elseif (preg_match('/^T([^\s]+\.)g (.*)$/s', $r, $m)) {/* graph filter */
        return 'G' . $m[1] . 't ' . $m[2];
      }
      elseif (preg_match('/^\(?V[^\s]+_g\.val .*$/s', $r, $m)) {/* graph value filter, @@improveMe */
        //return $r;
      }
      else {
        return 'FALSE';
      }
    }
    /* some really ugly tweaks */
    /* empty language filter: FILTER ( lang(?v) = '' ) */
    $r = preg_replace('/\(\/\* language call \*\/ ([^\s]+) = ""\)/s', '((\\1 = "") OR (\\1 LIKE "%:%"))', $r);
    return $r;
  }
  
  /*  */
  
  function hasUnconnectedFilterVars($filter_id) {
    $pattern = $this->getInitialPattern($filter_id);
    $gp = $this->getInitialPattern($pattern['parent_id']);
    $vars = array();
    foreach ($this->initial_index['patterns'] as $id => $p) {
      /* vars in given filter */
      if (preg_match('/^' .$filter_id. '.+/', $id)) {
        if ($p['type'] == 'var') {
          $vars[$p['value']][] = 'filter';
        }
        if (($p['type'] == 'built_in_call') && ($p['call'] == 'bound')) {
          $vars[$p['args'][0]['value']][] = 'filter';
        }
      }
      /* triple patterns if their scope is in the parent path of the filter */
      if ($p['type'] == 'triple') {
        $tp = $p;
        do {
          $proceed = 1;
          $tp = $this->getInitialPattern($tp['parent_id']);
          if ($tp['type'] == 'group') {
            $proceed = 0;
            if (isset($tp['parent_id']) && ($p_tp = $this->getInitialPattern($tp['parent_id'])) && ($p_tp['type'] == 'union')) {
              $proceed = 1;
            }
          }
        } while ($proceed);
        $tp_id = $tp['id'];
        $fp_id = $filter_id;
        $ok = 0;
        do {
          $fp = $this->getInitialPattern($fp_id);
          $fp_id = $fp['parent_id'];
          if (($fp['type'] != 'group') && ($fp_id === $tp_id)) {
            $ok = 1;
            break;
          }
        } while (($fp['parent_id'] != $fp['id']) && ($fp['type'] != 'group'));
        if ($ok) {
          foreach (array('s', 'p', 'o') as $term) {
            if ($p[$term . '_type'] == 'var') {
              $vars[$p[$term]][] = 'triple';
            }
          }
        }
      }
    }
    foreach ($vars as $var => $types) {
      if (!in_array('triple', $types)) {
        /* might be an alias */
        $r = 1;
        foreach ($this->infos['query']['result_vars'] as $r_var) {
          if ($r_var['alias'] == $var) $r = 'alias';
        }
        return 1;
      }
    }
    return 0;
  }

   /*  */

  function getExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $r = '';
    $nl = "\n";
    $type = $this->v1('type', '', $pattern);
    $sub_type = $this->v1('sub_type', $type, $pattern);
    if (preg_match('/^(and|or)$/', $sub_type)) {
      foreach ($pattern['patterns'] as $sub_id) {
        $sub_pattern = $this->getPattern($sub_id);
        $sub_pattern_type = $sub_pattern['type'];
        if ($sub_pattern_type == 'built_in_call') {
          $sub_r = $this->getBuiltInCallSQL($sub_pattern, $context, '', $parent_type);
        }
        else {
          $sub_r = $this->getExpressionSQL($sub_pattern, $context, '', $parent_type);
        }
        if ($sub_r) {
          $r .= $r ? ' ' . strtoupper($sub_type). ' (' .$sub_r. ')' : '(' . $sub_r . ')';
        }
      }
    }
    elseif ($sub_type == 'built_in_call') {
      $r = $this->getBuiltInCallSQL($pattern, $context, $val_type, $parent_type);
    }
    elseif (preg_match('/literal/', $sub_type)) {
      $r = $this->getLiteralExpressionSQL($pattern, $context, $val_type, $parent_type);
    }
    elseif ($sub_type) {
      $m = 'get' . ucfirst($sub_type) . 'ExpressionSQL';
      if (method_exists($this, $m)) {
        $r = $this->$m($pattern, $context, '', $parent_type);
      }
    }
    /* skip expressions that reference non-yet-joined tables */
    if (preg_match('/__(T|V|G)_(.+)$/', $context, $m)) {
      $context_pattern_id = $m[2];
      $context_table_type = $m[1];
      if (preg_match_all('/((T|V|G)(\_[0-9])+)/', $r, $m)) {
        $aliases = $m[1];
        $keep = 1;
        foreach ($aliases as $alias) {
          if (preg_match('/(T|V|G)_(.*)$/', $alias, $m)) {
            $tbl_type = $m[1];
            $tbl = $m[2];
            if (!$this->isJoinedBefore($tbl, $context_pattern_id)) {
              $keep = 0;
            }
            elseif (($context_pattern_id == $tbl) && preg_match('/(TV)/', $context_table_type . $tbl_type)) {
              $keep = 0;
            }
          }
        }
        $r = $keep ? $r : '';
      }
    }
    return $r;
  }
  
  function detectExpressionValueType($pattern_ids) {
    foreach ($pattern_ids as $id) {
      $pattern = $this->getPattern($id);
      $type = $this->v('type', '', $pattern);
      if (($type == 'literal') && isset($pattern['datatype'])) {
        if (in_array($pattern['datatype'], array($this->xsd . 'integer', $this->xsd . 'float', $this->xsd . 'double'))) {
          return 'numeric';
        }
      }
    }
    return '';
  }

  /*  */

  function getRelationalExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $r = '';
    $val_type = $this->detectExpressionValueType($pattern['patterns']);
    $op = $pattern['operator'];
    foreach ($pattern['patterns'] as $sub_id) {
      $sub_pattern = $this->getPattern($sub_id);
      $sub_pattern['parent_op'] = $op;
      $sub_type = $sub_pattern['type'];
      $m = ($sub_type == 'built_in_call') ? 'getBuiltInCallSQL' : 'get' . ucfirst($sub_type) . 'ExpressionSQL';
      $m = str_replace('ExpressionExpression', 'Expression', $m);
      $sub_r = method_exists($this, $m) ? $this->$m($sub_pattern, $context, $val_type, 'relational') : '';
      $r .= $r ? ' ' . $op . ' ' . $sub_r : $sub_r;
    }
    return $r ? '(' . $r . ')' : $r;
  }

  function getAdditiveExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $r = '';
    $val_type = $this->detectExpressionValueType($pattern['patterns']);
    foreach ($pattern['patterns'] as $sub_id) {
      $sub_pattern = $this->getPattern($sub_id);
      $sub_type = $this->v('type', '', $sub_pattern);
      $m = ($sub_type == 'built_in_call') ? 'getBuiltInCallSQL' : 'get' . ucfirst($sub_type) . 'ExpressionSQL';
      $m = str_replace('ExpressionExpression', 'Expression', $m);
      $sub_r = method_exists($this, $m) ? $this->$m($sub_pattern, $context, $val_type, 'additive') : '';
      $r .= $r ? ' ' . $sub_r : $sub_r;
    }
    return $r;
  }

  function getMultiplicativeExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $r = '';
    $val_type = $this->detectExpressionValueType($pattern['patterns']);
    foreach ($pattern['patterns'] as $sub_id) {
      $sub_pattern = $this->getPattern($sub_id);
      $sub_type = $sub_pattern['type'];
      $m = ($sub_type == 'built_in_call') ? 'getBuiltInCallSQL' : 'get' . ucfirst($sub_type) . 'ExpressionSQL';
      $m = str_replace('ExpressionExpression', 'Expression', $m);
      $sub_r = method_exists($this, $m) ? $this->$m($sub_pattern, $context, $val_type, 'multiplicative') : '';
      $r .= $r ? ' ' . $sub_r : $sub_r;
    }
    return $r;
  }

  /*  */

  function getVarExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $var = $pattern['value'];
    $info = $this->getVarTableInfos($var);
    if (!$tbl = $info['table']) {
      /* might be an aggregate var */
      $vars = $this->infos['query']['result_vars'];
      foreach ($vars as $test_var) {
        if ($test_var['alias'] == $pattern['value']) {
          return '`' . $pattern['value'] . '`';
        }
      }
      return '';
    }
    $col = $info['col'];
    if (($context == 'order') && ($col == 'o')) {
      $tbl_alias = 'T_' . $tbl . '.o_comp';
    }
    elseif ($context == 'sameterm') {
      $tbl_alias = 'T_' . $tbl . '.' . $col;
    }
    elseif (($parent_type == 'relational') && ($col == 'o') && (preg_match('/[\<\>]/', $this->v('parent_op', '', $pattern)))) {
      $tbl_alias = 'T_' . $tbl . '.o_comp';
    }
    else {
      $tbl_alias = 'V_' . $tbl . '_' . $col . '.val';
      if (!in_array($tbl_alias, $this->index['sub_joins'])) {
        $this->index['sub_joins'][] = $tbl_alias;
      }
    }
    $op = $this->v('operator', '', $pattern);
    if (preg_match('/^(filter|and)/', $parent_type)) {
      if ($op == '!') {
        $r = '(((' . $tbl_alias . ' = 0) AND (CONCAT("1", ' . $tbl_alias . ') != 1))'; /* 0 and no string */
        $r .= ' OR (' . $tbl_alias . ' IN ("", "false")))'; /* or "", or "false" */
      }
      else {
        $r = '((' . $tbl_alias . ' != 0)'; /* not null */
        $r .= ' OR ((CONCAT("1", ' . $tbl_alias . ') = 1) AND (' . $tbl_alias . ' NOT IN ("", "false"))))'; /* string, and not "" or "false" */
      }
    }
    else {
      $r = trim($op . ' ' . $tbl_alias);
      if ($val_type == 'numeric') {
        if (preg_match('/__(T|V|G)_(.+)$/', $context, $m)) {
          $context_pattern_id = $m[2];
          $context_table_type = $m[1];
        }
        else {
          $context_pattern_id = $pattern['id'];
          $context_table_type = 'T';
        }
        if ($this->isJoinedBefore($tbl, $context_pattern_id)) {
          $add = ($tbl != $context_pattern_id) ? 1 : 0;
          $add = (!$add && ($context_table_type == 'V')) ? 1 : 0;
          if ($add) {
            $this->addConstraintSQLEntry($context_pattern_id, '(' .$r. ' = "0" OR ' . $r . '*1.0 != 0)');
          }
        }
      }
    }
    return $r;
  }
  
  /*  */

  function getUriExpressionSQL($pattern, $context, $val_type = '') {
    $val = $pattern['uri'];
    $r = $pattern['operator'];
    $r .= is_numeric($val) ? ' ' . $val : ' "' . mysql_real_escape_string($val, $this->store->getDBCon()) . '"';
    return $r;
  }
  
  /*  */

  function getLiteralExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $val = $pattern['value'];
    $r = $pattern['operator'];
    if (is_numeric($val) && $this->v('datatype', 0, $pattern)) {
      $r .= ' ' . $val;
    }
    elseif (preg_match('/^(true|false)$/i', $val) && ($this->v1('datatype', '', $pattern) == 'http://www.w3.org/2001/XMLSchema#boolean')) {
      $r .= ' ' . strtoupper($val);
    }
    elseif ($parent_type == 'regex') {
      $sub_r = mysql_real_escape_string($val, $this->store->getDBCon());
      $r .= ' "' . preg_replace('/\x5c\x5c/', '\\', $sub_r) . '"';
    }
    else {
      $r .= ' "' . mysql_real_escape_string($val, $this->store->getDBCon()) . '"';
    }
    if (($lang_dt = $this->v1('lang', '', $pattern)) || ($lang_dt = $this->v1('datatype', '', $pattern))) {
      /* try table/alias via var in siblings */
      if ($var = $this->findSiblingVarExpression($pattern['id'])) {
        if (isset($this->index['vars'][$var])) {
          $infos = $this->index['vars'][$var];
          foreach ($infos as $info) {
            if ($info['col'] == 'o') {
              $tbl = $info['table'];
              $term_id = $this->getTermID($lang_dt);
              if ($pattern['operator'] != '!=') {
                if (preg_match('/__(T|V|G)_(.+)$/', $context, $m)) {
                  $context_pattern_id = $m[2];
                  $context_table_type = $m[1];
                }
                elseif ($context == 'where') {
                  $context_pattern_id = $tbl;
                }
                else {
                  $context_pattern_id = $pattern['id'];
                }
                if ($tbl == $context_pattern_id) {/* @todo better dependency check */
                  if ($term_id || ($lang_dt != 'http://www.w3.org/2001/XMLSchema#integer')) {/* skip if simple int, but no id */
                    $this->addConstraintSQLEntry($context_pattern_id, 'T_' . $tbl . '.o_lang_dt = ' . $term_id . ' /* ' . str_replace('#' , '::', $lang_dt) . ' */');
                  }
                }
              }
              break;
            }
          }
        }
      }
    }
    return trim($r);
  }
  
  function findSiblingVarExpression($id) {
    $pattern = $this->getPattern($id);
    do {
      $pattern = $this->getPattern($pattern['parent_id']);
    } while ($pattern['parent_id'] && ($pattern['type'] != 'expression'));
    $sub_patterns = $this->v('patterns', array(), $pattern);
    foreach ($sub_patterns as $sub_id) {
      $sub_pattern = $this->getPattern($sub_id);
      if ($sub_pattern['type'] == 'var') {
        return $sub_pattern['value'];
      }
    }
    return '';
  }
  
  /*  */

  function getFunctionExpressionSQL($pattern, $context, $val_type = '', $parent_type = '') {
    $fnc_uri = $pattern['uri'];
    $op = $this->v('operator', '', $pattern);
    if ($op) $op .= ' ';
    if ($this->allow_extension_functions) {
      /* mysql functions */
      if (preg_match('/^http\:\/\/web\-semantics\.org\/ns\/mysql\/(.*)$/', $fnc_uri, $m)) {
        $fnc_name = strtoupper($m[1]);
        $sub_r = '';
        foreach ($pattern['args'] as $arg) {
          $sub_r .= $sub_r ? ', ' : '';
          $sub_r .= $this->getExpressionSQL($arg, $context, $val_type, $parent_type);
        }
        return $op . $fnc_name . '(' . $sub_r . ')';
      }
      /* any other: ignore */
    }
    /* simple type conversions */
    if (strpos($fnc_uri, 'http://www.w3.org/2001/XMLSchema#') === 0) {
      return $op . $this->getExpressionSQL($pattern['args'][0], $context, $val_type, $parent_type);
    }
    return '';
  }

  /*  */

  function getBuiltInCallSQL($pattern, $context) {
    $call = $pattern['call'];
    $m = 'get' . ucfirst($call) . 'CallSQL';
    if (method_exists($this, $m)) {
      return $this->$m($pattern, $context);
    }
    else {
      $this->addError('Unknown built-in call "' . $call . '"');
    }
    return '';
  }
  
  function getBoundCallSQL($pattern, $context) {
    $r = '';
    $var = $pattern['args'][0]['value'];
    $info = $this->getVarTableInfos($var);
    if (!$tbl = $info['table']) {
      return '';
    }
    $col = $info['col'];
    $tbl_alias = 'T_' . $tbl . '.' . $col;
    if ($pattern['operator'] == '!') {
      return $tbl_alias . ' IS NULL';
    }
    return $tbl_alias . ' IS NOT NULL';
  }

  function getHasTypeCallSQL($pattern, $context, $type) {
    $r = '';
    $var = $pattern['args'][0]['value'];
    $info = $this->getVarTableInfos($var);
    if (!$tbl = $info['table']) {
      return '';
    }
    $col = $info['col'];
    $tbl_alias = 'T_' . $tbl . '.' . $col . '_type';
    return $tbl_alias . ' ' .$this->v('operator', '', $pattern) . '= ' . $type;
  }

  function getIsliteralCallSQL($pattern, $context) {
    return $this->getHasTypeCallSQL($pattern, $context, 2);
  }

  function getIsblankCallSQL($pattern, $context) {
    return $this->getHasTypeCallSQL($pattern, $context, 1);
  }

  function getIsiriCallSQL($pattern, $context) {
    return $this->getHasTypeCallSQL($pattern, $context, 0);
  }

  function getIsuriCallSQL($pattern, $context) {
    return $this->getHasTypeCallSQL($pattern, $context, 0);
  }

  function getStrCallSQL($pattern, $context) {
    $sub_pattern = $pattern['args'][0];
    $sub_type = $sub_pattern['type'];
    $m = 'get' . ucfirst($sub_type) . 'ExpressionSQL';
    if (method_exists($this, $m)) {
      return $this->$m($sub_pattern, $context);
    }
  }
  
  function getFunctionCallSQL($pattern, $context) {
    $f_uri = $pattern['uri'];
    if (preg_match('/(integer|double|float|string)$/', $f_uri)) {/* skip conversions */
      $sub_pattern = $pattern['args'][0];
      $sub_type = $sub_pattern['type'];
      $m = 'get' . ucfirst($sub_type) . 'ExpressionSQL';
      if (method_exists($this, $m)) {
        return $this->$m($sub_pattern, $context);
      }
    }
  }
  
  function getLangDatatypeCallSQL($pattern, $context) {
    $r = '';
    if (isset($pattern['patterns'])) { /* proceed with first argument only (assumed as base type for type promotion) */
      $sub_pattern = array('args' => array($pattern['patterns'][0]));
      return $this->getLangDatatypeCallSQL($sub_pattern, $context);
    }
    if (!isset($pattern['args'])) {
      return 'FALSE';
    }
    $sub_type = $pattern['args'][0]['type'];
    if ($sub_type != 'var') {
      return $this->getLangDatatypeCallSQL($pattern['args'][0], $context);
    }
    $var = $pattern['args'][0]['value'];
    $info = $this->getVarTableInfos($var);
    if (!$tbl = $info['table']) {
      return '';
    }
    $col = 'o_lang_dt';
    $tbl_alias = 'V_' . $tbl . '_' . $col . '.val';
    if (!in_array($tbl_alias, $this->index['sub_joins'])) {
      $this->index['sub_joins'][] = $tbl_alias;
    }
    $op = $this->v('operator', '', $pattern);
    $r = trim($op . ' ' . $tbl_alias);
    return $r;
  }

  function getDatatypeCallSQL($pattern, $context) {
    return '/* datatype call */ ' . $this->getLangDatatypeCallSQL($pattern, $context);
  }

  function getLangCallSQL($pattern, $context) {
    return '/* language call */ ' . $this->getLangDatatypeCallSQL($pattern, $context);
  }
  
  function getLangmatchesCallSQL($pattern, $context) {
    if (count($pattern['args']) == 2) {
      $arg_1 = $pattern['args'][0];
      $arg_2 = $pattern['args'][1];
      $sub_r_1 = $this->getBuiltInCallSQL($arg_1, $context);/* adds value join */
      $sub_r_2 = $this->getExpressionSQL($arg_2, $context);
      $op = $this->v('operator', '', $pattern);
      if (preg_match('/^([\"\'])([^\'\"]+)/', $sub_r_2, $m)) {
        if ($m[2] == '*') {
          $r = ($op == '!') ? 'NOT (' . $sub_r_1 . ' REGEXP "^[a-zA-Z\-]+$"' . ')' : $sub_r_1 . ' REGEXP "^[a-zA-Z\-]+$"';
        }
        else {
          $r = ($op == '!') ? $sub_r_1 . ' NOT LIKE ' . $m[1] . $m[2] . '%' . $m[1] : $sub_r_1 . ' LIKE ' . $m[1] . $m[2] . '%' . $m[1];
        }
      }
      else {
        $r = ($op == '!') ? $sub_r_1 . ' NOT LIKE CONCAT(' . $sub_r_2 . ', "%")' : $sub_r_1 . ' LIKE CONCAT(' . $sub_r_2 . ', "%")';
      }
      return $r;
    }
    return '';
  }
  
  function getSametermCallSQL($pattern, $context) {
    if (count($pattern['args']) == 2) {
      $arg_1 = $pattern['args'][0];
      $arg_2 = $pattern['args'][1];
      $sub_r_1 = $this->getExpressionSQL($arg_1, 'sameterm');
      $sub_r_2 = $this->getExpressionSQL($arg_2, 'sameterm');
      $op = $this->v('operator', '', $pattern);
      $r = $sub_r_1 . ' ' . $op . '= ' . $sub_r_2; 
      return $r;
    }
    return '';
  }
  
  function getRegexCallSQL($pattern, $context) {
    $ac = count($pattern['args']);
    if ($ac >= 2) {
      foreach ($pattern['args'] as $i => $arg) {
        $var = 'sub_r_' . ($i + 1);
        $$var = $this->getExpressionSQL($arg, $context, '', 'regex');
      }
      $sub_r_3 = (isset($sub_r_3) && preg_match('/[\"\'](.+)[\"\']/', $sub_r_3, $m)) ? strtolower($m[1]) : '';
      $op = ($this->v('operator', '', $pattern) == '!') ? ' NOT' : '';
      if (!$sub_r_1 || !$sub_r_2) return '';
      $opt = ($sub_r_3 == 'i') ? '' : 'BINARY ';
      /* use like for simple patterns */
      if (!$opt && preg_match('/^\"(\^)?([a-z0-9\_\-]+)(\$)?\"$/i', $sub_r_2, $m)) {
        $sub_r_2 = $m[1] ? $m[2] : '%' . $m[2];
        $sub_r_2 .= isset($m[3]) && $m[3] ? '' : '%';
        return $sub_r_1 . $op . ' LIKE "' . $sub_r_2 . '"';
      }
      /* use REGEXP */
      else {
        return $sub_r_1 . $op . ' REGEXP ' . $opt . $sub_r_2;
      }
    }
    return '';
  }
  
  /*  */

  function getGROUPSQL() {
    $r = '';
    $nl = "\n";
    $infos = $this->v('group_infos', array(), $this->infos['query']);
    foreach ($infos as $info) {
      $var = $info['value'];
      if ($tbl_infos = $this->getVarTableInfos($var, 0)) {
        $tbl_alias = $tbl_infos['table_alias'];
        $r .= $r ? ', ' : 'GROUP BY '; 
        $r .= $tbl_alias;
      }
    }
    $hr = '';
    foreach ($this->index['havings'] as $having) {
      $hr .= $hr ? ' AND' : ' HAVING';
      $hr .= '(' . $having . ')';
    }
    $r .= $hr;
    return $r ? $nl . $r : $r;
  }
  
  /*  */
  
  function getORDERSQL() {
    $r = '';
    $nl = "\n";
    $infos = $this->v('order_infos', array(), $this->infos['query']);
    foreach ($infos as $info) {
      $type = $info['type'];
      $ms = array('expression' => 'getExpressionSQL', 'built_in_call' => 'getBuiltInCallSQL', 'function_call' => 'getFunctionCallSQL');
      $m = isset($ms[$type]) ? $ms[$type] : 'get' . ucfirst($type) . 'ExpressionSQL';
      if (method_exists($this, $m)) {
        $sub_r = '(' . $this->$m($info, 'order') . ')';
        $sub_r .= $this->v('direction', '', $info) == 'desc' ? ' DESC' : '';
        $r .= $r ? ',' .$nl . $sub_r : $sub_r;
      }
    }
    return $r ? $nl . 'ORDER BY ' . $r : '';
  }
  
  /*  */
  
  function getLIMITSQL() {
    $r = '';
    $nl = "\n";
    $limit = $this->v('limit', -1, $this->infos['query']);
    $offset = $this->v('offset', -1, $this->infos['query']);
    if ($limit != -1) {
      $offset = ($offset == -1) ? 0 : mysql_real_escape_string($offset, $this->store->getDBCon());
      $r = 'LIMIT ' . $offset . ',' . $limit; 
    }
    elseif ($offset != -1) {
      $r = 'LIMIT ' . mysql_real_escape_string($offset, $this->store->getDBCon()) . ',999999999999'; /* mysql doesn't support stand-alone offsets .. */
    }
    return $r ? $nl . $r : '';
  }

  /*  */
  
  function getValueSQL($q_tbl) {
    $r = '';
    /* result vars */
    $vars = $this->infos['query']['result_vars'];
    $nl = "\n";
    $v_tbls = array('JOIN' => array(), 'LEFT JOIN' => array());
    $vc = 1;
    foreach ($vars as $var) {
      $var_name = $var['var'];
      $r .= $r ? ',' . $nl . '  ' : '  ';
      $col = '';
      $tbl = '';
      if ($var_name != '*') {
        if (in_array($var_name, $this->infos['null_vars'])) {
          if (isset($this->initial_index['vars'][$var_name])) {
            $col = $this->initial_index['vars'][$var_name][0]['col'];
            $tbl = $this->initial_index['vars'][$var_name][0]['table'];
          }
          if (isset($this->initial_index['graph_vars'][$var_name])) {
            $col = 'g';
            $tbl = $this->initial_index['graph_vars'][$var_name][0]['table'];
          }
        }
        elseif (isset($this->index['vars'][$var_name])) {
          $col = $this->index['vars'][$var_name][0]['col'];
          $tbl = $this->index['vars'][$var_name][0]['table'];
        }
      }
      if ($var['aggregate']) {
        $r .= 'TMP.`' . $var['alias'] . '`';
      }
      else {
        $join_type = in_array($tbl, array_merge($this->index['from'], $this->index['join'])) ? 'JOIN' : 'LEFT JOIN';/* val may be NULL */
        $v_tbls[$join_type][] = array('t_col' => $col, 'q_col' => $var_name, 'vc' => $vc);
        $r .= 'V' . $vc . '.val AS `' . $var_name . '`';
        if (in_array($col, array('s', 'o'))) {
          $r .= ', ' . $nl . '    TMP.`' . $var_name . ' type` AS `' . $var_name . ' type`';
          //$r .= ', ' . $nl . '    CASE TMP.`' . $var_name . ' type` WHEN 2 THEN "literal" WHEN 1 THEN "bnode" ELSE "uri" END AS `' . $var_name . ' type`';
        }
        $vc++;
        if ($col == 'o') {
          $v_tbls[$join_type][] = array('t_col' => 'id', 'q_col' => $var_name . ' lang_dt', 'vc' => $vc);
          $r .= ', ' .$nl. '    V' . $vc . '.val AS `' . $var_name . ' lang_dt`';
          $vc++;
        }
      }
    }
    /* from */
    $r .= $nl . 'FROM (' . $q_tbl . ' TMP)';
    foreach (array('JOIN', 'LEFT JOIN') as $join_type) {
      foreach ($v_tbls[$join_type] as $v_tbl) {
        $tbl = $this->getValueTable($v_tbl['t_col']);
        $var_name = preg_replace('/^([^\s]+)(.*)$/', '\\1', $v_tbl['q_col']);
        $cur_join_type = in_array($var_name, $this->infos['null_vars']) ? 'LEFT JOIN' : $join_type;
        $id_col = in_array($v_tbl['t_col'], array('s', 'o')) ? 'cid' : 'id';
        $r .= $nl . ' ' . $cur_join_type . ' ' . $tbl . ' V' . $v_tbl['vc'] . ' ON (
            (V' . $v_tbl['vc'] . '.' . $id_col . ' = TMP.`' . $v_tbl['q_col'].'`)
            ' . (($id_col == 'cid') ? ' AND (V' . $v_tbl['vc'] . '.id = V' . $v_tbl['vc'] . '.cid)' : '') . '
        )';
      }
    }
    /* create pos columns, id needed */
    if ($this->v('order_infos', array(), $this->infos['query'])) {
      $r .= $nl . ' ORDER BY _pos_';
    }
    return 'SELECT' . $nl . $r;
  }
  
  /*  */

}


