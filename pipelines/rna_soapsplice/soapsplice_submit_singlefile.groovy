// BPIPE PIPELINE to align with soapsplice a single file or pair
about title: "RNA single files alignment pipeline with soapsplice: IOS GFU 009."

// USAGE: bpipe run <pipeline.groovy> *.fastq.gz

// ENVIRONMENT
ENVIRONMENT_FILE="gfu_environment.sh"

/*

 * RUNNER 
 */
Bpipe.run 
{
    "%_R*" * [split_fastq_pairs_gfu.using(SPLIT_READS_SIZE: 2000000)] + "read*_%.fastq" * [align_soapsplice_gfu] + merge_bam_gfu + bam_flagstat_gfu
}