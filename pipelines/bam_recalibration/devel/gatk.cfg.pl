BEGIN {
    # This section contains the library configuration
    package main;
    
    %conf = (
                #path for tools and programs
                'GATK'=>'java -Djava.io.tmpdir=/lustre2/scratch/ -Xmx16g -jar /lustre1/tools/bin/GenomeAnalysisTK.jar',
                'MarkDuplicates'=>'java -Djava.io.tmpdir=/lustre2/scratch/ -Xmx16g -jar /usr/local/cluster/bin/MarkDuplicates.jar', 
                'MergeSam'=>'java -Djava.io.tmpdir=/lustre2/scratch/ -Xmx16g -jar /usr/local/cluster/bin/MergeSamFiles.jar',
                'vcfutils' => '/lustre1/tools/libexec/vcftools_0.1.9/perl/',
                'SnpSift'=>'java -Xmx16g -jar /lustre1/tools/bin/SnpSift.jar',
                
                #path for standard data
                'dbsnp'=>'/lustre1/genomes/hg19/annotation/dbSNP-137.chr.vcf',
                '1kg_omni'=>'/lustre1/genomes/hg19/annotation/1000G_omni2.5.hg19.sites.vcf.gz',
                'hapmap'=>'/lustre1/genomes/hg19/annotation/hapmap_3.3.hg19.sites.vcf.gz',
                'mills'=>'/lustre1/genomes/hg19/annotation/Mills_and_1000G_gold_standard.indels.hg19.vcf',
                '1000G'=>'/lustre1/genomes/hg19/annotation/1000G_phase1.snps.high_confidence.hg19.vcf',
                'ref_genome'=>'/lustre1/genomes/hg19/fa/hg19.fa',
                'chr_length'=>'/lustre1/genomes/hg19/annotation/hg19.tab',
                'chunk_file'=>'/lustre1/genomes/hg19/annotation/TruSeq_10k.intervals', #for exomes
                #'chunk_file'=>'/lustre1/genomes/hg19/annotation/hg19_chr_list', #for genomes
                
                #parameters definition
                'CALL_CONF' =>'20.0',
                'EMIT_CONF' => '10.0',
                'HAPLO_CALL_CONF' =>'50.0',
                'WHOLE_GENOME'=>[0|1], #0 For exomes
                'remove_duplicates'=>['false'|'true'],
                'windows'=>50000,  
                'sample_no'=>48,
                'java_mem'=>32,
                'trios'=>0,
                'ped'=>0,
               
                
                #project specific path for data and parameters/options
                'mail_list'=>'surname.name@hsr.it',
                'experiment_name'=>'TagForThe Experiment',
                'workdir'=>"/lustre1/workspace/PI/ID_ProjectName/",
                'workout'=>"/lustre1/workspace/PI/ID_ProjectName/Annotation/",
                'scratchin'=>"/lustre2/scratch/PI/ID_ProjectName/",
                'scratchout'=>"/lustre2/scratch/PI/ID_ProjectName/processed/",
                'ped_file'=>undef,
                
            );
}
unless (-e $conf{'scratchout'}){`mkdir -p $conf{'scratchout'}`}
unless (-e $conf{'workout'}){`mkdir -p $conf{'workout'}`}

1
