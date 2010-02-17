===============================================================================
 
     xajax PHP & Javascript Library
     The easiest way to develop powerful Ajax applications with PHP

     Version 0.5 (Beta 4)
     README Text File
     September 5, 2007

     ------------------------------------------------------
     | ** Release Notes:                                  |
     | See release_notes.txt in this download archive     |
     |                                                    |
     | ** Project Managers:                               |
     | Joseph Woolley (joe@calledtoconstruct.net)         |
     | J. Max Wilson (jmaxwilson@users.sourceforge.net)   |
     |                                                    |
     | ** Developers:                                     |
     | Steffen Konerow (steffen@nrg-media.de)             |
     | Jared White (jared@intuitivefuture.com)            |
     | Eion Robb (eion@bigfoot.com)                       | 
     ------------------------------------------------------

===============================================================================

    :: To find out what's changed since the 0.5 Beta 3 release of xajax, ::
    :: view the Release Notes in the file listed above.                  ::

===============================================================================

xajax 0.5 is leaps and bounds forward from the last stable release, however, 
xajax 0.2.5 was released to help bridge the gap.  xajax 0.5 has an 
extensible and flexible plugin interface so both the php and javascript
engines can be adapted to fit your needs.  With xajax 0.5 beta 3, we've added
an HTML control library with built in support for both plain and xajax based
javascript and added new features that will give you a development platform
to build on for years to come.  In this release, we've added the ability
to customize debug, warning and error messages, thus allowing for easier 
development and debugging for non-english speaking programmers.

Beta 4 represents a solidification of the xajax php and javascript code, so
from this point, the xajax interface will remain mostly unchanged until stable
release.  We have greatly improved the documentation that is available for
the xajax api.  In addition, we will continue to work hard to resolve any
issues that are discovered in the code, the documentation, wiki and to answer
questions in the forum.

We published an updated website at http://xajaxproject.com  Thanks to Jared and
Steffen for putting together a great looking site with a lot of functionality 
and flexibility!  The forums continue to be active and interesting, so be sure 
to check in from time to time to keep current with all things xajax at 
http://community.xajaxproject.com and visit sourceforge for posting feature
requests, bug fix requests and patches at http://www.sourceforge.net/projects/xajax

Thank you for downloading xajax 0.5 beta 4!

____________________________________________________________________


"it's safer, better, faster and even more bright and shiny" - Steffen

____________________________________________________________________

1. Introduction

xajax is a PHP library that you can include in your PHP scripts
to provide an easy way for Web pages to call PHP functions or
object methods using Ajax (Asynchronous Javascript And XML). Simply
register one or more functions/methods with the xajax object that
return a proper XML response using the supplied response class, add
a statement in your HTML header to print the Javascript include,
and run a request processor prior to outputting any HTML. Then add
some simple Javascript function calls to your HTML, and xajax takes
care of the rest!

xajax includes a Javascript object to facilitate the communication
between the browser and the server, and it can also be used as a
Javascript library directly to simplify certain DOM and event
manipulations. However, you can definitely choose to use a
dedicated Javascript "engine" of your liking and integrate it with
xajax's client/server communication features. Since xajax is moving
towards a highly modular, plugin-based system, you can alter and extend
its behavior in a number of ways.

2. For More Information

The official xajax Web site is located at:
http://www.xajaxproject.org

Visit the xajax Forums at:
http://community.xajaxproject.org
to keep track of the latest news and participate in the community
discussion.

There is also a wiki with documentation, tips & tricks, and other
information located at:
http://wiki.xajaxproject.org

3. Installation

To run xajax, you need:
* Apache Web Server or IIS for Windows XP/2003 Server
   (other servers may or may not work and are not supported at this
   time)
* PHP 4.3.x or PHP 5.x
* Minimum supported browsers: Internet Explorer 5.5, Firefox 1.0 (or
   equivalent Gecko-based browser), Safari 1.3, Opera 8.5 (older
   versions only work with GET requests)

To install xajax:
Unpack the contents of this archive and copy them to your main Web
site folder. Or if you wish, you can put all of the files in a
dedicated "xajax" folder on your Web server (make sure that you
know what that URL is relative your site pages so you can provide
xajax with the correct installed folder URL). Note that the
"thewall" folder in the "examples" folder needs to be writable by
the Web server for that example to function.

Within the main xajax folder there are four folders: "examples",
"tests", "xajax_js", and "xajax_core". Only "xajax_js" and
"xajax_core" are required to use xajax.

You should be able to view the PHP pages in "tests" from your
Web browser and see xajax working in action. If you can view the
pages but the AJAX calls are not working, there may be something
wrong with your server setup or perhaps your browser is not
supported or configured correctly. If worst comes to worst, post
a message in our forums and someone may be able to help you.

4. Documentation

Detailed documentation for the xajax PHP classes is available on
our wiki (URL listed above in section 2), and more is on the way
(particularly in regards to the Javascript component of xajax).
Another good way of learning xajax is to look at the code for the
examples and tests. If you need any help, pop in the forums and
ask for assistance (and the more specific your questions are,
the better the answers will be).

5. Contributing to xajax

xajax is released under the BSD open source license. If you wish
to contribute to the project or suggest new features, introduce
yourself on the forums or you can e-mail the project managers
and developers at the addresses listed at the top of this README.

6. Good luck and enjoy!

====================================================================
