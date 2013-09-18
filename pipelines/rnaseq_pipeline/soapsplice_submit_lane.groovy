// BPIPE PIPELINE to compute all alignment in a dir with soapsplice
about title: "RNA alignment pipeline with soapsplice: IOS GFU 009.",
	  desc: "Pipeline description"

// Usage: bpipe run -r soapsplice_submit_lane.groovy *.fastq.gz

// SOFTWARES PATHS AND OPTIONS
SSPLICE="/lustre1/tools/bin/soapsplice"
SAMTOOLS="/usr/local/cluster/bin/samtools"
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"
PICMERGE="/usr/local/cluster/bin/MergeSamFiles.jar"

// File Paths
ENVIRONMENT_FILE="gfu_environment.sh"

// STAGES SCRIPTS
GFU_PREPARE_ALIGN_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/soapsplice_prepare_align.sh"
GFU_VERIFY_BAM           = "/home/drambaldi/bpipe_gfu_pipelines/bin/verify_bam.sh"

@Transform("bam")
align_gfu_soapsplice = 
{
	doc title: "Soapsplice alignment task",
		desc: "Align with soapsplice. Generate temporary files in /dev/shm on the node",
		author: "davide.rambaldi@gmail.com"
	from("*.fastq.gz","*.fastq.gz") produce(input.prefix - ".fastq" + ".bam") 
	{
		exec"""
			HEADER_FILE=`$GFU_PREPARE_ALIGN_SCRIPT $ENVIRONMENT_FILE $input1.gz`;
			TMP_SCRATCH=`dirname $HEADER_FILE`
			TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;

			source $ENVIRONMENT_FILE;
			
			echo -e "[align_gfu_soapsplice]: soapsplice alignment on node $HOSTNAME" >&2;

			$SSPLICE -d $REFERENCE_GENOME -1 $input1.gz -2 $input2.gz -o $TMP_OUTPUT_PREFIX -p 4 $SSPLICEOPT_ALN;

			cat $HEADER_FILE ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $TMP_OUTPUT_PREFIX

			$GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;

			mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH}/;

			for F in ${TMP_SCRATCH}/*.junc; do
				if [[ -e $F ]]; then
					mv $F ${LOCAL_SCRATCH}/; 
				fi;
			done;

			ln -s ${LOCAL_SCRATCH}/$output.bam $output.bam;

			rm -rf ${TMP_SCRATCH};
		""", "soapsplice"
	}
}

@preserve
merge_bam_gfu = 
{
	doc title: "GFU merge bam files with $PICMERGE",
		desc: "Merge bam files with $PICMERGE, combine junction usage",
		author: "davide.rambaldi@gmail.com"
	def output_prefix = input.prefix.replaceFirst(/_R.*/,"")
	def output_bam = output_prefix + ".merge.bam"
	def output_junc = output_prefix + ".junc"
	input_strings = inputs.collect() { return "I=" + it}.join(" ")
	produce(output_bam, output_junc) 
	{
		exec """
			source $ENVIRONMENT_FILE;
			echo -e "[merge_bam_gfu]: Merging BAM files $inputs in output file $output_bam" >&2;
			java -jar $PICMERGE $input_strings O=$output_bam
				VALIDATION_STRINGENCY=SILENT
				CREATE_INDEX=true
				MSD=true
				ASSUME_SORTED=true
				USE_THREADING=true

			echo -e "[merge_bam_gfu]: Merging junc files in $output_junc" >&2;
			touch $output_junc;
			for F in ${LOCAL_SCRATCH}/*.junc; do
				if [[ -e $F ]]; then
					cat $F >> $output_junc;
				fi;
			done;
		""","merge_bam_files"
	}
}

@Transform("log")
bam_flagstat_gfu =
{
	doc title: "GFU falgstat on BAM file",
		desc: "Launch $SAMTOOLS flagstat on the final (merged) bam file, produce a log file $output",
		author: "davide.rambaldi@gmail.com"
	exec """
		echo -e "[bam_flagstat_gfu]: flagstat on output file $output" >&2;
		$SAMTOOLS flagstat $input > $output;
	"""
}

/*
 * RUNNER 
 */
Bpipe.run 
{ 
	"_R*_%.fastq.gz" * [align_gfu_soapsplice] + merge_bam_gfu + bam_flagstat_gfu
}