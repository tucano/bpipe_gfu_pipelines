// BPIPE PIPELINE to compute all alignment in a dir with soapsplice
about title: "RNA alignment pipeline with soapsplice: IOS GFU 009."

// Usage: bpipe run -r soapsplice_submit_lane.groovy data/*.fastq.gz 

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

// STAGES SCRIPTS
GFU_PREPARE_LANE_SCRIPT  = "/home/drambaldi/bpipe_gfu_pipelines/bin/soapsplice_prepare_lane.sh"
GFU_PREPARE_ALIGN_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/soapsplice_prepare_align.sh"
GFU_VERIFY_BAM           = "/home/drambaldi/bpipe_gfu_pipelines/bin/verify_bam.sh"

@intermediate
prepare_lane_gfu_soapsplice = {
	doc title: "Soapsplice RNA alignment preprocessing",
		desc: "Extract from $SAMPLESHEET info about the experiment, create the LOCAL_SCRATCH directory, prepare the gfu_enviroment.sh file",
		constraints: "search for a $SAMPLESHEET file in current working dir",
		author: "davide.rambaldi@gmail.com"
	produce("$ENVIRONMENT_FILE") {
		exec "$GFU_PREPARE_LANE_SCRIPT $SAMPLESHEET $GENOMES_PREFIX $SCRATCH_PREFIX $SSPLICE > $ENVIRONMENT_FILE"
	}
}


clean_scratch_gfu_soapsplice = {
	doc title: "Soapsplice RNA alignment postprocessing cleanup",
		desc: "Clean the scratch directory located in $SCRATCH_PREFIX",
		author: "davide.rambaldi@gmail.com"
	exec """
		echo "[clean_scratch_gfu_soapsplice]: Collecting variables from $ENVIRONMENT_FILE" >&2;
		source $ENVIRONMENT_FILE;
		echo "[clean_scratch_gfu_soapsplice]: Removing scratch directory ${LOCAL_SCRATCH}" >&2;
		rm -rf \${LOCAL_SCRATCH}
	"""
}

@Transform("bam")
align_gfu_soapsplice = {
	doc title: "Soapsplice alignment task",
		desc: "Align with soapsplice. Generate temporary files in /dev/shm on the node",
		author: "davide.rambaldi@gmail.com"
	from("fastq.gz","fastq.gz") produce(input.prefix - ".fastq" +".bam") {
		exec"""
			HEADER_FILE=`$GFU_PREPARE_ALIGN_SCRIPT $ENVIRONMENT_FILE $input1`;
			TMP_SCRATCH=`dirname $HEADER_FILE`
			TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;

			source $ENVIRONMENT_FILE;
			
			echo -e "[align_gfu_soapsplice]: align on node $HOSTNAME with command -->\\n\\t$SSPLICE -d $REFERENCE_GENOME -1 $input1 -2 $input2 -o $TMP_OUTPUT_PREFIX -p 4 $SSPLICEOPT_ALN" >&2;
			$SSPLICE -d $REFERENCE_GENOME -1 $input1 -2 $input2 -o $TMP_OUTPUT_PREFIX -p 4 $SSPLICEOPT_ALN;

			echo -e "[align_gfu_soapsplice]: creating BAM file from ${TMP_OUTPUT_PREFIX}.sam and header $HEADER_FILE with command-->\\n\\tcat $HEADER_FILE ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $TMP_OUTPUT_PREFIX" >&2;
			cat $HEADER_FILE ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $TMP_OUTPUT_PREFIX

			echo -e "[align_gfu_soapsplice]: verifying bam file: ${TMP_OUTPUT_PREFIX}.bam with command -->\\n\\t$GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam";			
			$GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;

			echo -e "[align_gfu_soapsplice]: moving output bam ${TMP_OUTPUT_PREFIX}.bam to ${LOCAL_SCRATCH} with command -->\\n\\tmv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH}/" >&2;
			mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH}/

			echo -e "[align_gfu_soapsplice]: moving junc files in $TMP_SCRATCH to $LOCAL_SCRATCH with command -->\\n\\tmv ${TMP_SCRATCH}/[*].junc ${LOCAL_SCRATCH}/" >&2;
			mv ${TMP_SCRATCH}/[*].junc ${LOCAL_SCRATCH}/

			echo -e "[align_gfu_soapsplice]: linking bam intermediate to bpipe working directory ($PWD) with command -->\\n\\tln ${LOCAL_SCRATCH}/$output.bam $output.bam" >&2;
			ln -s ${LOCAL_SCRATCH}/$output.bam $output.bam

			echo -e "[align_gfu_soapsplice]: Removing $TMP_SCRATCH RAM directory from node $HOSTNAME with command -->\\n\\trm -rf ${TMP_SCRATCH}" >&2;
			rm -rf ${TMP_SCRATCH}
		"""
	}
}

/*
 * RUNNER 
 */
Bpipe.run { 
	prepare_lane_gfu_soapsplice + "_R*_%.fastq.gz" * [align_gfu_soapsplice] //+ clean_scratch_gfu_soapsplice 
}