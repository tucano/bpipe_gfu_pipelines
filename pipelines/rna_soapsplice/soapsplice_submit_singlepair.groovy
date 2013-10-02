// BPIPE PIPELINE to align with soapsplice a single file or pair
about title: "RNA single pair (R1-R2) alignment pipeline with soapsplice: IOS GFU 009."

// USAGE: bpipe run <pipeline.groovy> *.fastq.gz

// ENVIRONMENT
ENVIRONMENT_FILE="gfu_environment.sh"

/*
 * RUNNER 
 */
Bpipe.run 
{
    "%_R*" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: true)] + "read*_%.fastq" * [align_soapsplice_gfu] + merge_bam_gfu + bam_flagstat_gfu
}