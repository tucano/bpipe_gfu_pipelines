// BPIPE PIPELINE to align with bwa a single pairs (R1-R2)
about title: "DNA alignment with bwa (single pair): IOS GFU 009"

// USAGE: bpipe run <pipeline.groovy> *R*.gz

ENVIRONMENT_FILE = "gfu_environment.sh"

// for base64 Illumina quality use: [align_bwa_gfu.using(BWAOPT_ALN: "-I")]
// pass options to bwa sampe with string BWAOPT_SE: [sampe_bwa_gfu.using(BWAOPT_SE: "")
// sampe_bwa_gfu options:
// PAIRED = R1 and R2
// COMPRESSED = the fastq files are compressed (fastq.gz)
Bpipe.run 
{
	"%_R*" * [split_fastq_gfu.using(SPLIT_READS_SIZE: 2000000, paired: true)] + 
	"%.fastq" * [align_bwa_gfu.using(BWAOPT_ALN: "")] + 
	"read*_%.sai" * [sampe_bwa_gfu.using(BWAOPT_PE: "", paired: true, lane:false, compressed : false)] + 
	merge_bam_gfu + bam_flagstat_gfu
}
