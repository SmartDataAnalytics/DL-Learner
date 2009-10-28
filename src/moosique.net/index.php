<?php 
  session_start(); 
  /* 
  Welcome to moosique.net - a semantic web based internet-radio 
  
  see README.txt for more details
  */
?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <link rel="stylesheet" href="css/" />
  <title>moosique.net</title>
</head>
<body>
<div id="container">
  <div id="header">
    <h1>moosique.net</h1>
    
    <div id="nav">
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
          <option value="albumSearch">Album</option>
          <option value="songSearch">Song</option>
          <option value="lastFM">last.fm</option>
        </select>
        <input id="searchValue" name="searchValue" type="text" />
        <input id="searchSubmit" name="searchSubmit" value="Search" title="Search" type="submit" />
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
      <div id="results" class="results">
        
      </div>
    </div>

    <div id="recommendations">
      <form id="autoAdd" method="get" action="">
        <div>
          <label for="autoAddCheckbox" title="Check this to automatically add a random song from your recommendations to your playlist everytime your recommendations are updated.">Autoadd recommendations</label>
          <input type="checkbox" id="autoAddCheckbox" checked="checked" />
        </div>
      </form>
      <h2>Recommendations</h2>
      <p>
        These recommendations are generated every time you listen to a song 
        for at least half it's length, assuming that you liked it. You can click on a
        recommended album to add it to the playlist, or you can <a href="#" id="addRandom">click
        here to just add a random song from your recommendations</a>.<br />
        
      </p>
      <p>
        <a href="#" id="generateRecommendations" class="button" title="If there is nothing showing up here, you can generate your list of recommendations by clicking here.">Reload recommendations</a>
      </p>
      <div id="recommendationResults" class="results">
      
      
      </div>
    </div>

    <div id="information">
      <h2>About the artist...</h2>
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
      <p><a href="#" id="resetPlaylist" class="button" title="Click here to delete all tracks from your playlist.">Delete all</a></p>
      
      <h2>Recently Listened to</h2>
      <p>These are the songs you recently listened to. Click on a song to re-enqueue it to your current playlist.</p>
      <ol id="recently">
        <li></li>
      </ol>
      <p><a href="#" id="resetRecently" class="button" title="Click here to reset your &raquo;recently listened to&laquo;-list.">Reset</a></p>
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
  if ($c->getConfig('debug') == 1) /* if debugging is active include js without compressing anything */ {
?>
<script src="http://mediaplayer.yahoo.com/js"></script>
<script src="js/mootools-1.2.4-core-nc.js"></script>
<script src="js/mootools-1.2.4.2-more-nc.js"></script>
<script src="js/moosique.js"></script>
<script src="js/debug.js"></script>
<script src="js/start.js"></script>
<?php } else /* compress for production and dont include debugger */ { ?>
<script src="http://mediaplayer.yahoo.com/js"></script>
<script src="js/"></script>
<?php } ?>
</body>
</html>