// MODULE BASE RECALIBRATOR GFU
GATK="java -Djava.io.tmpdir=/lustre2/scratch/ -Xmx32g -jar /lustre1/tools/bin/GenomeAnalysisTK.jar"

@Transform("grp")
base_recalibrator_gfu = {
    // stage vars
    var ref_genome : "/lustre1/genomes/hg19/fa/hg19.fa"
    var dbsnp : "/lustre1/genomes/hg19/annotation/dbSNP-137.chr.vcf"
    var truseq : "/lustre1/genomes/hg19/annotation/TruSeq_10k.intervals"
    var pretend : false

    doc title: "GFU Base recalibration with GATK",
        desc: "Base recalibration with GATK tool: BaseRecalibrator",
        author: "davide.rambaldi@gmail.com"

    // this stage is really slow, to test I use the pretend mode (see: mocking)
    if (pretend)
    {
      println"""
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -R $ref_genome
              -knownSites $dbsnp
              -I $input.bam
              -L $truseq
              -T BaseRecalibrator
              --covariate QualityScoreCovariate
              --covariate CycleCovariate
              --covariate ContextCovariate
              --covariate ReadGroupCovariate
              --unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY
              -nct 64
              -o $output.grp
      """
      exec "touch $output.grp"
    }
    else
    {
      exec"""
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -R $ref_genome
              -knownSites $dbsnp
              -I $input.bam
              -L $truseq
              -T BaseRecalibrator
              --covariate QualityScoreCovariate
              --covariate CycleCovariate
              --covariate ContextCovariate
              --covariate ReadGroupCovariate
              --unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY
              -nct 64
              -o $output.grp
      """
    }
    forward input.bam
}