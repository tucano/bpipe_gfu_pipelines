#!/bin/bash

# 0. clean
bpipe cleanup -y

# 1. clean output and logs
rm -f *.recalibrated.bam

# 2. clean previous run
../../bin/clean_bpipe_run.sh -aced
