#!/bin/sh

# $1 the version passed by the maven call

PACKAGE_NAME=dllearner-interfaces
VERSION=$1

echo "Building Debian package for ${MODULE_NAME}"
echo

rm -rf ../../target/deb-pkg
mkdir -p ../../target/deb-pkg/bin

# Extract the tarball to the package workspace
#tar xfz data.tar.gz --directory ../../target/deb-pkg

# copy war file to package workspace
# remove the version in the name
cp ../../target/dl-learner-dist/bin/cli ../../target/deb-pkg/bin/
cp ../../target/dl-learner-dist/bin/enrichment ../../target/deb-pkg/bin/
cp -r ../../target/dl-learner-dist/lib/ ../../target/deb-pkg/
cp -r ../../target/original-interfaces.jar ../../target/deb-pkg/lib/interfaces.jar

# Add the Debian control files
cp -r debian ../../target/deb-pkg

# Build the package
cd ../../target/deb-pkg
debuild --check-dirname-level 0 -b

