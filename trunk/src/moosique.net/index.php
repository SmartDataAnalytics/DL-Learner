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
      <ol>
        <li>
          <select name="searchType" id="searchType">
            <option value="allSearch">All</option>
            <option value="artistSearch">Artist</option>
            <option value="tagSearch">Tag</option>
            <option value="songSearch">Song</option>
            <?php /* TODO <option value="lastfm">Last.fm-User</option> */ ?>
          </select>
          <input id="searchValue" name="searchValue" type="text" />
          <input id="searchSubmit" name="searchSubmit" value="Search" title="Search" type="submit" />
          <img id="loadingImg" src="img/loading.gif" alt="Loading..." />
        </li>
      </ol>
    </form>
    <div id="playerControls">
      <a href="#" id="playPause" title="Play/Pause">Play / Pause</a>
      <a href="#" id="stop" title="Stop playing">Stop</a>
      <a href="#" id="prev" title="Play previous Track in Playlist">Previous Tack</a>
      <a href="#" id="next" title="Play next Track in Playlist">Next Track</a>
      <a href="#" id="mute" title="Sound on/off">Mute</a>
    </div>
    <h4 id="status"> </h4>
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
          You can find information about the song currently playing in the tab &raquo;Info&laquo; and edit and view
          your current Playlist in the &raquo;Playlist&laquo;-Tab.
        </p>
        <p>
          Now get started and add something to the Playlist!
        </p>
        
        <pre>
          <?php
          
          // recently Heard == posExamples testing 
          /*
          if (!empty($_COOKIE['moosique'])) {
            $recent = json_decode(stripslashes($_COOKIE['moosique']))->recentlyListened;
            $posExamples = array();
            foreach($recent as $link) {
              preg_match_all('#<a\s*(?:rel=[\'"]([^\'"]+)[\'"])?.*?>((?:(?!</a>).)*)</a>#i', $link, $record);
              array_push($posExamples, $record[1][0]);
            }
          }
          
          print_r(array_unique($posExamples));
          */
          ?>
        </pre>
        
      </div>
      <div id="results">
        
      </div>
    </div>

    <div id="recommendations">
      <h2>Recommended Songs</h2>
      <p>These are the automatically generated recommendations. Click on a song to add it to your playlist.</p>
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
      <h2>Playlist</h2>
      <p>
        You can delete entries from the playlist by clicking the small x on the left and change their order by clicking on the small up- and down-arrows.<br />
      </p>   
      <ol id="playlist">
        <li></li>  
      </ol>
      
      <h2>Recently Listened to</h2>
      <p>These are the songs you recently listened to. Click on a song to re-enqueue it to your current playlist.</p>
      <ol id="recently">
        <li></li>
      </ol>
    </div>

    <div id="help">
      
    </div>

  </div> <!-- end content -->

  <div id="footer">
    <a href="http://jamendo.com">Jamendo</a> |
    <a href="http://mediaplayer.yahoo.com/">Yahoo! Media Player</a> | 
    <a href="http://aksw.org/Projects/DLLearner">DL-Learner</a>      
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