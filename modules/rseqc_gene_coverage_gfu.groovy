// MODULE GENE COVERAGE FROM RSEQC
GENECOVERAGE="""
    export PYTHONPATH=/usr/local/cluster/python/usr/lib64/python2.6/site-packages/:\$PYTHONPATH && 
    /usr/local/cluster/python2.7/bin/python2.7 /usr/local/cluster/python/usr/bin/geneBody_coverage.py
""".stripIndent().trim()

@preserve
rseqc_gene_coverage_gfu =
{
    doc title: "GFU: rseqc quality control of bam files: gene_coverage",
        desc: """
            Read coverage over gene body. 
            This module is used to check if reads coverage is uniform and if there is any 5’/3’ bias. 
            This module scales all transcripts to 100 nt and calculates the number of reads covering 
            each nucleotide position. Finally, it generates a plot illustrating the coverage profile 
            along the gene body.
        """,
        constrains: "I am forcing export of site-packages to get qcmodule",
        author: "davide.rambaldi@gmail.com"

    produce("*.geneBodyCoverage.pdf","*.geneBodyCoverage_plot.r","*.geneBodyCoverage.txt") {
        exec"""
            $GENECOVERAGE -r $BED12_ANNOTATION -i $input.bam  -o $input.prefix
        """
    }

    forward input.bam
}