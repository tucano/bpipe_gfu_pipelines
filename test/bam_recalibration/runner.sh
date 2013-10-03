#!/bin/bash

# OUTPUT
OUTPUT="B1_TTAGGC_L003.merge.dedup.indel_realigned.recalibrated.bam"

# SCRIPT_NAME
SCRIPT_NAME="test_bam_recalibration"

# 1. run cleaner
../../bin/pbs_config.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. run bipe
bpipe run --param pretend=true  ../../pipelines/bam_recalibration/bam_recalibration.groovy *.bam

# 4. check if output exists
if [[ -f $OUTPUT ]]; then
	exit 0
else
	exit 1
fi
