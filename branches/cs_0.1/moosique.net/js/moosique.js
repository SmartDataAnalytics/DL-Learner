/**
 * moosique-Player-Class
 *
 * @package moosique.net
 * @author Steffen Becker
 */
var Moosique = new Class({ Implements: Options, 

  /* used for the ymp-bug when refreshing the playlist
  Explanation: If the playlist for the YMP ist reloaded using
  YAHOO.MediaPlayer.addTracks(that.playlist, null, true) the "active"
  track will always be reset to the first on in the playlist.
  Thus we try to remember where the active track was before reloading
  the playlist and use this number as next/previous multiplicator */
  currentPlaylistPosition: 0, 
  
  // set some default options
  options: {
    messageFadeTime: 4000, // Time until a Status message fades out in milliseconds, default: 4000 = 4 seconds
    timeToScrobble:  0.5,  // factor for max time for scrobbling/recommendations, default: 0.5 = half the song
    minSearchLength: 3     // minimum number of chars for a search value (tag, artist, song), default: 3
  },
  
  /**
   * Initializes the Object and sets the default options
   * activating search functionality and the interface for using the player 
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
  },
  
  
  /**
   * This function stores references to DOM-Objects in this class
   * for easier access in the other methods, if sth. in the DOM is
   * changed, just change the references here and everything will work fine.
   */
  initVars: function() {
    // buttons
    this.playPause = document.id('playPause');
    this.prev = document.id('prev');
    this.next = document.id('next');
    this.stop = document.id('stop');
    this.mute = document.id('mute');
    // player info & status display
    this.nowPlayingInfo = $$('#playing .info');
    this.nowPlayingTrack = $$('#playing .track');
    this.nowPlayingTime = $$('#playing .time');
    this.download = $$('#playing .download a');
    this.status = document.id('status');
    // searchForm elements
    this.searchForm = document.id('searchForm');
    this.searchSubmit = document.id('searchSubmit');
    this.searchValue = document.id('searchValue');
    this.searchType = document.id('searchType');
    this.results = document.id('results');
    // playlist and recently
    this.playlist = document.id('playlist');
    this.recently = document.id('recently');
    this.log = document.id('log'); 
    this.resetPlaylist = document.id('resetPlaylist');
    this.resetRecently = document.id('resetRecently');
    // recommendations
    this.recommendations = document.id('recommendations');
    this.generate = document.id('generateRecommendations');
    this.recResults = document.id('recommendationResults');
    this.addRandom = document.id('addRandom');
    this.autoAddCheckbox = document.id('autoAddCheckbox');    
    // other
    this.content = document.id('content');
    this.nav = document.id('nav');
    this.welcome = document.id('welcome');
    this.help = document.id('help');
    this.info = document.id('info');
  },
  
  
  /**
   * Applies Config-Vars to the Yahoo Media Player as described in  http://mediaplayer.yahoo.com/api/ 
   * for the YMP-Events and initializes the Player. Events are:
   * onProgress, onPlaylistUpdate, onTrackPause, onTrackStart, onTrackComplete
   * where onProgress is the most important event-handler, because this is where the learning-process is fired
   */
  applyYahooMediaPlayerConfig: function() {
    var that = this;
    var playerConfig = function() {
      // display ready-status message
      that.displayStatusMessage('Player ready.');
      /**
       * progress: Change the track position and duration displayed every time, as usual in players
       * 
       * every time the song is played for a given percent of it's length (set when initializing 
       * the moosique-Class), we assume that the song is liked (like last.fm-scobling), so we add 
       * it to the recently listened list AND we create the recommendations and learn
       */
      var progress = function() {
        that.nowPlayingTime.set('text', 
          that.secondsToMinutesAndSeconds(YAHOO.MediaPlayer.getTrackPosition()) + ' / ' + 
          that.secondsToMinutesAndSeconds(YAHOO.MediaPlayer.getTrackDuration()) );

        if (Math.ceil(YAHOO.MediaPlayer.getTrackPosition()) == 
            Math.ceil(YAHOO.MediaPlayer.getTrackDuration() * that.options.timeToScrobble)) {
          // first, we update the cookie with the last played song and then
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
          // stripped of all unneccessary links and buttons, but easy to re-add or learn from
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
          recentlyListened.push(last); // add the last played to the array
          // update the cookie
          recentlyListenedObject = { 'recentlyListened' : recentlyListened };
          recentlyListenedCookie = Cookie.write( /* save for one year */
            'moosique', JSON.encode(recentlyListenedObject), { duration: 365 } 
          );
          // update the recently played list
          that.updateRecently();
          that.displayStatusMessage('Added this song to your recently listened to songs.');
          that.generateRecommendations();
        }
      };

      /**
       * playlistUpdate: every time the playlist is updated we add the events for 
       * delete/up/down-buttons to each playlistitem
       */
      var playlistUpdate = function() {
        that.addEventsToPlaylistButtons();
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
       * and fetch/display more information about the artist in the
       * more-info-tab
       */
      var trackStart = function() {
        that.nowPlayingInfo.set('text', 'Currently playing:');
        that.nowPlayingTrack.set('text', YAHOO.MediaPlayer.getMetaData().title);
        that.download.set('href', YAHOO.MediaPlayer.getMetaData().anchor.get('href'));
        that.playPause.setStyle('background-position', '0px -40px'); // sprite offset
        that.toggleCurrentlyPlaying();
        
        // send a request to gather additional artist-information
        var nowPlayingAlbum = YAHOO.MediaPlayer.getMetaData().anchor.get('rel');
        var getInfo = new Request({
          method: 'get', 
          url: 'moosique/index.php',
          onSuccess: function(response) {
            that.info.set('html', response);
            // make the additionals iframe-clickable
            that.addLinkListItemToIframe();
          }
        }).send('info=' + nowPlayingAlbum);
      };
      
      /**
       * trackComplete: we change the Pause-Button to a Play-Button
       * and stop playing, use the special nextTrack() function to get rid
       * of the "restart from the beginning"-YMP-bug and start start playing again
       */
      var trackComplete = function() {
        that.playPause.setStyle('background-position', '0px 0px');
        that.stopPlaying(); 
        that.nextTrack();
        YAHOO.MediaPlayer.play();
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
  
  
  /**
   * Send an ajax-request to generate the recommendations and passes the
   * result to the corresponding html-container
   */
  generateRecommendations: function() {
    var that = this;
    // send ajax request and save the scrobbled song
    // and retrieve and update the recommendations
    var getRecommendations = new Request({
      method: 'get', 
      url: 'moosique/index.php',
      onRequest: function() {
        that.recResults.set('html', '<h2>Generating new recommendations...</h2><p>Please be patient, this may take up to a minute...</p>');
      },
      onFailure: function() {
        that.recResults.set('html', '<h2>Unable to get recommendations. Please reset and try again.</h2>');      
      },
      onSuccess: function(response) {
        response = response.trim();
        if (response != '') {
          that.recResults.set('html', response);
          that.makeAddable($$('a.addToPlaylist'));
          that.showTab('recommendations');
          that.displayStatusMessage('You have new recommendations!');
          
          if (that.autoAddCheckbox.checked) {
            that.addRandomToPlaylist('auto');
          } else {
            debug.log('Autoadding songs from recommendations is disabled.');
          }
        } else {
          debug.log('Response from server empty.');
          that.recResults.set('html', '<h2>There is nothing in your recently list.</h2><p>You have to listen to some music first, before you can get any recommendations.</p>');
        }
      }
    }).send('get=recommendations');
  },
  
  
  /**
   * Updates the recently-Listened-To UL-List Element with the contents
   * from the recentlyListened-Cookie and makes them re-addable to the playlist
   */
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
      } else { // cookie is set, but no Songs in recently
        that.recently.set('html', '<li></li>');
      }
    } else { // no Cookie found, emptying recently
      that.recently.set('html', '<li></li>');
    }
  },
  
  
  /**
   * This function is called everytime a track starts playing
   * and adds the class currentlyPlaying to the parent-li of
   * the currently playing song
   */
  toggleCurrentlyPlaying: function() {
    var that = this;
    that.playlist.getElements('li.currentlyPlaying').removeClass('currentlyPlaying');
    var currentTrack = YAHOO.MediaPlayer.getMetaData().anchor;
    currentTrack.getParent().addClass('currentlyPlaying');
  },
  
  
  /**
   * Set the current active playlist position using the class
   * currentlyPlaying to determine the active item. Saves the
   * position as an int in this.currentPlaylistPosition
   */
  setCurrentPlaylistPosition: function() {
    var that = this;
    var tracks = that.playlist.getChildren();
    var tracksInPlaylist = tracks.length;
    var playing = false;
    // go through all playlistitems and save the position of the currentlyPlaying one
    for (var i = 0; i < tracksInPlaylist; i++ ) {
      if (tracks[i].get('class') == 'currentlyPlaying') {
        that.currentPlaylistPosition = i;
        playing = true;
      }
    }
    // if there was no currentlyPlaying-item set the currentPlaylistPosition to 0
    if (!playing) {
      that.currentPlaylistPosition = 0;
    }
  },
  
  
  /**
   * This function stops the player and displays the default
   * status-message "Player stopped", also refreshes the playlist
   */
  stopPlaying: function() {
    var that = this;
    that.playPause.setStyle('background-position', '0px 0px');
    that.nowPlayingInfo.set('text', 'Player stopped.');
    that.nowPlayingTrack.set('text', '...');
    that.nowPlayingTime.set('text', '0:00 / 0:00');
    that.download.set('href', '#');
    YAHOO.MediaPlayer.stop();
    // and refresh the playlist
    that.refreshPlaylist();
  },
  
  
  /**
   * Skips to the track for x+1 times from the beginning, where x is
   * the current active position in the playlist
   */
  nextTrack: function() {
    var that = this;
    for (var i = 0; i < that.currentPlaylistPosition + 1; i++) {
      YAHOO.MediaPlayer.next();
    }
  },
  
  
  /**
   * Skips to the track for x-1 times from the beginning, where x is
   * the current active position in the playlist
   */
  previousTrack: function() {
    var that = this;
    for (var i = 0; i < that.currentPlaylistPosition - 1; i++) {
      YAHOO.MediaPlayer.next();
    }
  },
  
  
  /**
   * For Recommendations and Search-Results
   * This function searches for all links with the class addToPlaylist
   * and makes them addable to the playlist, which means clicking on
   * them adds them to the playlist and makes them playable. this 
   * is working for links to whole albums and single tracks also
   *
   * @param {Object} links All links to make addable
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
        if (href.match(/jamendo\.com\/get\/track\/id\//gi)) { type = 'playlist'; }      
        if (href.match(/\.mp3/)) { type = 'mp3File'; }
        
        // if the addable item is a playlist, we have to get the playlistitems
        if (type == 'playlist') {
          var getPlaylist = new Request({method: 'get', url: 'moosique/index.php',
            onSuccess: function(response) {
              that.insertIntoPlaylist(response);
              that.addToLog(a, 0, '');
              that.showTab('player');
            }
          }).send('get=' + type + '&playlist=' + href + '&rel=' + rel);
        }
        if (type == 'mp3File') {
          var itemHTML = '<li>' + a.getParent().get('html') + '</li>';
          that.insertIntoPlaylist(itemHTML);
          that.addToLog(a, 0, '');
          that.showTab('player');
        }
      });
    });
  },
  
  
  /**
   * Resets the class for all music-links in the playlist to htrack
   */
  cleanPlaylist: function() {
    var that = this;
    // remove all classes from the links, except for htrack
    that.playlist.getElements('a').each(function(a) {
      if (a.hasClass('moveUp') || a.hasClass('moveDown') || a.hasClass('delete')) {
        // we don't touch the playlist-moving-buttons
      } else { // reset to htrack, nothing else
        a.set('class', 'htrack');
      }
    });
  },
  
  
  /**
   * appends prepared html code to the playlist, empties the playlist if the first
   * element is an empty li and refreshed the playlist and shows the playlist tab
   *
   * @param {String} HTML-Code with new Items to add to the playlist
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
    that.cleanPlaylist();
    
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
  },
  
  
  /**
   * Refreshes the YMP-playlist by emptying the current one
   * and re-adding all items from the the playlist-container
   */
  refreshPlaylist: function() {
    var that = this;
    that.cleanPlaylist();    
    that.setCurrentPlaylistPosition();
    YAHOO.MediaPlayer.addTracks(that.playlist, null, true);
  },
  
  
  /**
   * This function adds a random song from a random record from the
   * recommendations results to the playlist (enqueue at the end)
   *
   * @param {String} type can be set to auto
   */
  addRandomToPlaylist: function(type) {
    var that = this;
    var addableAlbums = that.recResults.getElements('.addToPlaylist');
    // pick a random album
    var randomAlbum = addableAlbums.getRandom();
    // if there is at least one album to add items from
    if (randomAlbum) {
      var href = randomAlbum.get('href');
      var rel = randomAlbum.get('rel');
      
      var getPlaylist = new Request({method: 'get', url: 'moosique/index.php',
        onSuccess: function(response) {
          // yay, we have the playlist, choose a random song and add it
          // therefore save it as Element in temp and extract a random song
          var songs = Elements.from(response);
          var randomSong = songs.getRandom();
          that.insertIntoPlaylist('<li>' + randomSong.get('html') + '</li>'); 
          // add an item to the history-log with additionalInfo
          var extraInfoH3 = randomAlbum.getParent().getParent().getParent().getPrevious();
          var extraInfo = '(From ' + extraInfoH3.get('text') + ')';
          if (type == 'auto') {
            that.addToLog(randomSong.get('html'), 2, extraInfo);
          } else {
            that.addToLog(randomSong.get('html'), 0, extraInfo);
          }
          that.displayStatusMessage('Added a random song to your playlist.');
        }
      }).send('get=playlist&playlist=' + href + '&rel=' + rel);
    } else {
      that.displayStatusMessage('You currently have no recommendations, nothing was added.');
    }
  },
  
  
  /**
   * Adds a history-log entry for given link-elements
   *
   * @param {String} text The Text to add to the history-log
   * @param {int} deleted If set to 1, the text displayed will be "You deleted..." if 2 "The System added"
   */
  addToLog: function(a, mode, extraString) {
    var that = this;
    var item = false;
    if ($type(a) == 'string') { item = Elements.from(a)[0]; } 
    else { item = a.clone(); }
    // if the first item of the log is empty, remove it
    if (that.log.getFirst()) {
      if (that.log.getFirst().get('text') == 'Nothing happened yet.') {
        that.log.empty();
      }  
    }
    // slighty format the link
    item.set('href', item.get('rel'));
    item.removeProperties('rel', 'title', 'class');
    
    if (item.getFirst()) {
      if (item.getFirst().get('tag') == 'img') { 
        var title = item.getFirst().get('alt'); 
        item.set('text', title);
      }
    }
    // a helper parent to convert to html
    var parent = new Element('div');
    item.inject(parent);
    
    var text = 'You added <em>' + parent.get('html') + '</em> to your playlist';
    if (mode == 1) { text = 'You deleted the song <em>' + parent.get('html') + '</em> from your playlist.'; }
    if (mode == 2) { text = 'The system added the song <em>' + parent.get('html') + 'to your playlist.'; }
    
    if (extraString != '') { text = text +  '  ' + extraString; }
    
    var time = new Date().format('%H:%M:%S');    
    var newLogEntry = new Element('li', {
      'class': 'someclass',
      'html': time + " &mdash; " + text
    });
    newLogEntry.inject(that.log, 'top');
  },
  
  
  /**
   * Adds click-Events to the Interface for Tabs and invokes
   * addEventsToPlayerButtons()
   */
  initInterface: function() {
    var that = this;
    // tabbed nav
    that.nav.getElements('a').each(function(tab) {
      tab.addEvent('click', function(e) {
        e.stop(); // dont follow link
        that.showTab(tab.get('class').toString());
      }); 
    });
    // generating recommendations clickable
    that.generate.addEvent('click', function(e) {
      e.stop();
      that.generateRecommendations();
    });
    // enable resetting recently list
    that.resetRecently.addEvent('click', function(e) {
      e.stop();
      Cookie.dispose('moosique');
      that.updateRecently();
    });
    // enable resetting the playlist
    that.resetPlaylist.addEvent('click', function(e) {
      e.stop();
      that.playlist.empty();
      that.stopPlaying();
    });
    // enable the manual add random to playlist
    that.addRandom.addEvent('click', function(e) {
      e.stop();
      that.addRandomToPlaylist('');
    });
    // make player-buttons functional
    this.addEventsToPlayerButtons();
  },
  
  
  /**
   * Adds the events to the playlist buttons for removing or moving
   * them around from/in the playlist
   */
  addEventsToPlaylistButtons: function() {
    var that = this;
    // all buttons
    that.playlist.getElements('.delete, .moveUp, .moveDown').each(function(all) {
      all.removeEvents();
      all.addEvent('click', function(e) {
        e.stop(); // don't folow link
        // if current or the last song from the playlist stop playing
        if (YAHOO.MediaPlayer.getMetaData().anchor.getParent() == this.getParent()) {
          that.stopPlaying();
        } 
        // switch/case different buttons
        var typeOfButton = all.get('class');
        var li = all.getParent();
        switch(typeOfButton) {
          case 'delete' :
            that.addToLog(this.getParent().getFirst(), 1, '');
            li.destroy(); // deletes the li-element
          break;
          case 'moveUp' :
            var before = li.getPrevious();
            if (before) { // it's not the first one
              li.inject(before, 'before');
              that.refreshPlaylist();
            }
          break;
          case 'moveDown' :
            var after = li.getNext();
            if (after) { // it's not the first one
              li.inject(after, 'after');
              that.refreshPlaylist();
            }
          break;
        }
        // always refresh the playlist
        that.refreshPlaylist();
      });
    });
  },
  
  
  /**
   * Adds click-events to all player-related buttons, like play, next etc. buttons
   */
  addEventsToPlayerButtons: function() {
    var that = this;
    
    that.prev.addEvent('click', function(e) { 
      e.stop();
      that.stopPlaying();
      that.previousTrack();
      YAHOO.MediaPlayer.play();
    });
    
    that.next.addEvent('click', function(e) { 
      e.stop();
      that.stopPlaying();
      that.nextTrack();
      YAHOO.MediaPlayer.play();
    });
      
    // the Play-Pause Button
    that.playPause.addEvent('click', function(e) {
      e.stop();
      // STOPPED: 0, PAUSED: 1, PLAYING: 2,BUFFERING: 5, ENDED: 7
      // see http://mediaplayer.yahoo.com/api/
      if (YAHOO.MediaPlayer.getPlayerState() == 0 ||
          YAHOO.MediaPlayer.getPlayerState() == 1 ||
          YAHOO.MediaPlayer.getPlayerState() == 7) {
        YAHOO.MediaPlayer.play();
      } else {
        YAHOO.MediaPlayer.pause();
      }
    });
    
    // the Stop-Playing Button
    that.stop.addEvent('click', function(e) {
      e.stop();
      that.stopPlaying();
    });
    
    // Mute-Toggle-Switch
    that.mute.addEvent('click', function(e) {
      e.stop();
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
   * Make the search-Form an ajax-Search form, displaying the results
   * on the homepage if successful
   */
  activateSearch: function() {
    var that = this;
    var spinner = new Spinner(that.searchSubmit);
        
    that.searchForm.addEvent('submit', function(e) {
      e.stop(); // prevent form submitting the non-ajax way
      this.set('send', {
        
        onRequest: function() {
          spinner.show();
          that.searchSubmit.set('disabled', 'disabled');
          that.showTab('home');
          that.results.set('html', '<h2>Searching...</h2><p>Please be patient, this may take up to a minute...</p>');
        },
        
        onFailure: function() {
          spinner.hide();
          that.results.set('html', '<h2>Unable to process your search. Try again.</h2>');      
        },
        
        onSuccess: function(response) {
          spinner.hide();
          that.searchSubmit.erase('disabled'); // reenable submitbutton
          that.results.set('html', response);
          // addEvents to result-links
          that.makeAddable($$('a.addToPlaylist'));
        }
      });
      
      // only send form if value is at least 3 chars long
      if (that.searchValue.get('value').length >= 3) {
        this.send();
      } else {
        that.displayStatusMessage('Please enter at least 3 chars for searching...');
      }
    });
  },
  
  
  /**
   * Displays a status message, fades out nicely
   * 
   * @param {String} message
   */
  displayStatusMessage: function(message) {
    // Update Status and fade out
    var that = this;
    var fadeFX = new Fx.Tween(that.status, {
      property: 'opacity',
      duration: that.options.messageFadeTime / 5,
      transition: Fx.Transitions.Expo.easeOut,
      link: 'chain'
    });
    
    that.status.set('text', message);    
    fadeFX.start(0, 1).wait(that.options.messageFadeTime).start(1, 0);
  },
  
  
  /**
   * This function makes the external links from more Information behave
   * so, that clicking opens the link in the iframe below. 
   */
  addLinkListItemToIframe: function() {
    var that = this;
    that.info.getElements('.externalLinks a').each(function(a) {
      a.addEvent('click', function(e) {
        e.stop();
        that.info.getElements('.externalLinks li').removeClass('active');
        a.getParent().addClass('active');
        href = a.get('href');
        that.info.getElements('iframe').set('src', href);
      });
    });
  },
  
  
  /**
   * Shows the given tab in the nav, and hides all others
   *
   * @param {String} tabID ID of the Tab to show
   */
  showTab: function(tabID) {
    var that = this;
    that.nav.getElements('li').removeClass('active');
    that.nav.getElements('a.' + tabID).getParent().toggleClass('active');
    that.content.getChildren().setStyle('display', 'none');
    document.id(tabID).setStyle('display', 'block');  
  },
  
  
  /**
   * Converts seconds into a string formatted minutes:seconds
   * with leading zeros for seconds for a nicer display
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