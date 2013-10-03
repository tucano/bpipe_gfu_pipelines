// MODULE SORT BAM FILE GFU
SAMTOOLS="/usr/local/cluster/bin/samtools"
// STAGES SCRIPTS
GFU_PREPARE_SCRATCH_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/get_project_name_from_bam.sh"

@Filter("sorted")
sort_bam_gfu =
{
    doc title: "htseq-count prepare bam file",
        desc: "If needed, run a script that search for LOCAL_SCRATCH and create tmp dir; then sort bam file",
        author: "davide.rambaldi@gmail.com"
    // conditional on presence of ENVIRONMENT_FILE
    if (new File(ENVIRONMENT_FILE).exists() == false)
    {
        exec "$GFU_PREPARE_SCRATCH_SCRIPT $input.bam $SCRATCH_PREFIX > $ENVIRONMENT_FILE"
    }
    exec """
        source $ENVIRONMENT_FILE;
        $SAMTOOLS sort -n $input.bam $output.prefix
    """
}