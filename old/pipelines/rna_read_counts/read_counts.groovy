// BPIPE PIPELINE to read counts with htseq-count
about title: "RNA-seq reads count with htseq-count and differential expression analysis with DEXSeq: IOS GFU 007."

// USAGE: bpipe run <pipeline.groovy> input.bam annotation.gtf

ENVIRONMENT_FILE="gfu_environment.sh"
SCRATCH_PREFIX="/lustre2/scratch"

/*
 * RUNNER 
 */
Bpipe.run 
{ 
	sort_bam_by_name_gfu + htseq_count_gfu.using(stranded: "no", mode: "union", id_attribute: "gene_name", feature_type: "exon")
}