#!/bin/bash

## DESCRIPTION: generate a bpipe.config file in current directory

## AUTHOR: davide.rambaldi AT gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]
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

# [[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

bpipeconfig_file="bpipe.config"
username=`id -u -n`
queue="workq"
executor="pbspro"

echo -e "Generationg bpipe config file $bpipeconfig_file in $PWD" >&2

cat << EOF > $bpipeconfig_file
executor="$executor"
account="$username"
queue="$queue"
EOF