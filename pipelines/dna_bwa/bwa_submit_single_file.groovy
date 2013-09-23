// BPIPE PIPELINE to align with bwa a single file
about title: "DNA lane alignment with bwa (single files): IOS GFU 009"

BWA="/usr/local/cluster/bin/bwa"
BWAOPT_ALN=""
BWAOPT_PE=""
BWAOPT_SE=""

SAMTOOLS="/usr/local/cluster/bin/samtools"
GFU_SPLIT_FASTQ  = "/home/drambaldi/bpipe_gfu_pipelines/bin/split_fastq.sh"
GFU_VERIFY_BAM   = "/home/drambaldi/bpipe_gfu_pipelines/bin/verify_bam.sh"
ENVIRONMENT_FILE = "gfu_environment.sh"

SPLIT_READS_SIZE = 2000000

split_fastq_pairs_gfu =
{
	doc title: "GFU split fastq files (R1 and R2)",
		desc: "use split to subdivide a fastq pair (R1 and R2) in chunks",
		author: "davide.rambaldi@gmail.com"
	
	produce("*.fastq")
	{
		exec """
			$GFU_SPLIT_FASTQ -1 $input1 -2 $input2 -r $SPLIT_READS_SIZE $ENVIRONMENT_FILE
		"""
	}
}

split_fastq_single_gfu =
{
	doc title: "GFU split fastq single files",
		desc: "use split to subdivide a fastq file in chunks",
		author: "davide.rambaldi@gmail.com"
	
	produce("*.fastq")
	{
		exec """
			$GFU_SPLIT_FASTQ -1 $input -r $SPLIT_READS_SIZE $ENVIRONMENT_FILE
		"""
	}
}

@Transform("sai")
align_bwa_gfu =
{
	doc title: "GFU align DNA reads with bwa",
		desc: "use bwa aln to align reads (paired ends) on the reference genome",
		author: "davide.rambaldi@gmail.com"
	exec """
		source $ENVIRONMENT_FILE;
		$BWA aln -t 2 $BWAOPT_ALN $REFERENCE_GENOME $input > $LOCAL_SCRATCH/$output;
		ln -s ${LOCAL_SCRATCH}/$output $output;
	""","bwa_aln"
}

@Transform("bam")
sampe_bwa_gfu =
{
	doc title: "GFU align DNA reads with bwa: merge paired ends with sampe",
		desc: "Generate alignments in the SAM format given paired-end reads.",
		author: "davide.rambaldi@gmail.com"
	
	def input1_fastq = input1.prefix + ".fastq"
	def input2_fastq = input2.prefix + ".fastq"

	exec """
		source $ENVIRONMENT_FILE;
		TMP_SCRATCH=\$(/bin/mktemp -d /dev/shm/${PROJECTNAME}.XXXXXXXXXXXXX);
		TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;
		echo -e "[sampe_bwa_gfu]: soapsplice alignment on node $HOSTNAME with TMP_SCRATCH: $TMP_SCRATCH" >&2;
		$BWA sampe $BWAOPT_PE -r "@RG\tID:${ID}\tPL:${PL}\tPU:${PU}\tLB:${LB}\tSM:${SM}\tCN:${CN}" $REFERENCE_GENOME ${LOCAL_SCRATCH}/$input1.sai ${LOCAL_SCRATCH}/$input2.sai $input1_fastq $input2_fastq > ${TMP_OUTPUT_PREFIX}.sam;
		$SAMTOOLS view -Su ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS sort - ${TMP_OUTPUT_PREFIX};
		$GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;
		mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH};
		ln -s ${LOCAL_SCRATCH}/$output $output;
		rm -rf ${TMP_SCRATCH};
	""","bwa_sampe"
}

Bpipe.run 
{
	// RUNNER FOR PAIRS R1 - R2 (bpipe run pipe.groovy *R*.gz)
	"%_R*" * [split_fastq_pairs_gfu] + "%.fastq" * [align_bwa_gfu] + "read*_%.sai" * [sampe_bwa_gfu] + merge_bam_gfu + bam_flagstat_gfu

	// RUNNER FOR SINGLE FILE TODO FIXME
	// split_fastq_single_gfu
}