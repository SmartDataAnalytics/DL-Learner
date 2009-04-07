// Default Player Config for moosique.net
var YMPParams = {
  autoplay: false,
  volume: 1.0,
  displaystate: 3 // 1 for debugging, displays YahooGUI, 3 hides the YMP
}

/**
 * 
 */
var ympPlayerConfig = function() {
  var y = YAHOO.MediaPlayer;
  
  /**
   * progress: Change the track position and duration every time
   * 
   * TODO: not as a class, since moo and yui are somewhat incompatible 
   * when it comes to subscribers, maybe find a nicer method here
   * 
   */
  var progress = function() {
    $$('#now p').set('text', 
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
      'title': 'Remove from playlist',
      'html': 'X',
      'events': {
        'click': function() {
          console.log('Deleted Song from PLaylist');
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
    // Update Status and fade out
    $('status').set({
      'text': 'Playlist updated.',
      'tween': {duration: 3000}
    });
    $('status').tween('opacity', [1, 0]);
  }  
  
  /**
   * trackPause: we change the Pause-Button to a Play-Button
   * and Update the status on #now
   */
  var trackPause = function() {
    $$('#now h2').set('text', 'Player Paused');
    $('playPause').setStyle('background-position', '0px 0px');
  }
  
  /**
   * trackStart: we change the Play-Button to a Pause-Button
   * and Update the status on #now and display whats playing
   */
  var trackStart = function() {
    $$('#now h2').set('text', 'You are listening to:');
    $$('#now h3').set('text', y.getMetaData().title);
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
    this.playlist = $('playlist');
    this.playlistContainer = $('playlistContainer');
    this.closePlaylist = $('closePlaylist');
    this.toggleMute = $('toggleMute');
    this.togglePlaylist = $('togglePlaylist');
    this.nowPlayingHeader = $$('#now h2');
    this.nowPlayingTrack = $$('#now h3');
    this.nowPlayingTime = $$('#now p');
     
    
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
      console.log('Clicked Play/Pause');
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
      console.log('Clicked Prev Button');
      that.y.previous();
    });
    
    // the next-Track Button
    that.next.addEvent('click', function() {
      console.log('Clicked Next Button');
      that.y.next();
    });
    
    // the Stop-Playing Button
    that.stop.addEvent('click', function() {
      console.log('Clicked Stop Button');
      that.playPause.setStyle('background-position', '0px 0px');
      that.nowPlayingHeader.set('text', 'Player stopped');
      that.nowPlayingTrack.set('text', '...');
      that.nowPlayingTime.set('text', '0:00 / 0:00');
      that.y.stop();
    });
    
    // Mute-Toggle-Switch
    that.toggleMute.addEvent('click', function() {
      console.log('Clicked Mute Switch');
      if (that.y.getVolume() > 0) {
        that.y.setVolume(0);
        that.toggleMute.setStyle('text-decoration', 'line-through');
      } else {
        that.y.setVolume(1);
        that.toggleMute.setStyle('text-decoration', 'none');
      }
    });
  },
  
  /**
   * Playlist related functions
   */
  initPlaylist: function() {
    var that = this;
    
    that.togglePlaylist.addEvent('click', function() {
      if (that.playlistContainer.getStyle('display') == 'none') {
        that.playlistContainer.setStyle('display', 'block');
        that.togglePlaylist.setStyle('text-decoration', 'line-through');
      } else {
        that.playlistContainer.setStyle('display', 'none');
        that.togglePlaylist.setStyle('text-decoration', 'none');
      }
    });
    // same for the closePlaylist-Button
    that.closePlaylist.addEvent('click', function() {
      that.playlistContainer.setStyle('display', 'none');
      that.togglePlaylist.setStyle('text-decoration', 'none');
    });
    
    // nifty UI-Stuff, draggable and resizable
    that.playlistContainer.makeDraggable({
      handle: $('playlistHeader')
    });
    that.playlistContainer.makeResizable({
      handle: $('playlistFooter'),
      limit: {x: [300, 600], y: [150, 1000]}
    });

    // opacity and intial hide    
    that.playlistContainer.setStyle('opacity', 0.9); // easier than css hacks
    // this.playlistContainer.setStyle('display', 'none');
    
    // Make the playlist, samples and recommendations sortable
    that.makeSortableLists($$('#playlist, #recommendations ol'));
    // make links unclickable for recommendations and samples
    that.makeLinksUnclickable($$('#recommendations li a'));
    
  },
  
  /**
   * Makes links unclickable by adding event.stop()
   * 
   * @param {Object} links An Element-Collection of links
   */
  makeLinksUnclickable: function(links) {
    links.each(function(a) {
      a.addEvent('click', function(e) {
        e.stop();
      });      
    });
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
          li.getFirst('a').set('class', 'htrack');
          // reload playlist
          that.y.addTracks(that.playlist, '', true);
          console.log('Updated Playlist');
        }
      }
    });
  }
  
});

// Create an instance of the moosique.net Player UI
var mooPlayer = new moosiquePlayer();

