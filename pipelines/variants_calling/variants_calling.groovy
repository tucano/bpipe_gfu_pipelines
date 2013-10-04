// BPIPE PIPELINE Variants calling
about title: "Variants calling pipeline: IOS GFU 015 and IOS GFO 005"

// for chr multistaging see: https://code.google.com/p/bpipe/wiki/Chr
@Transform("vcf")
unified_genotyper_gfu = {
    // STAGE VARS
    var ref_genome : "/lustre1/genomes/hg19/fa/hg19.fa"
    var dbsnp : "/lustre1/genomes/hg19/annotation/dbSNP-137.chr.vcf"
    var truseq : "/lustre1/genomes/hg19/annotation/TruSeq_10k.intervals"
    var call_conf : 20.0

    doc title: "GATK: Unified Genotyper",
        desc: "Produce a VCF file with SNP calls and INDELs",
        author: "davide.rambaldi@gmail.com"

    exec"""
        ulimit -l unlimited;
        ulimit -s unlimited;
        $GATK -R $ref_genome
              -I $input.bam
              --dbsnp $dbsnp
              -T UnifiedGenotyper
              -nct 8
              -stand_call_conf $call_conf
              -glm BOTH
              -o $output.vcf
              -L FIXME (MULTI STAGE HERE!!!!!!)
    ""","gatk"
}