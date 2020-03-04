#!/bin/sh

apt-get update
apt-get install -y --no-install-recommends curl

mkdir -p /opt/gradle
cd /opt/gradle

GRADLEURL=https://services.gradle.org/distributions/
GRADLEFILE=`curl -s $GRADLEURL | grep -oP "<span\s*class=\"name\">\K[^<]*-bin.zip" | head -1`
echo "Fetching remote file $GRADLEFILE"
wget -O gradle.zip "$GRADLEURL$GRADLEFILE"
unzip gradle.zip
rm gradle.zip
mv gradle* current
