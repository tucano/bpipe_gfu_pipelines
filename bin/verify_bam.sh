#!/bin/bash

## DESCRIPTION: 

## AUTHOR: davide.rambaldi AT gmail DOT com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]... <BAMFILE>
  -h    display this help and exit
"

## print the usage and exit the shell(default status code: 2)
usage() {
    declare status=2
    if [[ "$1" =~ ^[0-9]+$ ]]; then
        status=$1
        shift
    fi
    bail "${1}$HELP_MSG" $status
}

while getopts ":h" opt; do
    case $opt in
        h)
            usage 0
            ;;
        \?)
            usage "Invalid option: -$OPTARG \n"
            ;;
    esac
done

shift $(($OPTIND - 1))
[[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

BGZF_EOF=1F8B08040000000000FF0600424302001B00030000000000*
bamfile=$1

[[ -s $bamfile ]] || exit 2

size=`stat -c "%s" $bamfile`
seek=$(( $size - 28 ))
BAM_EOF=`hexdump -e  '4/1 "%02X"' -s $seek ${bamfile}`

if [[ $BAM_EOF == $BGZF_EOF ]]
then
    exit 0
else
    exit 1
fi
