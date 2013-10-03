// MODULE MARK DUPLICATES IN BAM FILE
MARKDUP="/usr/local/cluster/bin/MarkDuplicates.jar"

mark_duplicates_gfu = 
{
    doc title: "GFU mark duplicates in bam files with $MARKDUP : IOS GFU 0019",
        desc: "Mark duplicates in bam files with $MARKDUP",
        constrains: "Require a sorted BAM (ASSUME_SORTED=true)",
        author: "davide.rambaldi@gmail.com"
    def output_prefix  = input.prefix.replaceFirst(/_R.*/,"")
    def output_bam     = output_prefix + ".dedup.bam"
    def output_metrics = output_prefix + ".dedup.metrics"
    exec"""
        source $ENVIRONMENT_FILE;
        echo -e "[mark_duplicates_gfu]: Marking duplicates in $input.bam, output file: $output_bam" >&2;
        ulimit -l unlimited;
        ulimit -s unlimited;
        java -Djava.io.tmpdir=$SCRATCH_PREFIX -Xmx32g -jar $MARKDUP
            I=$input.bam
            O=$output_bam
            CREATE_INDEX=true
            VALIDATION_STRINGENCY=SILENT
            REMOVE_DUPLICATES=false
            ASSUME_SORTED=true
            METRICS_FILE=$output_metrics
    ""","mark_duplicates"
}
