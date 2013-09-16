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
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION] [EXPERIMENT NAME]
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

# PROJECT NAME PARSING
# Project name qstat specs:
# Project name can contain any characters except for the following: 
# Slash ("/ "), left bracket ("["), right bracket ("]"), double quote ("""), 
# semicolon (";"), colon (":"), vertical bar ("|"), left angle bracket ("<"), 
# right angle bracket (">"), plus ("+"), comma (","), question mark ("?"), and asterisk ("*").

SAMPLE_SHEET=${PWD}/SampleSheet.csv

if [[ "$#" == "0" ]]; then
	if [ -f $SAMPLE_SHEET ]; then
		echo -e "No experiment name provided: using info from $SAMPLE_SHEET" >&2
		PROJECTNAME=`tail -1 $SAMPLE_SHEET | cut -d',' -f3`
	else
		PROJECTNAME=${username}_pipe_${RANDOM}
		echo -e "No experiment name provided and No $SAMPLE_SHEET. Using random generated project name: $PROJECTNAME" >&2
	fi
else
	echo -e "Using provided user defined experiment name: $1" >&2
	PROJECTNAME=$1
fi

# parse project name according to specs
PROJECTNAME=`echo $PROJECTNAME | sed -e 's/[]\/()$*.^|<>;:"+,?[]/_/g'`

echo -e "Generationg bpipe config file $bpipeconfig_file in $PWD" >&2

cat << EOF > ${PWD}/$bpipeconfig_file
executor="$executor"
account="$username"
queue="$queue"
project="$PROJECTNAME"
EOF