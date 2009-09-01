<?php
/* This little Script takes all included js-files and
 * compresses them with the PHP-Variant of js-min
 * found here: http://code.google.com/p/jsmin-php/
 */
header('Content-type: application/javascript');
// set offset to 365 days = 1 year
$offset = 60 * 60 * 24 * 365;
header('Expires: ' . gmdate("D, d M Y H:i:s", time() + $offset) . ' GMT');

ob_start('compress');
 
function compress($buffer) {
  include('jsmin-1.1.1.php');
  $buffer = JSMin::minify($buffer);
  return $buffer;
}
 
/* the javascript-files to include and compress */
include('mootools-1.2.3-core-yc.js');
include('player.js');
include('interface.js');
include('request.js');


// Since we include the IE6-Fixes with a CC, we don't 
// add and compress them here
 
ob_end_flush();
?>