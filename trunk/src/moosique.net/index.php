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
<div id="topBorder"></div>
<div id="container">
  <div id="header">
    <h1>moosique.net</h1>
    <div id="nav">
      <ul class="clearfix">
        <li class="active"><a href="search/" class="home">Search</a></li>
        <li><a href="playlist/" class="player">Playlist</a></li>
        <li><a href="recommendations/" class="recommendations">Recommendations</a></li>
        <li><a href="more-info/" class="info">More Info</a></li>
        <li><a href="help/" class="help">Help</a></li>
      </ul>
    </div>
    <form id="searchForm" method="get" action="moosique/">
      <div>
        <select name="searchType" id="searchType">
          <option value="allSearch">All</option>
          <option value="artistSearch">Artists</option>
          <option value="tagSearch">Tags</option>
          <option value="albumSearch">Albums</option>
          <option value="songSearch">Songs</option>
          <option value="lastFM">last.fm</option>
        </select>
        <input id="searchValue" name="searchValue" type="text" />
        <input id="searchSubmit" name="searchSubmit" value="Search" title="Search" type="submit" />
      </div>
    </form>
    <div id="playerControls">
      <a href="play-pause/" id="playPause" title="Play/Pause">Play / Pause</a>
      <a href="stop/" id="stop" title="Stop playing">Stop</a>
      <a href="previous/" id="prev" title="Play previous Track in Playlist">Previous Tack</a>
      <a href="next/" id="next" title="Play next Track in Playlist">Next Track</a>
      <a href="mute/" id="mute" title="Sound on/off">Mute</a>
    </div>
    <div id="status"> </div>
    <div id="playing">
      <span class="info">Player stopped</span>
      <span class="track">...</span>
      <span class="time">0:00 / 0:00</span>
      <span class="download"><a href="#">Download this song</a></span>
    </div>
  </div>
  <div id="content">
    <div id="home">
      <div id="results" class="results">
        <h2>Welcome to moosique.net!</h2>
        <p>
          Want to listen to some good free music? Search for something you like and add it to your playlist.
          By listening to songs you like, moosique will automatically try to learn about
          your musical taste and generate recommendations. You can find them in the tab &raquo;Recommendations&laquo;.
          You can also enter your <a href="http://last.fm">last.fm</a>-username to automatically use your
          most-used tags to generate a initial list of recommendations.
        </p>
        <p>
          After you have found something you want to listen to, just add it to your playlist and click the play-button.
        </p>
        <p>
          Need help? Click on the &raquo;Help&laquo;-Tab to get more information about how to use moosique.net
        </p>
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
    <div id="info">
      <h2>Listen to a song to get more info about it.</h2>
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
      <h2>How to use moosique.net</h2>
      <h3>Searching</h3>
      <p>
        Before you can listen to music, you first have to search for something to add your first song or album to 
        your playlist. You can search for <em>artists</em>, <em>tags</em>, <em>albums</em> or <em>songs</em>. 
        If you are lazy and a last.fm-user, you can choose <em>last.fm</em> from the search-dropdown and just enter 
        your username, moosique.net will then start a search using your most used tags from last.fm.<br />
        If you are searching for tags, and your search is more than one word, the results will be better.
        For example: a search for &quot;rock&quot; will give you lots of results, where a search for "hard rock"
        will be more specific, giving you better search results. Just try it, you can't break anything.
      </p>
      <h3>Recommendations</h3>
      <p>
        moosique.net uses the mighty <a href="http://aksw.org/Projects/DLLearner">DL-Learner</a> to generate recommendations
        based on the songs you have listened to. 
      </p>
      <h3>Requirements</h3>
      <p>
        To use moosique.net you should have:
      </p>
      <ul>
        <li>A decent, modern browser, such as <a href="http://getfirefox.com">Firefox</a>, <a href="http://apple.com/de/safari/download/">Safari</a> or <a href="http://google.com/chrome/">Google Chrome</a></li>
        <li>JavaScript activated</li>
        <li>The <a href="http://www.adobe.com/se/products/flashplayer/">Adobe Flash Player</a> plugin for your browser</li>
        <li>A fast internet connection</li>
        <li>Some good headphones or loudspeakers of course. It's moosique after all!</li>
      </ul>
    </div>
  </div> <!-- end content -->
  <div id="footer">
    <a href="http://aksw.org/Projects/DLLearner">DL-Learner</a>
    <a href="http://bis.informatik.uni-leipzig.de/">Universität Leipzig BIS</a>
    <a href="http://jamendo.com">Jamendo</a>
    <a href="http://mediaplayer.yahoo.com/">Yahoo! Media Player</a> 
    <a href="http://mootools.net">mootools</a>
    <a href="http://webgefrickel.de">webgefrickel.de</a>
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