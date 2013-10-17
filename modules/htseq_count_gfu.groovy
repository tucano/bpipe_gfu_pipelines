// MODULE HTSEQ COUNT GFU
HTSEQ_COUNT="/usr/local/cluster/python2.7/bin/python2.7 /usr/local/cluster/python2.7/bin/htseq-count"
SAMTOOLS="/usr/local/cluster/bin/samtools"

htseq_count_gfu =
{
    var stranded : "no"
    var mode    : "union"
    var id_attribute : "gene_name"  
    var feature_type : "exon"

    doc title: "GFU: htseq-count on bam file with GTF annotation file",
        desc: """
            Run htseq-count on sorted BAM file and check output consistency with awk in file $output.txt
            Inputs: a gtf annotation file ($input.gtf) and a bam file ($input.bam). 
            Outputs: sam files of reads ($output.sam) and reads count ($output.txt).
        """,
        constraints: """
            Generate a $output.sam file without Headers. 
            Check the var id_attribute, feature_type, mode and stranded before launch.
            This stage forward the last bam file to create headers in sort_and_convert_sam_gfu
        """,
        author: "davide.rambaldi@gmail.com"

    from ("sorted_by_name.bam", "gtf") produce(input.bam.prefix.replaceFirst(~/\..*$/, '') + "_reads_count.txt",
        input.bam.prefix.replaceFirst(~/\..*$/, '') + "_reads.sam")
    {
        exec"""
            $SAMTOOLS view $input.bam | $HTSEQ_COUNT -m $mode -s $stranded -i $id_attribute -o $output.sam - $input.gtf > $output.txt;
            test \$(awk '{sum += \$2} END {print sum}' $output.txt) -gt 0;
        """, "htseq_count"
    }
    forward input.bam
}