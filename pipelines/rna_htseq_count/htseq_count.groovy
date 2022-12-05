about title: "RNA-seq reads count with htseq-count: IOS GFU 007."

// Usage line will be used to infer the correct bpipe command
// USAGE: bpipe run -r $pipeline_filename input.bam annotation.gtf

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
 * Options for htseq_count:
 * stranded : "no"
 * mode    : "union"
 * id_attribute : "gene_name"  
 * feature_type : "exon"
 */
Bpipe.run {
    sort_bam_by_name_gfu + htseq_count_gfu.using(
        stranded: "no", 
        mode: "union", 
        id_attribute: "gene_name", 
        feature_type: "exon") +
    sort_and_convert_sam_gfu + verify_bam_gfu + samtools_index_gfu
}