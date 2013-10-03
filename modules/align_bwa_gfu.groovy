// MODULE ALIGN BWA GFU
BWA="/usr/local/cluster/bin/bwa"

@Transform("sai")
align_bwa_gfu =
{
    // use -I for base64 Illumina quality
    var BWAOPT_ALN : ""
    doc title: "GFU align DNA reads with bwa",
        desc: "Use bwa aln to align reads (paired ends) on the reference genome. Bwa options: $BWAOPT_ALN",
        author: "davide.rambaldi@gmail.com"
    if (input.endsWith(".gz"))
    {
        from("*.fastq.gz") produce(input.prefix - ".fastq" + ".sai")
        {
            exec """
                source $ENVIRONMENT_FILE;
                echo -e "[align_bwa_gfu]: bwa aln on node $HOSTNAME with input $input" >&2;
                $BWA aln -t 2 $BWAOPT_ALN $REFERENCE_GENOME $input > $LOCAL_SCRATCH/$output;
                ln -s ${LOCAL_SCRATCH}/$output $output;
            ""","bwa_aln"
        }
    }
    else
    {
        exec """
            source $ENVIRONMENT_FILE;
            echo -e "[align_bwa_gfu]: bwa aln on node $HOSTNAME with input $input" >&2;
            $BWA aln -t 2 $BWAOPT_ALN $REFERENCE_GENOME $input > $LOCAL_SCRATCH/$output;
            ln -s ${LOCAL_SCRATCH}/$output $output;
        ""","bwa_aln"
    }
}