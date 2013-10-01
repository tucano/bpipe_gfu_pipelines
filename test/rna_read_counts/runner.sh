#!/bin/bash

# OUTPUT
OUTPUT="cbs1_r6_1395_B_GTGTTA_L007_reads_count.txt"

# SCRIPT_NAME
SCRIPT_NAME="test_rna_read_counts"

# 1. run cleaner
./cleaner.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. run bipe
bpipe run -r ../../pipelines/rna_read_counts/read_counts.groovy cbs1_r6_1395_B_GTGTTA_L007.merge.bam /lustre1/genomes/hg19/annotation/hg19.ensGene_withGeneName.gtf 

# 4. diff
diff $OUTPUT expected.txt > /dev/null 2>&1

RESULT=$?
exit $RESULT
