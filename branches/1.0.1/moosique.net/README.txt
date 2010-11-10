Installation-Requirements:
==========================
- PHP 5.2.x or above (yes, 5.3 works)
  - output_buffering has to be enabled to use debugging features (if enabled in config.ini)
    (FirePHP is included in this package, but you have to install FirePHP in Firefox of course)
  - PEAR-Packages HTTP and HTTP_Request (used by Utilities.php from DL-Learner)
  - PHP Soap functionality has to be enabled (compile --enable-soap or use sth. like "yum install php-soap")
- A running DL-Learner Webservice Instance
  - Set paths/URLs in config.ini (default values are set)
  - For compiling and usage instructions visit: http://dl-learner.org/Projects/DLLearner
  - Java Version 6 is required!
- If you want to use your own sparql-endpoint (virtuoso, sesame etc.)  just set 
  "jamendo" in config.ini
- Have a look at /moosique/config.ini for all configuration options and explanation
  
  
Notes:
======
- This is a modern piece of websoftware, use a modern browser!
  Tested and working in:
  - Firefox 3.5.x
  - Safari 4.x and Webkit nightly builds
  - Chromium nightly builds and Google Chrome 4.x
  - Opera 10.x (though not as beautiful)
- Not tested in Internet Explorer, IE8 should work, though
- JavaScript has to be enabled, this is after all an AJAX-Application
- Debugging makes use of Firebug/FirePHP, thus only working in Firefox
- A decent broadband connection will be helpful for streaming and downloading music


Known Bugs:
===========
- On Mac OS X Snow Leopard if using the default JVM everything will be terribly
  slow. Using another JVM (soylatte, openjdk) will cause some problems, but there
  is a fix for this in the config.ini


Planned Features:
=================
- RDFa Support for artist information
- Unique URLs for ajax (bookmarkable, Back-Button-Support),
  see: http://ajaxpatterns.org/Unique_URLs
- Better accessibility and keyboard support


Maybe someday:
==============
- User profiles -->
  - Scrobbling-Support for last.fm 
  - Playlist export and Download, saving the playlist state for future visits
- Geonames-based search



Questions? Visit me at http://nebelschwa.de and write me an email, or use
the DL-Learner racker for reporting bugs or posting feature Requests:
http://sourceforge.net/tracker/?group_id=203619

Best regards,

Steffen Becker.