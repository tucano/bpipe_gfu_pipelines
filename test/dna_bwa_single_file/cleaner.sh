#!/bin/bash

# 0. clean
bpipe cleanup -y

# 1. clean output and logs
rm -f *merge* commandlog.txt run.log run.err

# 2. clean previous run
../../bin/clean_bpipe_run.sh -aced
