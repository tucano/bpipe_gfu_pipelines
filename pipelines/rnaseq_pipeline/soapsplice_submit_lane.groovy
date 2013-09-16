// BPIPE PIPELINE to compute all alignment in a dir with soapsplice
about title: "RNA alignment pipeline with soapsplice: IOS GFU 009."

// Usage: bpipe run -r soapsplice_submit_lane.groovy *.fastq.gz 

// SOFTWARES PATHS AND OPTIONS
SSPLICE="/lustre1/tools/bin/soapsplice"
SAMTOOLS="/usr/local/cluster/bin/samtools"
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"
PICMERGE="/usr/local/cluster/bin/MergeSamFiles.jar"
VALIDATION_STRINGENCY="SILENT"
CREATE_INDEX=true
MSD=true
ASSUME_SORTED=true

// File Paths
SAMPLESHEET="SampleSheet.csv"
GENOMES_PREFIX="/lustre1/genomes"
SCRATCH_PREFIX="/lustre2/scratch"
ENVIRONMENT_FILE="gfu_environment.sh"

// SPECIFIC SCRIPTS (FIXME now in my home)
GFU_PREPARE_LANE_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/prepare_lane.sh"

prepare_lane_gfu_soapsplice = {
	doc title: "Soapsplice RNA alignment preprocessing",
		desc: "Extract from $SAMPLESHEET info about the experiment, create the LOCAL_SCRATCH directory, prepare the gfu_enviroment.sh file",
		constraints: "search for a $SAMPLESHEET file in current working dir",
		author: "davide.rambaldi@gmail.com"
	exec "$GFU_PREPARE_LANE_SCRIPT $SAMPLESHEET $GENOMES_PREFIX $SCRATCH_PREFIX > $ENVIRONMENT_FILE"
}

clean_scratch_gfu_soapsplice = {
	doc title: "Soapsplice RNA alignment postprocessing cleanup",
		desc: "Clean the scratch directory located in $SCRATCH_PREFIX"
	exec """
		echo "Collecting variables from $ENVIRONMENT_FILE";
		source $ENVIRONMENT_FILE;
		echo "Removing scratch directory ${LOCAL_SCRATCH}";
		rm -rf ${LOCAL_SCRATCH}
	"""
}

align_gfu_soapsplice = {
	exec"""
		printf "input 1 is $input1, input 2 is $input2. Output is $output. X is $X" > $output
	"""
}

Bpipe.run { prepare_lane_gfu_soapsplice + clean_scratch_gfu_soapsplice }
//Bpipe.run { gfu_soapsplice_prepare_lane + "_R*_%.fastq.gz" * [gfu_soapsplice_align] + gfu_soapsplice_clean_scratch }
