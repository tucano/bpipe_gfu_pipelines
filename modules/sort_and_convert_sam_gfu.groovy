// MODULE SORT SAM BY COORDINATES AND CONVERT TO BAM
SAMTOOLS="/usr/local/cluster/bin/samtools"

@preserve
sort_and_convert_sam_gfu = 
{
    doc title: "GFU: sort sam file by coordinates and convert to bam",
        desc: "This stage is used by htseq-count pipelines to reconvert the output reads sam file to bam file",
        constraints: "I take the headers from the last forwarded bam file (the input of htseq-count stage)",
        author: "davide.rambaldi@gmail.com"
    transform("_reads.sam") to("_reads_sorted.bam")
    {
        exec """
            echo -e "[sort_and_convert_sam_gfu]: sort sam by coordinates and convert to bam. SAM: $input.sam HEADERS FROM: $input.bam";
            $SAMTOOLS view -H $input.bam | cat - $input.sam | $SAMTOOLS view -b -S - | $SAMTOOLS sort - $output.prefix
        """
    }
}