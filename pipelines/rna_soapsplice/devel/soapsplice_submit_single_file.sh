#!/bin/bash
# simple script to compute alll bwa alignment in a dir.

function get_help {
cat << _EOF
Usage:

bwa_submit_single_file.sh [options] -1 file_R1 [-2 file_R2] [-r]
	-n name		name of the experiment
	-1 file		file of read1
	-2 file		file of read2
	-g ref		name of the reference genome
	-I id		Sample ID
	-P name		Platform [illumina]
	-U id		Platform unit (flowcell ID)
	-L id		Library
	-S name		Sample name
	-C name		Center [CTGB]
	-r		run the script
_EOF

exit 1 
}

pairedEnd=false
run=false


name="run_name"
ID="sample_id"
PL="illumina"
PU="sample_fcid"
LB="sample_library"
SM="sample_name"
CN="CTGB"
reference="hg19"

JOBLIST=
BAMFILES=

SSPLICE=/lustre1/tools/bin/soapsplice
SAMTOOLS=/usr/local/cluster/bin/samtools
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"
PICMERGE=/usr/local/cluster/bin/MergeSamFiles.jar
PICOPTS=
VALIDATION_STRINGENCY=SILENT
CREATE_INDEX=true
MSD=true
ASSUME_SORTED=true

[[ ${#*} == 0 ]] && get_help
while getopts "hn:r1:2:g:I:P:U:L:S:C:" opt
do
  case $opt in
   h)
     get_help
     ;;
   n)
     name=$OPTARG
     ;;
   1) 
     FR1=$(cd $(dirname $OPTARG); pwd)/$(basename $OPTARG) 
     ;;
   2) 
     FR2=$(cd $(dirname $OPTARG); pwd)/$(basename $OPTARG) 
     pairedEnd=true
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
   r)
     run=true
     ;;  
   *)
     get_help
     ;;  
  esac
done
shift $(($OPTIND - 1))

#fcid=`tail -1 SampleSheet.csv | cut -d',' -f1 `
#reference=`tail -1 SampleSheet.csv | cut -d',' -f4`
#name=`tail -1 SampleSheet.csv | cut -d',' -f3`
#rindex=`tail -1 SampleSheet.csv | cut -d',' -f5`
#[[ "$rindex" == "" ]] && rindex=NoIndex

ref_genome=/lustre1/genomes/$reference/SOAPsplice/${reference}.index
faidx=/lustre1/genomes/$reference/fa/${reference}.fa.fai
experiment_name=$name

WDIR=`dirname $FR1`
LOCAL_SCRATCH=/lustre2/scratch/${RANDOM}/${experiment_name}

cat << EOF
Read1: $FR1
Read2: $FR2
paired: $pairedEnd
reference: $ref_genome
name: $name
ID: $ID
PL: $PL
PU: $PU
LB: $LB
SM: $SM
CN: $CN
run: $run
working dir: $WDIR
scratch: $LOCAL_SCRATCH

EOF


mkdir -p $LOCAL_SCRATCH
cd $LOCAL_SCRATCH

# split FR1
if [ $FR1 == ${FR1%%.gz} ]
then
  split -l 8000000 -d -a 4 $FR1 read1_
else
  zcat $FR1 | split -l 8000000 -d -a 4 - read1_
fi

if [[ $pairedEnd == 'true' ]]
then
  if [ $FR2 == ${FR2%%.gz} ]
  then
    split -l 8000000 -d -a 4 $FR2 read2_
  else
    zcat $FR2 | split -l 8000000 -d -a 4 - read2_
  fi
fi    


#



