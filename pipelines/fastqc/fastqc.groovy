about title: "FASTQC: quality control of fastq files: IOS XXX"

// Usage line will be used to infer the correct bpipe command
// USAGE: bpipe run -r $pipeline_filename *.fastq.gz

PLATFORM         = "illumina"
CENTER           = "CTGB"
ENVIRONMENT_FILE = "gfu_environment.sh"

//--BPIPE_ENVIRONMENT_HERE--

/*
 * THIS PIPELINE HAVE A DIFFERENT SCOPE:
 * Here we want to acces the data in (example):
 * /lustre2/raw_data/121002_SN859_0084_AC177AACXX/Project_Toniolo/Sample_1050181646
 * And recover the RUN name from the basename using something like:
 * name=`tail -1 SampleSheet.csv | cut -d',' -f3`
 * project=`tail -1 SampleSheet.csv | cut -d',' -f10`
 * rundir=`basename ${PWD%%Project_$project/Sample_$name}`
 *
 * I add a var local : true 
 * set to false in order to launch the pipe over fastq.gz files in:
 * /lustre2/raw_data/RUN_NAME/PROJECT_NAME/SAMPLE
 */ 
 Bpipe.run {
    fastqc_gfu
 }