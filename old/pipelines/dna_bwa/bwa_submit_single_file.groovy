// BPIPE PIPELINE to align with bwa a single file
about title: "DNA alignment with bwa (single file): IOS GFU 009"

// USAGE: bpipe run <pipeline.groovy> *.gz

// ENVIRONMENT
ENVIRONMENT_FILE="gfu_environment.sh"

// for base64 Illumina quality use: [align_bwa_gfu.using(BWAOPT_ALN: "-I")]
// pass options to bwa sampe with string BWAOPT_SE: [sampe_bwa_gfu.using(BWAOPT_SE: "")
// sampe_bwa_gfu options:
// PAIRED = R1 and R2
// COMPRESSED = the fastq files are compressed (fastq.gz)
 Bpipe.run
 {
    "*" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: false)] +
    "_%.fastq" * [align_bwa_gfu.using(BWAOPT_ALN: "")] +
    "_%.sai" * [sampe_bwa_gfu.using(BWAOPT_SE: "", paired: false, lane: false, compressed : true)] +
    merge_bam_gfu + mark_duplicates_gfu + bam_flagstat_gfu
 }