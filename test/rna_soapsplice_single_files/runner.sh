#!/bin/bash

# OUTPUT
OUTPUT="read1_0000.merge.bam"

# SCRIPT_NAME
SCRIPT_NAME="rna_soapsplice_single_files"

# 1. run cleaner
./cleaner.sh

# 2. generate config
../../bin/pbs_config.sh

# 3. call prepare scripts (example)
../../bin/soapsplice_prepare_single_file.sh -n "test_rna_soapsplice_single_file" -g hg19 /lustre1/genomes /lustre2/scratch /lustre1/tools/bin/soapsplice > gfu_environment.sh

# 4. run bipe
bpipe run ../../pipelines/rna_soapsplice/soapsplice_submit_singlefile.groovy *R*.gz

# 5. convert output to sam and return 0 or >0
/usr/local/cluster/bin/samtools view -h $OUTPUT | diff - expected.sam > /dev/null 2>&1


