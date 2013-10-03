#!/bin/bash

# 0. clean
bpipe cleanup -y
../../bin/clean_scratch_dir.sh gfu_environment.sh

# 1. clean output and logs
rm -f read* *.sai *.bam *merge* *dedup* commandlog.txt run.log run.err *.junc

# 2. clean previous run
../../bin/clean_bpipe_run.sh -aced
