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

# 5. check output with verify_bam
../../bin/verify_bam.sh $OUTPUT

RESULT=$?
exit $RESULT
