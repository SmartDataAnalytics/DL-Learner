<?php
/** 
 * This little Script takes all included css-files and
 * compresses them by removing comments, line-breaks and
 * useless space-characters
 *
 * @package moosique.net
 * @author Steffen Becker
 */
header('Content-type: text/css');
// set offset to 365 days = 1 year
$offset = 60 * 60 * 24 * 365;
header('Expires: ' . gmdate("D, d M Y H:i:s", time() + $offset) . ' GMT');

ob_start('compress'); /* comment for development, smaller css-files */
// ob_start();
 
function compress($buffer) {
  // remove comments
  $buffer = preg_replace('!/\*[^*]*\*+([^/][^*]*\*+)*/!', '', $buffer);
  // remove tabs, spaces, newlines, etc.
  $buffer = str_replace(array("\r\n", "\r", "\n", "\t", '  ', '    ', '    '), '', $buffer);
  return $buffer;
}
 
/* the css-files to include and compress */
include('reset.css');
include('style.css');
 
ob_end_flush();
?>