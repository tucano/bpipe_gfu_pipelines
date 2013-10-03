// MODULE BASE RECALIBRATOR GFU
GATK="java -Djava.io.tmpdir=/lustre2/scratch/ -Xmx32g -jar /lustre1/tools/bin/GenomeAnalysisTK.jar"

@Filter("recalibrated")
base_print_reads_gfu = {
    // stage vars
    var ref_genome : "/lustre1/genomes/hg19/fa/hg19.fa"
    var truseq : "/lustre1/genomes/hg19/annotation/TruSeq_10k.intervals"
    var pretend : false

    doc title: "GFU Base recalibration with GATK: generate a new recalibrated BAM file",
        desc: "Generate BAM file after recalibration with PrintReads",
        author: "davide.rambaldi@gmail.com"
    
    // this stage is really slow, to test I use the pretend mode (see: mocking)
    if (pretend)
    {
      println"""
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -R $ref_genome
              -I $input.bam
              -o $output.bam
              -T PrintReads
              -L $truseq
              -nct 64
              -BQSR $input.grp
      """
      exec "touch $output.bam"
    }
    else
    {
      exec"""
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -R $ref_genome
              -I $input.bam
              -o $output.bam
              -T PrintReads
              -L $truseq
              -nct 64
              -BQSR $input.grp
      """
    }
}