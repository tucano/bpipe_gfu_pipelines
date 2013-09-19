// BPIPE PIPELINE to read counts with htseq-count
about title: "RNA-seq reads count with htseq-count and differential expression analysis with DEXSeq: IOS GFU 007."

// USAGE: bpipe run <pipeline.groovy> input.bam annotation.gtf

SAMTOOLS="/usr/local/cluster/bin/samtools"
HTSEQ_COUNT="/usr/local/cluster/python2.7/bin/python2.7 /usr/local/cluster/python2.7/bin/htseq-count"

ENVIRONMENT_FILE="gfu_environment.sh"
SCRATCH_PREFIX="/lustre2/scratch"


// STAGES SCRIPTS
GFU_PREPARE_HTSEQ_COUNT_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/htseq_count_prepare_bam.sh"

@Filter("sorted")
sort_bam_htseq_gfu =
{
	doc title: "htseq-count prepare bam file",
		desc: "If needed, run a script that search for LOCAL_SCRATCH and create tmp dir; then sort bam file",
		author: "davide.rambaldi@gmail.com"
	// conditional on presence of ENVIRONMENT_FILE
	if (new File(ENVIRONMENT_FILE).exists() == false)
	{
		exec "$GFU_PREPARE_HTSEQ_COUNT_SCRIPT $input.bam $SCRATCH_PREFIX > $ENVIRONMENT_FILE"
	}
	exec """
		source $ENVIRONMENT_FILE;
		$SAMTOOLS sort -n $input.bam $output.prefix
	"""
	forward input.gtf
}

htseq_count_gfu =
{
	doc title: "htseq-count stage",
		desc: """
			Run htseq-count on sorted BAM file and check output consistency with awk for $output.txt
			Inputs: a gtf annotation file ($input.gtf) and a bam file ($input.bam). 
			Outputs: sam files of reads ($output.sam) and reads count ($output.txt).
			""",
		constraints: "Generate a $output.sam file without Headers, should I copy the headers from input?"
		author: "davide.rambaldi@gmail.com"
	from("sorted.bam", "gtf") produce(input.bam.prefix.replaceFirst(~/\..*$/, '') + "_reads_count.txt",
		input.bam.prefix.replaceFirst(~/\..*$/, '') + "_reads.sam")
	{
		exec """
			$SAMTOOLS view $input.bam | $HTSEQ_COUNT -m union -s no -o $output.sam - $input.gtf > $output.txt;
			test \$(awk '{sum += \$2} END {print sum}' cbs1_r6_1395_B_GTGTTA_L007_reads_count.txt) -gt 0 || exit 1;
		""", "htseq_count"
	}
}

/*
 * RUNNER 
 */
Bpipe.run 
{ 
	sort_bam_htseq_gfu + htseq_count_gfu
}