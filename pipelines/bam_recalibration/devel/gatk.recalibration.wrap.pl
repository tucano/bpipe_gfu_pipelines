#! /usr/bin/perl -w

=head1 author

Vincenza Maselli
vincenza.maselli@hsr.it

=head1 decription

This script write the wrapper for the first steps of GATK, from deduplictate bam to processed bam

    # TargetCreator, LocalRealignment, BaseRecalibration, PrintReads
    
    The second part runs for BAM and for each region defined in a chunck file
    It is divided in blocks, one for each command, the block have this format
    the output file name of the command 
    the command string
    the call to the writeJob function (see documentation for the function itself)
    the call to the submit function (see documentation for the function itself)


=head1 usage
    
    This script require a config file and a specification of which type of argument is passed by command line, it could be a list of files name or a directory of bam files
     
    perl gatk.recalibration.wrap.pl -c config_file -t (file|dir) -a file/dir name
    
=cut 

use vars;
use strict;
use Data::Dumper;
use Getopt::Long;
my $test;
my ($cfg, $tag, $arg,$help);

my $PWD = `pwd`;
chomp $PWD;
my $USAGE;
BEGIN {
    $USAGE = "perl gatk.recalibration.wrap.pl -c config_file -t (file|dir) -a file/dir name\n";    
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
                      type perlodoc gatk.recalibration.wrap.pl for full documentation\n";
        exit;
    }

    require $cfg;
}

unless (defined $test){$test = 0}

my %conf =  %::conf;
my $worktag    = $conf{'workout'}.$conf{'experiment_name'};
my $inputdir = $conf{'workdir'};
my $scratchtag = $conf{'scratchout'}.$conf{'experiment_name'};
my $mem = $conf{'java_mem'};

my $truseq = $conf{'chunk_file'};

my @bams;
my $dir;
if ($tag eq 'dir'){ 
    $dir = $arg;
    opendir (DIR,$dir) || die "$! $dir\n";
    while (my $file = readdir(DIR) ){ 
        next if $file =~ /^\./;
        next unless $file =~ /bam$/;
        next unless $file =~ /dedup/;
        push(@bams, $file);
    }
}elsif($tag eq 'file'){
    $dir = $conf{'scratchout'};
    @bams = split / /,$arg; 
}else{die "$USAGE\n";}

if ($test == 2){
    @bams = ($bams[0]);
}

=head1 Main 


=head2 TargetCreator 

    Description:   process the file that comes from a dir specification or from a list of name
    Input:         BAM file e.g. test.bam or BAM file with technical duplicates marked e.g test_dedup.bam
    Exceptions:    none
    Returns:       Intervals file e.g test_dedup.intervals
                   job file N_1_TargetCreator.sumbit.job, where N is a progressive number for the first to the last BAM in the list or dir

=cut

=head2 IndelRealigner 

    Description:   process the file that comes from a dir specification or from a list of name
    Input:         BAM file e.g. test.bam or BAM file with technical duplicates marked e.g test_dedup.bam
                   Intervals file e.g test_dedup.intervals 
    Exceptions:    none
    Returns:       Realigned bam file e.g test_dedup.local.realign.bam
                   job file N_2_Realigner.sumbit.job, where N is a progressive number for the first to the last BAM in the list or dir

=cut

my $bamcount = 0;
my $bam_string;
my $job_string;
foreach my $dedupfile (sort {$a cmp $b} @bams){
    my $dedupbam = $dir.$dedupfile;
    my $file = $conf{'scratchout'}.$dedupfile;
    $bamcount ++;   
    my $intervals = $file.".intervals"; 

    my $TargetCreator =	qq{
    ulimit -l unlimited
    ulimit -s unlimited
    $conf{'GATK'} \\
    -I $dedupbam \\
    -T RealignerTargetCreator \\
    -L $truseq \\
    -R $conf{'ref_genome'} \\
    -o $intervals \\
    --unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY \\
    --known $conf{'dbsnp'}};
    my $job2 = &writeJob($bamcount.".1_TargetCreator",$TargetCreator,1,$intervals);
    my ($job_id2) = &submit($job2);

    my $out = $file.".local.realign.bam";
    $bam_string .= "-I $out "; 
 
    my $IndelRealigner = qq{
    ulimit -l unlimited
    ulimit -s unlimited
    $conf{'GATK'} \\
    -I $dedupbam \\
    -R $conf{'ref_genome'} \\
    -L $truseq \\
    -T IndelRealigner \\
    -targetIntervals $intervals \\
    -o $out \\
    --unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY \\
    -known $conf{'dbsnp'}};
    my $job3 = &writeJob($bamcount.".2_Realigner",$IndelRealigner,1,$out,$job_id2);
    my ($job_id3) = &submit($job3);
    $job_string .= "$job_id3:";
    
}
$job_string =~ s/:$//;


