#!/usr/bin/env bash

#find --version
#which find

if [[ "$1" == "-x" ]] ; then
find -name '*.sch.[1-3]' -o -name '*.RMVBL.svrl' | xargs rm -v
else
find -name '*.sch.[1-3]' -o -name '*.RMVBL.svrl'
fi
