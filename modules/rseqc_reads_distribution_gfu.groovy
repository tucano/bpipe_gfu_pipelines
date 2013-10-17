// MODULE READS DISTRIBUTIONS FROM RSEQC
READS_DISTRIBUTION = """
    export PYTHONPATH=/usr/local/cluster/python/usr/lib64/python2.6/site-packages/:\$PYTHONPATH && 
    /usr/local/cluster/python2.7/bin/python2.7 /usr/local/cluster/python/usr/bin/read_distribution.py
""".stripIndent().trim()

@preserve
rseqc_reads_distribution_gfu = 
{
    doc title: "GFU: rseqc quality control of bam files: reads_distribution",
        desc: """
           Provided a BAM/SAM file and reference gene model, this module will calculate 
           how mapped reads were distributed over genome feature 
           (like CDS exon, 5’UTR exon, 3’ UTR exon, Intron, Intergenic regions). 
        """,
        constrains: "I am forcing export of site-packages to get qcmodule",
        author: "davide.rambaldi@gmail.com"
    
    transform("reads_distribution.log") {
        exec """
            echo -e "[rseqc_reads_distribution]: input file $input.bam";
            $READS_DISTRIBUTION -i $input.bam -r $BED12_ANNOTATION 1> $output
        """    
    }
    forward input.bam
}