#!/usr/local/bin/bash

#find --version
#which find

if [[ "$1" == "-x" ]] ; then
gfind -name '*.sch.[1-3]' -o -name '*.RMVBL.svrl' | xargs rm -v
else
gfind -name '*.sch.[1-3]' -o -name '*.RMVBL.svrl'
fi
