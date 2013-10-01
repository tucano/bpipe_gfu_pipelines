
# BPIPE GFU PIPELINES

Author: davide.rambaldi AT gmail DOT com

### Wiki Documentation

* [WIKI HOME](bpipe_gfu_pipelines/wiki/Home)
 * [USER HELP](bpipe_gfu_pipelines/wiki/UserHelp)
 * [DEVELOPERS HELP](bpipe_gfu_pipelines/wiki/DeveloperHelp)
 * [TODO](bpipe_gfu_pipelines/wiki/Todo)

### DIRECTORY STRUCTURE

* __bin__: scripts
* __config__: config files
* __modules__: bpipe modules
* __pipelines__: bpipe pipelines
* __test__: test scripts
* __templates__: script templates
* __data__: directory for test/scratch data

#### SCRIPTS

* __clean_bpipe_run.sh__ : cleanup bpipe trash and dirs
* __clean_scratch_dir__ : clean LOCAL_SCRATCH
* __pbs_config.sh__ : generate a bpipe.config file dor PBS professional
* __pbs_env.sh__ : get info about the node environment
* __soapsplice_prepare_lane.sh__ : prepare Illumina lane for alignment with soapsplice
* __soapsplice_prepare_align.sh__ : prepare fastq.gz files for alignment with soapsplice on a node
* __verify_bam.sh__ : verify bam file
* __split_fastq.sh__ : split a fastq file in chunks
* __bwa_prepare_single_files.sh__ : prepare single files for bwa alignment
* __htseq_count_prepare_bam.sh__ : preapre bam for htseq count

