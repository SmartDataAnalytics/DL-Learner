<?php
/**
 * This little Script takes all included js-files and
 * compresses them with the PHP-Variant of js-min
 * found here: http://code.google.com/p/jsmin-php/
 * This is only used in non-debugging-mode
 *
 * @package moosique.net
 * @author Steffen Becker
 */
header('Content-type: application/javascript');
$offset = 60 * 60 * 24 * 365; // set offset to 365 days = 1 year
header('Expires: ' . gmdate("D, d M Y H:i:s", time() + $offset) . ' GMT');

ob_start('compress');
 
function compress($buffer) {
  include('jsmin-1.1.1.php');
  $buffer = JSMin::minify($buffer);
  return $buffer;
}
 
/* the javascript-files to include and compress */
include('mootools-1.2.4-core-yc.js');
include('mootools-1.2.4.2-more-yc.js');
include('moosique.js');
include('start.js');
 
ob_end_flush();
?>