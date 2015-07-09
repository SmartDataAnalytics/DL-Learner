
Note: DBpedia is always subject to change, so solutions will change over time.

When using the SPARQL component, files are written into the directory /cache to
avoid performing the same queries more than once.

Predefined Filters:

all predefined filters remove references to hompage, etc during the extraction
YAGO allows yago, 
SKOS allows skos, 
YAGOSKOS, allows yago and skos, 
YAGOONLY allows yago classes only, no skos , no umbel

***predefined Endpoints
List of values available for 
sparql.predefinedEndpoint("DBPEDIA"); (replace DBPEDIA)
Most of them are not used in any examples, but are available.
DBPEDIA the global DBpedia endpoint http://dbpedia.openlinksw.com:8890/sparql
LOCALJOSECKI a local joseki store, url pointing at "http://localhost:2020/books"
LOCALJOSEKIBIBLE a local joseki store, url pointing at "http://localhost:2020/bible"
GOVTRACK www.govtrack.us | sparql: http://www.rdfabout.com/sparql
MUSICBRAINZ  http://dbtune.org/musicbrainz/sparql
SPARQLETTE
SWCONFERENCE
REVYU
MYOPENLINK
SWCONFERENCE
