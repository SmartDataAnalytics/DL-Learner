/**
 * Debugger-Class using the mootools-log
 *
 * @package moosique.net
 * @author Steffen Becker
 */
var Debugger = new Class({
  Implements: Log,
  initialize: function(){
    this.enableLog().log('Log-Console for moosique.net activated.');
  }
});
// global debugger-instance
var debug = new Debugger;
