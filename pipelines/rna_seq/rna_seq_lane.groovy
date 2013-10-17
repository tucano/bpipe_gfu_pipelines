about title: "RNA-seq complete pipeline for lane: IOS GFU 009 + IOS GFU 007."

// Usage line will be used to infer the correct bpipe command
// USAGE: bpipe run -r $pipeline_filename *.fastq.gz annotation.gtf

// PROJECT VARS will be added by bpipe-config
// I don't wanna templates for a groovy file. Use simple regexp with PLACEHOLDERS
// Don't change my keywords in source pipeline file!

REFERENCE_GENOME = "/lustre1/genomes/BPIPE_REFERENCE_GENOME/SOAPsplice/BPIPE_REFERENCE_GENOME.index"
REFERENCE_FAIDX  = "/lustre1/genomes/BPIPE_REFERENCE_GENOME/fa/BPIPE_REFERENCE_GENOME.fa.fai"
PLATFORM         = "illumina"
CENTER           = "CTGB"
ENVIRONMENT_FILE = "gfu_environment.sh"

// Used by rseqc QA
BED12_ANNOTATION = "/lustre1/genomes/BPIPE_REFERENCE_GENOME/annotation/BPIPE_REFERENCE_GENOME.ensGene.bed"

//--BPIPE_ENVIRONMENT_HERE--

/*
 * PIPELINE NOTES:
 * Default soapsplice options: SSPLICEOPT_ALN : "-f 2 -q 1 -j 0"
 * Override sopasplice options with: align_soapsplice_gfu.using(paired: true, SSPLICEOPT_ALN : "<options>")
 */
Bpipe.run {
    set_stripe_gfu + "%.fastq.gz" * [soapsplice_prepare_headers_gfu] + 
    "_R*_%.fastq.gz" * [align_soapsplice_gfu.using(paired: true)] + 
    merge_bam_gfu.using(rename: false) + merge_junc_gfu + verify_bam_gfu + 
    mark_duplicates_gfu + bam_flagstat_gfu + 
    sort_bam_by_name_gfu + htseq_count_gfu.using(
        stranded: "no", 
        mode: "union", 
        id_attribute: "gene_name", 
        feature_type: "exon") +
    sort_and_convert_sam_gfu + verify_bam_gfu + samtools_index_gfu +
    rseqc_bam_stat_gfu + rseqc_gene_coverage_gfu + samtools_idxstats_gfu + 
    rseqc_reads_distribution_gfu + rseqc_read_GC_gfu + 
    rseqc_read_quality_gfu + rseqc_read_NVC_gfu
}