#!/bin/bash

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