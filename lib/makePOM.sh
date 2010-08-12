#!/bin/sh

#~ for i in $(ls -1) ; do echo $i ; done

rm -v pom.xml
cp -v pomBegin.xml  pomtmp.xml


for filepath in $(find . -name "*.jar") 
do 

artifactid=$(echo $filepath | sed 's/.*\///') 

#~ mvn deploy:deploy-file -Dfile=$filepath -DrepositoryId=archiva.snapshots -Durl=http://db0.aksw.org:8081/archiva/repository/snapshots -DartifactId=$artifactid  -DgroupId=dllearnerDependency -Dversion=snapshot -Dpackaging=jar

echo "writing to "$PWD"/pomtmp.xml"
echo "<dependency> <groupId>dllearnerDependency</groupId> <artifactId>"$artifactid"</artifactId>  <version>snapshot</version>  </dependency>" >> pomtmp.xml
echo "<dependency> <groupId>dllearnerDependency</groupId> <artifactId>"$artifactid"</artifactId>  <version>snapshot</version>  </dependency>" 
done

cat pomEnd.xml >>  pomtmp.xml

mv pomtmp.xml ../pom.xml


echo "in case the script fails with 401 you need to update credentials in ~/.m2/settings.xml"
echo "YOU MIGHT need to uncomment the line that uploads the jars to the server ;)"
echo "done"
