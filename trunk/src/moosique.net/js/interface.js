window.addEvent('domready', function() {  

// switching between search, recommendation and information 

$$('#mainMenu ul li a').each(function(a) {
  a.addEvent('click', function(e) {
    e.stop(); // dont follow link
    showTab(a.get('class').toString());
  }); 
  
});

$('searchValue').addEvent('keyup', function() {
  if (this.get('value').length > 2) {
    $('searchSubmit').erase('disabled');
  } else {
    $('searchSubmit').set('disabled', 'disabled');
  }
});


});

/**
 * 
 * @param {String} tabID ID of the Tab to show
 */
function showTab(tabID) {
  $$('#mainMenu ul li').removeClass('active');
  $$('#mainMenu ul li a.' + tabID).getParent().toggleClass('active');
  $('content').getChildren().setStyle('display', 'none')
  $(tabID).setStyle('display', 'block');  
  
}