for file in read1_*
do
	R1=$file
	R2=${file/read1_/read2_}
	chunk=${file##read1_}
	R_final=$experiment_name"_"$chunk

        job_nameR1="a1_${chunk}"
        job_nameR2="a2_${chunk}"
        job_nameR="s_"${chunk} 
if [[ $pairedEnd == 'true' ]]
then

	SCRIPT=job.final.$R_final
	cat <<__EOF__> $SCRIPT
#PBS -l select=1:ncpus=2:mem=24g:app=java
#PBS -N ${job_nameR:0:15}
#PBS -M cittaro.davide@hsr.it
#PBS -P ${experiment_name}
#PBS -o ${WDIR}/${job_nameR}.log
#PBS -e ${WDIR}/${job_nameR}.err
#PBS -m a

cd $PWD

TMP_SCRATCH=/dev/shm/\${RANDOM}/${experiment_name}
mkdir -p \$TMP_SCRATCH
awk '{OFS="\t"; print "@SQ","SN:"\$1,"LN:"\$2}' $faidx > \${TMP_SCRATCH}/header.$R_final
echo -e "@RG\tID:$ID\tPL:${PL}\tPU:${PU}\tLB:${LB}\tSM:$${SM}\tCN:${CN}" >> \${TMP_SCRATCH}/header.$R_final
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
		id_final="$(qsub $SCRIPT)"
		test "x$id_final" = "x" && { echo >&2 "*** error while submitting $SCRIPT" ; exit 1 ; }
		qstat   $id_final || { echo >&2 "*** couldn't check for job $id_final (final)" ; exit 1 ; }
	fi

	test "x$id_final" != "x" && JOBLIST=$JOBLIST:$id_final
        BAMFILES=$BAMFILES" "I=$R_final".bam"


else # is not paired

	SCRIPT=job.final.$R_final
	cat <<__EOF__> $SCRIPT
#PBS -l select=1:ncpus=2:mem=24g:app=java
#PBS -N ${job_nameR:0:15}
#PBS -M cittaro.davide@hsr.it
#PBS -P ${experiment_name}
#PBS -o ${WDIR}/${job_nameR}.log
#PBS -e ${WDIR}/${job_nameR}.err
#PBS -m a

cd $PWD
TMP_SCRATCH=/dev/shm/\${RANDOM}/${experiment_name}
mkdir -p \$TMP_SCRATCH
awk '{OFS="\t"; print "@SQ","SN:"\$1,"LN:"\$2}' $faidx > \${TMP_SCRATCH}/header.$R_final
echo -e "@RG\tID:$ID\tPL:${PL}\tPU:${PU}\tLB:${LB}\tSM:$${SM}\tCN:${CN}" >> \${TMP_SCRATCH}/header.$R_final
sversion=( \`$SSPLICE | head -n1\` )
echo -e "@PG\tID:soapsplice\tPN:soapsplice\tVN:\${sversion[2]}" >> \${TMP_SCRATCH}/header.$R_final
$SSPLICE -d $ref_genome -1 $R1 -o \${TMP_SCRATCH}/${R_final} -p 4 ${SSPLICEOPT_ALN}

cd \$TMP_SCRATCH
cat \${TMP_SCRATCH}/header.$R_final \$TMP_SCRATCH/${R_final}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $R_final
mv ${R_final}.bam ${LOCAL_SCRATCH}
mv \$TMP_SCRATCH/*.junc ${LOCAL_SCRATCH}
rm \$TMP_SCRATCH/${R_final}.sam 
rm -fr \`dirname \${TMP_SCRATCH}\`
__EOF__

	if $run
	then
		id_final="$(qsub $SCRIPT)"
		test "x$id_final" = "x" && { echo >&2 "*** error while submitting $SCRIPT" ; exit 1 ; }
		qstat   $id_final || { echo >&2 "*** couldn't check for job $id_final (final)" ; exit 1 ; }
	fi

	test "x$id_final" != "x" && JOBLIST=$JOBLIST:$id_final
        BAMFILES=$BAMFILES" "I=$R_final".bam"

fi #end of paired end test
done # end of files
#------------------------------------------------------------------------#

# 


JOBLIST=${JOBLIST#:}
BAMFILES=${BAMFILES#" "}

job_nameC="combin"$experiment_name 
SCRIPT=job.combine.$experiment_name

cat <<__EOF__> $SCRIPT
#PBS -l select=1:ncpus=8:mem=48g
#PBS -W depend=afterok:$JOBLIST
#PBS -N ${job_nameC:0:15}
#PBS -M cittaro.davide@hsr.it
#PBS -P ${experiment_name}
#PBS -o ${WDIR}/${job_nameC}.log
#PBS -e ${WDIR}/${job_nameC}.err
#PBS -m a
#PBS -m e

cd $PWD

java -jar $PICMERGE $BAMFILES \
        O=${experiment_name}.bam \
        CREATE_INDEX=true \
        MSD=true \
        VALIDATION_STRINGENCY=SILENT \
        ASSUME_SORTED=true \
        USE_THREADING=true

/usr/local/cluster/bin/samtools flagstat $experiment_name.bam
cat ${LOCAL_SCRATCH}/*.junc > ${experiment_name}.junc

/bin/mv $experiment_name.bam $experiment_name.bam.lock
/bin/mv $experiment_name.bai $experiment_name.bai.lock

/bin/rm -f *.bam *.bai

/bin/mv $experiment_name.bam.lock ${WDIR}/$experiment_name.bam 
/bin/mv $experiment_name.bai.lock ${WDIR}/$experiment_name.bai

rm -fr $LOCAL_SCRATCH
rm -fr `dirname ${LOCAL_SCRATCH}`
__EOF__

if $run
then
	id_combine="$(qsub $SCRIPT)"
	test "x$id_combine" = "x" && { echo >&2 "*** error while submitting $SCRIPT" ; exit 1 ; }
	qstat  $id_combine || { echo >&2 "*** couldn't check for job $id_combine (combine)" ; exit 1 ; }
fi

#------------------------------------------------------------------------#

exit
