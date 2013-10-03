// BPIPE PIPELINE to read counts with htseq-count
about title: "BAM recalibration on DNA samples: IOS GFU 020."

REFERENCE_GENOME = "/lustre1/genomes/hg19/fa/hg19.fa"
TRUSEQ           = "/lustre1/genomes/hg19/annotation/TruSeq_10k.intervals"
DBSNP            = "/lustre1/genomes/hg19/annotation/dbSNP-137.chr.vcf"

Bpipe.run 
{
    realiagner_target_creator_gfu.using(ref_genome : REFERENCE_GENOME, truseq : TRUSEQ, dbsnp : DBSNP) +
    indel_realigner_gfu.using(ref_genome : REFERENCE_GENOME, truseq : TRUSEQ, dbsnp : DBSNP) +
    base_recalibrator_gfu.using(ref_genome : REFERENCE_GENOME, truseq : TRUSEQ, dbsnp : DBSNP) +
    base_print_reads_gfu.using(ref_genome : REFERENCE_GENOME, truseq : TRUSEQ)
}