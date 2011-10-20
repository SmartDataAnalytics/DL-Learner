#!/bin/sh

curl http://nlp2rdf.lod2.eu/demo/NIFStanfordCore --data-urlencode input@sentences1.txt -d input-type=text -d nif=true  > data1.owl
curl http://nlp2rdf.lod2.eu/demo/NIFStanfordCore --data-urlencode input@sentences2.txt -d input-type=text -d nif=true  > data2.owl
#curl http://localhost:8080/demo/NIFStanfordCore --data-urlencode input@sentences.txt -d input-type=text -d nif=true  > data.owl
#curl http://nlp2rdf.lod2.eu/demo/NIFStanfordCore --data-urlencode input@sentences.txt -d input-type=text -d nif=true -d format=n3 > data.ttl
#curl http://nlp2rdf.lod2.eu/demo/NIFStanfordCore --data-urlencode input@sentences.txt -d input-type=text -d nif=true -d format=ntriples > data.nt
