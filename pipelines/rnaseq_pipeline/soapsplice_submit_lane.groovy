// BPIPE PIPELINE to compute all alignment in a dir with soapsplice
about title: "RNA alignment pipeline with soapsplice: IOS GFU 009"

// general vars
SSPLICE="/lustre1/tools/bin/soapsplice"
SAMTOOLS="/usr/local/cluster/bin/samtools"
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"
PICMERGE="/usr/local/cluster/bin/MergeSamFiles.jar"
VALIDATION_STRINGENCY="SILENT"
CREATE_INDEX=true
MSD=true
ASSUME_SORTED=true
SAMPLESHEET="SampleSheet.csv"

// SPECIFIC SCRIPTS (FIXME now in the same dir of the testing pipeline)
GFU_PREPARE_SAMPLE_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/prepare_lane.sh"


prepare_lane = {
	doc title: "Lane preprocessing",
		desc: "Extract from $SAMPLESHEET info about reference, experiment, etc ...",
		constraints: "search for a $SAMPLESHEET file in current working dir",
		author: "davide.rambaldi@gmail.com"
	exec ""
}


Bpipe.run { prepare_lane }