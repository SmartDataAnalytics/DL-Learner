#!/bin/sh
MODULE_NAME=interfaces-0.5-SNAPSHOT
echo "Building Debian package for ${MODULE_NAME}"
echo
rm -rf ../../target/dl-learner-interfaces-0.5
mkdir -p ../../target/dl-learner-interfaces-0.5/usr/share/dllearner
mkdir -p ../../target/dl-learner-interfaces-0.5/usr/share/pixmaps
mkdir -p ../../target/dl-learner-interfaces-0.5/usr/share/applications
# Extract the tarball to the package workspace
#tar xfz data.tar.gz --directory ../../target/deb-pkg
# copy war file to package workspace
cp ../../target/interfaces-jar-with-dependencies.jar ../../target/dl-learner-interfaces-0.5/usr/share/dllearner
cp dllearner-gui dllearner-gui.desktop
cp dllearner-cli dllearner-cli.desktop
mv -v dllearner-gui.desktop ../../target/dl-learner-interfaces-0.5/usr/share/applications
mv -v dllearner-cli.desktop ../../target/dl-learner-interfaces-0.5/usr/share/applications
cp ../../target/appassembler/bin/StartCLI ../../target/appassembler/bin/dllearner-CLI.sh
cp ../../target/appassembler/bin/StartGUI ../../target/appassembler/bin/dllearner-GUI.sh
mv ../../target/appassembler/bin/dllearner-CLI.sh ../../target/dl-learner-interfaces-0.5/usr/share/dllearner
mv ../../target/appassembler/bin/dllearner-GUI.sh ../../target/dl-learner-interfaces-0.5/usr/share/dllearner
cp ../../../images/logos/dllearner_small.png ../../target/dl-learner-interfaces-0.5/usr/share/pixmaps
# Add the Debian control files
cd dl-learner-interfaces-0.5/debian
dch -n
cd ..
cp -r debian ../../../target/dl-learner-interfaces-0.5
cd ../../../target/dl-learner-interfaces-0.5/debian
# Build the package and sign it.
cd ../../../target/dl-learner-interfaces-0.5
debuild --check-dirname-level 0

