// Default Player Config for moosique.net
var YMPParams = {
  autoplay: false,
  parse: false, // do not parse initial content
  volume: 1.0,
  displaystate: 3 // 1 for debugging, displays YahooGUI, 3 hides the YMP
};



/**
 * 
 */
var ympPlayerConfig = function() {
  var y = YAHOO.MediaPlayer;

  // display ready-status message
  document.id('status').set({
    'text': 'Player ready.', 
    'tween': {duration: 5000}
  }).tween('opacity', [1, 0]);
  
  /**
   * progress: Change the track position and 
   * duration displayed every time, as usual in players
   * 
   * every time the song is played for at least half it's
   * playtime, we assume that the song is liked, so we add it
   * to the list we create the recommendations from
   * (much like last.fm scrobbling)
   */
  var progress = function() {
    $$('#playing .time').set('text', 
      secondsToMinutesAndSeconds(y.getTrackPosition()) + ' / ' + 
      secondsToMinutesAndSeconds(y.getTrackDuration()) );
    
    // TODO 40 = 2, for testing purposes only
    if (Math.ceil(y.getTrackPosition()) == Math.ceil(y.getTrackDuration()/40)) {
      /* This is where the main magic happens
      
      After havin listened to a song for half its time
      we save this song to the positive examples list
      and then send a request to the dllearner who then 
      calulates new recommendations.
      */
      
      // first, we update the cookie with the last played song
      // we clone the parent list-item of the last played song
      var lastListenedListItem = y.getMetaData().anchor.getParent().clone();
      var lastListened = lastListenedListItem.getFirst();
      // remove the buttons and ymp-classes from the child-link
      lastListenedListItem.getChildren('.moveUp').destroy();
      lastListenedListItem.getChildren('.moveDown').destroy();
      lastListenedListItem.getChildren('.delete').destroy();
      lastListened.erase('class');
      lastListened.addClass('htrack');
      lastListened.getChildren('em').destroy();
      var last = lastListenedListItem.get('html');

      // get the current cookie
      var recentlyListenedCookie = Cookie.read('recentlyListened');
      var recentlyListened = [];
        
      if (recentlyListenedCookie) { // does the cookie exist?
        recentlyListened = JSON.decode(recentlyListenedCookie);
        if (recentlyListened) { // if the cookie is not totally empty
          // update recently listened and write the cookie, limit to 10 entries, 
          // due to cookie-max-size of 4KB
          // saves the whole link-element with rel to jamendo-album-id and mp3-link
          if (recentlyListened.length >= 10) { 
            recentlyListened.shift(); // remove first item and shift
          } 
        } 
      }
      // add the last played to the array
      recentlyListened.push(last); 
      // and write/update the cookie  
      recentlyListenedCookie = Cookie.write( /* save for one year */
        'recentlyListened', JSON.encode(recentlyListened), { duration: 365 } 
      );

      // update the #recently ol
      var count = recentlyListened.length;
      var recentlyHTML = '';
      for (var i = 0; i < count; i++ ) {
        recentlyHTML += '<li>';
        recentlyHTML += recentlyListened[i] + '<br />';
        recentlyHTML += '</li>';
      }
      document.id('recently').set('html', recentlyHTML);

      // after updating html-list, we use it to get the rel-tags
      // with the corresponding albums, to feed em to the dl-learner
      // dont use double values
      var positiveExamples = new Array();
      $$('#recently a').each(function(a) {
        var rel = a.get('rel');
        if (positiveExamples.indexOf(rel) < 0) {
          positiveExamples.push(rel);
        } 
      });
      

      // TODO
      console.log('Submit this list of positive examples to the dl-learner ' + positiveExamples);


      // TODO 
      // send ajax request and save the scrobbled song
      // and retrieve and update the recommendations
      var getRecommendations = new Request({
        method: 'get', 
        url: 'moosique/index.php',
        onSuccess: function(responseText, responseXML) {
          document.id('status').set({
            'text': 'Added this song to your recently listened to songs.', 
            'tween': {duration: 5000}
          }).tween('opacity', [1, 0]);
        }
      }).send(y.getMetaData().title);
      
      
    }
  };
  
  /**
   * playlistUpdate: every time the playlist is updated
   * we add the events for delete/up/down-buttons to each 
   * playlistitem and update the status on what happened
   */
  var playlistUpdate = function() {
    // delete button
    $$('#playlist .delete').each(function(del) {
      del.removeEvents();
      del.addEvent('click', function(e) {
        e.stop(); // don't folow link
        this.getParent().destroy(); // deletes the li-element
        // and refresh the playlist if clicked
        y.addTracks(document.id('playlist'), '', true);
      });
    });
    
    // up-button
    $$('#playlist .moveUp').each(function(up) {
      up.removeEvents();
      up.addEvent('click', function(e) {
        e.stop(); // don't folow link
        var li = up.getParent();
        var before = li.getPrevious();
        if (before) { // it's not the first one
          li.inject(before, 'before');
          // and refresh the playlist if clicked
          y.addTracks(document.id('playlist'), '', true);
        }
      });
    });
    
    // down button
    $$('#playlist .moveDown').each(function(down) {
      down.removeEvents();
      down.addEvent('click', function(e) {
        e.stop(); // don't folow link
        var li = down.getParent();
        var after = li.getNext();
        if (after) { // it's not the first one
          li.inject(after, 'after');
          // and refresh the playlist if clicked
          y.addTracks(document.id('playlist'), '', true);
        }
      });
    });

    document.id('status').set({'text': 'Playlist updated.', 'tween': {duration: 5000}});
    document.id('status').tween('opacity', [1, 0]);

    // TODO save to current-playlist cookie?
    
  };  
  
  /**
   * trackPause: we change the Pause-Button to a Play-Button
   * and Update the status on #now
   */
  var trackPause = function() {
    $$('#playing .info').set('text', 'Player paused.');
    document.id('playPause').setStyle('background-position', '0px 0px');
  };
  
  /**
   * trackStart: we change the Play-Button to a Pause-Button
   * and Update the status on #now and display whats playing
   */
  var trackStart = function() {
    $$('#playing .info').set('text', 'Currently playing:');
    $$('#playing .track').set('text', y.getMetaData().title);
    document.id('playPause').setStyle('background-position', '0px -40px');
  };
  
  /**
   * trackComplete: we change the Pause-Button to a Play-Button
   */
  var trackComplete = function() {
    document.id('playPause').setStyle('background-position', '0px 0px');
    /* TODO
     * Nice, someone listened a track until the end, this is a good thing,
     * we can assume our recommendation was not that bad... 
     * do sth. nifty stuff here.
     */ 
  };

  /**
   * Converts seconds into a string formatted minutes:seconds
   * with leading zeros for seconds 
   * 
   * @param {Float} seconds
   * @return {String} minsec minutes:seconds
   */
  function secondsToMinutesAndSeconds(seconds) {
    var min = Math.floor(seconds / 60);
    var sec = Math.floor(seconds % 60);
    if (sec < 10) {
      sec = '0' + sec;
    }
    var minsec = min + ":" + sec;
    return minsec;
  }
 
  y.onProgress.subscribe(progress);
  y.onPlaylistUpdate.subscribe(playlistUpdate);
  y.onTrackPause.subscribe(trackPause);
  y.onTrackStart.subscribe(trackStart);
  y.onTrackComplete.subscribe(trackComplete);
};

