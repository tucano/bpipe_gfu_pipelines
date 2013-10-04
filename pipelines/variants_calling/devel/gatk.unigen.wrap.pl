#! /usr/bin/perl -w

=head1 author

Vincenza Maselli
vincenza.maselli@hsr.it

=head1 decription

This script write the wrapper for the second step of GATK, from processed bam to raw vcf

    #UnifiedGenotyper CombineVariants VariantRecalibrator ApplyRecalibration VariantFiltration 
     
    It is divided in blocks, one for each command, the block have this format
    the output file name of the command 
    the command string
    the hash for parameters definitions
    the call to the writeJob function (see documentation for the function itself)
    the call to the submit function (see documentation for the function itself)

	last update 25 July 2013

=head1 usage
    
    This script require a config file and a bam file (or a directory of bam)from the previous pipeline
     
    perl gatk.unigen.wrap.pl -c config_file -t (file |dir) -a file name|dir name
    perl gatk.unigen.wrap.pl -h for verbose details
    
=cut 

use vars;
use strict;
use Data::Dumper;
use Getopt::Long;
use Vcf;

my ($test, $cfg, $tag, $arg,$help);
my $USAGE = "perl gatk.unigen.wrap.pl -c config_file -t (file|dir) -a file|dir name\n";
my $PWD = `pwd`;
chomp $PWD;

