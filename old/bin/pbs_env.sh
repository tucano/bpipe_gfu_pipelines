#!/bin/bash

## DESCRIPTION: print some diag on current node

## AUTHOR: davide.rambaldi AT gmail.com

declare -r SCRIPT_NAME=$(basename "$BASH_SOURCE" .sh)

## exit the shell(default status code: 1) after printing the message to stderr
bail() {
    echo -ne "$1" >&2
    exit ${2-1}
} 

## help message
declare -r HELP_MSG="Usage: $SCRIPT_NAME [OPTION]... [ARG]...
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

#[[ "$#" -lt 1 ]] && usage "Too few arguments\n"

#==========MAIN CODE BELOW==========

cat << __EOF__

PBS PRO INFO:

qsub host is $PBS_O_HOST
original queue is $PBS_O_QUEUE
qsub working directory absolute is $PBS_O_WORKDIR
pbs environment is $PBS_ENVIRONMENT
pbs batch id $PBS_JOBID
pbs job name from me is $PBS_JOBNAME
Name of file containing nodes is $PBS_NODEFILE
contents of nodefile is cat $PBS_NODEFILE
Name of queue to which job went is $PBS_QUEUE

__EOF__

exit 0