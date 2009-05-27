<?php session_start(); ?><!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  
<head>
  <title>moosique.net</title>
  <link href="css/default.css" rel="stylesheet" type="text/css" />
  <link href="css/style.css" rel="stylesheet" type="text/css" />
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
</head>

<!--[if IE 7]><body class="ie7"><![endif]-->
<!--[if lte IE 6]><body class="ie6"><![endif]-->
<!--[if !IE]><!--><body><!-- <![endif]-->

  <div id="headerContainer">
    <div id="header">
      <h1><a href="index.php">moosique.net</a></h1>
      
      <div id="mainMenu">
        <ul class="clearfix">
          <li class="active"><a href="#" class="home">Home</a></li>
          <li><a href="#" class="player">Player</a></li>
          <li><a href="#" class="recommendations">Recommendations</a></li>
          <li><a href="#" class="information">Information</a></li>
          <li><a href="#" class="help">Help</a></li>
        </ul>
      </div>
      
      <form id="searchForm" method="get" action="moosique/">
        <div class="clearfix">
           <select name="typeOfSearch" id="typeOfSearch">
            <option value="artistSearch">Artist</option>
            <option value="tagSearch">Tag</option>
            <?php /* 
            <option value="songSearch">Song</option>
            <option value="lastfm">Last.fm-User</option> 
            */ ?>
          </select>
          <input id="searchValue" name="searchValue" type="text" />
          <input id="searchSubmit" name="searchSubmit" value="Search" title="Search" type="submit" disabled="disabled" />
          <img id="loadingImg" src="img/loading.gif" alt="Loading..." />
        </div>
      </form>

      <div id="status"> </div>
  
      <div class="currentlyPlaying">
        <h2>Player stopped</h2>
        <h3>...</h3>
        <h4>0:00 / 0:00</h4>  
      </div>
      
    </div>
  </div>

  <div id="container">
    <div id="mainContainer">
      <div id="content" class="clearfix">
        
        <div id="home">
          <div id="welcome">
            <h2>Welcome to moosique.net!</h2>
            <p>
              Want to listen to some good free music? Just enter an artist or song 
              name or search for music using tags <!-- or enter your last.fm username --> and let the moogic
              happen. By listening to songs you like, the system will automatically learn about
              your musical taste and generate recommendations. You can find them in the tab &raquo;Recommendations&laquo;.
            </p>
            <p>
              You can find information about the song currently playing in the tab &raquo;Information&laquo; and view
              your Playlist and control the Player in the Tab &raquo;Player&laquo;.
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
          <p>Click a song to add it to your playlist.</p>
          <ol id="recommended">
            <li></li>
          </ol>
          
        </div>

        <div id="information">
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
        
        <div id="player">
          <h3>Playlist</h3>
          <p>
            You can delete entries from the playlist by clicking the small x on the left. <br />
            You also can move the playlist-entries around to change their order.
          </p>   

          <ol id="playlist">
            <li></li>  
          </ol>

          <div id="playerControls">
            <a href="#" id="playPause" title="Play/Pause">Play / Pause</a>
            <a href="#" id="stop" title="Stop playing">Stop</a>
            <a href="#" id="prev" title="Play previous Track in Playlist">Previous Tack</a>
            <a href="#" id="next" title="Play next Track in Playlist">Next Track</a>
            <a href="#" id="mute" title="Sound on/off">Mute</a>
          </div>
          
        </div>

        <div id="help">
          
        </div>

      </div> <!-- end content -->
    </div> <!-- end mainContainer -->
    <div id="footer">
      <a href="http://jamendo.com">Jamendo</a> |
      <a href="http://mediaplayer.yahoo.com/">Yahoo! Media Player</a> | 
      <a href="http://aksw.org/Projects/DLLearner">Powered by DL-Learner</a>      
    </div>
  </div> <!-- end container -->
  

  
  <!-- JS at the bottom, faster loading pages -->
  <script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script>
  
  <script type="text/javascript" src="js/mootools-core.js"></script>
  <script type="text/javascript" src="js/mootools-more.js"></script>
  
  <script type="text/javascript" src="js/player.js"></script>
  <script type="text/javascript" src="js/interface.js"></script>
  <script type="text/javascript" src="js/ajax.js"></script>
</body>
</html>