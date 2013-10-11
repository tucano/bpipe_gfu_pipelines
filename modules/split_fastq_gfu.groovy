// MODULE SPLIT FASTQ FILE
GFU_SPLIT_FASTQ  = "/home/drambaldi/bpipe_gfu_pipelines/bin/split_fastq.sh"

// SPLIT PAIR
split_fastq_gfu =
{
    var SPLIT_READS_SIZE : 2000000
    var paired : true

    doc title: "GFU split fastq.gz files in $SPLIT_READS_SIZE reads/file",
        desc: "Use split to subdivide a fastq pair (R1 and R2) in chunks (paired: true) or a single file (paired: false)",
        constraints: "Only work with @.fastq.gz files",
        author: "davide.rambaldi@gmail.com"        

    produce("*.fastq")
    {
        if (paired)
        {
            exec """
                echo -e "[split_fastq_pairs_gfu]: splitting fastq on node $HOSTNAME" >&2;
                $GFU_SPLIT_FASTQ -1 $input1.gz -2 $input2.gz -r $SPLIT_READS_SIZE $ENVIRONMENT_FILE
            """
        }
        else
        {
            exec """
                echo -e "[split_fastq_pairs_gfu]: splitting fastq on node $HOSTNAME" >&2;
                $GFU_SPLIT_FASTQ -1 $input.gz -r $SPLIT_READS_SIZE $ENVIRONMENT_FILE
            """
        }
    }
}
