window.addEvent('domready', function() {  

var responseObject = '';

// handle search requests
$('searchForm').addEvent('submit', function(e) {
  
  var info = $('info');
  var submit = $('searchSubmit');
  
  e.stop(); // prevent form submitting the non-ajax way
  this.set('send', {
    
    onRequest: function(response) {
      submit.set('disabled', 'disabled'); // disable submit button until request complete
      info.set('html', '<h2>Processing your search request...</h2>');
    },
    
    onFailure: function(response) {
      info.set('html', '<h2>Unable to process your search. Try again.</h2>');      
    },
     
    onSuccess: function(response) {
      submit.erase('disabled'); // reenable submitbutton
      responseObject = JSON.decode(response);
      // info.set('text', response);
      info.set('html', '<h2>Done.</h2>');
      // Firebug needed
      console.log(responseObject);
  
  
  
      /*
      var newPlaylistItem = new Element('a', {
        'href': responseObject.playlist.trackList.track.location, 
        'html': responseObject.playlist.trackList.track.creator + ' - ' + responseObject.playlist.trackList.track.title
      });
      
      newPlaylistItem.inject(info);
      */
    }
    
    
    
    
  });

  this.send();  
  
  
});



});