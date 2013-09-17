#!/bin/bash

## DESCRIPTION: Prepare a node for soapsplice alignment of a pair R1-R2

## AUTHOR: davide.rambaldi AT gmail DOT com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION] <ENVIRONMENT_FILE> <INPUT_FILE_R1.fastq.gz>
  -h    display this help and exit

  RETURN: position of the header file for this job node

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
[[ "$#" -lt 2 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

# check for mktemp: mktemp is not POSIX but should be present in all *nix distributions. Abort if don't find mktemp
# I use mktemp instead of $RANDOM can lead to race conditions, 
# what is there is already the directory and is owned by otheruser?
command -v /bin/mktemp >/dev/null 2>&1 || { echo >&2 "I require mktemp but it's not installed.  Aborting."; exit 1; }

# soure gfu_environment.sh
source $1

TMP_SCRATCH=$(/bin/mktemp -d /dev/shm/${PROJECTNAME}.XXXXXXXXXXXXX)

LANE=`echo $2 | rev | cut -d'_' -f 3 | rev`
INDEX=`echo $2 | rev| cut -d'_' -f 1 | rev`
INDX=`echo $INDEX | cut -d'.' -f 1`

HEADER_FILE=${TMP_SCRATCH}/${PROJECTNAME}_${RINDEX}_${LANE}_${INDX}.header

awk '{OFS="\t"; print "@SQ","SN:"$1,"LN:"$2}' $REFERENCE_FAIDX > $HEADER_FILE
echo -e "@RG\tID:$PROJECTNAME"_"$LANE\tPL:illumina\tPU:$FCID\tLB:$PROJECTNAME\tSM:$PROJECTNAME\tCN:CTGB" >> $HEADER_FILE
echo -e "@PG\tID:soapsplice\tPN:soapsplice\tVN:$SSVERSION" >> $HEADER_FILE

echo -e "$SCRIPT_NAME: created temporary directory $TMP_SCRATCH on node $HOSTNAME" >&2
echo -e "$SCRIPT_NAME: Project is $PROJECTNAME, Rindex is $RINDEX lane is $LANE, indx is $INDX, Soapsplice version $SSVERSION" >&2
echo -e "$SCRIPT_NAME: Creating headers using $REFERENCE_FAIDX in file $HEADER_FILE" >&2

# PRINT TO STDOUT TMP_SCRATCH
echo "$HEADER_FILE" >&1

exit 0