BEGIN {
    
     my $opt = &GetOptions( 
            "a=s"   =>  \$arg,
            "c=s"   =>  \$cfg,
            "t=s"   =>  \$tag,
            "test=s"=>  \$test,
            "h"     =>  \$help
            );
     
    if ($help || (!$cfg || !$tag || !$arg)){
        print STDERR "$USAGE
                      -c = the config file
                      -t = the type of input: a list of bamfiles (-t file) or a dir of bamfiles (-t dir)
                      -a file : the dir of bamfiles or a list of file names (-a \"bam1 bam2 bam3\"). NB rember to quote the file name and NOT to use the absolute path 
                      -test: 1 if you want to write the jobs but not submit them; 2 if you want to submit the job only for two chromosomes     		  
                      -h : this help\n
                      type perlodoc gatk.unigen.wrap.pl for full documentation\n";
        exit;
    }

    require $cfg;
}
unless (defined $test){$test = 0}
my %conf =  %::conf;
my $wholegenome = $conf{'WHOLE_GENOME'};
my $worktag  = $conf{'workout'}.$conf{'experiment_name'};
my $inputdir = $conf{'scratchin'};
my $scratchtag = $conf{'scratchout'}.$conf{'experiment_name'};
my $mem = $conf{'java_mem'};
my $param;
	
# This section calculate the number of region in which the job are split, starting from the chunk file, 
# which is a truseq region file for the exomes and a chromosome region file for the whole genome

my $truseq = $conf{'chunk_file'};
open (CHUNK, $truseq);
my @regs = <CHUNK>;
close(CHUNK);
if ($test == 2){@regs = ($regs[-2],$regs[-1]);}
my $max= scalar @regs;

# defining the bam file in input, whether the input is a list of file, a single file or a dir of bam, 
# the resulting variables is an array of file name

my @bams;
if ($tag eq 'dir'){ 
    my $dir = $arg;
    opendir (DIR,$dir) || die "$! $dir\n";
    while (my $file = readdir(DIR) ){ 
        next if $file =~ /^\./;
        next unless $file =~ /bam$/;
        next unless $file =~ /recal/;
        push(@bams, $file);
    }
}elsif($tag eq 'file'){
    @bams = split / /,$arg; 
}else{die "$USAGE\n";}

if ($test == 2){
    @bams = ($bams[0]);
}

# creating a variable with all the bam file name, as input for the first command

my $bam_string;
foreach my $file (sort {$a cmp $b} @bams){
    my $bam = $conf{'scratchin'}.$file;
    $bam_string .= "-I $bam ";
}

=head1 main

=head2 UnifiedGenotyper 

    Description:   process the file that comes from a specific dir or from a list of name
                   it works on slices of regions and submit the job divided by the number of region 
                    
    Input:         BAM file e.g. test_local.realign.recal_data.merged.bam
    Exceptions:    none
    Returns:       vcf file for each region ../processed/exp_name.I_realign.recal.vcf, where I is the trunch number 
                   file with tranche-region pair ../processed/exp_name.match.txt 
                   
=cut

my $ug_job_id;
my ($vcf_string, $vcf_list,$job_string);

for (my $j = 0; $j <$max; $j ++){
	next unless $regs[$j]; 
	my $vcf_file = $scratchtag.$j."_realign.recal.vcf";
	my $reg = $regs[$j];
	chomp $reg;
	if (-e $vcf_file){$vcf_string .= "--variant:VCF ".$vcf_file." \\\n";
	$vcf_list .= $vcf_file." \\\n";next}
	my $UnifiedGenotyper = qq{
		cd $PWD
		ulimit -l unlimited
		ulimit -s unlimited
	   $conf{'GATK'} \\
		-R $conf{'ref_genome'} \\
		--dbsnp $conf{'dbsnp'} \\
		$bam_string  \\
		-T UnifiedGenotyper \\
		-nct 8 \\
		-stand_call_conf $conf{'CALL_CONF'} \\
		-glm BOTH \\
		-o $vcf_file \\
		-L $reg \\
		-U ALLOW_SEQ_DICT_INCOMPATIBILITY \\};
	if ($conf{'ped'}){$UnifiedGenotyper .= qq{--pedigree $conf{'ped_file'}}}
	
	$param->{'job_name'} = $j."_UniGen";
	$param->{'command'} = $UnifiedGenotyper;
	$param->{'select'} = 1;
	$param->{'output_file'} = $vcf_file;
	$param->{'depending_id'} = undef;
	$param->{'number_cpu'} = 8;
	$param->{'depend'} = "afterok";
	
	my $ug_job = &writeJob($param);
	$ug_job_id = &submit($ug_job);
	$job_string .= $ug_job_id.":";
	$vcf_string .= "--variant:VCF ".$vcf_file." \\\n";     
	$vcf_list .= $vcf_file." \\\n";   
	    
}

$vcf_string =~ s/\n$//;
$job_string =~ s/:$// if $job_string =~ /:/;
$vcf_list =~ s/\n$//;  

=head2   vcf-concat
    
    
    Description:   merge all the vcf previuosly created, if CombineVariants doesn't work 
    Input:         ../processed/exp_name.I_realign.recal.vcf
    Exceptions:    none
    Returns:       ../processed/tmp.realign.recal.vcf

=cut  

my $merged = $conf{'scratchout'}."tmp.realign.recal.vcf";
my $vcfMerge = qq{
    $conf{'vcfutils'}vcf-concat $scratchtag*.vcf | vcf-sort > $merged
};

$param->{'job_name'} = "MergeRegVar";
$param->{'command'} = $vcfMerge;
$param->{'select'} = 1;
$param->{'output_file'} = $merged;
$param->{'depending_id'} = $job_string;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";
my $merge_cv_job = &writeJob($param);
my $merge_cv_job_id = &submit($merge_cv_job);

=pod    VariantRecalibrator
    
    Description:    Called 2 times in parallel, one for the indel one for the snp
    Input:          ../processed/tmp.realign.recal.vcf 
    Exceptions:     none
    Returns:        - recalFile ../processed/tmp.snp.recal.csv
                    - tranchesFile ../processed/tmp.snp.tranches
                    - rscriptFile ../processed/tmp.snp.plot.R
                    snpVarRecal.submit.job
                    - recalFile ../processed/tmp.indel.recal.csv
                    - tranchesFile ../processed/indel.snp.tranches
                    - rscriptFile ../processed/indel.snp.plot.R
                    indelVarRecal.submit.job
=cut


my $sample_number = $conf{'sample_no'};
my $input = $merged;
my $snpVariantRecalibrator=qq{
    ulimit -l unlimited
    ulimit -s unlimited
   $conf{'GATK'} \\
    -T VariantRecalibrator \\
    -R $conf{'ref_genome'} \\
    -input $input \\
    -resource:hapmap,VCF,known=false,training=true,truth=true,prior=15.0 $conf{'hapmap'} \\
    -resource:omni,VCF,known=false,training=true,truth=false,prior=12.0 $conf{'1kg_omni'} \\
    -resource:dbsnp,VCF,known=true,training=false,truth=false,prior=6.0 $conf{'dbsnp'} \\
    -resource:1000G,VCF,known=false,training=true,truth=false,prior=10.0 $conf{'1000G'} \\
    -an HaplotypeScore \\
    -an ReadPosRankSum \\
    -an FS \\
    -an QD \\
    -mode SNP \\
    -recalFile $conf{'scratchout'}tmp.snp.recal.csv \\
    -tranchesFile $conf{'scratchout'}tmp.snp.tranches \\
    -rscriptFile $conf{'scratchout'}tmp.snp.plot.R \\
    -U ALLOW_SEQ_DICT_INCOMPATIBILITY \\
};

if ($sample_number >= 10){$snpVariantRecalibrator .= qq{-an InbreedingCoeff \\}}
if ($conf{'ped'}){$snpVariantRecalibrator .= qq{--pedigree $conf{'ped_file'} \\}}
unless ($wholegenome){
	$snpVariantRecalibrator .= qq{-an MQRankSum \\};
	$snpVariantRecalibrator .= qq{-maxGaussians 4 -percentBad 0.05 -minNumBad 1000};
	}
if ($wholegenome){$snpVariantRecalibrator .= qq{\n-an DP};}

$param->{'job_name'} = "snpVarRecal";
$param->{'command'} = $snpVariantRecalibrator;
$param->{'select'} = 1;
$param->{'output_file'} = "$conf{'scratchout'}tmp.snp.recal.csv";
$param->{'depending_id'} = $merge_cv_job_id;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";

my $snp_vr_job = &writeJob($param);
my $snp_vr_job_id = &submit($snp_vr_job);

=pod    ApplyRecalibration
    
    Description:    Called 2 times in parallel, one for the indel one for the snp
    Input:          ../processed/tmp.realign.recal.vcf 
                    - recalFile ../processed/tmp.snp.recal.csv
                    - tranchesFile ../processed/tmp.snp.tranches
                    - rscriptFile ../processed/tmp.snp.plot.R
                    - recalFile ../processed/tmp.indel.recal.csv
                    - tranchesFile ../processed/indel.snp.tranches
                    - rscriptFile ../processed/indel.snp.plot.R
    Exceptions:     none
    Returns:        ../processed/tmp.snp.vcf
                    ../processed/tmp.indel.vcf
=cut

my $out2 = $conf{'scratchout'}."tmp.snp.vcf"; 
my $snpApplyRecalibration=qq{
    ulimit -l unlimited
    ulimit -s unlimited
    $conf{'GATK'} \\
    -T ApplyRecalibration \\
    -R $conf{'ref_genome'} \\
    -input $input \\
    -tranchesFile $conf{'scratchout'}tmp.snp.tranches \\
    -recalFile $conf{'scratchout'}tmp.snp.recal.csv \\
    --mode SNP \\
    -o $out2 \\
    -U ALLOW_SEQ_DICT_INCOMPATIBILITY \\};
if ($conf{'ped'}){$snpApplyRecalibration .= qq{--pedigree $conf{'ped_file'}}}

$param->{'job_name'} = "snpApRecal";
$param->{'command'} = $snpApplyRecalibration;
$param->{'select'} = 1;
$param->{'output_file'} = $out2;
$param->{'depending_id'} = $snp_vr_job_id;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";

my $snp_ar_job = &writeJob($param);
my $snp_ar_job_id = &submit($snp_ar_job);

my $out3 = $conf{'scratchout'}."tmp.indel.vcf";
my $indel_job_id;

my $indelVariantRecalibrator=qq{
    ulimit -l unlimited
    ulimit -s unlimited
   $conf{'GATK'} \\
    -T VariantRecalibrator \\
    -R $conf{'ref_genome'} \\
    -input $input \\
    -resource:mills,VCF,known=true,training=true,truth=true,prior=12.0 $conf{'mills'} \\
    -resource:dbsnp,VCF,known=true,training=false,truth=false,prior=2.0 $conf{'dbsnp'} \\
    -an ReadPosRankSum  \\
    -an FS \\
    -mode INDEL \\
    -recalFile $conf{'scratchout'}tmp.indel.recal.csv \\
    -tranchesFile $conf{'scratchout'}tmp.indel.tranches \\
    -rscriptFile $conf{'scratchout'}tmp.indel.plot.R \\
    -U ALLOW_SEQ_DICT_INCOMPATIBILITY \\
};


if ($sample_number >= 10){$indelVariantRecalibrator .= qq{-an InbreedingCoeff \\}}
if ($conf{'ped'}){$indelVariantRecalibrator .= qq{--pedigree $conf{'ped_file'}}}
unless ($wholegenome){
	$indelVariantRecalibrator .= qq{-an MQRankSum \\};
	$indelVariantRecalibrator .= qq{--maxGaussians 4 -percentBad 0.05 -minNumBad 1000};
}
if ($wholegenome){$snpVariantRecalibrator .= qq{-an DP};}

$param->{'job_name'} = "indelVarRecal";
$param->{'command'} = $indelVariantRecalibrator;
$param->{'select'} = 1;
$param->{'output_file'} = "$conf{'scratchout'}tmp.indel.recal.csv";
$param->{'depending_id'} = $merge_cv_job_id;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";

my $indel_vr_job = &writeJob($param);
my $indel_vr_job_id = &submit($indel_vr_job);
    
my $indelApplyRecalibration=qq{
    ulimit -l unlimited
    ulimit -s unlimited
    $conf{'GATK'} \\
    -T ApplyRecalibration  \\
    -R $conf{'ref_genome'} \\
    -input $input  \\
    -tranchesFile $conf{'scratchout'}tmp.indel.tranches \\
    -recalFile $conf{'scratchout'}tmp.indel.recal.csv \\
    --mode INDEL  \\
    -o $out3 \\
    -U ALLOW_SEQ_DICT_INCOMPATIBILITY \\};
if ($conf{'ped'}){$indelApplyRecalibration .= qq{--pedigree $conf{'ped_file'}}}

$param->{'job_name'} = "indelApRecal";
$param->{'command'} = $indelApplyRecalibration;
$param->{'select'} = 1;
$param->{'output_file'} = $out3;
$param->{'depending_id'} = $indel_vr_job_id;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";

my $indel_ar_job = &writeJob($param);
my $indel_ar_job_id = &submit($indel_ar_job);

my $finalout = $scratchtag.".merged.tmp.vcf";
my $final_cv_job_id; 

=head2   vcf-concat
        
    Description:   merge recalibrated vcf if combine variants doesn't work
    Input:         ../processed/tmp.snp.vcf
                    ../processed/tmp.indel.vcf
    Exceptions:    none
    Returns:       ..processed/exp_name.merged.tmp.vcf

=cut 

my $FinalMerge = qq{
$conf{'vcfutils'}vcf-concat $conf{'scratchout'}tmp.snp.vcf $conf{'scratchout'}tmp.indel.vcf | vcf-sort-mod -t /lustre2/scratch  > $finalout
};

$param->{'job_name'} = "FinalMerge";
$param->{'command'} = $FinalMerge;
$param->{'select'} = 1;
$param->{'output_file'} = $finalout;
$param->{'depending_id'} = $snp_ar_job_id.":".$indel_ar_job_id;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";

my $final_job = &writeJob($param);
$final_cv_job_id = &submit($final_job);

my $vcf = $worktag.".final.vcf";

=head2  PhaseByTransmission
       
    Description:   works only when a trios is analysed 
    Input:         ..processed/exp_name.merged.tmp.vcf
    Exceptions:    none
    Returns:       workout/exp_name.trios.vcf

=cut 

if ($conf{'trios'}){
    my $trios = $worktag.".trios.vcf"; 
    my $PhaseByTransmission = qq{
        ulimit -l unlimited
        ulimit -s unlimited
        $conf{'GATK'} \\
        -R $conf{'ref_genome'} \\
        -T PhaseByTransmission \\
        -V $vcf \\
        -ped $conf{'ped_file'} \\
        -o $trios};
    
    $param->{'job_name'} = "PhByTransm";
	$param->{'command'} = $PhaseByTransmission;
	$param->{'select'} = 1;
	$param->{'output_file'} = $trios;
	$param->{'depending_id'} =$final_cv_job_id;
	$param->{'number_cpu'} = 1;
	$param->{'depend'} = "afterok";
    
    my $pbt_job = &writeJob($param);
    $final_cv_job_id = &submit($pbt_job);
}

=head2  SnpSift filter
       
    Description:   removes the duplicated line
    Input:         workout/exp_name.final.vcf
    Exceptions:    none
    Returns:       workout/exp_name.final.vcf

=cut 


my $cmd1 = "$conf{'SnpSift'} filter -f $finalout \"((exists VQSLOD))\" > $vcf\nif [ -f  ".$vcf.".idx ]; then rm ".$vcf.".idx; fi"; 

$param->{'job_name'} = "FilterVqslod";
$param->{'command'} = $cmd1;
$param->{'select'} = 1;
$param->{'output_file'} = $vcf;
$param->{'depending_id'} =$final_cv_job_id;
$param->{'number_cpu'} = 1;
$param->{'depend'} = "afterok";

my $job1 = &writeJob($param);
my ($job_id1) = &submit($job1);

my $out_hash;


=head1 functions

   
=head2 submit
    
    Arg:            job file, last job id, last job file
    Description:    this function uses qsub to submit the job file
    Exceptions:     dies if the job is not submitted succesfully
    Returns:        the job id of the submitted job and the update last job id and last 
job

=cut

sub submit {
    my ($job) = @_;
    if ($test == 1){return 0}
    my $job_id = `qsub -V -q workq $job`;  
    chomp $job_id;  
    my $tid = `qstat $job_id` ; if ($tid =~ /Unknown/){ die "couldn't check for job $job_id of $job";}
    unless (defined $job_id){die "*** error while submitting job $job"}
    return ($job_id);
}

=head2 writeJob
    
    Arg:            $name,$command,$select,$output,$id,$cpu,$depend
    Description:    this function write the file in bash to submit with the job
    Exceptions:     none
    Returns:        the job file
job

=cut

sub writeJob {
    my ($param) = @_;
    
    my $name = $param->{'job_name'};
	my $command = $param->{'command'};
	my $select = $param->{'select'};
	my $output = $param->{'output_file'}; 
	my $id = $param->{'depending_id'};
	my $cpu = $param->{'number_cpu'};
	my $depend = $param->{'depend'};
	
    unless (defined $depend){$depend = 'afterok'}
    unless (defined $cpu){$cpu = 1}
    my $file = $name.".submit.job";
    open (OUT, ">$file");
   
    print OUT "#PBS -l select=$select:app=java:ncpus=$cpu:mem=32gb\n#PBS -N ".substr($name,0,15)."\n#PBS -V\n#PBS -P $conf{'experiment_name'}\n#PBS -q workq\n#PBS -M $conf{'mail_list'}\n#PBS -m ae\n";
    if ($id){print OUT "#PBS -W depend=$depend:$id\n\n"}
    
    if ($output){print OUT "cd $PWD\n[ -s $output ] || $command\n\n";}
    else{print OUT "$command\n\n";}
	
	return $file;
}

