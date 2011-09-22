#!/bin/sh
MODULE_NAME=components-core-0.5-SNAPSHOT
echo "Building Debian package for ${MODULE_NAME}"
echo
rm -rf ../../target/dl-learner-components-core-0.5
mkdir -p ../../target/dl-learner-components-core-0.5/usr/share/dllearner
# Extract the tarball to the package workspace
#tar xfz data.tar.gz --directory ../../target/deb-pkg
# copy war file to package workspace
cp ../../target/components-core-1.0-SNAPSHOT-jar-with-dependencies.jar ../../target/dl-learner-components-core-0.5/usr/share/dllearner
# Add the Debian control files
cp -r debian ../../target/dl-learner-components-core-0.5
cd ../../target/dl-learner-components-core-0.5/debian
dch -i
# Build the package and sign it.
cd ../../target/dl-learner-components-core-0.5
debuild --check-dirname-level 0
