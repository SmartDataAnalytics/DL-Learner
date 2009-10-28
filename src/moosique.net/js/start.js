// Default Yahoo Media Player Config for moosique.net
var YMPParams = {
  autoplay: false,
  parse: false, // do not parse initial content
  volume: 1.0, // play it loud!
  displaystate: 3 // hide the YMP-default-GUI (1 shows)
};

// Create an instance of the moosique.net
// TODO 0.025 for debugging purposes 
var moo = new Moosique({ timeToScrobble: 0.025 });
