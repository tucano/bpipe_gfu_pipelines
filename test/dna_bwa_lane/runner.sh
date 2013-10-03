#!/bin/bash

# OUTPUT
OUTPUT="B1_TTAGGC_L003.merge.dedup.bam"

# SCRIPT_NAME
SCRIPT_NAME="test_dna_bwa_lane"

# 1. run cleaner
./cleaner.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. call prepare scripts (example)
../../bin/bwa_prepare_lane.sh SampleSheet.csv /lustre1/genomes /lustre2/scratch > gfu_environment.sh

# 4. run bipe
bpipe run ../../pipelines/dna_bwa/bwa_submit_lane.groovy *.fastq.gz

# 5. convert output to sam and return 0 or >0
# There is a problem with this expectation, the order are different ... FIXME (check indeep)
# in the meantime I use flagtat
#/usr/local/cluster/bin/samtools flagstat $OUTPUT | diff - expected_flagstat.log > /dev/null 2>&1
/usr/local/cluster/bin/samtools view -h $OUTPUT | diff - expected.sam > /dev/null 2>&1

RESULT=$?
exit $RESULT
