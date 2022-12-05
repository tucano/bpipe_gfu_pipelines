about title: "DNA alignment with bwa (lane): IOS GFU 009"

// Usage line will be used to infer the correct bpipe command
// USAGE: bpipe run -r $pipeline_filename *.fastq.gz

// PROJECT VARS will be added by bpipe-config
// I don't wanna templates for a groovy file. Use simple regexp with PLACEHOLDERS
// Don't change my keywords in source pipeline file!

REFERENCE_GENOME = "/lustre1/genomes/BPIPE_REFERENCE_GENOME/bwa/BPIPE_REFERENCE_GENOME"
PLATFORM         = "illumina"
CENTER           = "CTGB"
ENVIRONMENT_FILE = "gfu_environment.sh"

//--BPIPE_ENVIRONMENT_HERE--


/*
 * PIPELINE NOTES:
 * Use -q INT to trim quality of reads (example -q 30)
 * Use -I for base64 Illumina quality
 */ 
Bpipe.run {
    set_stripe_gfu + "_R*_%.fastq.gz" * [align_bwa_gfu.using(BWAOPT_ALN: "-q 30")] +
    "_R*_%.sai" * [sam_bwa_gfu.using(BWAOPT_PE: "", paired: true, compressed : true)] +
    merge_bam_gfu.using(rename: false) + verify_bam_gfu +  mark_duplicates_gfu + bam_flagstat_gfu
}
