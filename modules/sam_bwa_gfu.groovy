// MODULE SAMPE/SAMSE BWA GFU
BWA="/usr/local/cluster/bin/bwa"
SAMTOOLS="/usr/local/cluster/bin/samtools"

@Transform("bam")
sam_bwa_gfu =
{
    var BWAOPT_SE : ""
    var BWAOPT_PE : ""
    var paired : true
    var compressed : true

    doc title: "GFU: sampe/samse bwa: merge paired ends with sampe or single end with samse",
        desc: """
            Generate alignments in the SAM format given paired-end reads (repetitive read pairs will be placed randomly).
            Sort by coordinates and generate a bam file.
        """,
        constraints: "The user should define if files are compressed (compressed: true) and if reads are paired (paired: true)",
        author: "davide.rambaldi@gmail.com"

    def input1_fastq
    def input2_fastq
    def header = '@RG' + "\tID:${EXPERIMENT_NAME}\tPL:${PLATFORM}\tPU:${FCID}\tLB:${EXPERIMENT_NAME}\tSM:${SAMPLEID}\tCN:${CENTER}"

    println "HEADER: $header"
    if (paired) {

        if (compressed) {
            input1_fastq = input1.prefix + ".fastq.gz"
            input2_fastq = input2.prefix + ".fastq.gz"
        } else {
            input1_fastq = input1.prefix + ".fastq"
            input2_fastq = input2.prefix + ".fastq" 
        }
        exec"""
            TMP_SCRATCH=\$(/bin/mktemp -d /dev/shm/${PROJECTNAME}.XXXXXXXXXXXXX);
            TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;
            echo -e "[sam_bwa_gfu]: bwa sampe on node $HOSTNAME with TMP_SCRATCH: $TMP_SCRATCH" >&2;
            $BWA sampe $BWAOPT_PE -r \"$header\" $REFERENCE_GENOME $input1.sai $input2.sai $input1_fastq $input2_fastq > ${TMP_OUTPUT_PREFIX}.sam;
            $SAMTOOLS view -Su ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS sort - ${TMP_OUTPUT_PREFIX};
            mv ${TMP_SCRATCH}/$output.bam $output.bam;
            rm -rf ${TMP_SCRATCH};
        ""","bwa_sampe"
    } else {

        if (compressed) {
            input_fastq = input.prefix + ".fastq"
        } else {
            input_fastq = input.prefix + ".fastq.gz"
        }

        exec"""
            TMP_SCRATCH=\$(/bin/mktemp -d /dev/shm/${PROJECTNAME}.XXXXXXXXXXXXX);
            TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;
            echo -e "[sam_bwa_gfu]: bwa samse on node $HOSTNAME with TMP_SCRATCH: $TMP_SCRATCH" >&2;
            echo -e "[sam_bwa_gfu]: header is $header"
            $BWA samse $BWAOPT_SE -r \"$header\" $REFERENCE_GENOME $input1.sai $input_fastq > ${TMP_OUTPUT_PREFIX}.sam;
            $SAMTOOLS view -Su ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS sort - ${TMP_OUTPUT_PREFIX};
            mv ${TMP_SCRATCH}/$output.bam $output.bam;
            rm -rf ${TMP_SCRATCH};
        ""","bwa_samse"
    }
}