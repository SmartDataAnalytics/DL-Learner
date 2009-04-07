window.addEvent('domready', function() {  

// switching between search, recommendation and information 

$$('#mainMenu ul li a').each(function(a) {
  a.addEvent('click', function(e) {
    e.stop(); // dont follow link
    $$('#mainMenu ul li').removeClass('active');
    a.getParent().toggleClass('active');
    console.log(a.get('class'));
    $('content').getChildren().setStyle('display', 'none')
    $(a.get('class')).setStyle('display', 'block');
  }); 
  
});

});