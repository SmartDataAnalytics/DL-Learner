<?php session_start(); ?>
<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
  <link href="css/" rel="stylesheet" type="text/css" />
  <title>moosique.net</title>
</head>
<body>
<div id="container">
  <div id="header">
    <h1>moosique.net</h1>
    <div id="mainMenu">
      <ul class="clearfix">
        <li class="active"><a href="#" class="home">Search</a></li>
        <li><a href="#" class="player">Playlist</a></li>
        <li><a href="#" class="recommendations">Recommendations</a></li>
        <li><a href="#" class="information">Info</a></li>
        <li><a href="#" class="help">?</a></li>
      </ul>
    </div>
    <form id="searchForm" method="get" action="moosique/">
      <div>
        <select name="searchType" id="searchType">
          <option value="allSearch">All</option>
          <option value="artistSearch">Artist</option>
          <option value="tagSearch">Tag</option>
          <option value="songSearch">Song</option>
          <option value="lastFM">last.fm</option>
        </select>
        <input id="searchValue" name="searchValue" type="text" />
        <input id="searchSubmit" name="searchSubmit" value="Search" title="Search" type="submit" />
        <img id="loadingImg" src="img/loading.gif" alt="Loading..." />
      </div>
    </form>
    <div id="playerControls">
      <a href="#" id="playPause" title="Play/Pause">Play / Pause</a>
      <a href="#" id="stop" title="Stop playing">Stop</a>
      <a href="#" id="prev" title="Play previous Track in Playlist">Previous Tack</a>
      <a href="#" id="next" title="Play next Track in Playlist">Next Track</a>
      <a href="#" id="mute" title="Sound on/off">Mute</a>
    </div>
    <div id="status"> </div>
    <div id="playing">
      <span class="info">Player stopped</span>
      <span class="track">...</span>
      <span class="time">0:00 / 0:00</span>
    </div>
  </div>

  <div id="content">
    <div id="home">
      <div id="welcome">
        <h2>Welcome to moosique.net!</h2>
        <p>
          Want to listen to some good free music? Just enter an artist or song 
          name or search for music using tags and let the moogic
          happen. By listening to songs you like, the system will automatically learn about
          your musical taste and generate recommendations. You can find them in the tab &raquo;Recommendations&laquo;.
        </p>
        <p>
          You can also enter your <a href="http://last.fm">last.fm</a>-username to automatically use your
          most-used tags to generate a initial list of recommendations.
        </p>
        <p>
          You can find information about the song currently playing in the tab &raquo;Info&laquo; and edit and view
          your current Playlist in the &raquo;Playlist&laquo;-Tab.
        </p>
        <p>
          Now get started and add something to the Playlist!
        </p>
      </div>
      <div id="results">
        
      </div>
    </div>

    <div id="recommendations">
      <h2>Recommended Songs</h2>
      <p>
        These recommendations are generated every time you listen to a song 
        for at least half it's length, assuming that you liked it.
      </p>
      
      <div id="recommendationResults">
      
      
      </div>
      <p>
        <a href="#" id="generateRecommendations">Nothing showing up here? You can also 
        generate your list of recommendations by clicking here.</a>
      </p>
    </div>

    <div id="information">
      <div id="moreInfo">

      </div>
    </div>
    
    <div id="player">
      <h2>Playlist</h2>
      <p>
        You can delete entries from the playlist by clicking the small x on the right and 
        change their order by clicking on the small up- and down-arrows.<br />
      </p>   
      <ol id="playlist">
        <li></li>  
      </ol>
      
      <h2>Recently Listened to</h2>
      <p>These are the songs you recently listened to. Click on a song to re-enqueue it to your current playlist.</p>
      <ol id="recently">
        <li></li>
      </ol>
      <p><a href="#" id="reset">Click here to reset your &raquo;recently listened to&laquo;-list.</a></p>
    </div>

    <div id="help">
      
    </div>

  </div> <!-- end content -->

  <div id="footer">
    <a href="http://aksw.org/Projects/DLLearner">DL-Learner</a> |
    <a href="http://jamendo.com">Jamendo</a> |
    <a href="http://mediaplayer.yahoo.com/">Yahoo! Media Player</a> 
  </div>
</div> <!-- end container -->

<?php
  include('moosique/classes/Config.php');
  $c = new Config();
  if ($c->getConfig('debug') == 1) /* debugging active */ {
?>
<script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script>
<script type="text/javascript" src="js/mootools-1.2.3-core-nc.js"></script>
<script type="text/javascript" src="js/moosique.js"></script>
<script type="text/javascript" src="js/debug.js"></script>
<script type="text/javascript" src="js/start.js"></script>

<?php } else /* compress for production and dont include debugger */ { ?>
<script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script>
<script type="text/javascript" src="js/"></script>
<?php } ?>
</body>
</html>