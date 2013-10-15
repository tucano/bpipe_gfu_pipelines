about title: "DNA alignment with bwa (lane): IOS GFU 009"

// Usage line will be used to infer the correct bpipe command
// USAGE: bpipe run -r $pipeline_filename *.fastq.gz

// PROJECT VARS Should be changed by bpipe-config
// I don't wanna templates for a groovy file. Use simple regexp with PLACEHOLDERS
// Don't change my keywords in source!

REFERENCE_GENOME = "/lustre1/genomes/hg19/bwa/hg19"
PLATFORM         = "illumina"
CENTER           = "CTGB"

PROJECTNAME="bpipeconfig_1_test"
REFERENCE="hg19"
EXPERIMENT_NAME="D2A8DACXX_B1"
FCID="D2A8DACXX"
LANE="3"
SAMPLEID="B1"


/*
 * PIPELINE NOTES:
 * Use -q INT to trim quality of reads (-q 30)
 * Use -I for base64 Illumina quality
 */ 
Bpipe.run {
    set_stripe_gfu + "%.fastq.gz" * [align_bwa_gfu.using(BWAOPT_ALN: "-q 30")] + 
	"_R*_%.sai" * [sam_bwa_gfu.using(BWAOPT_PE: "", paired: true, lane: true, compressed : true)]
}
