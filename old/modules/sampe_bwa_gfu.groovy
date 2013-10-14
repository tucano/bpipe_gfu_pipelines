// MODULE SAMPE BWA GFU
BWA="/usr/local/cluster/bin/bwa"
SAMTOOLS="/usr/local/cluster/bin/samtools"
GFU_VERIFY_BAM   = "/home/drambaldi/bpipe_gfu_pipelines/bin/verify_bam.sh"

@Transform("bam")
sampe_bwa_gfu =
{
    var BWAOPT_SE : ""
    var BWAOPT_PE : ""
    var paired : true
    var lane : true
    var compressed : true

    doc title: "GFU sampe/samse bwa: merge paired ends with sampe or signle end with samse",
        desc: "1. Generate alignments in the SAM format given paired-end reads (repetitive read pairs will be placed randomly). 2. Sort and convert to bam. 3. Verify bam",
        author: "davide.rambaldi@gmail.com"
    
    if (paired)
    {
        if (compressed)
        {
            input1_fastq = input1.prefix + ".fastq.gz"
            input2_fastq = input2.prefix + ".fastq.gz"
        }
        else
        {
            input1_fastq = input1.prefix + ".fastq"
            input2_fastq = input2.prefix + ".fastq"    
        }
        
        // we must define headers out of exec
        if (lane)
        {
            msplit = input1.prefix.split("_")
            ID = input1.prefix.split("_")[0] + "_" + input1.prefix.split("_")[2]
            PL = "illumina"
            PU = "${FCID}"
            LB = input1.prefix.split("_")[0]
            SM = "${PROJECTNAME}"
            CN = "CTBG"
        }
        
        exec"""
            source $ENVIRONMENT_FILE;
            TMP_SCRATCH=\$(/bin/mktemp -d /dev/shm/${PROJECTNAME}.XXXXXXXXXXXXX);
            TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;
            echo -e "[sampe_bwa_gfu]: bwa sampe/samse on node $HOSTNAME with TMP_SCRATCH: $TMP_SCRATCH" >&2;
            $BWA sampe $BWAOPT_PE -r "@RG\tID:${ID}\tPL:${PL}\tPU:${PU}\tLB:${LB}\tSM:${SM}\tCN:${CN}" $REFERENCE_GENOME ${LOCAL_SCRATCH}/$input1.sai ${LOCAL_SCRATCH}/$input2.sai $input1_fastq $input2_fastq > ${TMP_OUTPUT_PREFIX}.sam;
            $SAMTOOLS view -Su ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS sort - ${TMP_OUTPUT_PREFIX};
            $GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;
            mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH};
            ln -s ${LOCAL_SCRATCH}/$output $output;
            rm -rf ${TMP_SCRATCH};
        ""","bwa_sampe"
    }
    else
    {
        if (compressed)
        {
            input_fastq = input.prefix + ".fastq"
        }
        else
        {
            input_fastq = input.prefix + ".fastq.gz"
        }
        
        exec"""
            source $ENVIRONMENT_FILE;
            TMP_SCRATCH=\$(/bin/mktemp -d /dev/shm/${PROJECTNAME}.XXXXXXXXXXXXX);
            TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;
            echo -e "[sampe_bwa_gfu]: bwa sampe/samse on node $HOSTNAME with TMP_SCRATCH: $TMP_SCRATCH" >&2;
            $BWA samse $BWAOPT_SE -r "@RG\tID:${ID}\tPL:${PL}\tPU:${PU}\tLB:${LB}\tSM:${SM}\tCN:${CN}" $REFERENCE_GENOME ${LOCAL_SCRATCH}/$input.sai $input_fastq > ${TMP_OUTPUT_PREFIX}.sam;
            $SAMTOOLS view -Su ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS sort - ${TMP_OUTPUT_PREFIX};
            $GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;
            mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH};
            ln -s ${LOCAL_SCRATCH}/$output $output;
            rm -rf ${TMP_SCRATCH};
        ""","bwa_sampe"
    }
}
