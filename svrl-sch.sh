#!/usr/bin/env bash

#find --version
#which find

if [[ "$1" == "-x" ]] ; then
find -name '*RMVBL*' | xargs rm -v
else
find -name '*RMVBL*'
fi
