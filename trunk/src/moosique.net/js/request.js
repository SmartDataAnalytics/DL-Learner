window.addEvent('domready', function() {  
  // make every addAble link addable to the playlist
  makeAddable();

  // handle search requests
  document.id('searchForm').addEvent('submit', function(e) {
    e.stop(); // prevent form submitting the non-ajax way
    
    
    
    var results = document.id('results');
    var submit = document.id('searchSubmit');
    var loading = document.id('loadingImg');
  

    this.set('send', {
      onRequest: function() {
        submit.set('disabled', 'disabled'); // disable submit button until request complete
        // show homescreen for resultdisplaying
        showTab('home');
        submit.setStyle('display', 'none');
        loading.setStyle('display', 'inline');
        results.set('html', '<h2>Searching...</h2>');
      },
      onFailure: function() {
        results.set('html', '<h2>Unable to process your search. Try again.</h2>');      
      },
      onSuccess: function(response) {
        submit.erase('disabled'); // reenable submitbutton
        submit.setStyle('display', 'inline');
        loading.setStyle('display', 'none');
      
        // if the welcome-text ist present, cut & paste it to help
        if (document.id('welcome')) {
          if (document.id('welcome').get('html').length > 100) {
            document.id('help').set('html', document.id('welcome').get('html'));
            document.id('welcome').destroy();
          }
        }
        // display results
        results.set('html', response);
        // addEvents to result-links
        makeAddable();
      }
    });
    // only send form if value is at least 3 chars long
    if (document.id('searchValue').get('value').length > 2) {
      this.send();
    }  
  });

}); // end domready


/**
 * For Recommendations and Search-Results
 * This function searches for all links with the class addToPlaylist
 * and makes them addable to the playlist, which means clicking on
 * them adds them to the playlist and makes them playable. this 
 * is working for links to whole albums and single tracks also
 */
function makeAddable() {
  $$('a.addToPlaylist').each(function(a) {
    a.addEvent('click', function(e) {
      e.stop(); // dont follow link
      // remove the class from preventing adding again
      a.removeClass('addToPlaylist');
      // determine if the link is to an album or a single track
      var href = a.get('href');
      var rel = a.get('rel');
      
      var type = '';
      if (href.match(/jamendo\.com\/get\/track\/id\/album\//gi)) {
        type = 'albumPlaylist';
      }      
      if (href.match(/jamendo\.com\/get\/track\/id\/track\//gi)) {
        type = 'trackPlaylist';
      }

      var getPlaylist = new Request({
        method: 'get', 
        url: 'moosique/index.php',
        
        onSuccess: function(response) {
          // if the Playlist is empty, remove entries
          if (document.id('playlist').getFirst()) {
            if (document.id('playlist').getFirst().get('text') == '') {
              document.id('playlist').empty();
            }  
          }

          // append new playlist          
          var oldPlaylist = document.id('playlist').get('html');
          document.id('playlist').set('html', oldPlaylist + response);
          
          mooPlayer.refreshPlaylist();     
          showTab('player');
           
        }
      }).send('get=' + type + '&playlist=' + href + '&rel=' + rel);
    });
  });
}