// Initialize YMP if ready
YAHOO.MediaPlayer.onAPIReady.subscribe(ympPlayerConfig);



/**
 * moosique-Player-Class
 * 
 * 
 * TODO Split out functions and comment it
 */
var moosiquePlayer = new Class({
  Implements: [Options, Events],
  
  options: {
    
  },
  
  /**
   * Initializes the Object and sets some default options
   * 
   * @param {Object} options
   */
  initialize: function(options) {
    this.y = YAHOO.MediaPlayer;
    this.playPause = document.id('playPause');
    this.prev = document.id('prev');
    this.next = document.id('next');
    this.stop = document.id('stop');
    this.mute = document.id('mute');

    this.status = document.id('status');
    this.playlist = document.id('playlist');

    this.nowPlayingInfo = $$('#playing .info');
    this.nowPlayingTrack = $$('#playing .track');
    this.nowPlayingTime = $$('#playing .time');
    
    this.setOptions(options);
    this.addEventsToButtons();
    this.initPlaylist();
    
    this.duration = 5000; // status-message fadeout
    
  },
  
 
  /**
   * adding functionality for the player-GUI and the play, next etc. buttons
   */
  addEventsToButtons: function() {
    
    var that = this;
      
    // the Play-Pause Button
    that.playPause.addEvent('click', function() {
      // STOPPED: 0, PAUSED: 1, PLAYING: 2,BUFFERING: 5, ENDED: 7
      if (that.y.getPlayerState() == 0 ||
          that.y.getPlayerState() == 1 ||
          that.y.getPlayerState() == 7) {
        that.y.play();
      } else {
        that.y.pause();
      }
    });
    
    // the previous-Track Button
    that.prev.addEvent('click', function() {
      that.y.previous();
    });
    
    // the next-Track Button
    that.next.addEvent('click', function() {
      that.y.next();
    });
    
    // the Stop-Playing Button
    that.stop.addEvent('click', function() {
      that.playPause.setStyle('background-position', '0px 0px');
      that.nowPlayingInfo.set('text', 'Player stopped.');
      that.nowPlayingTrack.set('text', '...');
      that.nowPlayingTime.set('text', '0:00 / 0:00');
      that.y.stop();
      // and reload the playlist
      that.refreshPlaylist();
    });
    
    // Mute-Toggle-Switch
    that.mute.addEvent('click', function() {
      if (that.y.getVolume() > 0) {
        that.y.setVolume(0);
        that.mute.setStyle('background-position', '0px -240px');
        that.displayStatusMessage('Player muted.');
      } else {
        that.y.setVolume(1);
        that.mute.setStyle('background-position', '0px -280px');
        that.displayStatusMessage('Player unmuted.');
      }
    });
  },
  
  /**
   * Playlist related functions
   */
  initPlaylist: function() {
    var that = this;
    $$('#recommended a').each(function(a) {
      a.addEvent('click', function(e) {
        // prevent link following
        e.stop();
        a.set('class', 'htrack');

        var liItem = a.getParent();
        // move to the playlist
        liItem.inject(that.playlist);
        that.refreshPlaylist();   
      });    
    });
  },
  
  /**
   * Refreshes the playlist by emptying the current one
   * and reReading the #playlist-container
   */
  refreshPlaylist: function() {
    var that = this;
    that.y.addTracks(that.playlist, '', true);
    that.displayStatusMessage('Playlist updated.');
  },


  /**
   * Displays a status message
   * 
   * @param {Object} message
   */
  displayStatusMessage: function(message) {
    // Update Status and fade out
    var that = this;
    this.status.set({
      'text': message,
      'tween': {duration: that.duration}
    });
    this.status.tween('opacity', [1, 0]);
  }
  
});

// Create an instance of the moosique.net Player UI
var mooPlayer = new moosiquePlayer();

