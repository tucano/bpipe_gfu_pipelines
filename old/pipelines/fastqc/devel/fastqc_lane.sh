#!/bin/bash
# simple script to compute alll bwa alignment in a dir.

fastqc=/lustre1/tools/bin/fastqc



# to replace with PBS_WKDIR
cd $PWD

name=`tail -1 SampleSheet.csv | cut -d',' -f3`
project=`tail -1 SampleSheet.csv | cut -d',' -f10`
rundir=`basename ${PWD%%Project_$project/Sample_$name}`

outdir=/lustre1/QC-Illumina/${rundir}/Project_${project}

mkdir -p $outdir

SCRIPT=job.QC_$name
	cat <<__EOF__> $SCRIPT
#PBS -l select=1:ncpus=4:mem=16g:app=java
#PBS -m a

cd $PWD
$fastqc -f fastq --casava --nogroup -t 4  -o ${outdir} *.fastq.gz
__EOF__

qsub $SCRIPT
