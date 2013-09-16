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
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTIONS] <WORKING DIRECTORY>
  Remove the trash dir from the bpipe working directory. With option -a remove the .bpipe directory
  -a    remove the .bpipe directory (logs, commands, everything)
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
while getopts ":hpa" opt; do
    case $opt in
        a) 
            ALL=true
            ;;
        p) 
            DRY_RUN=true
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
[[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========
if [[ ! -z $ALL ]]; then
    MYDIR=$1/.bpipe
    echo "Removing bpipe dir $MYDIR" >&2
else
    MYDIR=$1/.bpipe/trash
    echo "Removing bpipe trash dir $MYDIR" >&2
fi


if [[ ! -z $DRY_RUN ]]; then
    echo -e "PRETEND MODE:\n\trm -rf $MYDIR"
else
    rm -rf $MYDIR
fi

exit 0
