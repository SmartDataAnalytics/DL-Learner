window.addEvent('domready', function() {  

// handle search requests
$('searchForm').addEvent('submit', function(e) {
  
  var results = $('results');
  var submit = $('searchSubmit');
  var loading = $('loadingImg');
  
  e.stop(); // prevent form submitting the non-ajax way
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
      
      // if the welcome-text ist present, cut it to help
      if ($('welcome')) {
        if ($('welcome').get('html').length > 100) {
          $('help').set('html', $('welcome').get('html'));
          $('welcome').destroy();
        }
      }
      
      // display results
      results.set('html', response);
      
      // addEvents to result-links
      makeAddable();
    }
    
  });

  // only send form if value is at least 3
  if ($('searchValue').get('value').length > 2) {
    this.send();
  }  
  
});

makeAddable();

});


/**
 * For Recommendations and Search-Results
 */
function makeAddable() {
  $$('a.addToPlaylist').each(function(a) {
    a.addEvent('click', function(e) {
      e.stop(); // dont follow link
      // remove the class from preventing adding again

      a.removeClass('addToPlaylist');
 
      // TODO, now using xspfs, later we only will use mp3-links 
      a.set('type', 'application/xspf+xml');

      // if the Playlist is empty, remove entries
      if ($('playlist').getFirst()) {
        if ($('playlist').getFirst().get('text') == '') {
          $('playlist').empty();
        }  
      }
      
      
      a.getParent().inject($('playlist'));
      if (a.get('title')) {
        a.set('text', a.get('title'));
      }

      // 
      mooPlayer.refreshPlaylist();     
      showTab('player'); 

    });
  });
}