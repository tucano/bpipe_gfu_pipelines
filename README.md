# BPIPE GFU PIPELINES

Author: davide.rambaldi AT gmail DOT com

### DIRECTORY STRUCTURE

* __modules__: general modules
* __pipelines__: complete pipelines
* __bin__: scripts


### INSTALLATION & CONFIGURATION

* __bpipe.config__ local configuration file for the executor

* __.bpipeconfig__ global configuration file for the executor (place in $HOME)



### USAGE	

__Pipeline file (groovy) must be in the data directory__

#### PIPELINES STAGES

* __prepare_lane_gfu_soapsplice__ : prepare lane for soapsplice alignment
* __clean_scratch_gfu_soapsplice__ : clean scratch dirs after sopasplice alignment
* __align_gfu_soapsplice__ : align with sopasplice


#### SCRIPTS

* __clean_bpipe_run.sh__ : cleanup bpipe trash and dirs
* __pbs_config.sh__ : generate a bpipe.config file dor PBS professional
* __pbs_env.sh__ : get info about the node environment
* __prepare_lane.sh__ : prepare Illumina lane alignment

### TODO

* a "which" script to find the path of all executables?

### DEVEL INFO

#### PBS pro directives: 
* http://portal.ivec.org/docs/Supercomputers/PBS_Pro
* https://www.osc.edu/supercomputing/batch-processing-at-osc/pbs-directives-summary

