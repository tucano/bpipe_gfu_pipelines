#!/bin/bash

## DESCRIPTION: Clean a bpipe working directory

## AUTHOR: davide.rambaldi AT gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTIONS]
  Remove the trash dir and .bpipe dir from the current working directory. 
  -a    remove the .bpipe directory (logs, commands, everything)
  -c    remove also bpipe.config
  -e    remove also gfu_environment.sh
  -d    remove also the doc directory
  -p    test mode (pretend): only show what would be done. 
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

# getopts function
while getopts ":hpacde" opt; do
    case $opt in
        a) 
            ALL=true
            ;;
        p) 
            DRY_RUN=true
            ;;
        c) 
            CONFIG=true
            ;;
        e)
            GFUENV=true
            ;;
        d)
            DOC=true
            ;;
        h)
            usage 0
            ;;
        \?)
            usage "Invalid option: -$OPTARG \n"
            ;;
    esac
done

shift $(($OPTIND - 1))
#[[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========
if [[ ! -z $ALL ]]; then
    MYDIR=${PWD}/.bpipe
    echo "Removing bpipe dir $MYDIR" >&2
else
    MYDIR=${PWD}/.bpipe/trash
    echo "Removing bpipe trash dir $MYDIR" >&2
fi

CONFIG_FILE="${PWD}/bpipe.config"

if [[ ! -z $CONFIG ]]; then
    echo "Removing config files: $CONFIG_FILE" >&2
    MYDIR="$MYDIR $CONFIG_FILE"
fi

GFUENV_FILE="${PWD}/gfu_environment.sh"

if [[ ! -z $GFUENV ]]; then
    echo "Removing config files: $GFUENV_FILE" >&2
    MYDIR="$MYDIR $GFUENV_FILE"
fi


if [[ ! -z $DOC ]]; then
    echo "Removing doc dir ./doc" >&2
    MYDIR="$MYDIR ${PWD}/doc"
fi

if [[ ! -z $DRY_RUN ]]; then
    echo -e "PRETEND MODE:\n\trm -rf $MYDIR"
else
    rm -rf $MYDIR
fi

exit 0
