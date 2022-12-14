// BPIPE PIPELINE to compute all alignment in a dir with soapsplice
about title: "RNA lane alignment pipeline with soapsplice: IOS GFU 009."

// USAGE: bpipe run <pipeline.groovy> *.fastq.gz

// ENVIRONMENT FILE
ENVIRONMENT_FILE="gfu_environment.sh"
SCRATCH_PREFIX="/lustre2/scratch"

// SUBMIT LANE
Bpipe.run 
{ 
	"_R*_%.fastq.gz" * [align_soapsplice_gfu] + merge_bam_gfu + mark_duplicates_gfu + bam_flagstat_gfu
}
