// Default Player Config for moosique.net
var YMPParams = {
  autoplay: false,
  parse: false, // do not parse initial content
  volume: 1.0,
  displaystate: 3 // hide the YMP (1 shows)
};

// Create an instance of the moosique.net
// 0.025 for debugging purposes TODO
var moo = new Moosique({ timeToScrobble: 0.025 });

// default debug
debug.log(moo);

