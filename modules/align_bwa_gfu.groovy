// MODULE ALIGN BWA GFU
BWA="/usr/local/cluster/bin/bwa"

@intermediate
align_bwa_gfu =
{
    // use -I for base64 Illumina quality
    // use -q for trim quality (Es: -q 30)
    var BWAOPT_ALN : ""

    // INFO
    doc title: "GFU: align DNA reads with bwa",
        desc: "Use bwa aln to align reads on the reference genome. Bwa options: $BWAOPT_ALN",
        constraints: "Work with fastq and fastq.gz files.",
        author: "davide.rambaldi@gmail.com"

    // TWO VERSIONS: Compressed and NOT compressed.
    if (input.endsWith(".gz")) {
        from("fastq.gz") produce(input.prefix - ".fastq" + ".sai") {
            exec """
                echo -e "[align_bwa_gfu]: bwa aln on node $HOSTNAME with input (compressed) $input.gz and output $output.sai" >&2;
                $BWA aln -t 2 $BWAOPT_ALN $REFERENCE_GENOME $input.gz > $output.sai
            ""","bwa_aln"
        }
    } else {
        from("fastq") produce(input.prefix - ".fastq" + ".sai") {
            exec """
                echo -e "[align_bwa_gfu]: bwa aln on node $HOSTNAME with input (not compressed) $input.fastq and output $output.sai" >&2;
                $BWA aln -t 2 $BWAOPT_ALN $REFERENCE_GENOME $input.fastq > $output.sai
            ""","bwa_aln"
        }
    }
}