// MODULE SPLIT FASTQ FILE
GFU_SPLIT_FASTQ  = "/home/drambaldi/bpipe_gfu_pipelines/bin/split_fastq.sh"

// SPLIT PAIR
split_fastq_pairs_gfu =
{
    var SPLIT_READS_SIZE : 2000000
    doc title: "GFU split fastq files (R1 and R2) in $SPLIT_READS_SIZE reads/file",
        desc: "use split to subdivide a fastq pair (R1 and R2) in chunks",
        author: "davide.rambaldi@gmail.com"
    
    produce("*.fastq")
    {
        exec """
            echo -e "[split_fastq_pairs_gfu]: splitting fastq on node $HOSTNAME" >&2;
            $GFU_SPLIT_FASTQ -1 $input1 -2 $input2 -r $SPLIT_READS_SIZE $ENVIRONMENT_FILE
        """
    }
}

//SPLIT SINGLE FILE FIXME: TEST!!!
split_fastq_single_gfu =
{
    var SPLIT_READS_SIZE : 2000000
    doc title: "GFU split fastq single files (R1 only) in $SPLIT_READS_SIZE reads/file",
        desc: "use split to subdivide a fastq file in chunks",
        author: "davide.rambaldi@gmail.com"
    
    produce("*.fastq")
    {
        exec """
            $GFU_SPLIT_FASTQ -1 $input -r $SPLIT_READS_SIZE $ENVIRONMENT_FILE
        """
    }
}