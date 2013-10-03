// BPIPE PIPELINE to align with bwa a LANE
about title: "DNA alignment with bwa (lane): IOS GFU 009"

// ENVIRONMENT
ENVIRONMENT_FILE="gfu_environment.sh"
SCRATCH_PREFIX="/lustre2/scratch"

// USAGE: bpipe run <pipeline.groovy> *.fastq.gz
// I founded this specific option (-q 30) in bwa_submit_lane.sh by Davide Cittaro
// Compared to the signle pair/file version there is an additional step: mark_duplicates_gfu
// with /usr/local/cluster/bin/MarkDuplicates.jar
// sampe_bwa_gfu options:
// PAIRED = R1 and R2
// COMPRESSED = the fastq files are compressed (fastq.gz)
Bpipe.run
{
    "%.fastq.gz" * [align_bwa_gfu.using(BWAOPT_ALN: "-q 30")] + 
    "_R*_%.sai" * [sampe_bwa_gfu.using(BWAOPT_PE: "", paired: true, lane: true, compressed : true)] +
    merge_bam_gfu + mark_duplicates_gfu + bam_flagstat_gfu
}