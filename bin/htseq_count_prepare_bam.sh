#!/bin/bash

## DESCRIPTION: prepare for htseq-count
## input: final BAM from soapsplice
## NOTE: 

## AUTHOR: davide.rambaldi@gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION] [input.bam] [SCRATCH_PREFIX]
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
[[ "$#" -lt 2 ]] && usage "You must provide bam input file and a SCRATCH_PREFIX\n"
#==========MAIN CODE BELOW==========

command -v /bin/mktemp >/dev/null 2>&1 || { echo >&2 "I require mktemp but it's not installed.  Aborting."; exit 1; }

SAMTOOLS=/usr/local/cluster/bin/samtools

PROJECTNAME=$($SAMTOOLS view -H $1 | grep LB | awk -F "\t" '{print $6}' | sed -e 's/SM://')
SCRATCH_PREFIX=$2
LOCAL_SCRATCH=$(/bin/mktemp -d ${SCRATCH_PREFIX}/${PROJECTNAME}.XXXXXXXXXXXXX)
    
# OUTPUT for ENVIRONMENT FILE FROM HERE
#------------------------------------------------------------------------#
cat << EOF
PROJECTNAME=$PROJECTNAME
LOCAL_SCRATCH=$LOCAL_SCRATCH
EOF
#------------------------------------------------------------------------#