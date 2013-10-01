
# BPIPE GFU PIPELINES

Author: davide.rambaldi AT gmail DOT com

### DIRECTORY STRUCTURE

* __modules__: general modules
* __pipelines__: complete pipelines
* __bin__: scripts
* __test__: test scripts
* __templates__: script templates
* __data__: directory for test/scratch data

### INSTALLATION & CONFIGURATION

* __bpipe.config__ local configuration file for the executor autocreated by _pbs_config.sh_

* __.bpipeconfig__ global configuration file for the executor (place in $HOME)

### CURRENT CRITICAL CHECKS TO DO ON NODES

* check for space in /dev/shm
* check for mounting in the node! Noticed that node b003 lost /illumina/ mount 





### BUGS

* BWA options -I for base64 Illumina quality!!!

### USAGE	

* Configure ulimits to avoid _java.lang.OutOfMemoryError: unable to create new native thread_ adding to .profile
	
	`ulimit -u 4096`

* da fare ...

#### PIPELINES

* __RNA__

* __DNA__

#### SCRIPTS

* __clean_bpipe_run.sh__ : cleanup bpipe trash and dirs
* __pbs_config.sh__ : generate a bpipe.config file dor PBS professional
* __pbs_env.sh__ : get info about the node environment
* __soapsplice_prepare_lane.sh__ : prepare Illumina lane for alignment with soapsplice
* __soapsplice_prepare_align.sh__ : prepare fastq.gz files for alignment with soapsplice on a node
* __verify_bam.sh__ : verify bam file

### TODO

* rewrite bpipe-config in groovy?
* a "which" script to find the path of all executables?
* fastqc script
* bam quality scripts
* send mail to user on job complete: see [NOTIFICATION](https://code.google.com/p/bpipe/wiki/Notifications)
* Sopasplice submit single file

### DEVELOPMENT INFO

#### PBS pro directives: 
* http://portal.ivec.org/docs/Supercomputers/PBS_Pro
* https://www.osc.edu/supercomputing/batch-processing-at-osc/pbs-directives-summary

