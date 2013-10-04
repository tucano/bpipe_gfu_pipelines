// BPIPE PIPELINE RNA-seq 
about title: "RNA-seq pipeline: IOS GFU 007 and IOS GFU 009."

// ENVIRONMENT FILE
ENVIRONMENT_FILE="gfu_environment.sh"
SCRATCH_PREFIX="/lustre2/scratch"

// LANE
// USAGE: bpipe run <pipeline.groovy> *.fastq.gz <Annotation.gtf>
Bpipe.run 
{ 
    "_R*_%.fastq.gz" * [align_soapsplice_gfu] + 
    merge_bam_gfu + bam_flagstat_gfu + 
    sort_bam_by_name_gfu + htseq_count_gfu
}

/*
// SINGLE PAIR VERSION
// USAGE: bpipe run <pipeline.groovy> *.fastq.gz <Annotation.gtf>
Bpipe.run 
{
    "%_R*" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: true)] + 
    "read*_%.fastq" * [align_soapsplice_gfu] + 
    merge_bam_gfu + bam_flagstat_gfu + 
    sort_bam_by_name_gfu + htseq_count_gfu
}
*/

/*
// SINGLE FILE VERSION
// USAGE: bpipe run <pipeline.groovy> *.fastq.gz <Annotation.gtf>
Bpipe.run 
{
    "*" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: false)] +
    "_%.fastq" * [align_soapsplice_gfu.using(paired: false)] +
    merge_bam_gfu + bam_flagstat_gfu + sort_bam_by_name_gfu + htseq_count_gfu
}
*/