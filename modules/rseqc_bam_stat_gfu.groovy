// MODULE BAM STAT FROM RSEQC
BAMSTAT="""
     export PYTHONPATH=/usr/local/cluster/python/usr/lib64/python2.6/site-packages/:\$PYTHONPATH && 
     /usr/local/cluster/python2.7/bin/python2.7 /usr/local/cluster/python/usr/bin/bam_stat.py
""".stripIndent().trim()

@preserve
rseqc_bam_stat_gfu =
{
    doc title: "GFU: rseqc quality control of bam files: bam_stat",
        desc: """
            This program is used to calculate reads mapping statistics from provided BAM file. 
            This script determines “uniquely mapped reads” from mapping quality, which quality 
            the probability that a read is misplaced (Do NOT confused with sequence quality, 
            sequence quality measures the probability that a base-calling was wrong) .
        """,
        constrains: "I am forcing export of site-packages to get qcmodule",
        author: "davide.rambaldi@gmail.com"

    transform("bam_stat.log") {
        exec"""
            echo -e "[bam_stat_gfu]: bam stats on file $input.bam";
            $BAMSTAT -i $input.bam 2> $output;
        """
    }
    forward input.bam
}