#!/bin/bash
# simple script to compute alll bwa alignment in a dir.
run=false
test "x$1" = "x--run" && run=true

# from original makefile 

SSPLICE=/lustre1/tools/bin/soapsplice
SAMTOOLS=/usr/local/cluster/bin/samtools
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"
PICMERGE=/usr/local/cluster/bin/MergeSamFiles.jar
PICOPTS=
VALIDATION_STRINGENCY=SILENT
CREATE_INDEX=true
MSD=true
ASSUME_SORTED=true

#
# to easily change where to write temporary files scratch: 
#  default: present directory..


# to replace with PBS_WKDIR
cd $PWD

fcid=`tail -1 SampleSheet.csv | cut -d',' -f1 `
reference=`tail -1 SampleSheet.csv | cut -d',' -f4`
name=`tail -1 SampleSheet.csv | cut -d',' -f3`
rindex=`tail -1 SampleSheet.csv | cut -d',' -f5`
ref_genome=/lustre1/genomes/$reference/SOAPsplice/${reference}.index
faidx=/lustre1/genomes/$reference/fa/${reference}.fa.fai

echo "$fcid"
echo "$reference"

JOBLIST=
BAMFILES=

experiment_name=$name

LOCAL_SCRATCH=/lustre2/scratch/${RANDOM}/${experiment_name}
mkdir -p $LOCAL_SCRATCH

#set lfs 

lfs setstripe -c -1 -i -1 -s 2M ${LOCAL_SCRATCH}

echo $experiment_name
#------------------------------------------------------------------------#

for file in *$experiment_name*R1*.fastq.gz
do
	# foreach combination of file write and submit the job

	# identify rindex lane and index

	echo $file
	lane=`echo $file | rev | cut -d'_' -f 3 | rev`
	index=`echo $file  | rev| cut -d'_' -f 1 | rev`
	indx=` echo $index| cut -d'.' -f 1 `
#	echo $lane, $index ,$indx

	# this is the name:

	R1=$experiment_name"_"$rindex"_"$lane"_R1_"$indx".fastq.gz"
	R2=$experiment_name"_"$rindex"_"$lane"_R2_"$indx".fastq.gz"
	R2_final=$experiment_name"_"$rindex"_"$lane"_R2_"$indx
	R1_final=$experiment_name"_"$rindex"_"$lane"_R1_"$indx
	R_final=$experiment_name"_"$rindex"_"$lane"_"$indx

#        job_nameR1="gz_"${R1_final:(-11)} 
#        job_nameR2="gz_"${R2_final:(-11)} 
#        job_nameR="sampe_"${R_final:(-8)} 
        job_nameR="a_"${R_final} 
	#------------------------------------------------------------------------#

	# write the first job script
	SCRIPT=job.align.$R_final
	cat <<__EOF__> $SCRIPT
#PBS -l select=1:ncpus=4:mem=32g
#PBS -N a_${R_final:0:13}
#PBS -M cittaro.davide@hsr.it
#PBS -P ${experiment_name}
#PBS -m a

cd $PWD

TMP_SCRATCH=/dev/shm/\${RANDOM}/${experiment_name}
mkdir -p \$TMP_SCRATCH

#create header
awk '{OFS="\t"; print "@SQ","SN:"\$1,"LN:"\$2}' $faidx > \${TMP_SCRATCH}/header.$R_final
echo -e "@RG\tID:$experiment_name"_"$lane\tPL:illumina\tPU:$fcid\tLB:$experiment_name\tSM:$experiment_name\tCN:CTGB" >> \${TMP_SCRATCH}/header.$R_final
sversion=( \`$SSPLICE | head -n1\` )
echo -e "@PG\tID:soapsplice\tPN:soapsplice\tVN:\${sversion[2]}" >> \${TMP_SCRATCH}/header.$R_final

$SSPLICE -d $ref_genome -1 $R1 -2 $R2 -o \${TMP_SCRATCH}/${R_final} -p 4 ${SSPLICEOPT_ALN}

cd \$TMP_SCRATCH
cat \${TMP_SCRATCH}/header.$R_final \$TMP_SCRATCH/${R_final}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $R_final
mv ${R_final}.bam ${LOCAL_SCRATCH}
mv \$TMP_SCRATCH/*.junc ${LOCAL_SCRATCH}
rm -fr \`dirname \${TMP_SCRATCH}\`

__EOF__

	if $run
	then
		job_final="$(qsub ${SCRIPT})"
		test "x$job_final" = "x" && { echo >&2 "*** error while submitting job $SCRIPT" ; exit 1 ; }
		qstat  $job_final || { echo >&2 "*** couldn't check for job $job_final (R1)" ; exit 1 ; }
	fi

	# create the third job: runs only if previous two successful.

	test "x$job_final" != "x" && JOBLIST=$JOBLIST:$job_final
        BAMFILES=$BAMFILES" "I=${LOCAL_SCRATCH}/$R_final".bam"

done

#------------------------------------------------------------------------#

# 


JOBLIST=${JOBLIST#:}
BAMFILES=${BAMFILES#" "}

job_nameC="combin"$experiment_name 
SCRIPT=job.combine.$R_final

cat <<__EOF__> $SCRIPT
#PBS -l select=1:ncpus=8:mem=48g
#PBS -W depend=afterok:$JOBLIST
#PBS -N ${job_nameC:0:15}
#PBS -M cittaro.davide@hsr.it
#PBS -P ${experiment_name}
#PBS -m a
#PBS -m e

cd $PWD

/usr/bin/time  java -jar $PICMERGE $BAMFILES \
        O=$experiment_name.bam \
        CREATE_INDEX=true \
        MSD=true \
        VALIDATION_STRINGENCY=SILENT \
        ASSUME_SORTED=true \
        USE_THREADING=true


# combine junction usage
cat ${LOCAL_SCRATCH}/*.junc > ${experiment_name}.junc

/usr/local/cluster/bin/samtools flagstat $experiment_name.bam
/bin/rm -f ${LOCAL_SCRATCH}/${R_final}.sam
/bin/rm -f ${LOCAL_SCRATCH}/header.$R_final 
/bin/rm -f ${LOCAL_SCRATCH}/*.junc

/bin/mv $experiment_name.bam $experiment_name.bam.lock
/bin/mv $experiment_name.bai $experiment_name.bai.lock

/bin/rm -f *.bam *.bai

/bin/mv $experiment_name.bam.lock $experiment_name.bam 
/bin/mv $experiment_name.bai.lock $experiment_name.bai

rm -fr $LOCAL_SCRATCH
__EOF__

if $run
then
	id_combine="$(qsub $SCRIPT)"
	test "x$id_combine" = "x" && { echo >&2 "*** error while submitting $SCRIPT" ; exit 1 ; }
	qstat  $id_combine || { echo >&2 "*** couldn't check for job $id_combine (combine)" ; exit 1 ; }
fi

#------------------------------------------------------------------------#

exit
