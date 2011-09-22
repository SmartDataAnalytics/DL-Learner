#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2001-2006 The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------

#   Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
#   reserved.

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`



# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# If a specific java binary isn't specified search for the standard 'java' binary
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$REPO" ]
then
  REPO="$BASEDIR"/repo
fi

CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/etc:"$REPO"/org/dllearner/components-core/1.0-SNAPSHOT/components-core-1.0-SNAPSHOT-jar-with-dependencies.jar:"$REPO"/com/jamonapi/jamon/2.7/jamon-2.7.jar:"$REPO"/org/aksw/commons/sparql/0.2-SNAPSHOT/sparql-0.2-20110917.162637-14.jar:"$REPO"/org/aksw/commons/util/0.2-SNAPSHOT/util-0.2-20110920.143923-15.jar:"$REPO"/org/aksw/commons/collections/0.2-SNAPSHOT/collections-0.2-20110920.143941-14.jar:"$REPO"/org/aksw/commons/collections-scala/0.2-SNAPSHOT/collections-scala-0.2-20110917.162637-13.jar:"$REPO"/com/hp/hpl/jena/jena/2.6.4/jena-2.6.4.jar:"$REPO"/com/hp/hpl/jena/iri/0.8/iri-0.8.jar:"$REPO"/com/ibm/icu/icu4j/3.4.4/icu4j-3.4.4.jar:"$REPO"/xerces/xercesImpl/2.7.1/xercesImpl-2.7.1.jar:"$REPO"/org/slf4j/slf4j-api/1.6.0/slf4j-api-1.6.0.jar:"$REPO"/log4j/log4j/1.2.16/log4j-1.2.16.jar:"$REPO"/com/hp/hpl/jena/arq/2.8.8/arq-2.8.8.jar:"$REPO"/org/codehaus/woodstox/wstx-asl/3.2.9/wstx-asl-3.2.9.jar:"$REPO"/stax/stax-api/1.0.1/stax-api-1.0.1.jar:"$REPO"/org/apache/lucene/lucene-core/2.9.3/lucene-core-2.9.3.jar:"$REPO"/com/owldl/pellet/2.2.2/pellet-2.2.2.jar:"$REPO"/aterm/aterm-java/1.6/aterm-java-1.6.jar:"$REPO"/xsdlib/xsdlib/20030225/xsdlib-20030225.jar:"$REPO"/relaxngDatatype/relaxngDatatype/20020414/relaxngDatatype-20020414.jar:"$REPO"/org/jgrapht/jgrapht-jdk1.5/0.7.3/jgrapht-jdk1.5-0.7.3.jar:"$REPO"/net/sourceforge/owlapi/owlapi/3.1.0/owlapi-3.1.0.jar:"$REPO"/com/openlink/virtuoso/virtjdbc3/6.1.2/virtjdbc3-6.1.2.jar:"$REPO"/com/openlink/virtuoso/virt_jena/6.1.2/virt_jena-6.1.2.jar:"$REPO"/com/google/guava/guava/r07/guava-r07.jar:"$REPO"/net/sourceforge/collections/collections-generic/4.01/collections-generic-4.01.jar:"$REPO"/com/thoughtworks/xstream/xstream/1.3.1/xstream-1.3.1.jar:"$REPO"/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:"$REPO"/com/hp/hpl/jena/sdb/1.3.4/sdb-1.3.4.jar:"$REPO"/com/hp/hpl/jena/tdb/0.8.9/tdb-0.8.9.jar:"$REPO"/com/hp/hpl/jena/arq-extra/2.7.0/arq-extra-2.7.0.jar:"$REPO"/velocity/velocity/1.5/velocity-1.5.jar:"$REPO"/commons-collections/commons-collections/3.1/commons-collections-3.1.jar:"$REPO"/commons-lang/commons-lang/2.4/commons-lang-2.4.jar:"$REPO"/oro/oro/2.0.8/oro-2.0.8.jar:"$REPO"/com/h2database/h2/1.2.143/h2-1.2.143.jar:"$REPO"/org/apache/solr/solr-core/3.3.0/solr-core-3.3.0.jar:"$REPO"/org/apache/solr/solr-solrj/3.3.0/solr-solrj-3.3.0.jar:"$REPO"/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar:"$REPO"/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar:"$REPO"/commons-codec/commons-codec/1.4/commons-codec-1.4.jar:"$REPO"/commons-io/commons-io/1.4/commons-io-1.4.jar:"$REPO"/org/apache/geronimo/specs/geronimo-stax-api_1.0_spec/1.0.1/geronimo-stax-api_1.0_spec-1.0.1.jar:"$REPO"/org/apache/zookeeper/zookeeper/3.3.1/zookeeper-3.3.1.jar:"$REPO"/jline/jline/0.9.94/jline-0.9.94.jar:"$REPO"/org/apache/solr/solr-noggit/3.3.0/solr-noggit-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-analyzers/3.3.0/lucene-analyzers-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-highlighter/3.3.0/lucene-highlighter-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-memory/3.3.0/lucene-memory-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-queries/3.3.0/lucene-queries-3.3.0.jar:"$REPO"/jakarta-regexp/jakarta-regexp/1.4/jakarta-regexp-1.4.jar:"$REPO"/org/apache/lucene/lucene-misc/3.3.0/lucene-misc-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-spatial/3.3.0/lucene-spatial-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-spellchecker/3.3.0/lucene-spellchecker-3.3.0.jar:"$REPO"/org/apache/lucene/lucene-grouping/3.3.0/lucene-grouping-3.3.0.jar:"$REPO"/org/apache/solr/solr-commons-csv/3.3.0/solr-commons-csv-3.3.0.jar:"$REPO"/commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.jar:"$REPO"/org/apache/velocity/velocity/1.6.4/velocity-1.6.4.jar:"$REPO"/org/apache/velocity/velocity-tools/2.0/velocity-tools-2.0.jar:"$REPO"/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:"$REPO"/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:"$REPO"/commons-chain/commons-chain/1.1/commons-chain-1.1.jar:"$REPO"/commons-validator/commons-validator/1.3.1/commons-validator-1.3.1.jar:"$REPO"/dom4j/dom4j/1.1/dom4j-1.1.jar:"$REPO"/sslext/sslext/1.2-0/sslext-1.2-0.jar:"$REPO"/org/apache/struts/struts-core/1.3.8/struts-core-1.3.8.jar:"$REPO"/antlr/antlr/2.7.2/antlr-2.7.2.jar:"$REPO"/org/apache/struts/struts-taglib/1.3.8/struts-taglib-1.3.8.jar:"$REPO"/org/apache/struts/struts-tiles/1.3.8/struts-tiles-1.3.8.jar:"$REPO"/org/slf4j/slf4j-jdk14/1.6.1/slf4j-jdk14-1.6.1.jar:"$REPO"/edu/stanford/postagger/3.0.2/postagger-3.0.2.jar:"$REPO"/lbj/library/1.0/library-1.0.jar:"$REPO"/lbj/core/1.0/core-1.0.jar:"$REPO"/lbj/ner/1.0/ner-1.0.jar:"$REPO"/jaws/core/1.0/core-1.0.jar:"$REPO"/uk/ac/shef/wit/simmetrics/1.6.2/simmetrics-1.6.2.jar:"$REPO"/woodstox/wstx-api/3.2.0/wstx-api-3.2.0.jar:"$REPO"/org/apache/opennlp/opennlp-tools/1.5.1-incubating/opennlp-tools-1.5.1-incubating.jar:"$REPO"/org/apache/opennlp/opennlp-maxent/3.0.1-incubating/opennlp-maxent-3.0.1-incubating.jar:"$REPO"/jwnl/jwnl/1.3.3/jwnl-1.3.3.jar:"$REPO"/com/aliasi/lingpipe/4.0.1/lingpipe-4.0.1.jar:"$REPO"/org/annolab/tt4j/org.annolab.tt4j/1.0.14/org.annolab.tt4j-1.0.14.jar:"$REPO"/org/ini4j/ini4j/0.5.2/ini4j-0.5.2.jar:"$REPO"/net/didion/jwnl/jwnl/1.4.1.RC2/jwnl-1.4.1.RC2.jar:"$REPO"/org/aksw/commons/model/0.2-SNAPSHOT/model-0.2-20110917.162637-16.jar:"$REPO"/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar:"$REPO"/org/dllearner/interfaces/1.0-SNAPSHOT/interfaces-1.0-SNAPSHOT.jar
EXTRA_JVM_ARGUMENTS=""

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$HOME" ] && HOME=`cygpath --path --windows "$HOME"`
  [ -n "$BASEDIR" ] && BASEDIR=`cygpath --path --windows "$BASEDIR"`
  [ -n "$REPO" ] && REPO=`cygpath --path --windows "$REPO"`
fi

exec "$JAVACMD" $JAVA_OPTS \
  $EXTRA_JVM_ARGUMENTS \
  -classpath "$CLASSPATH" \
  -Dapp.name="CLI-Start" \
  -Dapp.pid="$$" \
  -Dapp.repo="$REPO" \
  -Dbasedir="$BASEDIR" \
  org.dllearner.cli.Start \
  "$@"
