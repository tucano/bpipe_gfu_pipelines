// MODULE SORT BAM BY NAME FILE GFU
SAMTOOLS="/usr/local/cluster/bin/samtools"

// FIXME/TODO
// SAM reads diviene l'output finale after: SORTING BY COORDINATES and TO BAM and INDEX (bai)
// Link symbolici *.bam.bai e *.bai
// MARKDUPLICATES --> Anche per RNA-seq (tutte le pipelines)
// 

@Filter("sorted")
sort_bam_by_name_gfu =
{
    doc title: "Samtools: prepare bam file for htseq-count: sort by name",
        desc: "If needed, run a script that search for LOCAL_SCRATCH and create tmp dir; then sort bam file",
        author: "davide.rambaldi@gmail.com"
    exec """
        $SAMTOOLS sort -n $input.bam $output.prefix
    """
}
