#!/bin/bash

install_jar() {
    file=$1
    groupId=$2
    artifactId=$3
    version=$4
    echo "Installing $file to $groupId:$artifactId:$version"
    mvn install:install-file -Dfile=lib/$file \
        -DgroupId=$groupId \
        -DartifactId=$artifactId \
        -Dversion=$version \
        -Dpackaging=jar
}

# Local/Custom/Old Libs
install_jar "dynaop-1.0-beta.jar" "org.snipsnap.local" "dynaop" "1.0-beta"
install_jar "gabriel.jar" "org.snipsnap.local" "gabriel" "1.0"
install_jar "graph-snipsnap.jar" "org.snipsnap.local" "graph-snipsnap" "1.0"
install_jar "j2h.jar" "org.snipsnap.local" "j2h" "1.0"
install_jar "jdbcstorage-1.0-alpha-1.jar" "org.snipsnap.local" "jdbcstorage" "1.0-alpha-1"
install_jar "jdic.jar" "org.snipsnap.local" "jdic" "1.0"
install_jar "jena.jar" "org.snipsnap.local" "jena" "1.0"
install_jar "jmdns.jar" "org.snipsnap.local" "jmdns" "1.0"
install_jar "jython.jar" "org.snipsnap.local" "jython" "1.0"
install_jar "mckoidb.jar" "com.mckoi" "mckoidb" "1.0.3"
install_jar "nanocontainer-dynaop-1.0-beta-1-SNAPSHOT.jar" "org.snipsnap.local" "nanocontainer-dynaop" "1.0-beta-1-SNAPSHOT"
install_jar "org.apache.jasper.jar" "org.snipsnap.local" "jasper" "1.0"
install_jar "org.mortbay.jetty.jar" "org.snipsnap.local" "jetty" "1.0"
install_jar "radeox.jar" "org.snipsnap.local" "radeox" "1.0"
install_jar "rss-ng-1.0-alpha-1.jar" "org.snipsnap.local" "rss-ng" "1.0-alpha-1"
install_jar "search-ng-1.0-alpha-1.jar" "org.snipsnap.local" "search-ng" "1.0-alpha-1"
install_jar "smack.jar" "org.snipsnap.local" "smack" "1.0"
install_jar "spring-beans.jar" "org.springframework" "spring-beans" "1.2.9-snipsnap"
install_jar "spring-core.jar" "org.springframework" "spring-core" "1.2.9-snipsnap"
install_jar "groovy-1.0-beta-6.jar" "groovy" "groovy" "1.0-beta-6"
install_jar "nanocontainer-1.0.jar" "nanocontainer" "nanocontainer" "1.0"
install_jar "picocontainer-1.0.jar" "picocontainer" "picocontainer" "1.0"
install_jar "xmlrpc-2.0-beta.jar" "xmlrpc" "xmlrpc" "2.0-beta"
install_jar "rome-0.5.jar" "rome" "rome" "0.5"
install_jar "aspectjrt.jar" "org.aspectj" "aspectjrt" "1.2.0-snipsnap"
