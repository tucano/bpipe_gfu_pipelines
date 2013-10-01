// MODULE ALIGN SOAPSPLICE GFU
SSPLICE="/lustre1/tools/bin/soapsplice"
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"

// STAGES SCRIPTS
GFU_PREPARE_ALIGN_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/soapsplice_prepare_align.sh"
GFU_VERIFY_BAM           = "/home/drambaldi/bpipe_gfu_pipelines/bin/verify_bam.sh"

@Transform("bam")
align_soapsplice_gfu = 
{
    doc title: "Soapsplice alignment task",
        desc: "Align with soapsplice. Generate temporary files in /dev/shm on the node",
        author: "davide.rambaldi@gmail.com"
    // check if input is compressed, this mess is due to the double extension fastq.gz
    if (input.endsWith(".gz"))
    {
        from("*.fastq.gz","*.fastq.gz") produce(input.prefix - ".fastq" + ".bam") 
        {
            exec"""
                HEADER_FILE=`$GFU_PREPARE_ALIGN_SCRIPT $ENVIRONMENT_FILE $input1.gz`;
                TMP_SCRATCH=`dirname $HEADER_FILE`
                TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;

                source $ENVIRONMENT_FILE;
                
                echo -e "[align_soapsplice_gfu]: soapsplice alignment on node $HOSTNAME" >&2;

                $SSPLICE -d $REFERENCE_GENOME -1 $input1.gz -2 $input2.gz -o $TMP_OUTPUT_PREFIX -p 4 $SSPLICEOPT_ALN;

                cat $HEADER_FILE ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $TMP_OUTPUT_PREFIX

                $GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;

                mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH}/;

                for F in ${TMP_SCRATCH}/*.junc; do
                    if [[ -e $F ]]; then
                        mv $F ${LOCAL_SCRATCH}/; 
                    fi;
                done;

                ln -s ${LOCAL_SCRATCH}/$output.bam $output.bam;

                rm -rf ${TMP_SCRATCH};
            """, "soapsplice"
        }
    }
    else
    {
        exec"""
            HEADER_FILE=`$GFU_PREPARE_ALIGN_SCRIPT $ENVIRONMENT_FILE $input1.gz`;
            TMP_SCRATCH=`dirname $HEADER_FILE`
            TMP_OUTPUT_PREFIX=$TMP_SCRATCH/$output.prefix;

            source $ENVIRONMENT_FILE;
            
            echo -e "[align_soapsplice_gfu]: soapsplice alignment on node $HOSTNAME" >&2;

            $SSPLICE -d $REFERENCE_GENOME -1 $input1 -2 $input2 -o $TMP_OUTPUT_PREFIX -p 4 $SSPLICEOPT_ALN;

            cat $HEADER_FILE ${TMP_OUTPUT_PREFIX}.sam | $SAMTOOLS view -Su - | $SAMTOOLS sort - $TMP_OUTPUT_PREFIX

            $GFU_VERIFY_BAM ${TMP_OUTPUT_PREFIX}.bam || exit 1;

            mv ${TMP_OUTPUT_PREFIX}.bam ${LOCAL_SCRATCH}/;

            for F in ${TMP_SCRATCH}/*.junc; do
                if [[ -e $F ]]; then
                    mv $F ${LOCAL_SCRATCH}/; 
                fi;
            done;

            ln -s ${LOCAL_SCRATCH}/$output.bam $output.bam;

            rm -rf ${TMP_SCRATCH};
        """, "soapsplice"  
    }
}
