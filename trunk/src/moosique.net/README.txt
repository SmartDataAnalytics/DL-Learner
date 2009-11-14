Installation-Requirements:
==========================

TODO: write me!

See /moosique/config.ini

- PHP 5.2.x or above (yes, 5.3 works)
  - output_buffering has to be enabled to use debugging features (if enabled in config.ini)
    (FirePHP is included in this package)
  - PEAR-Packages HTTP and HTTP_Request (used by Utilities.php from DL-Learner)
  - 

- A running DL-Learner Webservice Instance
  - Set paths/URLs in config.ini
  
  
Notes:
======
- This is a modern piece of websoftware, use a modern browser!
  Tested and working in:
  - Firefox 3.5.x
  - Safari 4.x and Webkit nightly build r50383
  - Chromium Build 30678
  - Opera 10.x (though not as beautiful)
- JavaScript has to be enabled, this is an after all an AJAX-Application
- Debugging makes use of Firebug/FirePHP, thus only working in Firefox

- 


Known Bugs:
===========
- Moving the current playing song in the playlist down or up
  breaks the order of the playlist in the Yahoo Media Player``


Planned Features:
=================
- RDFa Support for artist information
- Unique URLs for ajax (bookmarkable, Back-Button)
- Scrobbling-Support for last.fm 
- Playlist export and Download
