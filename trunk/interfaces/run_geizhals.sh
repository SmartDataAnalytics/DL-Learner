#!/bin/sh
# Loop forever
while :
do
mvn -Djetty.port=8082 jetty:run
done # Start over
