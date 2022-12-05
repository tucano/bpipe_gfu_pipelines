// MODULE GENE READ GC RSEQC
READGC = """
    export PYTHONPATH=/usr/local/cluster/python/usr/lib64/python2.6/site-packages/:\$PYTHONPATH && 
    /usr/local/cluster/python2.7/bin/python2.7 /usr/local/cluster/python/usr/bin/read_GC.py
""".stripIndent().trim()

@preserve
rseqc_read_GC_gfu =
{
    doc title: "GFU: rseqc quality control of bam files: read_GC",
        desc: """
            Read GC content.
        """,
        constrains: "I am forcing export of site-packages to get qcmodule",
        author: "davide.rambaldi@gmail.com"

    produce("*.GC_plot.r","*.GC_plot.pdf","*.GC.xls") {
        exec"""
            echo -e "[rseqc_read_GC]: read GC content on $input.bam";
            $READGC -i $input.bam -o $input.prefix;
        """
    }
    forward input.bam
}