my $out2 =$scratchtag.".recal_data.grp";

=head2 BaseRecalibrator 

    Description:   process the bam files that come from the realignment
    Input:         test_dedup.local.realign.bam
    Exceptions:    none
    Returns:       Recalibration file  ../processed/espriement_name.recal_data.grp
                   
=cut


my $BaseRecalibrator = qq{
ulimit -l unlimited
ulimit -s unlimited\n
$conf{'GATK'} \\
-R $conf{'ref_genome'} \\
-knownSites $conf{'dbsnp'} \\
$bam_string \\
-L $truseq \\
-T BaseRecalibrator  \\
--covariate QualityScoreCovariate \\
--covariate CycleCovariate \\
--covariate ContextCovariate \\
--covariate ReadGroupCovariate \\
--unsafe ALLOW_SEQ_DICT_INCOMPATIBILITY \\
-nct 64 \\
-o $out2};
my $job4 = &writeJob("3_Recal",$BaseRecalibrator,1,$out2,$job_string,1,'afterok');
my ($job_id4) = &submit($job4) ;

=head2 PrintReads 

    Description:   process  file that comes from the IndelRealigner
    Input:         Realigned BAM file e.g. the test_dedup.local.realign.bam
                   ../processed/espriement_name.recal_data.grp 
    Exceptions:    none
    Returns:       Recalibreted bam e.g test_local.realign.recal_data.merged.bam
                   job file N_4_PrintReads.sumbit.job, where N is a progressive number for the first to the last BAM in the list or dir

=cut

my $bamcount = 0;
foreach my $dedupfile (sort {$a cmp $b} @bams){
    my $dedupbam = $conf{'scratchout'}.$dedupfile;
    my $file = $dedupbam;
    my $out = $file.".local.realign.bam";
    $bamcount ++;
    $file =~ s/dedup.bam/local.realign.recal_data.merged.bam/;
    my $PrintReads = qq{
    ulimit -l unlimited
    ulimit -s unlimited
    $conf{'GATK'} \\
    -R $conf{'ref_genome'} \\
    -I $out \\
    -T PrintReads \\
    -o $file \\
    -L $truseq \\
    -nct 64 \\
    -BQSR $out2};
    my $job5 = &writeJob($bamcount.".4_PrintReads",$PrintReads,1,$file,$job_id4,1,'afterok'); 
    &submit($job5) ; 
}

=head1 functions


=cut 



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
    
    Arg:            job name, command, output file, depending id, type of dipendence if different from afterok, hash of different output if any
    Description:    this function write the file in bash to submit with the job
    Exceptions:     none
    Returns:        the job file
job

=cut

sub writeJob {
    my ($name,$command,$select,$output,$id,$cpu,$depend) = @_;
    unless (defined $depend){$depend = 'afterany'}
    unless (defined $cpu){$cpu = 1}
    my $file = $name.".submit.job";
    open (OUT, ">$file");
   
   print OUT "#PBS -l select=$select:app=java:ncpus=$cpu:mem=$conf{java_mem}gb\n#PBS -P ".$conf{'experiment_name'}."\n#PBS -N ".substr($name,0,15)."\n#PBS -V\n#PBS -q workq\n#PBS -M $conf{'mail_list'}\n#PBS -m ae\n";
    if ($id){print OUT "#PBS -W depend=$depend:$id\n\n"}
    
    if ($output){print OUT "cd $PWD\n[ -s $output ] || \\$command\n\n";}
    else{print OUT "$command\n\n";}
	
	return $file;
}

