#!/bin/sh

curl http://nlp2rdf.lod2.eu/demo/NIFStanfordCore --data-urlencode input@sentences1.txt -d input-type=text -d nif=true  > /tmp/data.owl
rdfcat -x /tmp/data.owl import.owl > data1.owl

curl http://nlp2rdf.lod2.eu/demo/NIFStanfordCore --data-urlencode input@sentences2.txt -d input-type=text -d nif=true  > /tmp/data.owl
rdfcat -x /tmp/data.owl import.owl > data2.owl
