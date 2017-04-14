#!/usr/bin/env bash

SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled"
java $SBT_OPTS -jar sbt-launch-0.13.15.jar "$@"
