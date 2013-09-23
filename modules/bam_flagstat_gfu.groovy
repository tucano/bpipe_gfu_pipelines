// MODULE FLAGSTAT ON BAM
SAMTOOLS="/usr/local/cluster/bin/samtools"

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