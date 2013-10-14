#!/bin/bash

# OUTPUT
OUTPUT="cbs1_r6_1395_B_GTGTTA_L007_reads_count.txt"

# SCRIPT_NAME
SCRIPT_NAME="test_rna_seq"

# 1. run cleaner
./cleaner.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. call prepare scripts (example)
../../bin/soapsplice_prepare_lane.sh SampleSheet.csv /lustre1/genomes /lustre2/scratch /lustre1/tools/bin/soapsplice > gfu_environment.sh

# 4. run bipe
bpipe run ../../pipelines/rna_seq/rna_seq.groovy *.fastq.gz /lustre1/genomes/hg19/annotation/hg19.ensGene_withGeneName.gtf 

# 4. diff
diff $OUTPUT expected.txt > /dev/null 2>&1

RESULT=$?
exit $RESULT
