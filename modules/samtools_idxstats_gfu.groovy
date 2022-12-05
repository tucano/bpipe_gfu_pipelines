// MODULE GENE COVERAGE FROM RSEQC
SAMTOOLS="/usr/local/cluster/bin/samtools"

@preserve
samtools_idxstats_gfu = 
{
    doc title: "GFU: samtools idxstats on bam file (index)",
        desc: """
            Retrieve and print stats in the index file. 
            The output is TAB delimited with each line consisting of reference sequence name, 
            sequence length, # mapped reads and # unmapped reads.        
        """,
        constrains: "BAM file $input.bam must be indexed",
        author: "davide.rambaldi@gmail.com"
    
    transform("idxstats.log") {
        exec"""
            $SAMTOOLS idxstats $input.bam > $output
        """
    }
    forward input.bam 
}