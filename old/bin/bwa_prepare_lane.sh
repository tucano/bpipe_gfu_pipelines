#!/bin/bash

## DESCRIPTION: 
## Prepare a lane for alignment
## 1. Read sample sheet
## 2. Create temporarya global  directories (SCRATCH)
## AUTHOR:  davide DOT rambaldi AT gmail DOT com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]... <SampleSheet.csv> <reference_genomes_prefix_dir> <scratch_prefix_dir>
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

[[ "$#" -lt 3 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

SAMPLE_SHEET=$1
REFERENCE_PREFIX=$2
SCRATCH_PREFIX=$3

# check for mktemp: mktemp is not POSIX but should be present in all *nix distributions. Abort if don't find mktemp
# I use mktemp instead of $RANDOM can lead to race conditions, 
# what is there is already the directory and is owned by otheruser?
command -v /bin/mktemp >/dev/null 2>&1 || { echo >&2 "I require mktemp but it's not installed.  Aborting."; exit 1; }

echo "$SCRIPT_NAME: Current working dir is $PWD" >&2
echo "$SCRIPT_NAME: Illumina Sample Sheet is $SAMPLE_SHEET" >&2
echo "$SCRIPT_NAME: Reference genomes prefix is $REFERENCE_PREFIX" >&2
echo "$SCRIPT_NAME: Scratch prefix is $SCRATCH_PREFIX" >&2

FCID=`tail -1 $1 | cut -d',' -f1 `
REFERENCE=`tail -1 $1 | cut -d',' -f4`
PROJECTNAME=`tail -1 $1 | cut -d',' -f3`
RINDEX=`tail -1 $1 | cut -d',' -f5`
EXPERIMENT_NAME=${FCID}_${PROJECTNAME}

[[ "$RINDEX" == "" ]] && RINDEX=NoIndex

echo -e "$SCRIPT_NAME: collected info from $SAMPLE_SHEET --> fcid=$FCID, reference=$REFERENCE, experiment_name=$EXPERIMENT_NAME, rindex=$RINDEX" >&2

# Global scratch dir for the project/experiment
LOCAL_SCRATCH=$(/bin/mktemp -d ${SCRATCH_PREFIX}/${PROJECTNAME}.XXXXXXXXXXXXX)

echo -e "$SCRIPT_NAME: scratch directory: $LOCAL_SCRATCH" >&2

# check for exit status mktemp
if [[ $? != 0 ]]; then
    echo "$SCRIPT_NAME: could not create scratch directory $LOCAL_SCRATCH" >&2
fi

# lustre filesystem settings for global scratch:
command -v /usr/bin/lfs >/dev/null 2>&1 || { echo >&2 "I require lfs but it's not installed.  Aborting."; exit 1; }
/usr/bin/lfs setstripe -c -1 -i -1 -s 2M $LOCAL_SCRATCH
# check for exit status lsf
if [[ $? != 0 ]]; then
    echo "$SCRIPT_NAME: could not set lustre options with:\nlsf setstip -c -1 -i -1 -s 2M\nin directory $LOCAL_SCRATCH" >&2
fi

# Reference genome prefix
REFERENCE_GENOME=${REFERENCE_PREFIX}/$REFERENCE/bwa/$REFERENCE

# OUTPUT for ENVIRONMENT FILE FROM HERE
# For compatibility with single file version I must add also single file vars
#------------------------------------------------------------------------#
cat << EOF
FCID=$FCID
REFERENCE=$REFERENCE
PROJECTNAME=$PROJECTNAME
RINDEX=$RINDEX
LOCAL_SCRATCH=$LOCAL_SCRATCH
REFERENCE_GENOME=$REFERENCE_GENOME
EXPERIMENT_NAME=$EXPERIMENT_NAME
EOF
#------------------------------------------------------------------------#

exit 0
