// Default Player Config for moosique.net
var YMPParams = {
  autoplay: false,
  parse: false, // do not parse initial content
  volume: 1.0,
  displaystate: 3 // 1 for debugging, displays YahooGUI, 3 hides the YMP
}

/**
 * 
 */
var ympPlayerConfig = function() {
  var y = YAHOO.MediaPlayer;
  // display status message
  $('status').set({'text': 'Player ready.', 'tween': {duration: 3000}});
  $('status').tween('opacity', [1, 0]);
  
  /**
   * progress: Change the track position and duration every time
   * 
   * TODO: not as a class, since moo and yui are somewhat incompatible 
   * when it comes to subscribers, maybe find a nicer method here
   * 
   */
  var progress = function() {
    $$('.currentlyPlaying h4').set('text', 
      secondsToMinutesAndSeconds(y.getTrackPosition()) + ' / ' + 
      secondsToMinutesAndSeconds(y.getTrackDuration()) 
    );
  }
  
  /**
   * playlistUpdate: every time the playlist is updated
   * we add a delete-button to each playlistitem and
   * update the status on what happened
   */
  var playlistUpdate = function() {
    // delete button for playlist items
    var del = new Element('a', {
      'class': 'del', 
      'href': '#', 
      'title': 'Remove this Song from the playlist',
      'html': 'X',
      'events': {
        'click': function() {
          this.getParent().destroy();
          // and refresh the playlist if clicked
          y.addTracks($('playlist'), '', true);
        },
      }
    });
    // Every playlistitem gets the delete button on playlistrefresh
    $('playlist').getChildren().each(function(li) {
      if (!(li.getLast().match('.del'))) { 
        del.inject(li);
      }
    });
  }  
  
  /**
   * trackPause: we change the Pause-Button to a Play-Button
   * and Update the status on #now
   */
  var trackPause = function() {
    $$('.currentlyPlaying h2').set('text', 'Player paused.');
    $('playPause').setStyle('background-position', '0px 0px');
  }
  
  /**
   * trackStart: we change the Play-Button to a Pause-Button
   * and Update the status on #now and display whats playing
   */
  var trackStart = function() {
    $$('.currentlyPlaying h2').set('text', 'Currently playing:');
    $$('.currentlyPlaying h3').set('text', 
      y.getMetaData().artistName + ' - ' + y.getMetaData().title
    );
    $('playPause').setStyle('background-position', '0px -40px');
  }
  
  /**
   * trackComplete: we change the Pause-Button to a Play-Button
   */
  var trackComplete = function() {
    $('playPause').setStyle('background-position', '0px 0px');
    /* TODO
     * Nice, someone listened a track until the end, this is a good thing,
     * we can assume our recommendation was not that bad... 
     * do sth. nifty stuff here.
     */ 
  }

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
}

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
    this.playPause = $('playPause');
    this.prev = $('prev');
    this.next = $('next');
    this.stop = $('stop');
    this.mute = $('mute');

    this.status = $('status');
    this.playlist = $('playlist');

    this.nowPlayingHeader = $$('.currentlyPlaying h2');
    this.nowPlayingTrack = $$('.currentlyPlaying h3');
    this.nowPlayingTime = $$('.currentlyPlaying h4');
    
    this.setOptions(options);
    this.addEventsToButtons();
    this.initPlaylist();
    
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
      that.nowPlayingHeader.set('text', 'Player stopped.');
      that.nowPlayingTrack.set('text', '...');
      that.nowPlayingTime.set('text', '0:00 / 0:00');
      that.y.stop();
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
    
    // Make the playlist sortable
    that.makeSortableLists(that.playlist);
    
    // TODO this does not belong here?
    $$('#recommended li a').each(function(a) {
      a.addEvent('click', function(e) {
        // prevent link following
        e.stop();
        a.set('class', 'htrack');

        var liItem = a.getParent();
        // move to the playlist
        liItem.inject(that.playlist);
        that.refreshPlaylist();   
      });    
    })
    
  },
  
  /**
   * 
   */
  refreshPlaylist: function() {
    var that = this;
    
    that.y.addTracks(that.playlist, '', true);
    that.makeSortableLists(that.playlist);
    that.displayStatusMessage('Playlist updated.');
    
  },
  
  /**
   * Makes list items of given lists sortable and Draggable to 
   * other given lists
   * 
   * @param {Object} lists An Element-Collection of lists (ol, ul)
   */
  makeSortableLists: function(lists) {
    
    var that = this;
    
    new Sortables(lists, {
      // indicate moving state by adding styles
      onStart: function(li) {
        li.addClass('moving');
      },
      // when moving is done, remove style and update the playlist
      onComplete: function(li) {
        li.removeClass('moving');
        // add the htrack-class if dragged to the playlist so 
        // that ymp recognizes the track as playable
        if (li.getParent().get('id') == 'playlist') {
          // TODO   ---    li.getFirst('a').set('class', 'htrack');
          // reload playlist
          that.y.addTracks(that.playlist, '', true);
          that.displayStatusMessage('Playlist updated.');
        }
      }
    });
  },
  
  /**
   * Displays a status message
   * 
   * @param {Object} message
   */
  displayStatusMessage: function(message) {
    // Update Status and fade out
    this.status.set({
      'text': message,
      'tween': {duration: 3000}
    });
    this.status.tween('opacity', [1, 0]);
  }
  
});

// Create an instance of the moosique.net Player UI
var mooPlayer = new moosiquePlayer();

