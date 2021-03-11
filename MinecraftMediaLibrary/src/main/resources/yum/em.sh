#!/bin/sh
#
# em
# em.sh
#
# usEr-yuM
#
# Make based yum / rpm wrapper to insatll packages without privileges
#
# It will look for Makefiles in a children directory with the same path as the
# script but suffixed with .d: "{SCRIPT_PATH}.d/", then it will look into it's
# own directory: "{SCRIPT_DIR}/", and finally, in the current working directory:
# "./".
# The Makefile must contain the string "user-yum.sh" within its first ten lines
# to be detected as target Makefile.

SCRIPT_PATH="$(readlink -f "$0")"
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"

for MF in {"$SCRIPT_PATH.d/","$SCRIPT_DIR/","./"}"Makefile"; do
    if [ -f "$MF" ]; then
        if head "$MF" | grep 'user-yum.sh'  2>&1 >/dev/null; then
            cd "$(dirname "$MF")"
            make "$@"
            exit 0
        fi
    fi
done

echo "Couldn't find the Makefile with \"user-yum.sh\" in head." >&2
exit 4
