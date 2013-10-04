// MODULE SORT BAM BY NAME FILE GFU
SAMTOOLS="/usr/local/cluster/bin/samtools"
// STAGES SCRIPTS
GFU_PREPARE_SCRATCH_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/get_project_name_from_bam.sh"

//
// FIXME sort_bam_by_name_gfu
// SAM reads diviene l'output finale after: SORTING BY COORDINATES and TO BAM and INDEX (bai)
// Link symbolici *.bam.bai e *.bai
// MARKDUPLICATES --> Anche per RNA-seq (tutte le pipelines)
// 

@Filter("sorted")
sort_bam_by_name_gfu =
{
    doc title: "samtools prepare bam file for htseq-count: sort by name",
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
