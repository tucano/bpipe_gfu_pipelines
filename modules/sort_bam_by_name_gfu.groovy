// MODULE SORT BAM BY NAME FILE GFU
SAMTOOLS="/usr/local/cluster/bin/samtools"

@Filter("sorted_by_name")
sort_bam_by_name_gfu =
{
    doc title: "Samtools: sort by name bam file",
        desc: "Sort bam file by name",
        constrains: "...",
        author: "davide.rambaldi@gmail.com"
    exec"""
         $SAMTOOLS sort -n $input.bam $output.prefix
    """
}