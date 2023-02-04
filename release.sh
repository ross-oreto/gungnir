#!/bin/sh
mvn versions:set -DnewVersion="$1" && mvn --settings settings.xml clean deploy -P release