// MODULE REALIGNER CREATOR GFU
GATK="java -Djava.io.tmpdir=/lustre2/scratch/ -Xmx32g -jar /lustre1/tools/bin/GenomeAnalysisTK.jar"

@Transform("intervals")
realiagner_target_creator_gfu = {
    // stage vars
    var ref_genome : "/lustre1/genomes/hg19/fa/hg19.fa"
    var truseq : "/lustre1/genomes/hg19/annotation/TruSeq_10k.intervals"
    var dbsnp : "/lustre1/genomes/hg19/annotation/dbSNP-137.chr.vcf"
    var pretend : false

    doc title: "GFU find small intervals with GATK toolkit: RealignerTargetCreator",
        desc: "Find small intervals that should be realigned",
        author: "davide.rambaldi@gmail.com",
        constrains: "Require the index bai file for bam: $input"

    // this stage is really slow, to test I use the pretend mode (see: mocking)
    if (pretend)
    {
      println """
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -I $input.bam 
              -R $ref_genome 
              -T RealignerTargetCreator 
              -L $truseq
              -o $output.intervals
              --unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY
              --known $dbsnp;
      """
      exec """
        touch $output.intervals;
      """
    }
    else
    {
      exec"""
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -I $input.bam 
              -R $ref_genome 
              -T RealignerTargetCreator 
              -L $truseq
              -o $output.intervals
              --unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY
              --known $dbsnp;
      """
    }
    forward input
}
