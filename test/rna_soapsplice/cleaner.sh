#!/bin/bash

# 1. clean output
rm -f *.junc *merge* *.bam

# 2. clean previous run
../../bin/clean_bpipe_run.sh -aced
