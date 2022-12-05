// MODULE MERGE BAM FILES
PICMERGE="/usr/local/cluster/bin/MergeSamFiles.jar"

@intermediate
merge_bam_gfu = 
{
    var rename : false

    doc title: "GFU: merge bam files with $PICMERGE",
        desc: "Merge bam files with $PICMERGE",
        constraints: "If file came from split (es: read_0000) you should set rename: true. The output will be renamed with the variable EXPERIMENT_NAME (${EXPERIMENT_NAME})",
        author: "davide.rambaldi@gmail.com"

    def output_prefix  = input.prefix.replaceFirst(/_R.*/,"")
    if (rename) output_prefix = EXPERIMENT_NAME
    def output_bam = output_prefix + ".merge.bam"
    def output_bai = output_prefix + ".merge.bai"
    input_strings = inputs.collect() { return "I=" + it}.join(" ")

    produce(output_bam, output_bai)
    {
        exec """
            echo -e "[merge_bam_gfu]: Merging BAM files $inputs in output file $output_bam with index $output_bai" >&2;
            java -jar $PICMERGE $input_strings O=$output_bam
                VALIDATION_STRINGENCY=SILENT
                CREATE_INDEX=true
                MSD=true
                ASSUME_SORTED=true
                USE_THREADING=true
        ""","merge_bam_files"
    }
}