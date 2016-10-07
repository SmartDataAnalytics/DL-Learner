#!/bin/bash
# This script builds the DL-LEarner project and creates the Zip and Tarball file
# which contains the command line interface 'cli' and 'enrichment'.
mvn -N clean install
mvn -DskipTests=true -pl components-core,interfaces,components-ext,interfaces-ext clean install
cd interfaces-ext
mvn -Prelease package -DskipTests=true
cd ../interfaces
mvn -Prelease package -DskipTests=true
