#!/bin/bash

## DESCRIPTION: Clean the LOCAL_SCRATCH directory from a bpipe working directory
##              It require a gfu_enviroment.sh file as first arg
## AUTHOR: davide.rambaldi@gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]... <gfu_enviroment.sh>
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
source $1

for F in $PWD/*.bam; do
    if [[ -h $F ]]; then
        echo -e "Removing soft link to intermediate file: $F" >&2
        rm $F
    fi
done
echo -e "Cleaning directory: $LOCAL_SCRATCH" >&2
rm -rf $LOCAL_SCRATCH

