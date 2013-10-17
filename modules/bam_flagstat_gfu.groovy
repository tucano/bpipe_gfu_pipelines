// MODULE FLAGSTAT ON BAM
SAMTOOLS="/usr/local/cluster/bin/samtools"

@preserve
bam_flagstat_gfu =
{
    doc title: "GFU: falgstat on BAM file",
        desc: "Launch $SAMTOOLS flagstat on the final (merged) bam file, produce a log file $output",
        constraints: "Generate a LOG file and forward input to next stage",
        author: "davide.rambaldi@gmail.com"
    
    transform("log") {
        exec"""
            echo -e "[bam_flagstat_gfu]: flagstat with input $input.bam and output file $output" >&2;
            $SAMTOOLS flagstat $input.bam > $output;
        """
    }
    forward input.bam
}