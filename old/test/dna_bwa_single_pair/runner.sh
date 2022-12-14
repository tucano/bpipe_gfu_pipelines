#!/bin/bash

# OUTPUT
OUTPUT="read1_0000.merge.bam"

# SCRIPT_NAME
SCRIPT_NAME="test_dna_bwa_single_pair"

# 1. run cleaner
./cleaner.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. call prepare scripts (example)
../../bin/bwa_prepare_single_files.sh -n "test_dna_bwa_single_pair" /lustre1/genomes /lustre2/scratch > gfu_environment.sh

# 4. run bipe
bpipe run ../../pipelines/dna_bwa/bwa_submit_single_pair.groovy *R*.gz

# 5. check output with verify_bam
../../bin/verify_bam.sh $OUTPUT

RESULT=$?
exit $RESULT
