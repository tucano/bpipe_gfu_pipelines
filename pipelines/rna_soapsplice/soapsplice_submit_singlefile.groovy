// BPIPE PIPELINE to align with soapsplice a single file or pair
about title: "RNA single file (R1) alignment pipeline with soapsplice: IOS GFU 009."

// USAGE: bpipe run <pipeline.groovy> *.gz

// ENVIRONMENT
ENVIRONMENT_FILE="gfu_environment.sh"

/*
 * RUNNER 
 */
Bpipe.run 
{
    "*" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: false)] + "_%.fastq" * [align_soapsplice_gfu.using(paired: false)] + merge_bam_gfu + bam_flagstat_gfu
}