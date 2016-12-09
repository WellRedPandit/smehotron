#!/usr/local/bin/bash

if [[ "$1" == "-x" ]] ; then
gfind -name '*.sch.[1-3]' -o -name '*.svrl' | xargs rm -v
else
gfind -name '*.sch.[1-3]' -o -name '*.svrl'
fi
