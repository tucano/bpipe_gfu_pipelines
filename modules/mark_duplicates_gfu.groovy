// MODULE MARK DUPLICATES IN BAM FILE
MARKDUP="/usr/local/cluster/bin/MarkDuplicates.jar"

@preserve
mark_duplicates_gfu = 
{
    doc title: "GFU: mark duplicates in bam files with $MARKDUP : IOS GFU 0019",
        desc: "Mark duplicates in bam files with $MARKDUP",
        constrains: """
            Require a BAM sorted by coordinate (ASSUME_SORTED=true). 
            I decide to make all in current directory.
        """,
        author: "davide.rambaldi@gmail.com"

    def output_prefix  = input.prefix.replaceFirst(/_R.*/,"")
    def output_bam     = output_prefix + ".dedup.bam"
    def output_bai = output_prefix + ".dedup.bai"
    def output_metrics = output_prefix + ".dedup.metrics"

    produce(output_bam, output_bai, output_metrics)
    {
        exec"""
            echo -e "[mark_duplicates_gfu]: Marking duplicates in $input.bam, output file: $output_bam with index $output_bai" >&2;
            ulimit -l unlimited;
            ulimit -s unlimited;
            java -Djava.io.tmpdir=/lustre2/scratch -Xmx32g -jar $MARKDUP
                I=$input.bam
                O=$output_bam
                CREATE_INDEX=true
                VALIDATION_STRINGENCY=SILENT
                REMOVE_DUPLICATES=false
                ASSUME_SORTED=true
                METRICS_FILE=$output_metrics
        ""","mark_duplicates"
    }
}