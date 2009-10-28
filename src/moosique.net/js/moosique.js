/**
 * moosique-Player-Class
 *
 * TODO
 *
 *
 * @package moosique.net
 * @author Steffen Becker
 */
var Moosique = new Class({ Implements: Options, 
  
  // set some default options
  options: {
    messageFadeTime: 5000, // Time until a Status message fades out
    timeToScrobble:  0.5,  // factor for calculating max time a user has to listen to a track until its scrobbled
    minSearchLength: 3     // minimum number of chars for a search value (tag, artist, song)
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
    this.moreInfo = document.id('moreInfo');
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
       * delete/up/down-buttons to each playlistitem and update the status on what happened
       */
      var playlistUpdate = function() {
        // delete button
        that.playlist.getElements('.delete').each(function(del) {
          del.removeEvents();
          del.addEvent('click', function(e) {
            e.stop(); // don't folow link
            // if current or the last song from the playlist stop playing
            if (YAHOO.MediaPlayer.getMetaData().anchor.getParent() == this.getParent()) {
              that.stopPlaying();
            } 
            this.getParent().destroy(); // deletes the li-element
            that.refreshPlaylist();
          });
        });

        // up-button
        that.playlist.getElements('.moveUp').each(function(up) {
          up.removeEvents();
          up.addEvent('click', function(e) {
            e.stop(); 
            if (YAHOO.MediaPlayer.getMetaData().anchor.getParent() == this.getParent()) {
              that.stopPlaying();
            } 
            var li = up.getParent();
            var before = li.getPrevious();
            if (before) { // it's not the first one
              li.inject(before, 'before');
              that.refreshPlaylist();
            }
          });
        });

        // down button
        that.playlist.getElements('.moveDown').each(function(down) {
          down.removeEvents();
          down.addEvent('click', function(e) {
            e.stop();
            if (YAHOO.MediaPlayer.getMetaData().anchor.getParent() == this.getParent()) {
              that.stopPlaying();
            } 
            var li = down.getParent();
            var after = li.getNext();
            if (after) { // it's not the first one
              li.inject(after, 'after');
              that.refreshPlaylist();
            }
          });
        });
        that.displayStatusMessage('Playlist updated.');
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
       * TODO: when a track started playing, fetch additional information about artist etc. using musicbrainz
       */
      var trackStart = function() {
        that.nowPlayingInfo.set('text', 'Currently playing:');
        that.nowPlayingTrack.set('text', YAHOO.MediaPlayer.getMetaData().title);
        that.playPause.setStyle('background-position', '0px -40px'); // sprite offset
        
        // send a request to gather additional artist-information
        var nowPlayingAlbum = YAHOO.MediaPlayer.getMetaData().anchor.get('rel');
        var getInfo = new Request({
          method: 'get', 
          url: 'moosique/index.php',
          onSuccess: function(response) {
            that.moreInfo.set('html', response);
          }
        }).send('info=' + nowPlayingAlbum);
      };

      /**
       * trackComplete: we change the Pause-Button to a Play-Button
       * and execute the autoAdd-Function for adding recommendations
       */
      var trackComplete = function() {
        that.playPause.setStyle('background-position', '0px 0px');
        that.autoAddToPlaylist();
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
            that.addRandomToPlaylist();
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
   * Adds click-events to all player-related buttons, like play, next etc. buttons
   */
  addEventsToButtons: function() {
    var that = this;

    // the previous/next-Track Buttons
    that.prev.addEvent('click', function() { YAHOO.MediaPlayer.previous(); });
    that.next.addEvent('click', function() { YAHOO.MediaPlayer.next(); });
      
    // the Play-Pause Button
    that.playPause.addEvent('click', function() { 
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
    that.stop.addEvent('click', function() {
      that.stopPlaying();
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
   * Refreshes the YMP-playlist by emptying the current one
   * and re-adding all items from the the playlist-container
   */
  refreshPlaylist: function() {
    var that = this;
    YAHOO.MediaPlayer.addTracks(that.playlist, '', true);
    that.displayStatusMessage('Playlist updated.');
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
    YAHOO.MediaPlayer.stop();
    // and reload the playlist
    that.refreshPlaylist();
  },


  /**
   * Displays a status message
   * 
   * @param {String} message
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
   * Adds click-Events to the Interface for Tabs and invokes
   * addEventsToButtons()
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
      that.addRandomToPlaylist();
    });
    // make player-buttons functional
    this.addEventsToButtons();
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
          // if the welcome-text ist present, cut & paste it to help
          if (that.welcome) {
            if (that.welcome.get('html').length > 100) {
              that.help.set('html', that.welcome.get('html'));
              that.welcome.destroy();
            }
          }
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
              that.showTab('player');
            }
          }).send('get=' + type + '&playlist=' + href + '&rel=' + rel);
        }
        if (type == 'mp3File') {
          var itemHTML = '<li>' + a.getParent().get('html') + '</li>';
          that.insertIntoPlaylist(itemHTML);
          that.showTab('player');
        }
      });
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
   * This function adds a random song from a random record from the
   * recommendations results to the playlist (enqueue at the end)
   */
  addRandomToPlaylist: function() {
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
        }
      }).send('get=playlist&playlist=' + href + '&rel=' + rel);
    } else {
      debug.log('You currently have no recommendations, adding a random one will not work.');
    }
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