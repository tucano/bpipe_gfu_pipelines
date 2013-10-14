#!/bin/bash

## DESCRIPTION: soapsplice prepare single files for alignment

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
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]... <reference_genomes_prefix_dir> <scratch_prefix_dir> <soapsplice path>
  -h       display this help and exit
  -n name  project name
  -g ref   reference genome
  -I id    sample id
  -P name  Platform [$PL]
  -U id    Platform unit (flowcell ID)
  -L id    Library
  -S name  Sample name
  -C name  Center [$CN]
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

while getopts "hn:g:I:P:U:L:S:C:" opt; do
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
[[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

# check for mktemp: mktemp is not POSIX but should be present in all *nix distributions. Abort if don't find mktemp
# I use mktemp instead of $RANDOM can lead to race conditions, 
# what is there is already the directory and is owned by otheruser?
command -v /bin/mktemp >/dev/null 2>&1 || { echo >&2 "I require mktemp but it's not installed.  Aborting."; exit 1; }

REFERENCE_PREFIX=$1
SCRATCH_PREFIX=$2
SSPLICE=$3

echo "$SCRIPT_NAME: Current working dir is $PWD" >&2
echo "$SCRIPT_NAME: Reference genomes prefix is $REFERENCE_PREFIX" >&2
echo "$SCRIPT_NAME: Scratch prefix is $SCRATCH_PREFIX" >&2

REFERENCE_GENOME=$1/$reference/bwa/$reference
LOCAL_SCRATCH=$(/bin/mktemp -d ${SCRATCH_PREFIX}/${name}.XXXXXXXXXXXXX)

echo -e "$SCRIPT_NAME: scratch directory: $LOCAL_SCRATCH" >&2

# check for exit status mktemp
if [[ $? != 0 ]]; then
    echo "$SCRIPT_NAME: could not create scratch directory $LOCAL_SCRATCH" >&2
fi

# lustre filesystem settings for global scratch:
command -v /usr/bin/lfs >/dev/null 2>&1 || { echo >&2 "I require lfs but it's not installed.  Aborting."; exit 1; }
/usr/bin/lfs setstripe -c -1 -i -1 -s 2M $LOCAL_SCRATCH

# Reference genome prefix
REFERENCE_GENOME=${REFERENCE_PREFIX}/$reference/SOAPsplice/${reference}.index
# Reference genome index (samtools faidx)
REFERENCE_FAIDX=${REFERENCE_PREFIX}/$reference/fa/${reference}.fa.fai

SSVERSION=`$SSPLICE | head -n1 | awk '{print \$3}'`;
echo -e "$SCRIPT_NAME: soapsplice version = $SSVERSION" >&2

# OUTPUT for ENVIRONMENT FILE FROM HERE
# FIXME in LANE is FCID, here is PU
#------------------------------------------------------------------------#
cat << EOF
REFERENCE_GENOME=$REFERENCE_GENOME
REFERENCE_FAIDX=$REFERENCE_FAIDX
SSVERSION=$SSVERSION
REFERENCE=$reference
PROJECTNAME=$name
LOCAL_SCRATCH=$LOCAL_SCRATCH
ID=$ID
PL=$PL
PU=$PU
FCID=$PU
LB=$LB
SM=$SM
CN=$CN
EOF

exit 0;
#------------------------------------------------------------------------#
