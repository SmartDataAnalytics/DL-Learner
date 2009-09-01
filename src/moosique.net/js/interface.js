window.addEvent('domready', function() {  

// switching between search, recommendation and information 
$$('#mainMenu a').each(function(a) {
  a.addEvent('click', function(e) {
    e.stop(); // dont follow link
    showTab(a.get('class').toString());
  }); 
});

// update recently listened song if a cookie is set
// update the #recently ol
var rlc = Cookie.read('recentlyListened');
var rl = JSON.decode(rlc);

if (rl) {
  if (rl.length > 0) {
    var count = rl.length;
    var recentlyHTML = '';
    for (var i = 0; i < count; i++ ) {
      recentlyHTML += '<li>';
      recentlyHTML += rl[i] + '<br />';
      recentlyHTML += '</li>';
    }
    document.id('recently').set('html', recentlyHTML);
  }
}


});

/**
 * 
 * @param {String} tabID ID of the Tab to show
 */
function showTab(tabID) {
  $$('#mainMenu li').removeClass('active');
  $$('#mainMenu a.' + tabID).getParent().toggleClass('active');
  document.id('content').getChildren().setStyle('display', 'none');
  document.id(tabID).setStyle('display', 'block');  
  
}


