#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]-$0}" )" && pwd )"
for ROOT in $DIR $DIR/deb
do
export PATH="$DIR:$DIR/bin:$DIR/sbin:$ROOT/usr/bin:$ROOT/usr/sbin:$PATH:$ROOT/usr/games"
export CPATH="$ROOT/include:$ROOT/usr/include:$CPATH"
export LD_LIBRARY_PATH="$ROOT/usr/lib:$ROOT/lib:$ROOT/usr/lib/x86_64-linux-gnu:$ROOT/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH"
export LIBRARY_PATH="$ROOT/usr/lib:$ROOT/lib:$ROOT/usr/lib/x86_64-linux-gnu:$ROOT/lib/x86_64-linux-gnu:$LIBRARY_PATH"
export PKG_CONFIG_PATH="$ROOT/usr/share/pkgconfig:$ROOT/usr/lib/pkgconfig:$ROOT/usr/lib/x86_64-linux-gnu/pkgconfig:$PKG_CONFIG_PATH"
done
