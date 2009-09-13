// initialize an empty debugger, will be activated if firefox/firebug enabled
var debug = {};
debug.log = function(msg) { 
  return false;
};

/**
 * moosique-Player-Class
 * 
 * 
 * TODO Split out functions and comment it
 */
var Moosique = new Class({ Implements: Options, 
  
  // Some Default Options
  options: {
    messageFadeTime: 5000, // Time until a Status message fades out
    timeToScrobble:  0.5,  // factor for calculating max time a user has to listen to a track until its scrobbled
    minSearchLength: 3     // minimum number of chars for a search value (tag, artist, song)
  },
  
  /**
   * Initializes the Object and sets some default options
   * 
   * @param {Object} options
   */
  initialize: function(options) {
    this.setOptions(options);
    this.initVars();
    this.applyYahooMediaPlayerConfig();
    this.initInterface();
    this.updateRecently();
    this.activateSearch();
    /*
    this.initPlaylist();
    */
  },
  
  
  initVars: function() {
    this.main = document.id('content');
    this.menu = document.id('mainMenu');
    this.playPause = document.id('playPause');
    this.prev = document.id('prev');
    this.next = document.id('next');
    this.stop = document.id('stop');
    this.mute = document.id('mute');
    this.status = document.id('status');
    this.playlist = document.id('playlist');
    this.recently = document.id('recently');
    this.nowPlayingInfo = $$('#playing .info');
    this.nowPlayingTrack = $$('#playing .track');
    this.nowPlayingTime = $$('#playing .time');
    this.searchForm = document.id('searchForm');
    this.searchSubmit = document.id('searchSubmit');
    this.searchValue = document.id('searchValue');
    this.searchType = document.id('searchType');
    this.results = document.id('results');
    this.loading = document.id('loadingImg');
    this.welcome = document.id('welcome');
    this.help = document.id('help');

  },
  
  
  applyYahooMediaPlayerConfig: function() {
    var that = this;

    var playerConfig = function() {
      // display ready-status message
      that.displayStatusMessage('Player ready.');

      /**
       * progress: Change the track position and duration displayed every time, as usual in players
       * 
       * every time the song is played for at least half it's
       * playtime, we assume that the song is liked, so we add it
       * to the list we create the recommendations from
       * (much like last.fm scrobbling) and learn from the heard songs
       * BIG TODO
       */
      var progress = function() {
        that.nowPlayingTime.set('text', 
          that.secondsToMinutesAndSeconds(YAHOO.MediaPlayer.getTrackPosition()) + ' / ' + 
          that.secondsToMinutesAndSeconds(YAHOO.MediaPlayer.getTrackDuration()) );

        if (Math.ceil(YAHOO.MediaPlayer.getTrackPosition()) == 
            Math.ceil(YAHOO.MediaPlayer.getTrackDuration() * that.options.timeToScrobble)) {
          /* This is where the main magic happens

          After havin listened to a song for half its time we save this song to the positive examples list
          and then send a request to the dllearner who then calulates new recommendations.

          first, we update the cookie with the last played song and then
          */
          var lastListenedListItem = YAHOO.MediaPlayer.getMetaData().anchor.getParent().clone();
          var lastListened = lastListenedListItem.getFirst();
          
          // remove the buttons and ymp-classes from the child-link
          lastListenedListItem.getChildren('.moveUp').destroy();
          lastListenedListItem.getChildren('.moveDown').destroy();
          lastListenedListItem.getChildren('.delete').destroy();
          lastListened.erase('class');
          lastListened.addClass('htrack');
          lastListened.getChildren('em').destroy(); // yahoo buttons
          
          // this is the final link item we save in the cookie
          var last = lastListenedListItem.get('html');

          // get the current cookie
          var recentlyListenedCookie = Cookie.read('moosique');
          var recentlyListened = [];

          if (recentlyListenedCookie) { // does the cookie exist?
            recentlyListened = JSON.decode(recentlyListenedCookie).recentlyListened;

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

          // update the cookie
          recentlyListenedObject = { 'recentlyListened' : recentlyListened };
          recentlyListenedCookie = Cookie.write( /* save for one year */
            'moosique', JSON.encode(recentlyListenedObject), { duration: 365 } 
          );

          // update the recently played list
          that.updateRecently();

          // TODO 
          // send ajax request and save the scrobbled song
          // and retrieve and update the recommendations
          var getRecommendations = new Request({
            method: 'get', 
            url: 'moosique/index.php',
            onSuccess: function(responseText, responseXML) {
              mooPlayer.displayStatusMessage('Added this song to your recently listened to songs.');
            }
          }).send(YAHOO.MediaPlayer.getMetaData().title);


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
            e.stop();
            this.getParent().destroy(); // deletes the li-element
            // and refresh the playlist if clicked
            YAHOO.MediaPlayer.addTracks(that.playlist, '', true);
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
              YAHOO.MediaPlayer.addTracks(that.playlist, '', true);
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
              YAHOO.MediaPlayer.addTracks(that.playlist, '', true);
            }
          });
        });

        that.displayStatusMessage('Playlist updated.');
        // TODO save to current-playlist cookie?

      };  

      /**
       * trackPause: we change the Pause-Button to a Play-Button
       * and Update the status on #now
       */
      var trackPause = function() {
        that.nowPlayingInfo.set('text', 'Player paused.');
        that.playPause.setStyle('background-position', '0px 0px');
      };

      /**
       * trackStart: we change the Play-Button to a Pause-Button
       * and Update the status on #now and display whats playing
       */
      var trackStart = function() {
        that.nowPlayingInfo.set('text', 'Currently playing:');
        that.nowPlayingTrack.set('text', YAHOO.MediaPlayer.getMetaData().title);
        that.playPause.setStyle('background-position', '0px -40px');
      };

      /**
       * trackComplete: we change the Pause-Button to a Play-Button
       */
      var trackComplete = function() {
        that.playPause.setStyle('background-position', '0px 0px');
      };

      // add the configuration to the events by subscribing
      YAHOO.MediaPlayer.onProgress.subscribe(progress);
      YAHOO.MediaPlayer.onPlaylistUpdate.subscribe(playlistUpdate);
      YAHOO.MediaPlayer.onTrackPause.subscribe(trackPause);
      YAHOO.MediaPlayer.onTrackStart.subscribe(trackStart);
      YAHOO.MediaPlayer.onTrackComplete.subscribe(trackComplete);
    };

    // Initialize YMP if ready and apply the config
    YAHOO.MediaPlayer.onAPIReady.subscribe(playerConfig);
    
  },
  
  
  
  
  
  updateRecently: function() {
    var that = this;
    
    // Read the Cookie and Update the recently-listened-to list
    if (Cookie.read('moosique')) {
      var recentSongs = JSON.decode(Cookie.read('moosique')).recentlyListened;
      if (recentSongs) {
        var count = recentSongs.length;
        var recentlyHTML = '';
        for (var i = count-1; i >= 0; i-- ) {
          recentlyHTML += '<li>';
          recentlyHTML += recentSongs[i];
          recentlyHTML += '</li>';
        }
        that.recently.set('html', recentlyHTML);
        that.makeAddable(that.recently.getElements('a'));
      }
    }
  },
  
  
 
  /**
   * adding functionality for the player-GUI and the play, next etc. buttons
   */
  addEventsToButtons: function() {
    var that = this;

    // the previous/next-Track Buttons
    that.prev.addEvent('click', function() { YAHOO.MediaPlayer.previous(); });
    that.next.addEvent('click', function() { YAHOO.MediaPlayer.next(); });
      
    // the Play-Pause Button
    that.playPause.addEvent('click', function() {
      // STOPPED: 0, PAUSED: 1, PLAYING: 2,BUFFERING: 5, ENDED: 7
      if (YAHOO.MediaPlayer.getPlayerState() == 0 ||
          YAHOO.MediaPlayer.getPlayerState() == 1 ||
          YAHOO.MediaPlayer.getPlayerState() == 7) {
        YAHOO.MediaPlayer.play();
      } else {
        YAHOO.MediaPlayer.pause();
      }
    });
    
    // the Stop-Playing Button
    that.stop.addEvent('click', function() {
      that.playPause.setStyle('background-position', '0px 0px');
      that.nowPlayingInfo.set('text', 'Player stopped.');
      that.nowPlayingTrack.set('text', '...');
      that.nowPlayingTime.set('text', '0:00 / 0:00');
      YAHOO.MediaPlayer.stop();
      // and reload the playlist
      that.refreshPlaylist();
    });
    
    // Mute-Toggle-Switch
    that.mute.addEvent('click', function() {
      if (YAHOO.MediaPlayer.getVolume() > 0) {
        YAHOO.MediaPlayer.setVolume(0);
        that.mute.setStyle('background-position', '0px -240px');
        that.displayStatusMessage('Player muted.');
      } else {
        YAHOO.MediaPlayer.setVolume(1);
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
    
    that.refreshPlaylist(); 
  },
  
  /**
   * Refreshes the playlist by emptying the current one
   * and reReading the #playlist-container
   */
  refreshPlaylist: function() {
    var that = this;
    YAHOO.MediaPlayer.addTracks(that.playlist, '', true);
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
    that.status.set({
      'text': message,
      'tween': {duration: that.options.messageFadeTime}
    });
    that.status.tween('opacity', [1, 0]);
  },
  
  
  /**
   * initializes interface-functions, clicking buttons and tabs... 
   */
  initInterface: function() {
    var that = this;
    
    that.menu.getElements('a').each(function(tab) {
      tab.addEvent('click', function(e) {
        e.stop(); // dont follow link
        that.showTab(tab.get('class').toString());
      }); 
    });

    // make buttons functional
    this.addEventsToButtons();
    
  },
  

  /**
   * adds events to the search form for retrieving results etc.
   */
  activateSearch: function() {
    var that = this;
    
    that.searchForm.addEvent('submit', function(e) {
      e.stop(); // prevent form submitting the non-ajax way
      this.set('send', {
        
        onRequest: function() {
          that.searchSubmit.set('disabled', 'disabled');
          that.searchSubmit.setStyle('display', 'none');
          // show homescreen for resultdisplaying
          that.showTab('home');

          that.loading.setStyle('display', 'inline');
          that.results.set('html', '<h2>Searching...</h2><p>Please be patient, this may take a minute or two...</p>');
        },
        
        onFailure: function() {
          that.results.set('html', '<h2>Unable to process your search. Try again.</h2>');      
        },
        
        onSuccess: function(response) {
          that.searchSubmit.erase('disabled'); // reenable submitbutton
          that.searchSubmit.setStyle('display', 'inline');
          that.loading.setStyle('display', 'none');

          // if the welcome-text ist present, cut & paste it to help
          if (that.welcome) {
            if (that.welcome.get('html').length > 100) {
              that.help.set('html', that.welcome.get('html'));
              that.welcome.destroy();
            }
          }
          // display results
          that.results.set('html', response);
          // addEvents to result-links
          that.makeAddable($$('a.addToPlaylist'));
        }
      });
      
      // only send form if value is at least 3 chars long
      if (that.searchValue.get('value').length > 2) {
        this.send();
      }  
    });
    
    
    
    
    
    
  },
  
  
  
  
  
  
  
  /**
   * For Recommendations and Search-Results
   * This function searches for all links with the class addToPlaylist
   * and makes them addable to the playlist, which means clicking on
   * them adds them to the playlist and makes them playable. this 
   * is working for links to whole albums and single tracks also
   */
  makeAddable: function (links) {
    var that = this;
    links.each(function(a) {
      a.addEvent('click', function(e) {
        e.stop(); // dont follow link
        
        // remove the class from preventing adding again, if existing
        a.removeClass('addToPlaylist');
        // determine if the link is to an album or a single track
        var href = a.get('href');
        var rel = a.get('rel');

        var type = '';
        if (href.match(/jamendo\.com\/get\/track\/id\/album\//gi)) { type = 'albumPlaylist'; }      
        if (href.match(/jamendo\.com\/get\/track\/id\/track\//gi)) { type = 'trackPlaylist'; }
        if (href.match(/\.mp3/)) { type = 'mp3File'; }

        // if the addable item is a playlist, we have to get the playlistitems
        if (type == 'albumPlaylist' || type == 'trackPlaylist') {
          var getPlaylist = new Request({method: 'get', url: 'moosique/index.php',
            onSuccess: function(response) {
              that.insertIntoPlaylist(response);
            }
          }).send('get=' + type + '&playlist=' + href + '&rel=' + rel);
        }
        if (type == 'mp3File') {
          var itemHTML = '<li>' + a.getParent().get('html') + '</li>';
          that.insertIntoPlaylist(itemHTML);
        }
      });
    });
  },
  
  
  
  
  
  
  
  
  /**
   * appends prepared html code to the playlist, empties the playlist if the first
   * element is an empty li and refreshed the playlist and shows the playlist tab
   */
  insertIntoPlaylist: function(newItems) {
    var that = this;

    // if the first li item of the playlist is empty, kill it
    if (that.playlist.getFirst()) {
      if (that.playlist.getFirst().get('text') == '') {
        that.playlist.empty();
      }  
    }
    // append new html to the playlist
    var oldPlaylist = that.playlist.get('html');
    that.playlist.set('html', oldPlaylist + newItems);
    
    // add the delete, moveUp, moveDown Buttons
    that.playlist.getChildren().each(function(li) {
      var children = li.getChildren();

      // only add the buttons if they are not there yet
      if (children.length == 1) {
        var track = li.getFirst();
        var upButton = new Element('a', { 'href': '#', 'class': 'moveUp', 'title': 'Move up', 'html': '&uarr;' });
        var downButton = new Element('a', { 'href': '#', 'class': 'moveDown', 'title': 'Move down', 'html': '&darr;' });
        var delButton = new Element('a', { 'href': '#', 'class': 'delete', 'title': 'Delete from Playlist', 'html': 'X' });
        
        upButton.inject(track, 'after');
        downButton.inject(upButton, 'after');
        delButton.inject(downButton, 'after');
      }
    });
    
    // refresh the playlist and show the player-tab
    that.refreshPlaylist();
    that.showTab('player');
  },
  
  
  
  
  
  /**
   * 
   * @param {String} tabID ID of the Tab to show
   */
  showTab: function(tabID) {
    var that = this;
    that.menu.getElements('li').removeClass('active');
    that.menu.getElements('a.' + tabID).getParent().toggleClass('active');
    that.main.getChildren().setStyle('display', 'none');
    document.id(tabID).setStyle('display', 'block');  

  },
  
  
  
  /**
   * Converts seconds into a string formatted minutes:seconds
   * with leading zeros for seconds 
   * 
   * @param {Float} seconds
   * @return {String} minsec minutes:seconds
   */
  secondsToMinutesAndSeconds: function(seconds) {
    var min = Math.floor(seconds / 60);
    var sec = Math.floor(seconds % 60);
    if (sec < 10) {
      sec = '0' + sec;
    }
    var minsec = min + ":" + sec;
    return minsec;
  }
  
  
});