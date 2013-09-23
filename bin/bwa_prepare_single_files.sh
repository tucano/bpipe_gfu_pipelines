#!/bin/bash

## DESCRIPTION: bwa prepare single files for alignment

## AUTHOR: davide.rambaldi@gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## common vars
name="run_name"
reference="hg19"
ID="sample_id"
PL="illumina"
PU="sample_fcid"
LB="sample_library"
SM="sample_name"
CN="CTGB"

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]... <reference_genomes_prefix_dir> <scratch_prefix_dir>
  -n name  name of the experiment
  -g ref   reference genome
  -I id    sample id
  -P name  Platform [$PL]
  -U id    Platform unit (flowcell ID)
  -L id    Library
  -S name  Sample name
  -C name  Center [$CN]
  -h       display this help and exit
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

while getopts "hn:g:I:P:U:L:S:C:r:" opt; do
    case $opt in
        h)
            usage 0
            ;;
        n)
			name=$OPTARG
			;;
		g)
			reference=$OPTARG
			;;
		I)
			ID=$OPTARG
			;;
		P)
			PL=$OPTARG
			;;
		U)
			PU=$OPTARG
			;;
		L)
			LB=$OPTARG
			;;
		S)
			SM=$OPTARG
			;;
		C)
			CN=$OPTARG
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

REFERENCE_PREFIX=$1
SCRATCH_PREFIX=$2

REFERENCE_GENOME=$1/$reference/bwa/$reference
LOCAL_SCRATCH=$(/bin/mktemp -d ${SCRATCH_PREFIX}/${name}.XXXXXXXXXXXXX)

# OUTPUT for ENVIRONMENT FILE FROM HERE
#------------------------------------------------------------------------#
cat << EOF
REFERENCE_GENOME=$REFERENCE_GENOME
REFERENCE=$reference
PROJECTNAME=$name
LOCAL_SCRATCH=$LOCAL_SCRATCH
ID=$ID
PL=$PL
PU=$PU
LB=$LB
SM=$SM
CN=$CN
EOF

exit 0;
#------------------------------------------------------------------------#
