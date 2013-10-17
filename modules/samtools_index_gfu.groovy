// MODULE SAMTOOLS CREATE BAI FILE
SAMTOOLS="/usr/local/cluster/bin/samtools"

@preserve
samtools_index_gfu =
{
    doc title: "GFU: create BAi file from BAM file",
        desc: "Launch $SAMTOOLS index on $input.bam. Create link from bam.bai to .bai",
        constraints: "Generate a LOG file and forward input to next stage",
        author: "davide.rambaldi@gmail.com"

    transform("bai") {
        exec"""
            $SAMTOOLS index $input.bam;
            ln -s ${input}.bai $output;
        """
    }
    forward input.bam
}