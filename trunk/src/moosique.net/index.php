<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  
<head>
  <title>moosique.net</title>
  <link href="css/style.css" rel="stylesheet" type="text/css" />
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
</head>

<!--[if IE 7]><body class="ie7"><![endif]-->
<!--[if lte IE 6]><body class="ie6"><![endif]-->
<!--[if !IE]><!--><body><!-- <![endif]-->

  <div id="container">
    
    <div id="header">
      <h1><a href="index.php">moosique.net</a></h1>
      <div id="now">
        <h2>Player stopped</h2>
        <h3>...</h3>
        <p>0:00 / 0:00</p>  
      </div>
      
      <div id="status">
      </div>
      
      <div id="playerControls">
        <a href="#" id="prev" title="Play previous Track in Playlist">Previous Tack</a>
        <a href="#" id="playPause" title="Play/Pause">Play / Pause</a>
        <a href="#" id="stop" title="Stop playing">Stop</a>
        <a href="#" id="next" title="Play next Track in Playlist">Next Track</a>
      </div>
      
      <div id="toggles">
        <a href="#" id="toggleMute" title="Sound on/off">Mute</a>
        <a href="#" id="togglePlaylist" title="Show/Hide Playlist">Playlist</a>
      </div>
    </div>

  
    <div id="playlistContainer">
      <div id="playlistHeader">
        Playlist
        <a href="#" id="closePlaylist" title="Close Playlist Window">X</a>
      </div>
      <ol id="playlist">
        <li><a href="mp3/moosique.mp3" class="htrack">Welcome to moosique</a></li>
      </ol>
      <div id="playlistFooter"> &nbsp; </div>
    </div>
    
    <div id="mainContainer">
      <div id="content">
        <div id="searchContainer">
          <form id="searchForm" method="post" action="php/ajaxer.php">
            <div>
              <select name="typeOfSearch" id="typeOfSearch">
                <option value="artist">Artist</option>
                <option value="song">Songtitle</option>
                <option value="tag">Tag</option>
                <option value="lastfm">Last.fm-User</option>
              </select>
              <input id="search" name="search" type="text" />
              <input id="searchSubmit" name="searchSubmit" value="Search" title="Search" type="submit" />
            </div>
          </form>
        </div>

        <div id="info">
          <h2>Welcome to moosique.net!</h2>
          <p>
            Want to listen to some good free music? Just enter an artist or song 
            name, search for tags or enter your last.fm username and let the moogic
            happen...
          </p>
          <p>
            Or fill your Playlist with these Samples...<br />
            (Drag them to the Playlist)
          </p>
          <ol id="samples">
            <li><a href="http://stream6-3.jamendo.com/8654/mp31/01%20-%20Low%20Earth%20Orbit%20-%20My%20Mistakes.mp3">Low Earth Orbit - My Mistakes</a></li>
            <li><a href="http://stream6-3.jamendo.com/8654/mp31/02%20-%20Low%20Earth%20Orbit%20-%20Like%20Mud.mp3">Low Earth Orbit - Like Mud</a></li>
            <li><a href="http://stream6-3.jamendo.com/8654/mp31/03%20-%20Low%20Earth%20Orbit%20-%20Defend.mp3">Low Earth Orbit - Defend</a></li>
            <li><a href="http://stream6-3.jamendo.com/8654/mp31/04%20-%20Low%20Earth%20Orbit%20-%20What%20Can%20I%20Say.mp3">Low Earth Orbit - What Can I Say</a></li>
          </ol>
        </div>

      </div>
      
      
      <div id="sidebar">
        <div id="moreInfo">
          <h2>About the Artist</h2>
          <img src="http://imgjam.com/albums/8654/covers/1.200.jpg" alt="Cover" />
          <p>
            Iusto odio dignissim qui blandit praesent. Nisl ut aliquip ex ea commodo, consequat 
            duis autem vel eum. Nam liber tempor cum soluta nobis eleifend option congue nihil 
            imperdiet doming id. In hendrerit eu feugiat nulla luptatum zzril delenit augue duis 
            dolore te feugait. Quod ii legunt saepius claritas est etiam processus dynamicus 
            qui nobis videntur parum.
          </p>
        </div>

      </div>
        
      <div id="footer">
        <a href="http://mediaplayer.yahoo.com/">Yahoo! Media Player</a> | 
        <a href="http://aksw.org/Projects/DLLearner">Powered by DL-Learner</a>      
      </div>


    </div>
    
  </div>
  
  <!-- JS at the bottom, faster loading pages -->
  <script type="text/javascript" src="js/mootools-core.js"></script>
  <script type="text/javascript" src="js/mootools-more.js"></script>
  <script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script>

  <script type="text/javascript" src="js/player.js"></script>
  <script type="text/javascript" src="js/ajax.js"></script>
  <!--[if lte IE 6]><script type="text/javascript" src="js/ie6fixes.js"></script><![endif]-->
</body>
</html>