#!/bin/bash

# OUTPUT
OUTPUT="cbs1_r6_1395_B_GTGTTA_L007.merge.bam"

# 1. run cleaner
./cleaner.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. call prepare scripts (example)
../../bin/soapsplice_prepare_lane.sh SampleSheet.csv /lustre1/genomes /lustre2/scratch /lustre1/tools/bin/soapsplice > gfu_environment.sh

# 4. run bipe
bpipe run ../../pipelines/rnaseq_pipeline/soapsplice_submit_lane.groovy *.fastq.gz

# 5. convert output to sam and return 0 or >0
/usr/local/cluster/bin/samtools view -h $OUTPUT | diff - expected.sam > /dev/null 2>&1

RESULT=$?

exit $RESULT
