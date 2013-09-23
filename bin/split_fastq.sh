#!/bin/bash

## DESCRIPTION: split a single or pair of fastq files in chunks

## AUTHOR: davide.rambaldi@gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

# VARS
n_reads=2000000
pairedEnd=false

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTIONS] -1 <READS_R1.fastq|READS_R1.fastq.gz> [-2 READS_R2.fastq|READS_R2.fastq.gz] <gfu_environment.sh>
  -h    display this help and exit
  -1 file  file of reads R1
  -2 file  file of reads R2 (if any)
  -r reads number of reads per file [$n_reads]
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

while getopts "1:2:r:h" opt; do
    case $opt in
        h)
            usage 0
            ;;
        1)
            FR1=$(cd $(dirname $OPTARG); pwd)/$(basename $OPTARG) 
            ;;
        2)
            FR2=$(cd $(dirname $OPTARG); pwd)/$(basename $OPTARG) 
            pairedEnd=true
            ;;
        r)
            n_reads=$OPTARG
            ;;
        \?)
            usage "Invalid option: -$OPTARG \n"
            ;;
    esac
done

shift $(($OPTIND - 1))
[[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

# in fastq each 4 lines is a read, then
n_lines=$((n_reads * 4))

# source GFU ENV
source $1

WDIR=$PWD

cd $LOCAL_SCRATCH

# check if I can open $FR1 and split with mass rename
if [[ ! -f $FR1 ]]; then 
    echo -e "Can't open reads file: $FR1" >&2
    exit 1;
else
    echo -e "Splitting $FR1 in $n_reads reads ($n_lines lines)" >&2
    if [ $FR1 == ${FR1%%.gz} ]; then
        split -l $n_lines -d -a 4 $FR1 read1_
    else
        zcat $FR1 | split -l $n_lines -d -a 4 - read1_
    fi
    # mass rename
    for file in read1_*; do
        mv "$file" "$file.fastq"
    done
fi

# chceck if I can open $FR2 and split
if [[ $pairedEnd == 'true' ]]; then
    if [[ ! -f $FR2 ]]; then
        echo -e "Can't open reads file: $FR1" >&2
        exit 1;
    else
        echo -e "Splitting $FR2 in $n_reads reads ($n_lines lines)" >&2
        if [ $FR2 == ${FR2%%.gz} ]; then
            split -l $n_lines -d -a 4 $FR2 read2_
        else
            zcat $FR2 | split -l $n_lines -d -a 4 - read2_
        fi
        # mass rename
        for file in read2_*; do
            mv "$file" "$file.fastq"
        done
    fi
fi

cd $WDIR

# link files to WDIR
for i in $LOCAL_SCRATCH/*.fastq; do 
    ln -s $i .
done

exit 0
