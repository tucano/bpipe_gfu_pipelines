about title: "RNA paired ends alignment with soapsplice: IOS GFU 009."

// Usage line will be used to infer the correct bpipe command
// USAGE: bpipe run -r $pipeline_filename *.fastq.gz

// PROJECT VARS will be added by bpipe-config
// I don't wanna templates for a groovy file. Use simple regexp with PLACEHOLDERS
// Don't change my keywords in source pipeline file!

REFERENCE_GENOME = "/lustre1/genomes/BPIPE_REFERENCE_GENOME/SOAPsplice/BPIPE_REFERENCE_GENOME.index"
REFERENCE_FAIDX  = "/lustre1/genomes/BPIPE_REFERENCE_GENOME/fa/BPIPE_REFERENCE_GENOME.fa.fai"
PLATFORM         = "illumina"
CENTER           = "CTGB"
ENVIRONMENT_FILE = "gfu_environment.sh"

//--BPIPE_ENVIRONMENT_HERE--

/*
 * PIPELINE NOTES:
 * Default soapsplice options: SSPLICEOPT_ALN : "-f 2 -q 1 -j 0"
 * Override sopasplice options with: align_soapsplice_gfu.using(paired: true, SSPLICEOPT_ALN : "<options>")
 */
 Bpipe.run {
    set_stripe_gfu + "_R*_%.fastq.gz" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: true)] +
    "%.fastq" * [soapsplice_prepare_headers_gfu] +
    "read*_%.fastq" * [align_soapsplice_gfu.using(paired: true) ] + verify_bam_gfu + 
    merge_bam_gfu.using(rename: true) + merge_junc_gfu + mark_duplicates_gfu + bam_flagstat_gfu
 }